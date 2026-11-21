/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.semsportal.internal.PortalHandler;
import org.openhab.binding.semsportal.internal.dto.Inverter;
import org.openhab.binding.semsportal.internal.dto.Station;
import org.openhab.binding.semsportal.internal.dto.StationListResponse;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;

/**
 * @author Julio Vilmar Gesser - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StationDiscoveryServiceTest {
    private static final String STATION = "00000000-0000-0000-0000-0000000000a1";
    private static final long TIMEOUT_MS = 10_000;

    private @Mock @NonNullByDefault({}) PortalHandler portal;
    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) DiscoveryListener listener;

    private @NonNullByDefault({}) StationDiscoveryService service;

    @BeforeEach
    public void setUp() throws IOException {
        when(bridge.getUID()).thenReturn(new ThingUID("semsportal", "portal", "test"));
        when(portal.getThing()).thenReturn(bridge);
        String json = Files.readString(Paths.get("src/test/resources/success_list.json"));
        List<Station> stations = Objects.requireNonNull(new Gson().fromJson(json, StationListResponse.class))
                .getStations();
        when(portal.getAllStations()).thenReturn(stations);
        service = new StationDiscoveryService(portal);
        service.addDiscoveryListener(listener);
    }

    @Test
    public void testDiscoverStation() {
        // the model and serial number are those of the first inverter
        when(portal.getInverters(STATION)).thenReturn(
                List.of(new Inverter("99999MSU99XX0001", "GW8500", 5), new Inverter("99999MSU99XX0002", "GW5000", 5)));

        DiscoveryResult result = discover();

        assertEquals("semsportal:station:test:" + STATION, result.getThingUID().getAsString());
        assertEquals(new ThingUID("semsportal", "portal", "test"), result.getBridgeUID());
        Map<String, Object> properties = result.getProperties();
        assertEquals("99999MSU99XX0001", properties.get("serialNumber"));
        assertEquals("GW8500", properties.get("modelId"));
        assertEquals("Example station", properties.get("stationName"));
        assertEquals(8.19, properties.get("stationCapacity"));
        assertEquals(STATION, properties.get("stationUUID"));
        assertEquals("stationUUID", result.getRepresentationProperty());
    }

    @Test
    public void testDiscoverStationWithoutModel() {
        when(portal.getInverters(STATION)).thenReturn(List.of(new Inverter("99999MSU99XX0001", null, -1)));

        Map<String, Object> properties = discover().getProperties();

        assertEquals("99999MSU99XX0001", properties.get("serialNumber"));
        assertFalse(properties.containsKey("modelId"));
    }

    @Test
    public void testDiscoverStationWithoutInverters() {
        when(portal.getInverters(STATION)).thenReturn(List.of());

        Map<String, Object> properties = discover().getProperties();

        assertFalse(properties.containsKey("serialNumber"));
        assertFalse(properties.containsKey("modelId"));
        assertEquals(STATION, properties.get("stationUUID"));
    }

    private DiscoveryResult discover() {
        service.startScan();

        ArgumentCaptor<DiscoveryResult> result = ArgumentCaptor.forClass(DiscoveryResult.class);
        // the framework hands the results to the listeners on its scheduler
        verify(listener, timeout(TIMEOUT_MS)).thingDiscovered(any(), result.capture());
        return result.getValue();
    }
}
