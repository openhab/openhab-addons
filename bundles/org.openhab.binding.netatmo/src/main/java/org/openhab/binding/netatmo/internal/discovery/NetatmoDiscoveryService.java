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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.EQUIPMENT_ID;

import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.AircareApi;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ConnectionListener;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NetatmoModule;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoDiscoveryService} searches for available Netatmo devices and modules
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
        super(ModuleType.AS_SET.stream().filter(mt -> mt != ModuleType.UNKNOWN).map(mt -> mt.thingTypeUID)
                .collect(Collectors.toSet()), DISCOVER_TIMEOUT_SECONDS);
        this.apiBridge = apiBridge;
        this.localeProvider = localeProvider;
        this.i18nProvider = translationProvider;
        apiBridge.addConnectionListener(this);
    }

    @Override
    @Deactivate
    protected void deactivate() {
        apiBridge.removeConnectionListener(this);
        super.deactivate();
    }

    @Override
    public void connectionEvent(boolean connected) {
        if (connected) {
            super.activate(null);
        } else {
            deactivate();
        }
    }

    @Override
    public void startScan() {
        try {
            AircareApi airCareApi = apiBridge.getRestManager(AircareApi.class);
            if (airCareApi != null) { // Search Healthy Home Coaches
                airCareApi.getHomeCoachData(null).getBody().getElements().stream()
                        .forEach(homeCoach -> createThing(homeCoach, null));
            }
            WeatherApi weatherApi = apiBridge.getRestManager(WeatherApi.class);
            if (weatherApi != null) { // Search favorite stations
                weatherApi.getStationsData(null, true).getBody().getElements().stream().filter(NAMain::isReadOnly)
                        .forEach(station -> {
                            ThingUID bridgeUID = createThing(station, null);
                            station.getModules().values().stream().forEach(module -> createThing(module, bridgeUID));
                        });
            }
            HomeApi homeApi = apiBridge.getRestManager(HomeApi.class);
            if (homeApi != null) { // Search all the rest
                homeApi.getHomesData(null, null).stream().filter(h -> !h.getFeatures().isEmpty()).forEach(home -> {
                    ThingUID homeUID = createThing(home, null);
                    home.getKnownPersons().forEach(person -> createThing(person, homeUID));
                    home.getModules().values().stream().forEach(device -> {
                        ModuleType deviceType = device.getType();
                        String deviceBridge = device.getBridge();
                        ThingUID bridgeUID = deviceBridge != null && deviceType.getBridge() != ModuleType.NAHome
                                ? findThingUID(deviceType.getBridge(), deviceBridge, null)
                                : deviceType.getBridge() == ModuleType.NAHome ? homeUID : null;
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
            logger.warn("Error getting Home List", e);
        }
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

    private ThingUID createThing(NetatmoModule module, @Nullable ThingUID bridgeUID) {
        ThingUID moduleUID = findThingUID(module.getType(), module.getId(), bridgeUID);
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(moduleUID)
                .withProperty(EQUIPMENT_ID, module.getId()).withRepresentationProperty(EQUIPMENT_ID)
                .withLabel(module.getName() != null ? module.getName() : module.getId());
        if (bridgeUID != null) {
            resultBuilder.withBridge(bridgeUID);
        }
        thingDiscovered(resultBuilder.build());
        return moduleUID;
    }
}
