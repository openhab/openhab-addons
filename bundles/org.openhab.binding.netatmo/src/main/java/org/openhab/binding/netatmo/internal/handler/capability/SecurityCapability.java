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
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
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
        super(handler);
    }

    @Override
    public void initialize() {
        ApiBridgeHandler bridgeApi = handler.getRootBridge();
        if (bridgeApi != null) {
            api = Optional.ofNullable(bridgeApi.getRestManager(SecurityApi.class));
        }
    }

    @Override
    protected void updateHomeData(HomeData homeData) {
        NAObjectMap<HomeDataPerson> persons = homeData.getPersons();
        NAObjectMap<HomeDataModule> cameras = homeData.getModules();
        handler.getActiveChildren().forEach(handler -> {
            HomeDataPerson dataPerson = persons.get(handler.getId());
            if (dataPerson != null) {
                handler.setNewData(dataPerson);
            }
            HomeDataModule data = cameras.get(handler.getId());
            if (data != null) {
                handler.setNewData(data);
            }
        });
    }

    @Override
    protected void updateHomeStatus(HomeStatus homeStatus) {
        NAObjectMap<HomeStatusPerson> persons = homeStatus.getPersons();
        NAObjectMap<HomeStatusModule> cameras = homeStatus.getModules();
        handler.getActiveChildren().forEach(handler -> {
            HomeStatusPerson dataPerson = persons.get(handler.getId());
            if (dataPerson != null) {
                handler.setNewData(dataPerson);
            }
            HomeStatusModule dataCamera = cameras.get(handler.getId());
            if (dataCamera != null) {
                handler.setNewData(dataCamera);
            }
        });
    }

    @Override
    protected void updateHomeEvent(HomeEvent homeEvent) {
        String personId = homeEvent.getPersonId();
        if (personId != null) {
            handler.getActiveChildren().filter(handler -> personId.equals(handler.getId())).findFirst()
                    .ifPresent(handler -> handler.setNewData(homeEvent));
        }
        String cameraId = homeEvent.getCameraId();
        handler.getActiveChildren().filter(handler -> cameraId.equals(handler.getId())).findFirst()
                .ifPresent(handler -> handler.setNewData(homeEvent));
    }

    Collection<HomeEvent> getCameraEvents(String cameraId) {
        return api.map(api -> {

            try {
                return api.getCameraEvents(handler.getId(), cameraId);
            } catch (NetatmoException | NoSuchElementException e) {
                logger.warn("Error retrieving last events of camera '{}' : {}", cameraId, e.getMessage());
                return null;
            }
        }).orElse(List.of());
    }

    Collection<HomeEvent> getPersonEvents(String personId) {
        return api.map(api -> {
            try {
                return api.getPersonEvents(handler.getId(), personId);
            } catch (NetatmoException | NoSuchElementException e) {
                logger.warn("Error retrieving last events of person '{}' : {}", personId, e.getMessage());
                return null;
            }
        }).orElse(List.of());
    }

    void setPersonAway(String personId, boolean away) {
        api.ifPresent(api -> {
            try {
                api.setPersonAwayStatus(handler.getId(), personId, away);
                handler.expireData();
            } catch (NetatmoException | NoSuchElementException e) {
                logger.warn("Error setting person away/at home '{}' : {}", personId, e.getMessage());
            }
        });
    }

    @Nullable
    String ping(String vpnUrl) {
        return api.map(api -> {
            try {
                return api.ping(vpnUrl);
            } catch (NetatmoException | NoSuchElementException e) {
                logger.warn("Error pinging camera '{}' : {}", vpnUrl, e.getMessage());
                return null;
            }
        }).orElse(null);
    }

    void changeStatus(@Nullable String localURL, boolean status) {
        api.ifPresent(api -> {
            if (localURL != null) {
                try {
                    api.changeStatus(localURL, status);
                    handler.expireData();
                } catch (NetatmoException e) {
                    logger.warn("Error changing camera monitoring status '{}' : {}", localURL, e.getMessage());
                }
            } else {
                logger.info("Monitoring can only be done on local camera.");
            }
        });
    }

    void changeFloodlightMode(@Nullable String localURL, FloodLightMode mode) {
        api.ifPresent(api -> {
            if (localURL != null) {
                try {
                    api.changeFloodLightMode(localURL, mode);
                    handler.expireData();
                } catch (NetatmoException e) {
                    logger.warn("Error changing presence floodlight mode '{}' : {}", localURL, e.getMessage());
                }
            } else {
                logger.info("Changing floodlight mode can only be done on local camera.");
            }
        });
    }

    @Override
    protected List<NAObject> updateReadings(SecurityApi api) {
        return List.of();
    }
}
