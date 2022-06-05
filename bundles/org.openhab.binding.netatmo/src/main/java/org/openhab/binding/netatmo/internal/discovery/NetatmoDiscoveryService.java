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
package org.openhab.binding.netatmo.internal.discovery;

import static java.util.Comparator.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.AircareApi;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.ListBodyResponse;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataModule;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.config.NAThingConfiguration;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoDiscoveryService} searches for available Netatmo things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NetatmoDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 3;
    private final Logger logger = LoggerFactory.getLogger(NetatmoDiscoveryService.class);

    private @Nullable ApiBridgeHandler handler;

    public NetatmoDiscoveryService() {
        super(ModuleType.AS_SET.stream().filter(mt -> !mt.apiName.isBlank()).map(mt -> mt.thingTypeUID)
                .collect(Collectors.toSet()), DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    public void startScan() {
        ApiBridgeHandler localHandler = handler;
        if (localHandler != null) {
            ThingUID accountUID = localHandler.getThing().getUID();
            try {
                AircareApi airCareApi = localHandler.getRestManager(AircareApi.class);
                if (airCareApi != null) { // Search Healthy Home Coaches
                    ListBodyResponse<NAMain> body = airCareApi.getHomeCoachData(null).getBody();
                    if (body != null) {
                        body.getElements().stream().forEach(homeCoach -> createThing(homeCoach, accountUID));
                    }
                }
                WeatherApi weatherApi = localHandler.getRestManager(WeatherApi.class);
                if (weatherApi != null) { // Search owned or favorite stations
                    weatherApi.getFavoriteAndGuestStationsData().stream().forEach(station -> {
                        if (!station.isReadOnly() || localHandler.getReadFriends()) {
                            ThingUID stationUID = createThing(station, accountUID);
                            station.getModules().values().stream().forEach(module -> createThing(module, stationUID));
                        }
                    });
                }
                HomeApi homeApi = localHandler.getRestManager(HomeApi.class);
                if (homeApi != null) { // Search those depending from a home that has modules + not only weather modules
                    homeApi.getHomesData(null, null).stream()
                            .filter(h -> !(h.getFeatures().isEmpty()
                                    || h.getFeatures().contains(FeatureArea.WEATHER) && h.getFeatures().size() == 1))
                            .forEach(home -> {
                                ThingUID homeUID = createThing(home, accountUID);

                                home.getKnownPersons().forEach(person -> createThing(person, homeUID));

                                Map<String, ThingUID> bridgesUids = new HashMap<>();

                                home.getRooms().values().stream().forEach(room -> {
                                    room.getModuleIds().stream().map(id -> home.getModules().get(id))
                                            .map(m -> m != null ? m.getType().feature : FeatureArea.NONE)
                                            .filter(f -> FeatureArea.ENERGY.equals(f)).findAny()
                                            .ifPresent(f -> bridgesUids.put(room.getId(), createThing(room, homeUID)));
                                });

                                // Creating modules that have no bridge first, avoiding weather station itself
                                home.getModules().values().stream()
                                        .filter(module -> module.getType().feature != FeatureArea.WEATHER)
                                        .sorted(comparing(HomeDataModule::getBridge, nullsFirst(naturalOrder())))
                                        .forEach(module -> {
                                            String bridgeId = module.getBridge();
                                            if (bridgeId == null) {
                                                bridgesUids.put(module.getId(), createThing(module, homeUID));
                                            } else {
                                                createThing(module, bridgesUids.getOrDefault(bridgeId, homeUID));
                                            }
                                        });
                            });
                }
            } catch (NetatmoException e) {
                logger.warn("Error during discovery process : {}", e.getMessage());
            }
        }
    }

    private ThingUID findThingUID(ModuleType thingType, String thingId, ThingUID bridgeUID) {
        ThingTypeUID thingTypeUID = thingType.thingTypeUID;
        return getSupportedThingTypes().stream().filter(supported -> supported.equals(thingTypeUID)).findFirst()
                .map(supported -> new ThingUID(supported, bridgeUID, thingId.replaceAll("[^a-zA-Z0-9_]", "")))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported device type discovered : " + thingType));
    }

    private ThingUID createThing(NAModule module, ThingUID bridgeUID) {
        ThingUID moduleUID = findThingUID(module.getType(), module.getId(), bridgeUID);
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(moduleUID)
                .withProperty(NAThingConfiguration.ID, module.getId())
                .withRepresentationProperty(NAThingConfiguration.ID)
                .withLabel(module.getName() != null ? module.getName() : module.getId()).withBridge(bridgeUID);
        thingDiscovered(resultBuilder.build());
        return moduleUID;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof ApiBridgeHandler) {
            this.handler = (ApiBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
