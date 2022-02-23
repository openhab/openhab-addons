/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.NetatmoCalendarUtils.setpointEndTimeFromNow;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.EnergyApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataRoom;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NARoom;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.core.thing.Bridge;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergyCapability} is the base class for handler able to handle energy features
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class EnergyCapability extends Capability<EnergyApi> {
    private final Logger logger = LoggerFactory.getLogger(EnergyCapability.class);

    private int setPointDefaultDuration = -1;
    private final String homeId;

    public EnergyCapability(Bridge bridge, ApiBridge apiBridge, String homeId) {
        super(bridge, apiBridge.getRestManager(EnergyApi.class));
        this.homeId = homeId;
    }

    public void updateHomeData(NAHomeData homeData) {
        NAObjectMap<NAHomeDataRoom> rooms = homeData.getRooms();
        NAObjectMap<NAHomeDataModule> modules = homeData.getModules();
        getActiveChildren().forEach(handler -> {
            NAHomeDataRoom roomData = rooms.get(handler.getId());
            if (roomData != null) {
                handler.setNewData(roomData);
            }
            NAHomeDataModule moduleData = modules.get(handler.getId());
            if (moduleData != null) {
                handler.setNewData(moduleData);
            }
        });
    }

    public void updateHomeStatus(HomeStatus homeStatus) {
        NAObjectMap<NARoom> rooms = homeStatus.getRooms();
        NAObjectMap<NAHomeStatusModule> modules = homeStatus.getModules();
        getActiveChildren().forEach(handler -> {
            if (rooms != null) {
                NARoom roomData = rooms.get(handler.getId());
                if (roomData != null) {
                    handler.setNewData(roomData);
                }
            }
            if (modules != null) {
                NAHomeStatusModule data = modules.get(handler.getId());
                if (data != null) {
                    handler.setNewData(data);
                }
            }
        });
    }

    // TODO : ensure this is called by HomeHandler
    public void setNewData(NAHomeData home) {
        setPointDefaultDuration = home.getThermSetpointDefaultDuration();
    }

    public int getSetpointDefaultDuration() {
        return setPointDefaultDuration;
    }

    public void callSetRoomThermTemp(String roomId, double temperature) {
        try {
            api.setRoomThermpoint(homeId, roomId, SetpointMode.MANUAL, setpointEndTimeFromNow(setPointDefaultDuration),
                    temperature);
            expireData();
        } catch (NetatmoException e) {
            logger.warn("Error setting room target temperature '{}' : {}", roomId, e.getMessage());
        }
    }

    public void callSetRoomThermMode(String roomId, SetpointMode targetMode) {
        try {
            api.setRoomThermpoint(homeId, roomId, targetMode,
                    targetMode == SetpointMode.MAX ? setpointEndTimeFromNow(setPointDefaultDuration) : 0, 0);
            expireData();
        } catch (NetatmoException e) {
            logger.warn("Error setting room thermostat mode '{}' : {}", roomId, e.getMessage());
        }
    }

    public void callSetRoomThermTemp(String roomId, double temperature, long endtime, SetpointMode mode) {
        try {
            api.setRoomThermpoint(homeId, roomId, mode, endtime, temperature);
            expireData();
        } catch (NetatmoException e) {
            logger.warn("Error setting room thermostat mode '{}' : {}", roomId, e.getMessage());
        }
    }

    // TODO from UCDetector: Method "EnergyCapability.handleCommand(String,Command)" has 0 references
    public void handleCommand(String channelName, Command command) { // NO_UCD (unused code)
        try {
            switch (channelName) {
                case CHANNEL_PLANNING:
                    api.switchSchedule(homeId, command.toString());
                    break;
                case CHANNEL_SETPOINT_MODE:
                    SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                    if (targetMode == SetpointMode.MANUAL) {
                        logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
                        return;
                    }
                    api.setThermMode(homeId, targetMode.apiDescriptor);
            }
            expireData();
        } catch (NetatmoException e) {
            logger.warn("Error handling command '{}' : {}", command, e.getMessage());
        }
    }
}
