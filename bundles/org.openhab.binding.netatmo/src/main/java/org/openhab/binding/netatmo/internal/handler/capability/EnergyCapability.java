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

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

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
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergyCapability} is the base class for handler able to handle energy features
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class EnergyCapability extends RestCapability<EnergyApi> {
    private final Logger logger = LoggerFactory.getLogger(EnergyCapability.class);

    private int setPointDefaultDuration = -1;
    private final NetatmoDescriptionProvider descriptionProvider;

    public EnergyCapability(NACommonInterface handler, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(handler, apiBridge.getRestManager(EnergyApi.class));
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    protected void updateHomeData(NAHomeData homeData) {
        NAObjectMap<NAHomeDataRoom> rooms = homeData.getRooms();
        NAObjectMap<NAHomeDataModule> modules = homeData.getModules();
        handler.getActiveChildren().forEach(handler -> {
            NAHomeDataRoom roomData = rooms.get(handler.getId());
            if (roomData != null) {
                handler.setNewData(roomData);
            }
            NAHomeDataModule moduleData = modules.get(handler.getId());
            if (moduleData != null) {
                handler.setNewData(moduleData);
            }
        });
        descriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), GROUP_ENERGY, CHANNEL_PLANNING),
                homeData.getThermSchedules().stream().map(p -> new StateOption(p.getId(), p.getName()))
                        .collect(Collectors.toList()));
        setPointDefaultDuration = homeData.getThermSetpointDefaultDuration();
    }

    @Override
    protected void updateHomeStatus(HomeStatus homeStatus) {
        NAObjectMap<NARoom> rooms = homeStatus.getRooms();
        NAObjectMap<NAHomeStatusModule> modules = homeStatus.getModules();
        handler.getActiveChildren().forEach(handler -> {
            NARoom roomData = rooms.get(handler.getId());
            if (roomData != null) {
                handler.setNewData(roomData);
            }
            NAHomeStatusModule data = modules.get(handler.getId());
            if (data != null) {
                handler.setNewData(data);
            }
        });
    }

    public int getSetpointDefaultDuration() {
        return setPointDefaultDuration;
    }

    public void setRoomThermMode(String roomId, SetpointMode targetMode) {
        try {
            api.setRoomThermpoint(handlerId, roomId, targetMode,
                    targetMode == SetpointMode.MAX ? setpointEndTimeFromNow(setPointDefaultDuration) : 0, 0);
            handler.expireData();
        } catch (NetatmoException e) {
            logger.warn("Error setting room thermostat mode '{}' : {}", roomId, e.getMessage());
        }
    }

    public void setRoomThermTemp(String roomId, double temperature, long endtime, SetpointMode mode) {
        try {
            api.setRoomThermpoint(handlerId, roomId, mode, endtime, temperature);
            handler.expireData();
        } catch (NetatmoException e) {
            logger.warn("Error setting room thermostat mode '{}' : {}", roomId, e.getMessage());
        }
    }

    public void setRoomThermTemp(String roomId, double temperature) {
        setRoomThermTemp(roomId, temperature, setpointEndTimeFromNow(setPointDefaultDuration), SetpointMode.MANUAL);
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        try {
            switch (channelName) {
                case CHANNEL_PLANNING:
                    api.switchSchedule(handlerId, command.toString());
                    break;
                case CHANNEL_SETPOINT_MODE:
                    SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                    if (targetMode == SetpointMode.MANUAL) {
                        logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
                        return;
                    }
                    api.setThermMode(handlerId, targetMode.apiDescriptor);
            }
            handler.expireData();
        } catch (NetatmoException e) {
            logger.warn("Error handling command '{}' : {}", command, e.getMessage());
        }
    }

    public static long setpointEndTimeFromNow(int duration_min) {
        return ZonedDateTime.now().plusMinutes(duration_min).toEpochSecond();
    }
}
