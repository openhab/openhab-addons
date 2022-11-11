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
import org.openhab.binding.netatmo.internal.api.EnergyApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataRoom;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.Room;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
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

    EnergyCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider) {
        super(handler, EnergyApi.class);
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    protected void updateHomeData(HomeData homeData) {
        NAObjectMap<HomeDataRoom> rooms = homeData.getRooms();
        NAObjectMap<HomeDataModule> modules = homeData.getModules();
        handler.getActiveChildren().forEach(childHandler -> {
            String childId = childHandler.getId();
            rooms.getOpt(childId).ifPresentOrElse(roomData -> {
                roomData.setIgnoredForThingUpdate(true);
                childHandler.setNewData(roomData);
            }, () -> {
                modules.getOpt(childId).ifPresent(childData -> {
                    childData.setIgnoredForThingUpdate(true);
                    childHandler.setNewData(childData);
                });
                modules.values().stream().filter(module -> childId.equals(module.getBridge()))
                        .forEach(bridgedModule -> {
                            childHandler.setNewData(bridgedModule);
                        });
            });
        });
        descriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), GROUP_ENERGY, CHANNEL_PLANNING),
                homeData.getThermSchedules().stream().map(p -> new StateOption(p.getId(), p.getName()))
                        .collect(Collectors.toList()));
        setPointDefaultDuration = homeData.getThermSetpointDefaultDuration();
    }

    @Override
    protected void updateHomeStatus(HomeStatus homeStatus) {
        NAObjectMap<Room> rooms = homeStatus.getRooms();
        NAObjectMap<HomeStatusModule> modules = homeStatus.getModules();
        handler.getActiveChildren().forEach(childHandler -> {
            String childId = childHandler.getId();
            rooms.getOpt(childId).ifPresentOrElse(roomData -> childHandler.setNewData(roomData), () -> {
                modules.getOpt(childId).ifPresentOrElse(childData -> {
                    childHandler.setNewData(childData);
                    modules.values().stream().filter(module -> childId.equals(module.getBridge()))
                            .forEach(bridgedModule -> {
                                childHandler.setNewData(bridgedModule);
                            });

                }, () -> {
                    // This module is not present in the homestatus data, so it is considered as unreachable
                    HomeStatusModule module = new HomeStatusModule();
                    module.setReachable(false);
                    childHandler.setNewData(module);
                });
            });
        });
    }

    public int getSetpointDefaultDuration() {
        return setPointDefaultDuration;
    }

    public void setRoomThermMode(String roomId, SetpointMode targetMode) {
        getApi().ifPresent(api -> {
            try {
                api.setThermpoint(handler.getId(), roomId, targetMode,
                        targetMode == SetpointMode.MAX ? setpointEndTimeFromNow(setPointDefaultDuration) : 0, 0);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error setting room thermostat mode '{}' : {}", targetMode, e.getMessage());
            }
        });
    }

    public void setRoomThermTemp(String roomId, double temperature, long endtime, SetpointMode mode) {
        getApi().ifPresent(api -> {
            try {
                api.setThermpoint(handler.getId(), roomId, mode, endtime, temperature);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error setting room thermostat mode '{}' : {}", mode, e.getMessage());
            }
        });
    }

    public void setRoomThermTemp(String roomId, double temperature) {
        setRoomThermTemp(roomId, temperature, setpointEndTimeFromNow(setPointDefaultDuration), SetpointMode.MANUAL);
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        getApi().ifPresent(api -> {
            try {
                switch (channelName) {
                    case CHANNEL_PLANNING:
                        api.switchSchedule(handler.getId(), command.toString());
                        break;
                    case CHANNEL_SETPOINT_MODE:
                        SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                        if (targetMode == SetpointMode.MANUAL) {
                            logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
                            return;
                        }
                        api.setThermMode(handler.getId(), targetMode.apiDescriptor);
                        break;
                }
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error handling command '{}' : {}", command, e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.warn("Command '{}' sent to channel '{}' is not a valid setpoint mode.", command, channelName);
            }
        });
    }

    private static long setpointEndTimeFromNow(int duration_min) {
        return ZonedDateTime.now().plusMinutes(duration_min).toEpochSecond();
    }
}
