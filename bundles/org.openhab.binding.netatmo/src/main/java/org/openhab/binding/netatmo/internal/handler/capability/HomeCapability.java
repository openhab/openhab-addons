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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeCapability} is the base class for handler able to manage persons and modules
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeCapability extends Capability<HomeApi> {
    private final Logger logger = LoggerFactory.getLogger(HomeCapability.class);

    private final String homeId;

    public HomeCapability(Bridge bridge, ApiBridge apiBridge, String homeId) {
        super(bridge, apiBridge.getRestManager(HomeApi.class));
        this.homeId = homeId;
    }

    public List<NAObject> updateReadings() throws NetatmoException {
        List<NAObject> result = new ArrayList<>();
        try {
            NAHomeData homeData = api.getHomeData(homeId);
            if (homeData != null) {
                result.add(homeData);
            }
            HomeStatus homeStatus = api.getHomeStatus(homeId);
            if (homeStatus != null) {
                result.add(homeStatus);
            }
        } catch (NetatmoException e) {
            logger.warn("Error retrieving home detailed data : {}", e.getMessage());
        }
        return result;
    }

    private @Nullable NAHomeData getHomeData() {
        try {
            NAHomeData homeData = api.getHomeData(homeId);
            return homeData;
        } catch (NetatmoException e) {
            logger.warn("Error retrieving home id '{}' : {}", homeId, e.getMessage());
        }
        return null;
    }

    public NAObjectMap<NAHomeDataPerson> getPersons() {
        NAHomeData homeData = getHomeData();
        if (homeData != null) {
            return homeData.getPersons();
        }
        return new NAObjectMap<>();
    }

    public NAObjectMap<NAHomeDataModule> getModules() {
        NAHomeData homeData = getHomeData();
        if (homeData != null) {
            return homeData.getModules();
        }
        return new NAObjectMap<>();
    }
}
