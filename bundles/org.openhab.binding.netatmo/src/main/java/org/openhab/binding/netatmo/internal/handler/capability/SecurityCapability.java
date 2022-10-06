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

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
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

    SecurityCapability(CommonInterface handler) {
        super(handler, SecurityApi.class);
    }

    @Override
    protected void updateHomeData(HomeData homeData) {
        NAObjectMap<HomeDataPerson> persons = homeData.getPersons();
        NAObjectMap<HomeDataModule> modules = homeData.getModules();
        handler.getActiveChildren().forEach(childHandler -> {
            String childId = childHandler.getId();
            persons.getOpt(childId).ifPresentOrElse(personData -> {
                personData.setIgnoredForThingUpdate(true);
                childHandler.setNewData(personData);
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
    }

    @Override
    protected void updateHomeStatus(HomeStatus homeStatus) {
        NAObjectMap<HomeStatusPerson> persons = homeStatus.getPersons();
        NAObjectMap<HomeStatusModule> modules = homeStatus.getModules();
        handler.getActiveChildren().forEach(childHandler -> {
            String childId = childHandler.getId();
            persons.getOpt(childId).ifPresentOrElse(personData -> childHandler.setNewData(personData), () -> {
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

    @Override
    protected void updateHomeEvent(HomeEvent homeEvent) {
        String personId = homeEvent.getPersonId();
        if (personId != null) {
            handler.getActiveChildren().stream().filter(handler -> personId.equals(handler.getId())).findFirst()
                    .ifPresent(handler -> {
                        homeEvent.setIgnoredForThingUpdate(true);
                        handler.setNewData(homeEvent);
                    });
        }
        String cameraId = homeEvent.getCameraId();
        handler.getActiveChildren().stream().filter(handler -> cameraId.equals(handler.getId())).findFirst()
                .ifPresent(handler -> {
                    homeEvent.setIgnoredForThingUpdate(true);
                    handler.setNewData(homeEvent);
                });
    }

    public Collection<HomeEvent> getDeviceEvents(String cameraId, String deviceType) {
        return getApi().map(api -> {
            try {
                return api.getDeviceEvents(handler.getId(), cameraId, deviceType);
            } catch (NetatmoException e) {
                logger.warn("Error retrieving last events of camera '{}' : {}", cameraId, e.getMessage());
                return null;
            }
        }).orElse(List.of());
    }

    public Collection<HomeEvent> getPersonEvents(String personId) {
        return getApi().map(api -> {
            try {
                return api.getPersonEvents(handler.getId(), personId);
            } catch (NetatmoException e) {
                logger.warn("Error retrieving last events of person '{}' : {}", personId, e.getMessage());
                return null;
            }
        }).orElse(List.of());
    }

    public void setPersonAway(String personId, boolean away) {
        getApi().ifPresent(api -> {
            try {
                api.setPersonAwayStatus(handler.getId(), personId, away);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error setting person away/at home '{}' : {}", personId, e.getMessage());
            }
        });
    }

    public @Nullable String ping(String vpnUrl) {
        return getApi().map(api -> {
            try {
                return api.ping(vpnUrl);
            } catch (NetatmoException e) {
                logger.warn("Error pinging camera '{}' : {}", vpnUrl, e.getMessage());
                return null;
            }
        }).orElse(null);
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
                api.changeFloodLightMode(handler.getId(), cameraId, mode);
                handler.expireData();
            } catch (NetatmoException e) {
                logger.warn("Error changing Presence floodlight mode '{}' : {}", mode, e.getMessage());
            }
        });
    }
}
