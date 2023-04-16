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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.Location;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.config.HomeConfiguration;
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
    private final Set<FeatureArea> featureAreas = new HashSet<>();
    private final NetatmoDescriptionProvider descriptionProvider;
    private final Set<String> homeIds = new HashSet<>();

    public HomeCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider) {
        super(handler, HomeApi.class);
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    public void initialize() {
        super.initialize();
        HomeConfiguration config = handler.getConfiguration().as(HomeConfiguration.class);
        homeIds.add(config.getId());
        if (!config.energyId.isBlank()) {
            homeIds.add(config.energyId);
        }
        if (!config.securityId.isBlank()) {
            homeIds.add(config.securityId);
        }
    }

    @Override
    public void dispose() {
        homeIds.clear();
        super.dispose();
    }

    @Override
    protected void updateHomeData(HomeData home) {
        if (hasArea(FeatureArea.SECURITY) && !handler.getCapabilities().containsKey(SecurityCapability.class)) {
            handler.getCapabilities().put(new SecurityCapability(handler));
        }
        if (hasArea(FeatureArea.ENERGY) && !handler.getCapabilities().containsKey(EnergyCapability.class)) {
            handler.getCapabilities().put(new EnergyCapability(handler, descriptionProvider));
        }
        if (firstLaunch) {
            home.getCountry().map(country -> properties.put(PROPERTY_COUNTRY, country));
            home.getTimezone().map(tz -> properties.put(PROPERTY_TIMEZONE, tz));
            properties.put(GROUP_LOCATION, ((Location) home).getLocation().toString());
            properties.put(PROPERTY_FEATURE,
                    featureAreas.stream().map(FeatureArea::name).collect(Collectors.joining(",")));
        }
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        super.afterNewData(newData);
        if (firstLaunch && !hasArea(FeatureArea.SECURITY)) {
            handler.removeChannels(thing.getChannelsOfGroup(GROUP_SECURITY));
        }
        if (firstLaunch && !hasArea(FeatureArea.ENERGY)) {
            handler.removeChannels(thing.getChannelsOfGroup(GROUP_ENERGY));
        }
    }

    private boolean hasArea(FeatureArea searched) {
        return featureAreas.contains(searched);
    }

    @Override
    protected List<NAObject> updateReadings(HomeApi api) {
        List<NAObject> result = new ArrayList<>();
        homeIds.stream().filter(id -> !id.isEmpty()).forEach(id -> {
            try {
                HomeData homeData = api.getHomeData(id);
                if (homeData != null) {
                    result.add(homeData);
                    featureAreas.addAll(homeData.getFeatures());
                }
                HomeStatus homeStatus = api.getHomeStatus(id);
                if (homeStatus != null) {
                    result.add(homeStatus);
                }
            } catch (NetatmoException e) {
                logger.warn("Error getting Home informations : {}", e.getMessage());
            }
        });
        return result;
    }
}
