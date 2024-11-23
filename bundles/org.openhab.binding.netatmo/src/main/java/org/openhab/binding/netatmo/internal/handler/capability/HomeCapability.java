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

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAError;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.config.HomeConfiguration;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
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
public class HomeCapability extends CacheCapability<HomeApi> {
    private final Logger logger = LoggerFactory.getLogger(HomeCapability.class);
    private final Set<FeatureArea> featureAreas = new HashSet<>();
    private final NetatmoDescriptionProvider descriptionProvider;
    private final Set<String> homeIds = new HashSet<>(3);

    protected ZoneId zoneId = ZoneId.systemDefault();

    public HomeCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider) {
        super(handler, Duration.ofSeconds(2), HomeApi.class);
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    public void initialize() {
        super.initialize();
        HomeConfiguration config = handler.getThingConfigAs(HomeConfiguration.class);
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
        if (firstLaunch) {
            if (featureAreas.contains(FeatureArea.SECURITY)) {
                handler.getCapabilities().put(new SecurityCapability(handler));
            } else {
                handler.removeChannels(getThing().getChannelsOfGroup(GROUP_SECURITY));
            }
            if (featureAreas.contains(FeatureArea.ENERGY)) {
                handler.getCapabilities().put(new EnergyCapability(handler, descriptionProvider));
            } else {
                handler.removeChannels(getThing().getChannelsOfGroup(GROUP_ENERGY));
            }
            home.getCountry().map(country -> properties.put(PROPERTY_COUNTRY, country));
            zoneId = home.getZoneId(handler.getSystemTimeZone());
            properties.put(PROPERTY_TIMEZONE, zoneId.toString());
            properties.put(GROUP_LOCATION, home.getLocation().toString());
            properties.put(PROPERTY_FEATURE,
                    featureAreas.stream().map(FeatureArea::name).collect(Collectors.joining(",")));
        }
    }

    /**
     * Errored equipments are reported at home level - so we need to explore all the tree to identify modules
     * depending from a child device.
     */
    @Override
    protected void updateErrors(NAError error) {
        handler.getAllActiveChildren((Bridge) getThing()).stream()
                .filter(handler -> handler.getId().equals(error.getId())).findFirst()
                .ifPresent(handler -> handler.setNewData(error));
    }

    @Override
    protected List<NAObject> getFreshData(HomeApi api) {
        List<NAObject> result = new ArrayList<>();
        homeIds.stream().filter(id -> !id.isEmpty()).forEach(id -> {
            try {
                HomeData homeData = api.getHomeData(id);
                if (homeData != null) {
                    result.add(homeData);
                    if (featureAreas.isEmpty()) {
                        featureAreas.addAll(homeData.getFeatures());
                    }
                }

                api.getHomeStatus(id).ifPresent(body -> {
                    body.getHomeStatus().ifPresent(result::add);
                    result.addAll(body.getErrors());
                });
            } catch (NetatmoException e) {
                logger.warn("Error getting Home informations: {}", e.getMessage());
            }
        });
        return result;
    }
}
