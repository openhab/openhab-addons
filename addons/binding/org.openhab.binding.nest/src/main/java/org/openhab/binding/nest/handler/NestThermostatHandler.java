/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.Channel;
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
 * sent to one of the channels.
 *
 * @author David Bennett - Initial contribution
 */
public class NestThermostatHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(NestThermostatHandler.class);
    private Thermostat lastData;

    public NestThermostatHandler(Thing thing) {
        super(thing);
    }

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
                addUpdateRequest("hvac_mode", cmd.toString());
            }
        }
        if (channelUID.getId().equals(CHANNEL_MIN_SET_POINT)) {
            // Change the set point (celcius).
            if (command instanceof DecimalType) {
                DecimalType cmd = (DecimalType) command;
                // Set the setpoint to be the cmd value.
            }
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);
    }

    public void updateThermostat(Thermostat thermostat) {
        logger.debug("Updating thermostat " + thermostat.getDeviceId());
        if (lastData == null || !lastData.equals(thermostat)) {
            Channel chan = getThing().getChannel("temperature");
            updateState(chan.getUID(), new DecimalType(thermostat.getAmbientTemperature()));
            chan = getThing().getChannel("humidity");
            updateState(chan.getUID(), new PercentType(thermostat.getHumidity()));
            chan = getThing().getChannel(CHANNEL_MODE);
            updateState(chan.getUID(), new StringType(thermostat.getMode()));
            chan = getThing().getChannel("previous_hvac_mode");
            String previousMode = thermostat.getPreviousMode();
            if (previousMode.equals("")) {
                previousMode = thermostat.getMode();
            }
            updateState(chan.getUID(), new StringType(previousMode));
            chan = getThing().getChannel(CHANNEL_MIN_SET_POINT);
            updateState(chan.getUID(), new DecimalType(thermostat.getTargetTemperatureLow()));
            chan = getThing().getChannel(CHANNEL_MAX_SET_POINT);
            updateState(chan.getUID(), new DecimalType(thermostat.getTargetTemperatureHigh()));
            chan = getThing().getChannel("can_heat");
            updateState(chan.getUID(), thermostat.isCanHeat() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel("can_cool");
            updateState(chan.getUID(), thermostat.isCanCool() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel("has_fan");
            updateState(chan.getUID(), thermostat.isHasFan() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel("has_leaf");
            updateState(chan.getUID(), thermostat.isHasLeaf() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel("is_using_emergency_heat");
            updateState(chan.getUID(), thermostat.isUsingEmergencyHeat() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel("is_locked");
            updateState(chan.getUID(), thermostat.isLocked() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel("locked_max_set_point");
            updateState(chan.getUID(), new DecimalType(thermostat.getLockedTemperatureHigh()));
            chan = getThing().getChannel("locked_min_set_point");
            updateState(chan.getUID(), new DecimalType(thermostat.getLockedTemperatureLow()));
            chan = getThing().getChannel("time_to_target_mins");
            updateState(chan.getUID(), new DecimalType(thermostat.getTimeToTarget()));

            if (thermostat.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

            // Setup the properties for this device.
            updateProperty("id", thermostat.getDeviceId());
            updateProperty("firmware", thermostat.getSoftwareVersion());
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
