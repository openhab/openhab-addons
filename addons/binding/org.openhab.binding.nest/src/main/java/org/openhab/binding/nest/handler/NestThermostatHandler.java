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
import static org.openhab.binding.nest.NestBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * value of a channel by sending the request to Nest.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_MODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                // Update the HVAC mode to the command value
                addUpdateRequest("hvac_mode", Thermostat.Mode.valueOf(((StringType) command).toString()));
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
        }
    }

    private void addUpdateRequest(String field, Object value) {
        addUpdateRequest(NEST_THERMOSTAT_UPDATE_URL, field, value);
    }

    @Override
    public void onNewNestThermostatData(Thermostat thermostat) {
        if (isNotHandling(thermostat)) {
            return;
        }

        logger.debug("Updating thermostat {}", thermostat.getDeviceId());
        updateState(CHANNEL_TEMPERATURE, new DecimalType(thermostat.getAmbientTemperature()));
        updateState(CHANNEL_HUMIDITY, new PercentType(thermostat.getHumidity()));
        updateState(CHANNEL_MODE, new StringType(thermostat.getMode().name()));

        Thermostat.Mode previousMode = thermostat.getPreviousMode();
        if (previousMode == null) {
            previousMode = thermostat.getMode();
        }
        updateState(CHANNEL_PREVIOUS_MODE, new StringType(previousMode.name()));
        updateState(CHANNEL_MIN_SET_POINT, new DecimalType(thermostat.getTargetTemperatureLow()));
        updateState(CHANNEL_MAX_SET_POINT, new DecimalType(thermostat.getTargetTemperatureHigh()));
        updateState(CHANNEL_CAN_HEAT, thermostat.isCanHeat() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_CAN_COOL, thermostat.isCanCool() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_FAN_TIMER_ACTIVE, thermostat.isFanTimerActive() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_FAN_TIMER_DURATION, new DecimalType(thermostat.getFanTimerDuration()));
        updateState(CHANNEL_HAS_FAN, thermostat.isHasFan() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_HAS_LEAF, thermostat.isHasLeaf() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_USING_EMERGENCY_HEAT, thermostat.isUsingEmergencyHeat() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_LOCKED, thermostat.isLocked() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_LOCKED_MAX_SET_POINT, new DecimalType(thermostat.getLockedTemperatureHigh()));
        updateState(CHANNEL_LOCKED_MIN_SET_POINT, new DecimalType(thermostat.getLockedTemperatureLow()));
        updateState(CHANNEL_TIME_TO_TARGET_MINS, new DecimalType(thermostat.getTimeToTarget()));

        updateStatus(thermostat.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        // Setup the properties for this device.
        updateProperty(PROPERTY_FIRMWARE_VERSION, thermostat.getSoftwareVersion());
    }

    @Override
    public void onNewNestCameraData(Camera camera) {
        // ignore we are not a camera handler
    }

    @Override
    public void onNewNestSmokeDetectorData(SmokeDetector smokeDetector) {
        // ignore we are not a smoke sensor handler
    }

    @Override
    public void onNewNestStructureData(Structure struct) {
        // ignore we are not a structure handler
    }
}
