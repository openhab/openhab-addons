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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatusPerson;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SecurityCapability} is the base class for handler able to handle security features
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class SecurityCapability extends Capability<SecurityApi> {
    private final Logger logger = LoggerFactory.getLogger(SecurityCapability.class);

    private final String homeId;

    public SecurityCapability(Bridge bridge, ApiBridge apiBridge, String homeId) {
        super(bridge, apiBridge.getRestManager(SecurityApi.class));
        this.homeId = homeId;
    }

    public void updateHomeData(NAHomeData homeData) {
        NAObjectMap<NAHomeDataPerson> persons = homeData.getPersons();
        NAObjectMap<NAHomeDataModule> cameras = homeData.getModules();
        getActiveChildren().forEach(handler -> {
            NAHomeDataPerson dataPerson = persons.get(handler.getId());
            if (dataPerson != null) {
                handler.setNewData(dataPerson);
            }
            NAHomeDataModule data = cameras.get(handler.getId());
            if (data != null) {
                handler.setNewData(data);
            }
        });
    }

    public void updateHomeStatus(HomeStatus homeStatus) {
        NAObjectMap<NAHomeStatusPerson> persons = homeStatus.getPersons();
        NAObjectMap<NAHomeStatusModule> cameras = homeStatus.getModules();
        getActiveChildren().forEach(handler -> {
            if (persons != null) {
                NAHomeStatusPerson dataPerson = persons.get(handler.getId());
                if (dataPerson != null) {
                    handler.setNewData(dataPerson);
                }
            }
            if (cameras != null) {
                NAHomeStatusModule data = cameras.get(handler.getId());
                if (data != null) {
                    handler.setNewData(data);
                }
            }
        });
    }

    public void updateHomeEvent(NAHomeEvent homeEvent) {
        String personId = homeEvent.getPersonId();
        if (personId != null) {
            getActiveChildren().filter(handler -> personId.equals(handler.getId())).findFirst()
                    .ifPresent(handler -> handler.setNewData(homeEvent));
        }
        String cameraId = homeEvent.getCameraId();
        getActiveChildren().filter(handler -> cameraId.equals(handler.getId())).findFirst()
                .ifPresent(handler -> handler.setNewData(homeEvent));
    }

    public Collection<NAHomeEvent> getCameraEvents(String cameraId) {
        try {
            return api.getCameraEvents(homeId, cameraId);
        } catch (NetatmoException | NoSuchElementException e) {
            logger.warn("Error retrieving last events of camera '{}' : {}", cameraId, e.getMessage());
        }
        return List.of();
    }

    public Collection<NAHomeEvent> getPersonEvents(String personId) {
        try {
            return api.getPersonEvents(homeId, personId);
        } catch (NetatmoException | NoSuchElementException e) {
            logger.warn("Error retrieving last events of person '{}' : {}", personId, e.getMessage());
        }
        return List.of();
    }

    public void setPersonAway(String personId, boolean away) {
        try {
            api.setPersonAwayStatus(homeId, personId, away);
            expireData();
        } catch (NetatmoException | NoSuchElementException e) {
            logger.warn("Error setting person away/at home '{}' : {}", personId, e.getMessage());
        }
    }

    public void changeStatus(@Nullable String localURL, boolean status) {
        if (localURL != null) {
            try {
                api.changeStatus(localURL, status);
                expireData();
            } catch (NetatmoException e) {
                logger.warn("Error changing camera monitoring status '{}' : {}", localURL, e.getMessage());
            }
        } else {
            logger.info("Monitoring can only be done on local camera.");
        }
    }
}
