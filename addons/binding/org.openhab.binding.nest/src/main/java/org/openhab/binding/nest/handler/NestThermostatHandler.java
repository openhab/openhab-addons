/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.openhab.binding.nest.NestBindingConstants.*;

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

/**
 * The {@link NestThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels for the thermostat.
 *
 * @author David Bennett - Initial contribution
 */
public class NestThermostatHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(NestThermostatHandler.class);
    private Thermostat lastData;

    public NestThermostatHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handle the command to do things to the thermostat, this will change the
     * value of a channel by sending the request to nest.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_MODE)) {
            // Change the mode.
            if (command instanceof StringType) {
                StringType cmd = (StringType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest("hvac_mode", cmd.toString());
            }
        }
        if (channelUID.getId().equals(CHANNEL_MAX_SET_POINT)) {
            // Change the set point (celcius).
            if (command instanceof DecimalType) {
                DecimalType cmd = (DecimalType) command;
                // Set the setpoint to be the cmd value.
                addUpdateRequest("target_temperature_high_c", cmd.floatValue());
            }
        }
        if (channelUID.getId().equals(CHANNEL_MIN_SET_POINT)) {
            // Change the set point (celcius).
            if (command instanceof DecimalType) {
                DecimalType cmd = (DecimalType) command;
                // Set the setpoint to be the cmd value.
                addUpdateRequest("target_temperature_low_c", cmd.floatValue());
            }
        }
        if (channelUID.getId().equals(CHANNEL_FAN_TIMER_ACTIVE)) {
            // Change the set point (celcius).
            if (command instanceof DecimalType) {
                DecimalType cmd = (DecimalType) command;
                // Set the setpoint to be the cmd value.
                addUpdateRequest("fan_timer_active", cmd.intValue() == 0 ? "false" : "true");
            }
        }
        if (channelUID.getId().equals(CHANNEL_FAN_TIMER_DURATION)) {
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
     * Dispose the handler, cleaning up memory.
     */
    @Override
    public void dispose() {
        this.lastData = null;
        super.dispose();
    }

    /**
     * Handlers an incoming update from the nest system.
     *
     * @param thermostat The thermostat to update
     */
    public void updateThermostat(Thermostat thermostat) {
        logger.debug("Updating thermostat {}", thermostat.getDeviceId());
        if (lastData == null || !lastData.equals(thermostat)) {
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

            if (thermostat.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

            // Setup the properties for this device.
            updateProperty(PROPERTY_ID, thermostat.getDeviceId());
            updateProperty(PROPERTY_FIRMWARE_VERSION, thermostat.getSoftwareVersion());
        } else {
            logger.debug("Nothing to update, same as before.");
        }
    }

    /** Creates the url to set a specific value on the thermostat. */
    private void addUpdateRequest(String field, Object value) {
        String deviceId = getThing().getProperties().get(NestBindingConstants.PROPERTY_ID);
        StringBuilder builder = new StringBuilder().append(NestBindingConstants.NEST_THERMOSTAT_UPDATE_URL)
                .append(deviceId);
        NestUpdateRequest request = new NestUpdateRequest();
        request.setUpdateUrl(builder.toString());
        request.addValue(field, value);
        NestBridgeHandler bridge = (NestBridgeHandler) getBridge();
        bridge.addUpdateRequest(request);
    }
}
