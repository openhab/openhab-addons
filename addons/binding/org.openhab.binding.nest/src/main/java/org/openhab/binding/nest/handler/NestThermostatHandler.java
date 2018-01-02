/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.nest.NestBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.openhab.binding.nest.internal.data.Thermostat.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NestThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels for the thermostat.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
public class NestThermostatHandler extends NestBaseHandler<Thermostat> {
    private final Logger logger = LoggerFactory.getLogger(NestThermostatHandler.class);

    public NestThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, Thermostat thermostat) {
        switch (channelUID.getId()) {
            case CHANNEL_CAN_COOL:
                return getAsOnOffType(thermostat.isCanCool());
            case CHANNEL_CAN_HEAT:
                return getAsOnOffType(thermostat.isCanHeat());
            case CHANNEL_FAN_TIMER_ACTIVE:
                return getAsOnOffType(thermostat.isFanTimerActive());
            case CHANNEL_FAN_TIMER_DURATION:
                return new DecimalType(thermostat.getFanTimerDuration());
            case CHANNEL_FAN_TIMER_TIMEOUT:
                return getAsDateTimeTypeOrNull(thermostat.getFanTimerTimeout());
            case CHANNEL_HAS_FAN:
                return getAsOnOffType(thermostat.isHasFan());
            case CHANNEL_HAS_LEAF:
                return getAsOnOffType(thermostat.isHasLeaf());
            case CHANNEL_HUMIDITY:
                return new DecimalType(thermostat.getHumidity());
            case CHANNEL_LAST_CONNECTION:
                return getAsDateTimeTypeOrNull(thermostat.getLastConnection());
            case CHANNEL_LOCKED:
                return getAsOnOffType(thermostat.isLocked());
            case CHANNEL_LOCKED_MAX_SET_POINT:
                return new DecimalType(thermostat.getLockedTemperatureHigh());
            case CHANNEL_LOCKED_MIN_SET_POINT:
                return new DecimalType(thermostat.getLockedTemperatureLow());
            case CHANNEL_MAX_SET_POINT:
                return new DecimalType(thermostat.getTargetTemperatureHigh());
            case CHANNEL_MIN_SET_POINT:
                return new DecimalType(thermostat.getTargetTemperatureLow());
            case CHANNEL_MODE:
                return new StringType(thermostat.getMode().name());
            case CHANNEL_PREVIOUS_MODE:
                Mode previousMode = thermostat.getPreviousMode() != null ? thermostat.getPreviousMode()
                        : thermostat.getMode();
                return new StringType(previousMode.name());
            case CHANNEL_STATE:
                return new StringType(thermostat.getState().name());
            case CHANNEL_SET_POINT:
                return new DecimalType(thermostat.getTargetTemperature());
            case CHANNEL_SUNLIGHT_CORRECTION_ACTIVE:
                return getAsOnOffType(thermostat.isSunlightCorrectionActive());
            case CHANNEL_SUNLIGHT_CORRECTION_ENABLED:
                return getAsOnOffType(thermostat.isSunlightCorrectionEnabled());
            case CHANNEL_TEMPERATURE:
                return new DecimalType(thermostat.getAmbientTemperature());
            case CHANNEL_TIME_TO_TARGET_MINS:
                return new DecimalType(thermostat.getTimeToTarget());
            case CHANNEL_USING_EMERGENCY_HEAT:
                return getAsOnOffType(thermostat.isUsingEmergencyHeat());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    /**
     * Handle the command to do things to the thermostat, this will change the
     * value of a channel by sending the request to Nest.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            if (getLastUpdate() != null) {
                updateState(channelUID, getChannelState(channelUID, getLastUpdate()));
            }
        } else if (CHANNEL_FAN_TIMER_ACTIVE.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                // Update fan timer active to the command value
                addUpdateRequest("fan_timer_active", command == OnOffType.ON);
            }
        } else if (CHANNEL_FAN_TIMER_DURATION.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                // Update fan timer duration to the command value
                addUpdateRequest("fan_timer_duration", ((DecimalType) command).intValue());
            }
        } else if (CHANNEL_MAX_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                // Update maximum set point (Celsius) to the command value
                addUpdateRequest("target_temperature_high_c", ((DecimalType) command).floatValue());
            }
        } else if (CHANNEL_MIN_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                // Update minimum set point (Celsius) to the command value
                addUpdateRequest("target_temperature_low_c", ((DecimalType) command).floatValue());
            }
        } else if (CHANNEL_MODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                // Update the HVAC mode to the command value
                addUpdateRequest("hvac_mode", Mode.valueOf(((StringType) command).toString()));
            }
        } else if (CHANNEL_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                // Update maximum set point (Celsius) to the command value
                addUpdateRequest("target_temperature_c", ((DecimalType) command).floatValue());
            }
        }
    }

    private void addUpdateRequest(String field, Object value) {
        addUpdateRequest(NEST_THERMOSTAT_UPDATE_URL, field, value);
    }

    @Override
    public void onNewNestThermostatData(Thermostat thermostat) {
        if (isNotHandling(thermostat)) {
            logger.debug("Thermostat {} is not handling update for {}", getDeviceId(), thermostat.getDeviceId());
            return;
        }

        logger.debug("Updating thermostat {}", thermostat.getDeviceId());

        setLastUpdate(thermostat);
        updateChannels(thermostat);
        updateStatus(thermostat.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        updateProperty(PROPERTY_FIRMWARE_VERSION, thermostat.getSoftwareVersion());
    }

}
