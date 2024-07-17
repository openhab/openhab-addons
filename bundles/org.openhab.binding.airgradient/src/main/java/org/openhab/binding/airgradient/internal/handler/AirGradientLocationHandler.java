/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airgradient.internal.handler;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airgradient.internal.communication.AirGradientCommunicationException;
import org.openhab.binding.airgradient.internal.config.AirGradientLocationConfiguration;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirGradientAPIHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class AirGradientLocationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AirGradientLocationHandler.class);

    private @NonNullByDefault({}) AirGradientLocationConfiguration locationConfig = null;

    public AirGradientLocationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {}: {}", channelUID, command.toFullString());
        if (command instanceof RefreshType) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                if (bridge.getHandler() instanceof AirGradientAPIHandler handler) {
                    handler.pollingCode();
                }
            }
        } else if (CHANNEL_LEDS_MODE.equals(channelUID.getId())) {
            if (command instanceof StringType stringCommand) {
                setLedModeOnDevice(stringCommand.toFullString());
            } else {
                logger.warn("Received command {} for channel {}, but it needs a string command", command.toString(),
                        channelUID.getId());
            }
        } else if (CHANNEL_CALIBRATION.equals(channelUID.getId())) {
            if (command instanceof StringType stringCommand) {
                if ("co2".equals(stringCommand.toFullString())) {
                    calibrateCo2OnDevice();
                } else {
                    logger.warn(
                            "Received unknown command {} for calibration on channel {}, which we don't know how to handle",
                            command.toString(), channelUID.getId());
                }
            }
        } else {
            // This is read only
            logger.warn("Received command {} for channel {}, which we don't know how to handle", command.toString(),
                    channelUID.getId());
        }
    }

    private void setLedModeOnDevice(String mode) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            if (bridge.getHandler() instanceof AirGradientAPIHandler handler) {
                try {
                    handler.getApiController().setLedMode(getSerialNo(), mode);
                    updateStatus(ThingStatus.ONLINE);
                } catch (AirGradientCommunicationException agce) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, agce.getMessage());
                }
            }
        }
    }

    private void calibrateCo2OnDevice() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            if (bridge.getHandler() instanceof AirGradientAPIHandler handler) {
                try {
                    handler.getApiController().calibrateCo2(getSerialNo());
                    updateStatus(ThingStatus.ONLINE);
                } catch (AirGradientCommunicationException agce) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, agce.getMessage());
                }
            }
        }
    }

    @Override
    public void initialize() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);
        locationConfig = getConfigAs(AirGradientLocationConfiguration.class);

        Bridge controller = getBridge();
        if (controller == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        } else if (ThingStatus.OFFLINE.equals(controller.getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public String getLocationId() {
        return locationConfig.location;
    }

    public void setMeasurment(Measure measure) {
        updateProperties(MeasureHelper.createProperties(measure));
        Map<String, State> states = MeasureHelper.createStates(measure);
        for (Map.Entry<String, State> entry : states.entrySet()) {
            if (isLinked(entry.getKey())) {
                updateState(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Returns the serial number of this sensor.
     *
     * @return serial number of this sensor.
     */
    private String getSerialNo() {
        String serialNo = thing.getProperties().get(Thing.PROPERTY_SERIAL_NUMBER);
        if (serialNo == null) {
            serialNo = "";
        }

        return serialNo;
    }
}
