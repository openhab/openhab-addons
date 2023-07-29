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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.Location;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
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

    private final NetatmoDescriptionProvider descriptionProvider;

    private NAObjectMap<HomeDataPerson> persons = new NAObjectMap<>();
    private NAObjectMap<HomeDataModule> modules = new NAObjectMap<>();

    private Set<FeatureArea> featuresArea = Set.of();

    public HomeCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider) {
        super(handler, HomeApi.class);
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    protected void updateHomeData(HomeData home) {
        featuresArea = home.getFeatures();
        if (hasFeature(FeatureArea.SECURITY) && !handler.getCapabilities().containsKey(SecurityCapability.class)) {
            handler.getCapabilities().put(new SecurityCapability(handler));
        }
        if (hasFeature(FeatureArea.ENERGY) && !handler.getCapabilities().containsKey(EnergyCapability.class)) {
            handler.getCapabilities().put(new EnergyCapability(handler, descriptionProvider));
        }
        if (firstLaunch) {
            home.getCountry().map(country -> properties.put(PROPERTY_COUNTRY, country));
            home.getTimezone().map(tz -> properties.put(PROPERTY_TIMEZONE, tz));
            properties.put(GROUP_LOCATION, ((Location) home).getLocation().toString());
            properties.put(PROPERTY_FEATURE, featuresArea.stream().map(f -> f.name()).collect(Collectors.joining(",")));
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

    private boolean hasFeature(FeatureArea seeked) {
        return featuresArea.contains(seeked);
    }

    public NAObjectMap<HomeDataPerson> getPersons() {
        return persons;
    }

    public NAObjectMap<HomeDataModule> getModules() {
        return modules;
    }

    @Override
    protected List<NAObject> updateReadings(HomeApi api) {
        List<NAObject> result = new ArrayList<>();
        try {
            HomeData homeData = api.getHomeData(handler.getId());
            if (homeData != null) {
                result.add(homeData);
                persons = homeData.getPersons();
                modules = homeData.getModules();
            }
            HomeStatus homeStatus = api.getHomeStatus(handler.getId());
            if (homeStatus != null) {
                result.add(homeStatus);
            }
        } catch (NetatmoException e) {
            logger.warn("Error getting Home informations : {}", e.getMessage());
        }
        return result;
    }
}
