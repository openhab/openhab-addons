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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaWaterHeatingSystemHandler} is responsible for handling commands,
 * which are sent to one of the channels of the Water Heating system thing.
 *
 * @author Benjamin Lafois - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaWaterHeatingSystemHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaWaterHeatingSystemHandler.class);

    private boolean boostMode = false;
    private boolean awayMode = false;

    public SomfyTahomaWaterHeatingSystemHandler(Thing thing) {
        super(thing);

        stateNames.put(MIDDLEWATER_TEMPERATURE, MIDDLE_WATER_TEMPERATURE_STATE);
        stateNames.put(TARGET_TEMPERATURE, TARGET_TEMPERATURE_STATE);
        stateNames.put(WATER_HEATER_MODE, WATER_HEATER_MODE_STATE);

        stateNames.put(BOOST_MODE_DURATION, BOOST_MODE_DURATION_STATE);
        // override state type because the cloud sends consumption in percent
        cacheStateType(BOOST_MODE_DURATION_STATE, TYPE_DECIMAL);

        stateNames.put(AWAY_MODE_DURATION, AWAY_MODE_DURATION_STATE);
        // override state type because the cloud sends consumption in percent
        cacheStateType(AWAY_MODE_DURATION_STATE, TYPE_DECIMAL);

        stateNames.put(HEAT_PUMP_OPERATING_TIME, HEAT_PUMP_OPERATING_TIME_STATE);
        // override state type because the cloud sends consumption in percent
        cacheStateType(HEAT_PUMP_OPERATING_TIME_STATE, TYPE_DECIMAL);

        stateNames.put(ELECTRIC_BOOSTER_OPERATING_TIME, ELECTRIC_BOOSTER_OPERATING_TIME_STATE);
        // override state type because the cloud sends consumption in percent
        cacheStateType(ELECTRIC_BOOSTER_OPERATING_TIME_STATE, TYPE_DECIMAL);

        stateNames.put(POWER_HEAT_PUMP, POWER_HEAT_PUMP_STATE);
        // override state type because the cloud sends consumption in percent
        cacheStateType(POWER_HEAT_PUMP_STATE, TYPE_DECIMAL);

        stateNames.put(POWER_HEAT_ELEC, POWER_HEAT_ELEC_STATE);
        // override state type because the cloud sends consumption in percent
        cacheStateType(POWER_HEAT_ELEC_STATE, TYPE_DECIMAL);
    }

    @Override
    public void updateThingChannels(SomfyTahomaState state) {
        if (OPERATING_MODE_STATE.equals(state.getName()) && state.getValue() instanceof Map) {
            logger.debug("Operating Mode State: {}  {}", state.getValue().getClass().getName(), state.getValue());

            Map<String, String> data = (Map<String, String>) state.getValue();

            Object relaunchValue = data.get("relaunch");
            if (relaunchValue != null) {
                this.boostMode = relaunchValue.toString().equalsIgnoreCase("on");
                logger.debug("Boost Value: {}", this.boostMode);
                updateState(BOOST_MODE, OnOffType.from(this.boostMode));
            }

            Object awayValue = data.get("absence");
            if (awayValue != null) {
                this.awayMode = awayValue.toString().equalsIgnoreCase("on");
                logger.debug("Away Value: {}", this.awayMode);
                updateState(AWAY_MODE, OnOffType.from(this.awayMode));
            }
        } else if (TARGET_TEMPERATURE_STATE.equals(state.getName())) {
            logger.debug("Target Temperature: {}", state.getValue());
            // 50 -> 3
            // 54.5 -> 4
            // 62 -> 5
            Double temp = null;
            try {
                temp = Double.parseDouble(state.getValue().toString());

                int v = 0;
                if (temp == 50) {
                    v = 3;
                } else if (temp == 54.5) {
                    v = 4;
                } else if (temp == 62) {
                    v = 5;
                }

                updateState(SHOWERS, new DecimalType(v));
            } catch (NumberFormatException e) {
                logger.warn("Unexpected pre-defined value for Target State Temperature: {}", state.getValue());
                return;
            }

        }

        super.updateThingChannels(state);
    }

    private void sendOperatingMode() {
        sendCommand(COMMAND_SET_CURRENT_OPERATING_MODE, String.format("[ { \"relaunch\":\"%s\", \"absence\":\"%s\"} ]",
                (this.boostMode ? "on" : "off"), (this.awayMode ? "on" : "off")));
    }

    private void sendBoostDuration(int duration) {
        sendCommand(COMMAND_SET_BOOST_MODE_DURATION, "[ " + duration + " ]");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (command instanceof RefreshType) {
            return;
        } else {
            logger.debug("Command received: {}/{}", channelUID.getId(), command.toString());

            if (BOOST_MODE_DURATION.equals(channelUID.getId())) {
                int duration = 0;
                try {
                    duration = Integer.parseInt(command.toString());
                } catch (NumberFormatException e) {
                    logger.debug("Invalid value received for boost mode duration: {}", command);
                    return;
                }
                if (duration == 0) {
                    this.boostMode = false;
                    sendOperatingMode();
                } else if (duration > 0 && duration < 8) {
                    this.boostMode = true;
                    sendOperatingMode();
                    sendBoostDuration(duration);
                }
            } else if (WATER_HEATER_MODE.equals(channelUID.getId())) {
                sendCommand(COMMAND_SET_WATER_HEATER_MODE, "[ \"" + command.toString() + "\" ]");
            } else if (AWAY_MODE_DURATION.equals(channelUID.getId())) {
                sendCommand(COMMAND_SET_AWAY_MODE_DURATION, "[ \"" + command.toString() + "\" ]");
            } else if (BOOST_MODE.equals(channelUID.getId()) && command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    if (this.boostMode) {
                        return;
                    }
                    this.boostMode = true;

                    scheduler.execute(() -> {
                        sendBoostDuration(1); // by default, boost for 1 day
                    });

                    scheduler.schedule(() -> {
                        sendCommand(COMMAND_REFRESH_DHWMODE, "[ ]");
                    }, 1, TimeUnit.SECONDS);

                    scheduler.schedule(() -> {
                        sendOperatingMode();
                    }, 2, TimeUnit.SECONDS);

                    scheduler.schedule(() -> {
                        sendCommand(COMMAND_REFRESH_BOOST_MODE_DURATION, "[ ]");
                    }, 3, TimeUnit.SECONDS);

                } else {
                    this.boostMode = false;
                    sendOperatingMode();
                }
            } else if (AWAY_MODE.equals(channelUID.getId()) && command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    this.boostMode = false;
                    this.awayMode = true;
                } else {
                    this.awayMode = false;
                }
                sendOperatingMode();
            } else if (SHOWERS.equals(channelUID.getId())) {
                int showers = 0;
                try {
                    showers = Integer.parseInt(command.toString());
                } catch (NumberFormatException e) {
                    logger.info("Received an invalid value for desired number of showers: {}", command);
                    return;
                }
                Double value = 0.0;

                switch (showers) {
                    case 3:
                        value = 50.0;
                        break;
                    case 4:
                        value = 54.5;
                        break;
                    case 5:
                        value = 62.0;
                        break;
                    default:
                        break;
                }
                sendCommand(COMMAND_SET_TARGET_TEMPERATURE, "[ " + value.toString() + " ]");
            }

        }
    }
}
