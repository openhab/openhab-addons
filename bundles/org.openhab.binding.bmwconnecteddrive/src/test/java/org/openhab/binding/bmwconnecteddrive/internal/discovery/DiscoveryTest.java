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
package org.openhab.binding.bmwconnecteddrive.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.VehiclesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.handler.ConnectedDriveBridgeHandler;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link DiscoveryTest} Test Discovery Results
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DiscoveryTest {
    private final Logger logger = LoggerFactory.getLogger(DiscoveryTest.class);
    private static final Gson GSON = new Gson();
    private static final int DISCOVERY_VEHICLES = 9;

    @Test
    public void testDiscovery() {
        String content = FileReader.readFileInString("src/test/resources/webapi/connected-drive-account-info.json");
        ConnectedDriveBridgeHandler bh = mock(ConnectedDriveBridgeHandler.class);
        Bridge b = mock(Bridge.class);
        when(bh.getThing()).thenReturn(b);
        when(b.getUID()).thenReturn(new ThingUID("bmwconnecteddrive", "account", "abc"));
        VehicleDiscovery discovery = new VehicleDiscovery();
        discovery.setThingHandler(bh);
        DiscoveryListener listener = mock(DiscoveryListener.class);
        discovery.addDiscoveryListener(listener);
        VehiclesContainer container = GSON.fromJson(content, VehiclesContainer.class);
        ArgumentCaptor<DiscoveryResult> discoveries = ArgumentCaptor.forClass(DiscoveryResult.class);
        ArgumentCaptor<DiscoveryService> services = ArgumentCaptor.forClass(DiscoveryService.class);
        if (container != null) {
            discovery.onResponse(container);
            verify(listener, times(1)).thingDiscovered(services.capture(), discoveries.capture());
            List<DiscoveryResult> results = discoveries.getAllValues();
            assertEquals(1, results.size(), "Found Vehicles");
            DiscoveryResult result = results.get(0);
            assertEquals("bmwconnecteddrive:bev_rex:abc:MY_REAL_VIN", result.getThingUID().getAsString(), "Thing UID");
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testBimmerDiscovery() {
        String content = FileReader.readFileInString("src/test/resources/responses/vehicles.json");
        ConnectedDriveBridgeHandler bh = mock(ConnectedDriveBridgeHandler.class);
        Bridge b = mock(Bridge.class);
        when(bh.getThing()).thenReturn(b);
        when(b.getUID()).thenReturn(new ThingUID("bmwconnecteddrive", "account", "abc"));
        VehicleDiscovery discovery = new VehicleDiscovery();
        discovery.setThingHandler(bh);
        DiscoveryListener listener = mock(DiscoveryListener.class);
        discovery.addDiscoveryListener(listener);
        VehiclesContainer container = GSON.fromJson(content, VehiclesContainer.class);
        ArgumentCaptor<DiscoveryResult> discoveries = ArgumentCaptor.forClass(DiscoveryResult.class);
        ArgumentCaptor<DiscoveryService> services = ArgumentCaptor.forClass(DiscoveryService.class);

        if (container != null) {
            discovery.onResponse(container);
            verify(listener, times(DISCOVERY_VEHICLES)).thingDiscovered(services.capture(), discoveries.capture());
            List<DiscoveryResult> results = discoveries.getAllValues();
            results.forEach(entry -> {
                logger.info("{}", entry.toString());
            });
        } else {
            assertTrue(false);
        }
    }
}
