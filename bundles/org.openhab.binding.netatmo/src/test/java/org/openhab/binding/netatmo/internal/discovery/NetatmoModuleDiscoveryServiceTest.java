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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import io.swagger.client.model.NAMain;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAStationModule;

/**
 * @author Sven Strohschein - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class NetatmoModuleDiscoveryServiceTest {

    private NetatmoModuleDiscoveryServiceAccessible service;
    private NetatmoBridgeHandler bridgeHandlerSpy;

    @BeforeEach
    public void before() {
        Bridge bridgeMock = mock(Bridge.class);
        when(bridgeMock.getUID()).thenReturn(new ThingUID("netatmo", "bridge"));

        bridgeHandlerSpy = spy(new NetatmoBridgeHandler(bridgeMock, null));

        LocaleProvider localeProviderMock = mock(LocaleProvider.class);
        TranslationProvider translationProvider = mock(TranslationProvider.class);

        service = new NetatmoModuleDiscoveryServiceAccessible(bridgeHandlerSpy, localeProviderMock,
                translationProvider);
    }

    @Test
    public void testStartScanNothingActivated() {
        service.startScan();

        assertEquals(0, service.getDiscoveredThings().size());
    }

    @Test
    public void testStartScanDiscoverWeatherStationNoStationsBody() {
        activateDiscoveryWeatherStation();

        service.startScan();

        assertEquals(0, service.getDiscoveredThings().size());
    }

    @Test
    public void testStartScanDiscoverWeatherStationNoStations() {
        activateDiscoveryWeatherStation();

        when(bridgeHandlerSpy.getStationsDataBody(null)).thenReturn(Optional.of(new NAStationDataBody()));
        service.startScan();

        assertEquals(0, service.getDiscoveredThings().size());
    }

    @Test
    public void testStartScanDiscoverWeatherStationNoStationName() {
        recordStationBody(createStation());

        service.startScan();

        List<DiscoveryResult> discoveredThings = service.getDiscoveredThings();
        assertEquals(1, discoveredThings.size());
        // Expected is only the type name, because a station name isn't available
        assertEquals("NAMain", discoveredThings.get(0).getLabel());
    }

    @Test
    public void testStartScanDiscoverWeatherStation() {
        NAMain station = createStation();
        station.setStationName("Neu Wulmstorf");

        recordStationBody(station);

        service.startScan();

        List<DiscoveryResult> discoveredThings = service.getDiscoveredThings();
        assertEquals(1, discoveredThings.size());
        // Expected is the type name + station name, because both are available
        // and the station name contains only the city name by default which wouldn't be sufficient.
        assertEquals("NAMain Neu Wulmstorf", discoveredThings.get(0).getLabel());
    }

    @Test
    public void testStartScanDiscoverWeatherStationNoStationNameFavorite() {
        NAMain station = createStation();
        station.setFavorite(true);

        recordStationBody(station);

        service.startScan();

        List<DiscoveryResult> discoveredThings = service.getDiscoveredThings();
        assertEquals(1, discoveredThings.size());
        // Expected is "(favorite)" within the label to make clear that it is favorite station
        // (and not the station of the user)
        assertEquals("NAMain (favorite)", discoveredThings.get(0).getLabel());
    }

    @Test
    public void testStartScanDiscoverWeatherStationFavorite() {
        NAMain station = createStation();
        station.setStationName("Neu Wulmstorf");
        station.setFavorite(true);

        recordStationBody(station);

        service.startScan();

        List<DiscoveryResult> discoveredThings = service.getDiscoveredThings();
        assertEquals(1, discoveredThings.size());
        // Expected is "(favorite)" within the label to make clear that it is favorite station
        // (and not the station of the user)
        assertEquals("NAMain Neu Wulmstorf (favorite)", discoveredThings.get(0).getLabel());
    }

    @Test
    public void testStartScanDiscoverWeatherStationModuleNoModuleName() {
        NAMain station = createStation(createModule());
        station.setStationName("Neu Wulmstorf");

        recordStationBody(station);

        service.startScan();

        List<DiscoveryResult> discoveredThings = service.getDiscoveredThings();
        assertEquals(2, discoveredThings.size());
        assertEquals("NAMain Neu Wulmstorf", discoveredThings.get(0).getLabel());
        // Expected is the type name + station name to make clear that the module belongs to the station.
        // The module name isn't available, therefore the type name of the module is used.
        assertEquals("NAModule1 Neu Wulmstorf", discoveredThings.get(1).getLabel());
    }

    @Test
    public void testStartScanDiscoverWeatherStationModule() {
        NAStationModule module = createModule();
        module.setModuleName("Outdoor-Module");

        NAMain station = createStation(module);
        station.setStationName("Neu Wulmstorf");

        recordStationBody(station);

        service.startScan();

        List<DiscoveryResult> discoveredThings = service.getDiscoveredThings();
        assertEquals(2, discoveredThings.size());
        assertEquals("NAMain Neu Wulmstorf", discoveredThings.get(0).getLabel());
        // Expected is the module name + station name to make clear that the module belongs to the station.
        // Because an explicit module name is available, the module type name isn't required.
        assertEquals("Outdoor-Module Neu Wulmstorf", discoveredThings.get(1).getLabel());
    }

    @Test
    public void testStartScanDiscoverWeatherStationModuleNoModuleNameFavorite() {
        NAMain station = createStation(createModule());
        station.setStationName("Neu Wulmstorf");
        station.setFavorite(true);

        recordStationBody(station);

        service.startScan();

        List<DiscoveryResult> discoveredThings = service.getDiscoveredThings();
        assertEquals(2, discoveredThings.size());
        assertEquals("NAMain Neu Wulmstorf (favorite)", discoveredThings.get(0).getLabel());
        // Expected is "(favorite)" within the label to make clear that it is favorite station
        // (and not the station of the user)
        assertEquals("NAModule1 Neu Wulmstorf (favorite)", discoveredThings.get(1).getLabel());
    }

    @Test
    public void testStartScanDiscoverWeatherStationModuleFavorite() {
        NAStationModule module = createModule();
        module.setModuleName("Outdoor-Module");

        NAMain station = createStation(module);
        station.setStationName("Neu Wulmstorf");
        station.setFavorite(true);

        recordStationBody(station);

        service.startScan();

        List<DiscoveryResult> discoveredThings = service.getDiscoveredThings();
        assertEquals(2, discoveredThings.size());
        assertEquals("NAMain Neu Wulmstorf (favorite)", discoveredThings.get(0).getLabel());
        // Expected is "(favorite)" within the label to make clear that it is favorite station
        // (and not the station of the user)
        assertEquals("Outdoor-Module Neu Wulmstorf (favorite)", discoveredThings.get(1).getLabel());
    }

    private void recordStationBody(NAMain station) {
        activateDiscoveryWeatherStation();

        NAStationDataBody stationsBody = new NAStationDataBody();
        stationsBody.setDevices(Collections.singletonList(station));

        when(bridgeHandlerSpy.getStationsDataBody(null)).thenReturn(Optional.of(stationsBody));
    }

    private void activateDiscoveryWeatherStation() {
        bridgeHandlerSpy.configuration.readStation = true;
    }

    private static NAMain createStation() {
        NAMain station = new NAMain();
        station.setId("01:00:00:00:00:aa");
        station.setType("NAMain");
        return station;
    }

    private static NAMain createStation(NAStationModule module) {
        NAMain station = createStation();
        station.setModules(Collections.singletonList(module));
        return station;
    }

    private static NAStationModule createModule() {
        NAStationModule module = new NAStationModule();
        module.setId("01:00:00:00:01:aa");
        module.setType("NAModule1");
        return module;
    }

    @NonNullByDefault
    private static class NetatmoModuleDiscoveryServiceAccessible extends NetatmoModuleDiscoveryService {

        private final List<DiscoveryResult> discoveredThings;

        private NetatmoModuleDiscoveryServiceAccessible(NetatmoBridgeHandler netatmoBridgeHandler,
                LocaleProvider localeProvider, TranslationProvider translationProvider) {
            super(netatmoBridgeHandler, localeProvider, translationProvider);
            discoveredThings = new ArrayList<>();
        }

        @Override
        protected void thingDiscovered(DiscoveryResult discoveryResult) {
            super.thingDiscovered(discoveryResult);
            discoveredThings.add(discoveryResult);
        }

        private List<DiscoveryResult> getDiscoveredThings() {
            return discoveredThings;
        }
    }
}
