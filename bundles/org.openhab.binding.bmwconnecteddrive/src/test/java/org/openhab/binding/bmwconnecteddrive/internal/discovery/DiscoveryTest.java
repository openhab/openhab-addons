/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.VehiclesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.handler.ConnectedDriveBridgeHandler;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;
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

    @Test
    public void testDiscovery() {
        String content = FileReader.readFileInString("src/test/resources/webapi/connected-drive-account-info.json");
        ConnectedDriveBridgeHandler bh = mock(ConnectedDriveBridgeHandler.class);
        Bridge b = mock(Bridge.class);
        when(bh.getThing()).thenReturn(b);
        when(b.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        ConnectedCarDiscovery discovery = new ConnectedCarDiscovery(bh);
        DiscoveryListener listener = mock(DiscoveryListener.class);
        discovery.addDiscoveryListener(listener);
        VehiclesContainer container = GSON.fromJson(content, VehiclesContainer.class);
        ArgumentCaptor<DiscoveryResult> discoveries = ArgumentCaptor.forClass(DiscoveryResult.class);
        ArgumentCaptor<DiscoveryService> services = ArgumentCaptor.forClass(DiscoveryService.class);

        discovery.onResponse(container);
        verify(listener, times(1)).thingDiscovered(services.capture(), discoveries.capture());
        List<DiscoveryResult> results = discoveries.getAllValues();
        assertEquals("Found Vehicles", 1, results.size());
        DiscoveryResult result = results.get(0);
        assertEquals("Thing UID", "bmwconnecteddrive:BEV_REX:MY_REAL_VIN", result.getThingUID().getAsString());
    }

    @Test
    public void testBimmerDiscovery() {
        String content = FileReader.readFileInString("src/test/resources/responses/vehicles.json");
        ConnectedDriveBridgeHandler bh = mock(ConnectedDriveBridgeHandler.class);
        Bridge b = mock(Bridge.class);
        when(bh.getThing()).thenReturn(b);
        when(b.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        ConnectedCarDiscovery discovery = new ConnectedCarDiscovery(bh);
        DiscoveryListener listener = mock(DiscoveryListener.class);
        discovery.addDiscoveryListener(listener);
        VehiclesContainer container = GSON.fromJson(content, VehiclesContainer.class);
        ArgumentCaptor<DiscoveryResult> discoveries = ArgumentCaptor.forClass(DiscoveryResult.class);
        ArgumentCaptor<DiscoveryService> services = ArgumentCaptor.forClass(DiscoveryService.class);

        discovery.onResponse(container);
        verify(listener, times(8)).thingDiscovered(services.capture(), discoveries.capture());
        List<DiscoveryResult> results = discoveries.getAllValues();
        results.forEach(entry -> {
            logger.info("{}", entry.toString());
        });
    }
}
