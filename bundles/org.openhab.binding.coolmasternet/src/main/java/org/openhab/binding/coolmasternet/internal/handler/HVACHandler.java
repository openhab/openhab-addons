/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.coolmasternet.internal.handler;

import static org.openhab.binding.coolmasternet.internal.CoolMasterNetBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.coolmasternet.internal.ControllerHandler;
import org.openhab.binding.coolmasternet.internal.ControllerHandler.CoolMasterClientError;
import org.openhab.binding.coolmasternet.internal.config.HVACConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HVACHandler} is responsible for handling commands for a single
 * HVAC unit (a single UID on a CoolMasterNet controller.)
 *
 * @author Angus Gratton - Initial contribution
 * @author Wouter Born - Fix null pointer exceptions
 */
@NonNullByDefault
public class HVACHandler extends BaseThingHandler {

    /**
     * The CoolMasterNet protocol's query command returns numbers 0-5 for fan
     * speed, but the protocol's fan command (and matching binding command) use
     * single-letter abbreviations.
     */
    private static final Map<String, String> FAN_NUM_TO_STR;

    /**
     * The CoolMasterNet query command returns numbers 0-5 for operation modes,
     * but these don't map to any mode you can set on the device, so we use this
     * lookup table.
     */
    private static final Map<String, String> MODE_NUM_TO_STR;

    static {
        FAN_NUM_TO_STR = new HashMap<>();
        FAN_NUM_TO_STR.put("0", "l"); // low
        FAN_NUM_TO_STR.put("1", "m"); // medium
        FAN_NUM_TO_STR.put("2", "h"); // high
        FAN_NUM_TO_STR.put("3", "a"); // auto
        FAN_NUM_TO_STR.put("4", "t"); // top

        MODE_NUM_TO_STR = new HashMap<>();
        MODE_NUM_TO_STR.put("0", "cool");
        MODE_NUM_TO_STR.put("1", "heat");
        MODE_NUM_TO_STR.put("2", "auto");
        MODE_NUM_TO_STR.put("3", "dry");
        // 4=='haux' but this mode doesn't have an equivalent command to set it
        MODE_NUM_TO_STR.put("4", "heat");
        MODE_NUM_TO_STR.put("5", "fan");
    }

    private HVACConfiguration cfg = new HVACConfiguration();
    private final Logger logger = LoggerFactory.getLogger(HVACHandler.class);

    public HVACHandler(final Thing thing) {
        super(thing);
    }

    /**
     * Get the controller handler for this bridge.
     *
     * <p>
     * This method does not raise any exception, but if null is returned it will
     * always update the Thing status with the reason.
     *
     * <p>
     * The returned handler may or may not be connected. This method will not
     * change the Thing status simply because it is not connected, because a
     * caller may wish to attempt an operation that would result in connection.
     *
     * @return the controller handler or null if the controller is unavailable
     */
    private @Nullable ControllerHandler getControllerHandler() {
        final Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "CoolMasterNet Controller bridge not configured");
            return null;
        }

        final ControllerHandler handler = (ControllerHandler) bridge.getHandler();

        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "CoolMasterNet Controller bridge not initialized");
            return null;
        }

        return handler;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        final ControllerHandler controller = getControllerHandler();
        if (controller == null) {
            return;
        }

        final String uid = cfg.uid;
        final String channel = channelUID.getId();

        try {
            switch (channel) {
                case CURRENT_TEMP:
                    if (command instanceof RefreshType) {
                        final String currentTemp = query(controller, "a");
                        if (currentTemp != null) {
                            final Integer temp = Integer.parseInt(currentTemp);
                            final QuantityType<?> value = new QuantityType<>(temp, controller.getUnit());
                            updateState(CURRENT_TEMP, value);
                        }
                    }
                    break;
                case ON:
                    if (command instanceof RefreshType) {
                        final String on = query(controller, "o");
                        if (on != null) {
                            updateState(ON, "1".equals(on) ? OnOffType.ON : OnOffType.OFF);
                        }
                    } else if (command instanceof OnOffType onOffCommand) {
                        controller
                                .sendCommand(String.format("%s %s", onOffCommand == OnOffType.ON ? "on" : "off", uid));
                    }
                    break;
                case SET_TEMP:
                    if (command instanceof RefreshType) {
                        final String setTemp = query(controller, "t");
                        if (setTemp != null) {
                            final Integer temp = Integer.parseInt(setTemp);
                            final QuantityType<?> value = new QuantityType<>(temp, controller.getUnit());
                            updateState(SET_TEMP, value);
                        }
                    } else if (command instanceof QuantityType quantityCommand) {
                        final QuantityType<?> converted = quantityCommand.toUnit(controller.getUnit());
                        final String formatted = converted.format("%.1f");
                        controller.sendCommand(String.format("temp %s %s", uid, formatted));
                    }
                    break;
                case MODE:
                    if (command instanceof RefreshType) {
                        final String mode = MODE_NUM_TO_STR.get(query(controller, "m"));
                        if (mode != null) {
                            updateState(MODE, new StringType(mode));
                        }
                    } else if (command instanceof StringType stringCommand) {
                        final String mode = stringCommand.toString();
                        controller.sendCommand(String.format("%s %s", mode, uid));
                    }
                    break;
                case FAN_SPEED:
                    if (command instanceof RefreshType) {
                        final String fan = FAN_NUM_TO_STR.get(query(controller, "f"));
                        if (fan != null) {
                            updateState(FAN_SPEED, new StringType(fan));
                        }
                    } else if (command instanceof StringType stringCommand) {
                        final String fan = stringCommand.toString();
                        controller.sendCommand(String.format("fspeed %s %s", uid, fan));
                    }
                    break;
                case LOUVRE:
                    if (command instanceof RefreshType) {
                        final String louvre = query(controller, "s");
                        if (louvre != null) {
                            updateState(LOUVRE, new StringType(louvre));
                        }
                    } else if (command instanceof StringType stringCommand) {
                        final String louvre = stringCommand.toString();
                        controller.sendCommand(String.format("swing %s %s", uid, louvre));
                    }
                    break;
                default:
                    logger.warn("Unknown command '{}' on channel '{}' for unit '{}'", command, channel, uid);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (final IOException ioe) {
            logger.warn("Failed to handle command '{}' on channel '{}' for unit '{}' due to '{}'", command, channel,
                    uid, ioe.getLocalizedMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ioe.getLocalizedMessage());
        }
    }

    @Override
    public void initialize() {
        cfg = getConfigAs(HVACConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Update this HVAC unit's properties from the controller.
     */
    public void refresh() {
        for (final Channel channel : getThing().getChannels()) {
            handleCommand(channel.getUID(), RefreshType.REFRESH);
        }
    }

    private @Nullable String query(final ControllerHandler controller, final String queryChar)
            throws IOException, CoolMasterClientError {
        final String uid = getConfigAs(HVACConfiguration.class).uid;
        final String command = String.format("query %s %s", uid, queryChar);
        return controller.sendCommand(command);
    }
}
