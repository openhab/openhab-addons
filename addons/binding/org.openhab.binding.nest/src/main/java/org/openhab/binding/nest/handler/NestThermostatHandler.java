/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_CAN_COOL;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_CAN_HEAT;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_FAN_TIMER_ACTIVE;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_FAN_TIMER_DURATION;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_HAS_FAN;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_HAS_LEAF;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_HUMIDITY;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_LOCKED;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_LOCKED_MAX_SET_POINT;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_LOCKED_MIN_SET_POINT;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_MAX_SET_POINT;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_MIN_SET_POINT;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_PREVIOUS_MODE;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_TIME_TO_TARGET_MINS;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_USING_EMERGENCY_HEAT;
import static org.openhab.binding.nest.NestBindingConstants.NEST_THERMOSTAT_UPDATE_URL;
import static org.openhab.binding.nest.NestBindingConstants.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.nest.NestBindingConstants.PROPERTY_ID;

/**
 * The {@link NestThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels for the thermostat.
 *
 * @author David Bennett - Initial contribution
 */
public class NestThermostatHandler extends NestBaseHandler {
    private Logger logger = LoggerFactory.getLogger(NestThermostatHandler.class);

    public NestThermostatHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handle the command to do things to the thermostat, this will change the
     * value of a channel by sending the request to nest.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_MODE.equals(channelUID.getId())) {
            // Change the mode.
            if (command instanceof StringType) {
                StringType cmd = (StringType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest("hvac_mode", cmd.toString());
            }
        }
        if (CHANNEL_MAX_SET_POINT.equals(channelUID.getId())) {
            // Change the set point (celcius).
            if (command instanceof DecimalType) {
                DecimalType cmd = (DecimalType) command;
                // Set the setpoint to be the cmd value.
                addUpdateRequest("target_temperature_high_c", cmd.floatValue());
            }
        }
        if (CHANNEL_MIN_SET_POINT.equals(channelUID.getId())) {
            // Change the set point (celcius).
            if (command instanceof DecimalType) {
                DecimalType cmd = (DecimalType) command;
                // Set the setpoint to be the cmd value.
                addUpdateRequest("target_temperature_low_c", cmd.floatValue());
            }
        }
        if (CHANNEL_FAN_TIMER_ACTIVE.equals(channelUID.getId())) {
            // Change the set point (celcius).
            if (command instanceof DecimalType) {
                DecimalType cmd = (DecimalType) command;
                // Set the setpoint to be the cmd value.
                addUpdateRequest("fan_timer_active", cmd.intValue() == 0 ? "false" : "true");
            }
        }
        if (CHANNEL_FAN_TIMER_DURATION.equals(channelUID.getId())) {
            // Change the set point (celcius).
            if (command instanceof DecimalType) {
                DecimalType cmd = (DecimalType) command;
                // Set the setpoint to be the cmd value.
                addUpdateRequest("fan_timer_duration", cmd.intValue());
            }
        }
    }

    /**
     * Initialize the system.
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Handlers an incoming update from the nest system.
     *
     * @param thermostat The thermostat to update
     */
    void updateThermostat(Thermostat thermostat) {
        logger.debug("Updating thermostat {}", thermostat.getDeviceId());
        updateState(CHANNEL_TEMPERATURE, new DecimalType(thermostat.getAmbientTemperature()));
        updateState(CHANNEL_HUMIDITY, new PercentType(thermostat.getHumidity()));
        updateState(CHANNEL_MODE, new StringType(thermostat.getMode()));

        String previousMode = thermostat.getPreviousMode();
        if ("".equals(previousMode)) {
            previousMode = thermostat.getMode();
        }
        updateState(CHANNEL_PREVIOUS_MODE, new StringType(previousMode));
        updateState(CHANNEL_MIN_SET_POINT, new DecimalType(thermostat.getTargetTemperatureLow()));
        updateState(CHANNEL_MAX_SET_POINT, new DecimalType(thermostat.getTargetTemperatureHigh()));
        updateState(CHANNEL_CAN_HEAT, thermostat.isCanHeat() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_CAN_COOL, thermostat.isCanCool() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_HAS_FAN, thermostat.isHasFan() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_HAS_LEAF, thermostat.isHasLeaf() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_USING_EMERGENCY_HEAT, thermostat.isUsingEmergencyHeat() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_LOCKED, thermostat.isLocked() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_LOCKED_MAX_SET_POINT, new DecimalType(thermostat.getLockedTemperatureHigh()));
        updateState(CHANNEL_LOCKED_MIN_SET_POINT, new DecimalType(thermostat.getLockedTemperatureLow()));
        updateState(CHANNEL_TIME_TO_TARGET_MINS, new DecimalType(thermostat.getTimeToTarget()));


        updateStatus(thermostat.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        // Setup the properties for this device.
        updateProperty(PROPERTY_ID, thermostat.getDeviceId());
        updateProperty(PROPERTY_FIRMWARE_VERSION, thermostat.getSoftwareVersion());
    }

    private void addUpdateRequest(String field, Object value) {
        addUpdateRequest(NEST_THERMOSTAT_UPDATE_URL, field, value);
    }
}
