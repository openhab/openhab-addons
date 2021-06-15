/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.EQUIPMENT_ID;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.AircareApi;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ConnectionListener;
import org.openhab.binding.netatmo.internal.api.ConnectionStatus;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.WeatherApi.NAStationDataResponse;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@Component(service = DiscoveryService.class, configurationPid = "binding.netatmo")
@NonNullByDefault
public class NetatmoDiscoveryService extends AbstractDiscoveryService implements ConnectionListener {
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(NetatmoDiscoveryService.class);
    private final ApiBridge apiBridge;

    @Activate
    public NetatmoDiscoveryService(@Reference ApiBridge apiBridge, @Reference LocaleProvider localeProvider,
            @Reference TranslationProvider translationProvider) {

        super(ModuleType.asSet.stream().filter(mt -> mt != ModuleType.UNKNOWN).map(ModuleType::getThingTypeUID)
                .collect(Collectors.toSet()), DISCOVER_TIMEOUT_SECONDS);
        this.apiBridge = apiBridge;
        this.localeProvider = localeProvider;
        this.i18nProvider = translationProvider;
        apiBridge.addConnectionListener(this);
    }

    @Override
    public void notifyStatusChange(ConnectionStatus connectionStatus) {
        if (connectionStatus.isConnected()) {
            super.activate(null);
        } else {
            super.deactivate();
        }
    }

    @Override
    public void startScan() {
        try {
            Set<@Nullable String> roomsWithEnergyModules = new HashSet<>();
            apiBridge.getHomeApi().getHomeList(null, null).stream().filter(home -> !home.getModules().isEmpty())
                    .forEach(home -> {
                        ThingUID homeUID = createHomeThing(home);
                        home.getModules().values().stream().filter(module -> module.getBridge() == null)
                                .forEach(foundDevice -> {
                                    ModuleType deviceType = foundDevice.getType();
                                    ThingUID bridgeUID = createDiscoveredThing(
                                            deviceType.getBridgeType() == ModuleType.NAHome ? homeUID : null,
                                            foundDevice);
                                    home.getModules().values().stream()
                                            .filter(module -> foundDevice.getId().equalsIgnoreCase(module.getBridge()))
                                            .forEach(foundChild -> {
                                                ModuleType childType = foundChild.getType();
                                                if (childType == ModuleType.NRV) {
                                                    int a = 1;
                                                }
                                                createDiscoveredThing(
                                                        childType.getBridgeType() == ModuleType.NAHome ? homeUID
                                                                : bridgeUID,
                                                        foundChild);
                                                if ((foundChild.getType().getFeatures() == FeatureArea.ENERGY)
                                                        && (foundChild.getRoomId() != null)) {
                                                    roomsWithEnergyModules.add(foundChild.getRoomId());
                                                }
                                            });
                                });

                        home.getRooms().values().stream().filter(r -> roomsWithEnergyModules.contains(r.getId()))
                                .forEach(room -> createDiscoveredThing(homeUID, room));
                        home.getKnownPersons().forEach(person -> createDiscoveredThing(homeUID, person));
                    });

        } catch (

        NetatmoException e) {
            logger.warn("Error getting Home List", e);
        }

        apiBridge.getAirCareApi().ifPresent(api -> searchHomeCoach(api));
        apiBridge.getWeatherApi().ifPresent(api -> searchFavoriteWeather(api));
    }

    private ThingUID findThingUID(ModuleType thingType, String thingId, @Nullable ThingUID brigdeUID)
            throws IllegalArgumentException {
        for (ThingTypeUID supported : getSupportedThingTypes()) {
            if (supported.getId().equalsIgnoreCase(thingType.name())) {
                String id = thingId.replaceAll("[^a-zA-Z0-9_]", "");
                if (brigdeUID == null) {
                    return new ThingUID(supported, id);
                }
                return new ThingUID(supported, brigdeUID, id);
            }
        }
        throw new IllegalArgumentException("Unsupported device type discovered : " + thingType);
    }

    private ThingUID createHomeThing(NAHome home) {
        ThingUID moduleUID = findThingUID(home.getType(), home.getId(), null);
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(moduleUID)
                .withProperty(EQUIPMENT_ID, home.getId())
                .withLabel(home.getName() != null ? home.getName() : home.getId())
                .withRepresentationProperty(EQUIPMENT_ID);
        thingDiscovered(resultBuilder.build());
        return moduleUID;
    }

    private ThingUID createDiscoveredThing(@Nullable ThingUID bridgeUID, NAThing module) {
        ModuleType moduleType = module.getType();

        ThingUID moduleUID = findThingUID(moduleType, module.getId(), bridgeUID);
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(moduleUID)
                .withProperty(EQUIPMENT_ID, module.getId())
                .withLabel(module.getName() != null ? module.getName() : module.getId())
                .withRepresentationProperty(EQUIPMENT_ID);
        if (bridgeUID != null) {
            resultBuilder = resultBuilder.withBridge(bridgeUID);
        }
        thingDiscovered(resultBuilder.build());
        return moduleUID;
    }

    private void searchHomeCoach(AircareApi api) {
        try {
            NAStationDataResponse homeCoaches = api.getHomeCoachData(null);
            homeCoaches.getBody().getElementsCollection().stream()
                    .forEach(homeCoach -> createDiscoveredThing(null, homeCoach));
        } catch (NetatmoException e) {
            logger.warn("Error getting Home Coaches", e);
        }
    }

    private void searchFavoriteWeather(WeatherApi api) {
        try {
            NAStationDataResponse stations = api.getStationsData(null, true);
            stations.getBody().getElementsCollection().stream().filter(NAMain::isReadOnly).forEach(station -> {
                createDiscoveredThing(null, station);
                station.getModules().values().stream().filter(module -> module.getBridge() == null)
                        .forEach(foundBridge -> {
                            ThingUID bridgeUID = createDiscoveredThing(null, foundBridge);
                            station.getModules().values().stream()
                                    .filter(module -> foundBridge.getId().equalsIgnoreCase(module.getBridge()))
                                    .forEach(foundChild -> {
                                        createDiscoveredThing(bridgeUID, foundChild);
                                    });
                        });
            });
        } catch (NetatmoException e) {
            logger.warn("Error getting stations", e);
        }
    }
}
