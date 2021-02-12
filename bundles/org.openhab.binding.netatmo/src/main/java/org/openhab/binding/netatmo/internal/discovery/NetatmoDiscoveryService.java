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

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ConnectionListener;
import org.openhab.binding.netatmo.internal.api.ConnectionStatus;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAPerson;
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
 * @author Ing. Peter Weiss - Welcome camera implementation
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
            @Reference TranslationProvider translationProvider/* , ComponentContext componentContext */) {

        super(Stream.of(ModuleType.values()).map(supported -> supported.thingTypeUID).collect(Collectors.toSet()),
                DISCOVER_TIMEOUT_SECONDS);
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
        HashMap<String, ThingUID> localBridges = new HashMap<>();
        try {
            List<NAHome> result = apiBridge.getHomeApi().getHomeList(null);
            result.forEach(home -> {
                List<NAPerson> persons = home.getKnownPersons();
                // ThingUID homeUID = findThingUID(persons == null ? ModuleType.NAHomeEnergy :
                // ModuleType.NAHomeSecurity,
                // home.getId(), null);
                // addDiscoveredThing(homeUID, home.getId(), home.getNonNullName(), null);
                ThingUID homeUID = createDiscoveredThing(null, home,
                        persons == null ? ModuleType.NAHomeEnergy : ModuleType.NAHomeSecurity);
                home.getModules().values().stream().filter(module -> module.getBridge() == null).forEach(module -> {
                    ThingUID moduleUID = createDiscoveredThing(homeUID, module, module.getType());
                    // ThingUID moduleUID = findThingUID(module.getType(), module.getId(), homeUID);
                    // addDiscoveredThing(moduleUID, module.getId(), module.getNonNullName(), homeUID);
                    localBridges.put(module.getId(), moduleUID);
                });
                home.getModules().values().stream().filter(module -> module.getBridge() != null).forEach(module -> {
                    ThingUID bridgeUID = localBridges.get(module.getBridge());
                    if (bridgeUID != null) {
                        // ThingUID moduleUID = findThingUID(module.getType(), module.getId(), bridgeUID);
                        // addDiscoveredThing(moduleUID, module.getId(), module.getNonNullName(), homeUID);
                        createDiscoveredThing(bridgeUID, module, module.getType());
                    }
                });
                if (persons != null) {
                    persons.forEach(person -> createDiscoveredThing(homeUID, person, person.getType()));
                }
            });
        } catch (NetatmoException e) {
            logger.warn("Error getting Home List", e);
        }
        // apiBridge.getAirCareApi().ifPresent(api -> searchHomeCoach(api));
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

    private ThingUID createDiscoveredThing(@Nullable ThingUID bridgeUID, NAThing module, ModuleType moduleType) {
        ThingUID moduleUID = findThingUID(moduleType, module.getId(), bridgeUID);
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(moduleUID)
                .withProperty(EQUIPMENT_ID, module.getId()).withLabel(module.getNonNullName())
                .withRepresentationProperty(EQUIPMENT_ID);
        if (bridgeUID != null) {
            resultBuilder = resultBuilder.withBridge(bridgeUID);
        }
        thingDiscovered(resultBuilder.build());
        return moduleUID;
    }

    // private void searchHomeCoach(AircareApi api) {
    // try {
    // NADeviceDataBody<NAMain> result = api.getHomeCoachDataBody(null);
    // for (NAMain homeCoach : result.getDevices().values()) {
    // discoverHomeCoach(homeCoach);
    // }
    // } catch (NetatmoException e) {
    // logger.warn("Error retrieving thermostat(s)", e);
    // }
    // }
}
