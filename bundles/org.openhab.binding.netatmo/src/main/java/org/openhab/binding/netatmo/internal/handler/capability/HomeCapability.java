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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NetatmoLocation;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeCapability} is the base class for handler able to manage persons and modules
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeCapability extends RestCapability<HomeApi> {
    private final Logger logger = LoggerFactory.getLogger(HomeCapability.class);

    private final ApiBridge apiBridge;
    private final NetatmoDescriptionProvider descriptionProvider;

    private NAObjectMap<NAHomeDataPerson> persons = new NAObjectMap<>();
    private NAObjectMap<NAHomeDataModule> modules = new NAObjectMap<>();

    private Set<FeatureArea> featuresArea = Set.of();

    public HomeCapability(NACommonInterface handler, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(handler, apiBridge.getRestManager(HomeApi.class));
        this.apiBridge = apiBridge;
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    protected void updateHomeData(NAHomeData home) {
        featuresArea = home.getFeatures();
        if (hasFeature(FeatureArea.SECURITY) && !handler.getCapabilities().containsKey(SecurityCapability.class)) {
            handler.getCapabilities().put(new SecurityCapability(handler, apiBridge));
        }
        if (hasFeature(FeatureArea.ENERGY) && !handler.getCapabilities().containsKey(EnergyCapability.class)) {
            handler.getCapabilities().put(new EnergyCapability(handler, apiBridge, descriptionProvider));
        }
        if (firstLaunch) {
            home.getCountry().map(country -> properties.put(PROPERTY_COUNTRY, country));
            home.getTimezone().map(tz -> properties.put(PROPERTY_TIMEZONE, tz));
            properties.put(GROUP_LOCATION, ((NetatmoLocation) home).getLocation().toString());
            FeatureArea.AS_SET.stream().filter(area -> area != FeatureArea.NONE)
                    .forEach(area -> properties.put(area.name(), Boolean.toString(featuresArea.contains(area))));
        }
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        super.afterNewData(newData);
        if (firstLaunch && !hasFeature(FeatureArea.SECURITY)) {
            handler.removeChannels(thing.getChannelsOfGroup(GROUP_SECURITY));
        }
        if (firstLaunch && !hasFeature(FeatureArea.ENERGY)) {
            handler.removeChannels(thing.getChannelsOfGroup(GROUP_ENERGY));
        }
    }

    @Override
    public List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        try {
            NAHomeData homeData = api.getHomeData(handlerId);
            if (homeData != null) {
                result.add(homeData);
                persons = homeData.getPersons();
                modules = homeData.getModules();
            }
            HomeStatus homeStatus = api.getHomeStatus(handlerId);
            if (homeStatus != null) {
                result.add(homeStatus);
            }
        } catch (NetatmoException e) {
            logger.warn("Error gettting Home informations : {}", e.getMessage());
        }
        handler.getActiveChildren().forEach(handler -> result.addAll(handler.updateReadings()));
        return result;
    }

    public boolean hasFeature(FeatureArea seeked) {
        return featuresArea.contains(seeked);
    }

    public NAObjectMap<NAHomeDataPerson> getPersons() {
        return persons;
    }

    public NAObjectMap<NAHomeDataModule> getModules() {
        return modules;
    }
}
