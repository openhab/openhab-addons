/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal.handler;

import com.github.mob41.blapi.FloureonDevice;
import com.github.mob41.blapi.dev.hysen.BaseStatusInfo;
import com.github.mob41.blapi.mac.Mac;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

/**
 * The {@link FloureonThermostatHandler} is responsible for handling thermostats labeled as Floureon Thermostat.
 *
 * @author Florian Mueller - Initial contribution
 */
public class FloureonThermostatHandler extends BroadlinkHandler {

    private final Logger logger = LoggerFactory.getLogger(FloureonThermostatHandler.class);
    private FloureonDevice floureonDevice;

    /**
     * Creates a new instance of this class for the {@link FloureonThermostatHandler}.
     *
     * @param thing the thing that should be handled, not null
     */
    public FloureonThermostatHandler(Thing thing) {
        super(thing);
        try {
            blDevice = new FloureonDevice(host, new Mac(mac));
            this.floureonDevice = (FloureonDevice) blDevice;
        } catch (IOException e) {
            logger.error("Could not find broadlink device at Host {} with MAC {} ", host, mac, e);
            updateStatus(ThingStatus.OFFLINE);
        }

        authenticate();

        // schedule a new scan every minute
        scanJob = scheduler.scheduleWithFixedDelay(this::refreshData, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command class: {}", command.getClass());
        logger.debug("Command: {}", command.toFullString());

        if(command == RefreshType.REFRESH){
            refreshData();
            return;
        }

        switch (channelUID.getIdWithoutGroup()) {
            case SETPOINT:
                handleSetpointCommand(channelUID, command);
                break;
            case POWER:
                handlePowerCommand(channelUID, command);
                break;
            case MODE:
                handleModeCommand(channelUID, command);
                break;
            default:
                logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handlePowerCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            try {
                floureonDevice.setPower(command == OnOffType.ON);
            } catch (Exception e) {
                logger.error("Error while setting power of {} to {}", thing.getUID(), command, e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleModeCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            try {
                if(MODE_AUTO.equals(command.toFullString())){
                    floureonDevice.switchToAuto();
                }else{
                    floureonDevice.switchToManual();
                }
            } catch (Exception e) {
                logger.error("Error while setting power of {} to {}", thing.getUID(), command, e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleSetpointCommand(ChannelUID channelUID, Command command) {
        if (command instanceof QuantityType) {
            try {
                floureonDevice.setThermostatTemp(((QuantityType) command).doubleValue());
            } catch (Exception e) {
                logger.error("Error while setting setpoint of {} to {}", thing.getUID(), command, e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void refreshData() {
        try {
            BaseStatusInfo baseStatusInfo = floureonDevice.getBasicStatus();
            if(baseStatusInfo == null){
                logger.warn("Device {} did not return any data. Trying to reauthenticate...",thing.getUID());
                authenticate();
                baseStatusInfo = floureonDevice.getBasicStatus();
            }
            if(baseStatusInfo == null){
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,"Device not responding.");
                return;
            }
            logger.debug("Retrieved data from device {}: {}", thing.getUID(), baseStatusInfo);
            logger.debug("Updating channel {} with value {}", ROOM_TEMPERATURE, new DecimalType(baseStatusInfo.getRoomTemp()));
            logger.debug("Mode {}",StringType.valueOf(baseStatusInfo.getAutoMode() ? "auto" : "manual"));
            updateState(ROOM_TEMPERATURE, new DecimalType(baseStatusInfo.getRoomTemp()));
            updateState(SETPOINT, new DecimalType(baseStatusInfo.getThermostatTemp()));
            updateState(POWER, OnOffType.from(baseStatusInfo.getPower()));
            updateState(MODE, StringType.valueOf(baseStatusInfo.getAutoMode() ? "auto" : "manual"));
            updateState(TEMPTERATURE_OFFSET, new DecimalType(baseStatusInfo.getDif()));
            updateState(ACTIVE, OnOffType.from(baseStatusInfo.getActive()));
        } catch (Exception e) {
            logger.error("Error while retrieving data for {}", thing.getUID(), e);
        }

    }
}
