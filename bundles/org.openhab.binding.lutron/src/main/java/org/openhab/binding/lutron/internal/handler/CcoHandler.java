/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron contact closure outputs (CCOs).
 * e.g. VCRX CCOs and CCO RF module
 *
 * Note: For a RA2 Pulsed CCO, querying the output state with ?OUTPUT,<id>,1 is meaningless and will always
 * return 100 (on). Also, the main repeater will not report ~OUTPUT commands for a pulsed CCO regardless of
 * the #MONITORING setting. So this binding supports sending pulses ONLY.
 *
 * @author Bob Adair - Initial contribution
 *
 */
@NonNullByDefault
public class CcoHandler extends LutronHandler {
    private static final Integer ACTION_PULSE = 6;
    private static final Integer ACTION_STATE = 1;

    private final Logger logger = LoggerFactory.getLogger(CcoHandler.class);

    private int integrationId;
    private double defaultPulse = 0.5; // default pulse length (seconds)

    protected enum CcoOutputType {
        PULSED,
        MAINTAINED
    }

    protected @Nullable CcoOutputType outputType;

    public CcoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        return integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get(INTEGRATION_ID);

        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        integrationId = id.intValue();
        logger.debug("Initializing CCO handler for integration ID {}", id);

        // Determine output type from configuration if not pre-defined by subclass
        if (outputType == null) {
            String oType = (String) getThing().getConfiguration().get(OUTPUT_TYPE);

            if (oType == null || oType == OUTPUT_TYPE_PULSED) {
                logger.debug("Setting CCO type Pulsed for device {}.", integrationId);
                outputType = CcoOutputType.PULSED;
            } else if (oType == OUTPUT_TYPE_MAINTAINED) {
                logger.debug("Setting CCO type Maintained for device {}.", integrationId);
                outputType = CcoOutputType.MAINTAINED;
            } else {
                logger.warn("Invalid CCO type setting for device {}. Defaulting to Pulsed.", integrationId);
                outputType = CcoOutputType.PULSED;
            }
        }

        // If output type pulsed, determine pulse length
        if (outputType == CcoOutputType.PULSED) {
            Number defaultPulse = (Number) getThing().getConfiguration().get(DEFAULT_PULSE);

            if (defaultPulse != null) {
                double dp = defaultPulse.doubleValue();
                if (dp >= 0 && dp <= 99.0) {
                    defaultPulse = dp;
                    logger.debug("Pulse length set to {} seconds for device {}.", defaultPulse, integrationId);
                } else {
                    logger.warn("Invalid pulse length value set. Using default for device {}.", integrationId);
                }
            } else {
                logger.debug("Using default pulse length value for device {}", integrationId);
            }
        }
        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for CCO {}", integrationId);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            queryOutput(ACTION_STATE); // handleUpdate() will set thing status to online when response arrives
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            logger.debug("switch channel {} linked for CCO {}", channelUID.getId(), integrationId);

            if (outputType == CcoOutputType.PULSED) {
                // Since this is a pulsed CCO channel state is always OFF
                updateState(channelUID, OnOffType.OFF);
            } else if (outputType == CcoOutputType.MAINTAINED) {
                // Query the device state and let the service routine update the channel state
                queryOutput(ACTION_STATE);
            } else {
                logger.warn("invalid output type defined for CCO {}", integrationId);
            }
        } else {
            logger.warn("invalid channel {} linked for CCO {}", channelUID.getId(), integrationId);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            if (command instanceof OnOffType && command == OnOffType.ON) {
                if (outputType == CcoOutputType.PULSED) {
                    output(ACTION_PULSE, String.format(Locale.ROOT, "%.2f", defaultPulse));
                    updateState(channelUID, OnOffType.OFF);
                } else {
                    output(ACTION_STATE, 100);
                }
            }

            else if (command instanceof OnOffType && command == OnOffType.OFF) {
                if (outputType == CcoOutputType.MAINTAINED) {
                    output(ACTION_STATE, 0);
                }
            }

            else if (command instanceof RefreshType) {
                if (outputType == CcoOutputType.MAINTAINED) {
                    queryOutput(ACTION_STATE);
                } else {
                    updateState(CHANNEL_SWITCH, OnOffType.OFF);
                }
            } else {
                logger.debug("ignoring invalid command on channel {} for CCO {}", channelUID.getId(), integrationId);
            }
        } else {
            logger.debug("ignoring command on invalid channel {} for CCO {}", channelUID.getId(), integrationId);
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        logger.debug("Update received for CCO: {} {}", type, StringUtils.join(parameters, ","));

        if (outputType == CcoOutputType.MAINTAINED) {
            if (type == LutronCommandType.OUTPUT && parameters.length > 1
                    && ACTION_STATE.toString().equals(parameters[0])) {
                if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatus.ONLINE);
                }
                try {
                    BigDecimal state = new BigDecimal(parameters[1]);
                    updateState(CHANNEL_SWITCH, state.compareTo(BigDecimal.ZERO) == 0 ? OnOffType.OFF : OnOffType.ON);
                } catch (NumberFormatException e) {
                    logger.warn("Unable to parse update {} {} from CCO {}", type, StringUtils.join(parameters, ","),
                            integrationId);
                    return;
                }
            }
        } else {
            // Do nothing on receiving updates for pulsed CCO except update online status
            if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }
}
