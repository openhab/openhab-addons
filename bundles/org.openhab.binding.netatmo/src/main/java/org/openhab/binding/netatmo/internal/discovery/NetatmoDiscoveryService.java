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

import java.util.Set;
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
    private static final Set<ModuleType> SKIPPED_TYPES = Set.of(ModuleType.UNKNOWN, ModuleType.ACCOUNT);
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(NetatmoDiscoveryService.class);
    private @Nullable ApiBridgeHandler handler;

    public NetatmoDiscoveryService() {
        super(ModuleType.AS_SET.stream().filter(mt -> !SKIPPED_TYPES.contains(mt)).map(mt -> mt.thingTypeUID)
                .collect(Collectors.toSet()), DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    public void startScan() {
        ApiBridgeHandler localHandler = handler;
        if (localHandler != null) {
            ThingUID apiBridgeUID = localHandler.getThing().getUID();
            try {
                AircareApi airCareApi = localHandler.getRestManager(AircareApi.class);
                if (airCareApi != null) { // Search Healthy Home Coaches
                    ListBodyResponse<NAMain> body = airCareApi.getHomeCoachData(null).getBody();
                    if (body != null) {
                        body.getElements().stream().forEach(homeCoach -> createThing(homeCoach, apiBridgeUID));
                    }
                }
                if (localHandler.getReadFriends()) {
                    WeatherApi weatherApi = localHandler.getRestManager(WeatherApi.class);
                    if (weatherApi != null) { // Search favorite stations
                        weatherApi.getFavoriteAndGuestStationsData().stream().filter(NAMain::isReadOnly)
                                .forEach(station -> {
                                    ThingUID bridgeUID = createThing(station, apiBridgeUID);
                                    station.getModules().values().stream()
                                            .forEach(module -> createThing(module, bridgeUID));
                                });
                    }
                }
                HomeApi homeApi = localHandler.getRestManager(HomeApi.class);
                if (homeApi != null) { // Search all the rest
                    homeApi.getHomesData(null, null).stream().filter(h -> !h.getFeatures().isEmpty()).forEach(home -> {
                        ThingUID homeUID = createThing(home, apiBridgeUID);
                        home.getKnownPersons().forEach(person -> createThing(person, homeUID));
                        home.getModules().values().stream().forEach(device -> {
                            ModuleType deviceType = device.getType();
                            String deviceBridge = device.getBridge();
                            ThingUID bridgeUID = deviceBridge != null && deviceType.getBridge() != ModuleType.HOME
                                    ? findThingUID(deviceType.getBridge(), deviceBridge, apiBridgeUID)
                                    : deviceType.getBridge() == ModuleType.HOME ? homeUID : apiBridgeUID;
                            createThing(device, bridgeUID);
                        });
                        home.getRooms().values().stream().forEach(room -> {
                            room.getModuleIds().stream().map(id -> home.getModules().get(id))
                                    .map(m -> m != null ? m.getType().feature : FeatureArea.NONE)
                                    .filter(f -> FeatureArea.ENERGY.equals(f)).findAny()
                                    .ifPresent(f -> createThing(room, homeUID));
                        });
                    });
                }
            } catch (NetatmoException e) {
                logger.warn("Error during discovery process : {}", e.getMessage());
            }
        }
    }

    private ThingUID findThingUID(ModuleType thingType, String thingId, @Nullable ThingUID brigdeUID) {
        for (ThingTypeUID supported : getSupportedThingTypes()) {
            ThingTypeUID thingTypeUID = thingType.thingTypeUID;
            if (supported.equals(thingTypeUID)) {
                String id = thingId.replaceAll("[^a-zA-Z0-9_]", "");
                return brigdeUID == null ? new ThingUID(supported, id) : new ThingUID(supported, brigdeUID, id);
            }
        }
        throw new IllegalArgumentException("Unsupported device type discovered : " + thingType);
    }

    private ThingUID createThing(NAModule module, @Nullable ThingUID bridgeUID) {
        ThingUID moduleUID = findThingUID(module.getType(), module.getId(), bridgeUID);
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(moduleUID)
                .withProperty(NAThingConfiguration.ID, module.getId())
                .withRepresentationProperty(NAThingConfiguration.ID)
                .withLabel(module.getName() != null ? module.getName() : module.getId());
        if (bridgeUID != null) {
            resultBuilder.withBridge(bridgeUID);
        }
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
