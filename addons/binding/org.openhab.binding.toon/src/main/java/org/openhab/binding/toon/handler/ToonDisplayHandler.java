/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.toon.handler;

import static org.openhab.binding.toon.ToonBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.toon.internal.api.GasUsage;
import org.openhab.binding.toon.internal.api.PowerUsage;
import org.openhab.binding.toon.internal.api.ThermostatInfo;
import org.openhab.binding.toon.internal.api.ToonConnectionException;
import org.openhab.binding.toon.internal.api.ToonState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ToonDisplayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ToonDisplayHandler extends AbstractToonHandler {
    private Logger logger = LoggerFactory.getLogger(ToonDisplayHandler.class);

    public ToonDisplayHandler(Thing thing) {
        super(thing);
    }

    private void updateThermostatInfo(ThermostatInfo info) {
        if (info == null) {
            return;
        }

        if (info.getCurrentDisplayTemp() != null) {
            BigDecimal d = new BigDecimal(info.getCurrentTemp() / 100.0).setScale(2, BigDecimal.ROUND_HALF_UP);
            updateChannel(CHANNEL_TEMPERATURE, new DecimalType(d));
        }
        if (info.getCurrentSetpoint() != null) {
            BigDecimal d = new BigDecimal(info.getCurrentSetpoint() / 100.0).setScale(2, BigDecimal.ROUND_HALF_UP);
            updateChannel(CHANNEL_SETPOINT, new DecimalType(d));
        }
        if (info.getCurrentModulationLevel() != null) {
            updateChannel(CHANNEL_MODULATION_LEVEL, new DecimalType(info.getCurrentModulationLevel()));
        }
        if (info.getActiveState() != null) {
            updateChannel(CHANNEL_SETPOINT_MODE, new DecimalType(info.getActiveState()));
        }

        if (info.getBurnerInfo() != null) {
            switch (info.getBurnerInfo()) {
                case "1":
                    updateChannel(CHANNEL_HEATING_SWITCH, OnOffType.ON);
                    updateChannel(CHANNEL_TAPWATER_SWITCH, OnOffType.OFF);
                    updateChannel(CHANNEL_PREHEAT_SWITCH, OnOffType.OFF);
                    break;
                case "2":
                    updateChannel(CHANNEL_HEATING_SWITCH, OnOffType.OFF);
                    updateChannel(CHANNEL_TAPWATER_SWITCH, OnOffType.ON);
                    updateChannel(CHANNEL_PREHEAT_SWITCH, OnOffType.OFF);
                    break;
                case "3":
                    updateChannel(CHANNEL_HEATING_SWITCH, OnOffType.OFF);
                    updateChannel(CHANNEL_TAPWATER_SWITCH, OnOffType.OFF);
                    updateChannel(CHANNEL_PREHEAT_SWITCH, OnOffType.ON);
                    break;
                default:
                    updateChannel(CHANNEL_HEATING_SWITCH, OnOffType.OFF);
                    updateChannel(CHANNEL_TAPWATER_SWITCH, OnOffType.OFF);
                    updateChannel(CHANNEL_PREHEAT_SWITCH, OnOffType.OFF);
            }
        }
    }

    private void updateGasUsage(GasUsage info) {
        if (info == null) {
            return;
        }

        if (info.getMeterReading() != null) {
            updateChannel(CHANNEL_GAS_METER_READING, new DecimalType(info.getMeterReading()));
        }
    }

    private void updatePowerUsage(PowerUsage info) {
        if (info == null) {
            return;
        }

        if (info.getMeterReading() != null) {
            updateChannel(CHANNEL_POWER_METER_READING, new DecimalType(info.getMeterReading()));
        }
        if (info.getMeterReadingLow() != null) {
            updateChannel(CHANNEL_POWER_METER_READING_LOW, new DecimalType(info.getMeterReadingLow()));
        }
        if (info.getValue() != null) {
            updateChannel(CHANNEL_POWER_CONSUMPTION, new DecimalType(info.getValue()));
        }
    }

    @Override
    protected void updateChannels(ToonState state) {
        logger.debug("Updating channels");

        // bridge has collected new data samples
        // process results
        updateThermostatInfo(state.getThermostatInfo());
        updateGasUsage(state.getGasUsage());
        updatePowerUsage(state.getPowerUsage());

        if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {} for {}", command, channelUID.getAsString());
        try {
            if (command == RefreshType.REFRESH) {
                getToonBridgeHandler().requestRefresh();
                return;
            }

            switch (channelUID.getId()) {
                case CHANNEL_SETPOINT:
                    if (command instanceof DecimalType) {
                        BigDecimal setpoint = ((DecimalType) command).toBigDecimal();
                        setpoint = setpoint.setScale(2, BigDecimal.ROUND_HALF_UP).movePointRight(2);
                        getToonBridgeHandler().getApiClient().setSetpoint(setpoint.intValue());
                    }
                    break;
                case CHANNEL_SETPOINT_MODE:
                    if (command instanceof DecimalType) {
                        DecimalType d = (DecimalType) command;
                        getToonBridgeHandler().getApiClient().setSetpointMode(d.intValue());
                    }
                    break;
                default:
                    logger.warn("unknown channel:{} / command:{}", channelUID.getAsString(), command);
            }
        } catch (ToonConnectionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }
}
