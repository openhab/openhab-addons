/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SirenStatus;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.config.HomeConfiguration;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SecurityCapability} is the base class for handler able to handle security features
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
class SecurityCapability extends RestCapability<SecurityApi> {
    private final Logger logger = LoggerFactory.getLogger(SecurityCapability.class);

    private final Map<String, HomeEvent> eventBuffer = new HashMap<>();
    private @Nullable ZonedDateTime freshestEventTime;

    private NAObjectMap<HomeDataPerson> persons = new NAObjectMap<>();
    private NAObjectMap<HomeDataModule> modules = new NAObjectMap<>();
    private String securityId = "";

    SecurityCapability(CommonInterface handler) {
        super(handler, SecurityApi.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        freshestEventTime = null;
        securityId = handler.getConfiguration().as(HomeConfiguration.class).getIdForArea(FeatureArea.SECURITY);
    }

    @Override
    protected void updateHomeData(HomeData homeData) {
        if (homeData instanceof HomeData.Security securityData) {
            persons = securityData.getPersons();
            modules = homeData.getModules();
            handler.getActiveChildren(FeatureArea.SECURITY).forEach(childHandler -> {
                String childId = childHandler.getId();
                persons.getOpt(childId).ifPresentOrElse(
                        personData -> childHandler.setNewData(personData.ignoringForThingUpdate()), () -> {
                            modules.getOpt(childId).ifPresent(
                                    childData -> childHandler.setNewData(childData.ignoringForThingUpdate()));
                            modules.values().stream().filter(module -> childId.equals(module.getBridge()))
                                    .forEach(bridgedModule -> childHandler.setNewData(bridgedModule));
                        });
            });
        }
    }

    @Override
    protected void updateHomeStatus(HomeStatus homeStatus) {
        if (homeStatus instanceof NAHomeStatus.Security securityStatus) {
            NAObjectMap<HomeStatusPerson> persons = securityStatus.getPersons();
            NAObjectMap<HomeStatusModule> modules = securityStatus.getModules();
            handler.getActiveChildren(FeatureArea.SECURITY).forEach(childHandler -> {
                String childId = childHandler.getId();
                persons.getOpt(childId).ifPresentOrElse(personData -> childHandler.setNewData(personData), () -> {
                    modules.getOpt(childId).ifPresent(childData -> {
                        childHandler.setNewData(childData);
                        modules.values().stream().filter(module -> childId.equals(module.getBridge()))
                                .forEach(bridgedModule -> childHandler.setNewData(bridgedModule));
                    });
                });
            });
        }
    }

    @Override
    protected void updateHomeEvent(HomeEvent homeEvent) {
        addEventIfKnownObject(homeEvent, homeEvent.getPersonId());
        addEventIfKnownObject(homeEvent, homeEvent.getCameraId());
    }

    private void addEventIfKnownObject(HomeEvent homeEvent, @Nullable String objectId) {
        if (objectId == null) {
            return;
        }
        handler.getActiveChildren(FeatureArea.SECURITY).filter(child -> child.getId().equals(objectId))
                .forEach(child -> child.setNewData(homeEvent.ignoringForThingUpdate()));
    }

    @Override
    protected List<NAObject> updateReadings(SecurityApi api) {
        List<NAObject> result = new ArrayList<>();
        try {
            for (HomeEvent event : api.getHomeEvents(securityId, freshestEventTime)) {
                HomeEvent previousEvent = eventBuffer.get(event.getCameraId());
                if (previousEvent == null || previousEvent.getTime().isBefore(event.getTime())) {
                    eventBuffer.put(event.getCameraId(), event);
                }
                String personId = event.getPersonId();
                if (personId != null) {
                    previousEvent = eventBuffer.get(personId);
                    if (previousEvent == null || previousEvent.getTime().isBefore(event.getTime())) {
                        eventBuffer.put(personId, event);
                    }
                }
                if (freshestEventTime == null || event.getTime().isAfter(freshestEventTime)) {
                    freshestEventTime = event.getTime();
                }
            }
        } catch (NetatmoException e) {
            logger.warn("Error retrieving last events for home '{}' : {}", securityId, e.getMessage());
        }
        return result;
    }

    public NAObjectMap<HomeDataPerson> getPersons() {
        return persons;
    }

    public NAObjectMap<HomeDataModule> getModules() {
        return modules;
    }

    public @Nullable HomeEvent getLastPersonEvent(String personId) {
        HomeEvent event = eventBuffer.get(personId);
        if (event == null) {
            Collection<HomeEvent> events = requestPersonEvents(personId);
            if (!events.isEmpty()) {
                event = events.iterator().next();
                eventBuffer.put(personId, event);
            }
        }
        return event;
    }

    public @Nullable HomeEvent getDeviceLastEvent(String moduleId, String deviceType) {
        HomeEvent event = eventBuffer.get(moduleId);
        if (event == null) {
            Collection<HomeEvent> events = requestDeviceEvents(moduleId, deviceType);
            if (!events.isEmpty()) {
                event = events.iterator().next();
                eventBuffer.put(moduleId, event);
            }
        }
        return event;
    }

    private Collection<HomeEvent> requestDeviceEvents(String moduleId, String deviceType) {
        return getApi().map(api -> {
            try {
                return api.getDeviceEvents(securityId, moduleId, deviceType);
            } catch (NetatmoException e) {
                logger.warn("Error retrieving last events of camera '{}' : {}", moduleId, e.getMessage());
                return null;
            }
        }).orElse(List.of());
    }

    private Collection<HomeEvent> requestPersonEvents(String personId) {
        return getApi().map(api -> {
            try {
                return api.getPersonEvents(securityId, personId);
            } catch (NetatmoException e) {
                logger.warn("Error retrieving last events of person '{}' : {}", personId, e.getMessage());
                return null;
            }
        }).orElse(List.of());
    }

    public void setPersonAway(String personId, boolean away) {
        getApi().ifPresent(api -> {
            try {
                api.setPersonAwayStatus(securityId, personId, away);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error setting person away/at home '{}' : {}", personId, e.getMessage());
            }
        });
    }

    public @Nullable String ping(String vpnUrl) {
        return getApi().map(api -> api.ping(vpnUrl)).orElse(null);
    }

    public void changeStatus(@Nullable String localURL, boolean status) {
        if (localURL == null) {
            logger.info("Monitoring changes can only be done on local camera.");
            return;
        }
        getApi().ifPresent(api -> {
            try {
                api.changeStatus(localURL, status);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error changing camera monitoring status '{}' : {}", status, e.getMessage());
            }
        });
    }

    public void changeFloodlightMode(String cameraId, FloodLightMode mode) {
        getApi().ifPresent(api -> {
            try {
                api.changeFloodLightMode(securityId, cameraId, mode);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error changing Presence floodlight mode '{}' : {}", mode, e.getMessage());
            }
        });
    }

    public void changeSirenStatus(String moduleId, SirenStatus status) {
        getApi().ifPresent(api -> {
            try {
                api.changeSirenStatus(handler.getId(), moduleId, status);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error changing siren status '{}' : {}", status, e.getMessage());
            }
        });
    }
}
