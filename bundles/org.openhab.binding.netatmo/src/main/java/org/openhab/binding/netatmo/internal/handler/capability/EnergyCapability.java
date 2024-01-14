/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.EnergyApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataRoom;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.Room;
import org.openhab.binding.netatmo.internal.config.HomeConfiguration;
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
    private String energyId = "";

    EnergyCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider) {
        super(handler, EnergyApi.class);
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    public void initialize() {
        super.initialize();
        energyId = handler.getConfiguration().as(HomeConfiguration.class).getIdForArea(FeatureArea.ENERGY);
    }

    @Override
    protected void updateHomeData(HomeData homeData) {
        if (homeData instanceof HomeData.Energy energyData) {
            NAObjectMap<HomeDataRoom> rooms = energyData.getRooms();
            NAObjectMap<HomeDataModule> modules = energyData.getModules();
            handler.getActiveChildren(FeatureArea.ENERGY).forEach(childHandler -> {
                String childId = childHandler.getId();
                rooms.getOpt(childId)
                        .ifPresentOrElse(roomData -> childHandler.setNewData(roomData.ignoringForThingUpdate()), () -> {
                            modules.getOpt(childId).ifPresent(
                                    childData -> childHandler.setNewData(childData.ignoringForThingUpdate()));
                            modules.values().stream().filter(module -> childId.equals(module.getBridge()))
                                    .forEach(bridgedModule -> childHandler.setNewData(bridgedModule));
                        });
            });
            descriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), GROUP_ENERGY, CHANNEL_PLANNING),
                    energyData.getThermSchedules().stream().map(p -> new StateOption(p.getId(), p.getName())).toList());
            setPointDefaultDuration = energyData.getThermSetpointDefaultDuration();
        }
    }

    @Override
    protected void updateHomeStatus(HomeStatus energyStatus) {
        NAObjectMap<Room> rooms = energyStatus.getRooms();
        NAObjectMap<HomeStatusModule> modules = energyStatus.getModules();
        handler.getActiveChildren(FeatureArea.ENERGY).forEach(childHandler -> {
            String childId = childHandler.getId();
            logger.trace("childId: {}", childId);
            rooms.getOpt(childId).ifPresentOrElse(roomData -> {
                logger.trace("roomData: {}", roomData);
                childHandler.setNewData(roomData);
            }, () -> {
                modules.getOpt(childId).ifPresent(moduleData -> {
                    logger.trace("moduleData: {}", moduleData);
                    childHandler.setNewData(moduleData);
                    modules.values().stream().filter(module -> childId.equals(module.getBridge()))
                            .forEach(bridgedModule -> {
                                logger.trace("bridgedModule: {}", bridgedModule);
                                childHandler.setNewData(bridgedModule);
                            });
                });
            });
        });
    }

    public void setThermPoint(String roomId, SetpointMode mode, long endtime, double temp) {
        getApi().ifPresent(api -> {
            try {
                api.setThermpoint(energyId, roomId, mode, endtime, temp);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error setting room thermostat mode '{}' : {}", mode, e.getMessage());
            }
        });
    }

    public void setRoomThermTemp(String roomId, SetpointMode mode, long endtime, double temp) {
        setThermPoint(roomId, mode, endtime, temp);
    }

    public void setRoomThermMode(String roomId, SetpointMode targetMode) {
        setThermPoint(roomId, targetMode, targetMode == SetpointMode.MAX ? setpointEndTimeFromNow() : 0, 0);
    }

    public void setRoomThermTemp(String roomId, double temp) {
        setThermPoint(roomId, SetpointMode.MANUAL, setpointEndTimeFromNow(), temp);
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        getApi().ifPresent(api -> {
            try {
                switch (channelName) {
                    case CHANNEL_PLANNING:
                        api.switchSchedule(energyId, command.toString());
                        break;
                    case CHANNEL_SETPOINT_MODE:
                        SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                        if (targetMode == SetpointMode.MANUAL) {
                            logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
                            return;
                        }
                        api.setThermMode(energyId, targetMode.apiDescriptor);
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

    private long setpointEndTimeFromNow() {
        return ZonedDateTime.now().plusMinutes(setPointDefaultDuration).toEpochSecond();
    }
}
