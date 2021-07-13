/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.NetatmoCalendarUtils.setpointEndTimeFromNow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.EnergyApi;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAPerson;
import org.openhab.binding.netatmo.internal.api.dto.NARoom;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HomeHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeHandler extends DeviceWithEventHandler {
    private final Logger logger = LoggerFactory.getLogger(HomeHandler.class);

    private @NonNullByDefault({}) HomeApi homeApi;
    private @NonNullByDefault({}) NAHome home;
    private Optional<EnergyApi> energyApi = Optional.empty();
    private Optional<SecurityApi> securityApi = Optional.empty();
    private int setPointDefaultDuration = -1;

    public HomeHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        homeApi = apiBridge.getHomeApi();
        try {
            home = homeApi.getHomeList(config.id, null).iterator().next();
            Set<FeatureArea> capabilities = home.getModules().values().stream().map(m -> m.getType().getFeatures())
                    .collect(Collectors.toSet());
            logger.debug("Home {} will use : {}", config.id, capabilities);
            List<Channel> toBeRemovedChannels = new ArrayList<>();
            if (!capabilities.contains(FeatureArea.SECURITY)) {
                toBeRemovedChannels.addAll(channelsOfGroup(GROUP_HOME_SECURITY));
            } else {
                securityApi = apiBridge.getSecurityApi();
            }
            if (!capabilities.contains(FeatureArea.ENERGY)) {
                toBeRemovedChannels.addAll(channelsOfGroup(GROUP_HOME_ENERGY));
            } else {
                energyApi = apiBridge.getEnergyApi();
            }

            ThingBuilder builder = editThing().withoutChannels(toBeRemovedChannels);
            updateThing(builder.build());
        } catch (NetatmoException e) {
            logger.warn("Error retreiving home detailed informations : {}", e);
        }
    }

    /**
     * Collect all {@link Channel}s of the given channel group.
     *
     * @param channelGroupId the channel group id
     * @return a list of all {@link Channel}s in the given channel group
     */
    private List<Channel> channelsOfGroup(String channelGroupId) {
        logger.debug("Selecting channels of group '{}' from thing '{}'.", channelGroupId, getThing().getUID());
        return getThing().getChannelsOfGroup(channelGroupId);
    }

    @Override
    protected NAHome updateReadings() throws NetatmoException {
        home = homeApi.getHomeList(config.id, null).iterator().next();
        NAHome homeData = homeApi.getHomeData(config.id).iterator().next();
        home.setPlace(homeData.getPlace());
        energyApi.ifPresent(api -> {
            try {
                NAHome status = api.getHomeStatus(config.id);
                status.getRooms().keySet().forEach(id -> home.getModules().remove(id));
                home.getRooms().putAll(status.getRooms()); // Rooms are better handled with status data
                status.getModules().keySet().forEach(id -> home.getModules().remove(id));
                home.getModules().putAll(status.getModules()); // Energy modules are better handled with status data
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), GROUP_HOME_ENERGY, CHANNEL_PLANNING);
                descriptionProvider.setStateOptions(channelUID, home.getThermSchedules().stream()
                        .map(p -> new StateOption(p.getId(), p.getName())).collect(Collectors.toList()));
            } catch (NetatmoException e) {
                logger.warn("Error getting homestatus : {}", e);
            }
        });
        securityApi.ifPresent(api -> {
            home.getPersons().putAll(homeData.getPersons());
            home.getCameras().putAll(homeData.getCameras());
        });
        return home;
    }

    @Override
    public void setNewData(NAObject newData) {
        if (newData instanceof NAHome) {
            NAHome home = (NAHome) newData;
            this.setPointDefaultDuration = home.getThermSetpointDefaultDuration();
        }
        super.setNewData(newData);
    }

    @Override
    protected void internalSetNewEvent(NAEvent event) {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else {
            String channelName = channelUID.getIdWithoutGroup();
            if (CHANNEL_PLANNING.equals(channelName)) {
                apiBridge.getEnergyApi().ifPresent(api -> {
                    tryApiCall(() -> api.switchSchedule(config.id, command.toString()));
                });
            } else if (channelName.equals(CHANNEL_SETPOINT_MODE)) {
                SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                if (targetMode == SetpointMode.MANUAL) {
                    // updateState(channelUID, toStringType(currentData.getSetpointMode()));
                    logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
                } else {
                    callSetThermMode(config.id, targetMode);
                }
            }
        }
    }

    @Override
    protected void updateChildModules(NAObject newData) {
        super.updateChildModules(newData);
        if (newData instanceof NAHome) {
            NAHome homeData = (NAHome) newData;
            energyApi.ifPresent(api -> {
                dataListeners.entrySet().forEach(listener -> {
                    DeviceHandler child = listener.getValue();
                    if (child instanceof RoomHandler) {
                        RoomHandler room = (RoomHandler) child;
                        NARoom roomData = homeData.getRooms().get(listener.getKey());
                        if (roomData != null) {
                            room.setNewData(roomData);
                        }
                    }
                });
            });
            securityApi.ifPresent(api -> {
                dataListeners.entrySet().forEach(listener -> {
                    DeviceHandler child = listener.getValue();
                    if (child instanceof PersonHandler) {
                        PersonHandler person = (PersonHandler) child;
                        person.setCameras(homeData.getModules());
                        NAPerson personData = homeData.getPersons().get(listener.getKey());
                        if (personData != null) {
                            person.setNewData(personData);
                        }
                    } else if (child instanceof CameraHandler) {
                        CameraHandler camera = (CameraHandler) child;
                        camera.setPersons(homeData.getKnownPersons());
                        NAWelcome cameraData = homeData.getCameras().get(listener.getKey());
                        if (cameraData != null) {
                            camera.setNewData(cameraData);
                        }
                    }
                });
                /*
                 * localNaThing.getEvents().stream()// .filter(e -> e.getTime().isAfter(lastEventTime.get()))
                 * .sorted(Comparator.comparing(NAHomeEvent::getTime)).forEach(event -> {
                 * String personId = event.getPersonId();
                 * if (personId != null) {
                 * notifyListener(personId, event);
                 * }
                 * notifyListener(event.getCameraId(), event);
                 * lastEventTime.set(event.getTime());
                 * });
                 */
            });
        }
    }

    private void callSetThermMode(String homeId, SetpointMode targetMode) {
        energyApi.ifPresent(api -> {
            tryApiCall(() -> api.setThermMode(homeId, targetMode.getDescriptor()));
        });
    }

    public int getSetpointDefaultDuration() {
        return this.setPointDefaultDuration;
    }

    public void callSetPersonAway(String personId, boolean away) {
        tryApiCall(
                () -> away ? homeApi.setpersonsaway(config.id, personId) : homeApi.setpersonshome(config.id, personId));
    }

    public void callSetRoomThermMode(String roomId, SetpointMode targetMode) {
        energyApi.ifPresent(api -> {
            tryApiCall(() -> api.setRoomThermpoint(config.id, roomId, targetMode,
                    targetMode == SetpointMode.MAX ? setpointEndTimeFromNow(getSetpointDefaultDuration()) : 0, 0));
        });
    }

    public void callSetRoomThermTemp(String roomId, double temperature, long endtime, SetpointMode mode) {
        energyApi.ifPresent(api -> {
            tryApiCall(() -> api.setRoomThermpoint(config.id, roomId, mode, endtime, temperature));
        });
    }

    public void callSetRoomThermTemp(String roomId, double temperature) {
        energyApi.ifPresent(api -> {
            tryApiCall(() -> api.setRoomThermpoint(config.id, roomId, SetpointMode.MANUAL,
                    setpointEndTimeFromNow(getSetpointDefaultDuration()), temperature));
        });
    }

    public List<NAPerson> getKnownPersons() {
        return home.getKnownPersons();
    }

    public List<NAThing> getCameras() {
        return home.getModules().values().stream()
                .filter(module -> module.getType() == ModuleType.NACamera || module.getType() == ModuleType.NOC)
                .collect(Collectors.toList());
    }

    public @Nullable NAHomeEvent getLastEventOf(String personId) {
        if (securityApi.isPresent()) {
            SecurityApi api = securityApi.get();
            try {
                Collection<NAHomeEvent> events = api.getLastEventsOf(config.id, personId);
                return events.isEmpty() ? null : events.iterator().next();
            } catch (NetatmoException | NoSuchElementException e) {
                logger.warn("Error retrieving last events of person '{}' : {}", personId, e);
            }
        }
        return null;
    }

    // @Override
    // public void setEvent(NAEvent event) {
    // if (event instanceof NAWebhookEvent) {
    // NAWebhookEvent whEvent = (NAWebhookEvent) event;
    // Set<String> modules = new HashSet<>();
    // if (whEvent.getEventType().appliesOn(ModuleType.NACamera)
    // || whEvent.getEventType().appliesOn(ModuleType.NOC)) {
    // modules.add(whEvent.getCameraId());
    // }
    // if (event.getEventType().appliesOn(ModuleType.NAPerson)) {
    // modules.addAll(whEvent.getPersons().keySet());
    // }
    // modules.forEach(module -> notifyListener(module, whEvent));
    // } else {
    // super.setEvent(event);
    // }
    // }
}
