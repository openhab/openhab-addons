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
package org.openhab.binding.mybmw.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.mybmw.internal.MyBMWConstants;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWHttpProxy;
import org.openhab.binding.mybmw.internal.handler.backend.NetworkException;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;

/**
 * The {@link VehicleDiscoveryTest} Test Discovery Results
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - updates
 */
@NonNullByDefault
public class VehicleDiscoveryTest {

    @Test
    public void testDiscovery() {
        String content = FileReader.fileToString("responses/vehicles.json");
        List<Vehicle> vehicleList = Arrays.asList(new Gson().fromJson(content, Vehicle[].class));

        VehicleDiscovery vehicleDiscovery = new VehicleDiscovery();

        MyBMWBridgeHandler bridgeHandler = mock(MyBMWBridgeHandler.class);

        List<Thing> things = new ArrayList<>();

        Thing thing1 = mock(Thing.class);
        when(thing1.getConfiguration()).thenReturn(createConfiguration("VIN1234567"));
        things.add(thing1);
        Thing thing2 = mock(Thing.class);
        when(thing2.getConfiguration()).thenReturn(createConfiguration("VIN1234568"));
        things.add(thing2);

        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID("mybmw", "account", "abc"));

        when(bridgeHandler.getThing()).thenReturn(bridge);

        MyBMWHttpProxy myBMWProxy = mock(MyBMWHttpProxy.class);
        try {
            when(myBMWProxy.requestVehicles()).thenReturn(vehicleList);
        } catch (NetworkException e) {
        }

        when(bridgeHandler.getMyBmwProxy()).thenReturn(Optional.of(myBMWProxy));

        vehicleDiscovery.setThingHandler(bridgeHandler);
        vehicleDiscovery.initialize();
        assertNotNull(vehicleDiscovery.getThingHandler());

        DiscoveryListener listener = mock(DiscoveryListener.class);
        vehicleDiscovery.addDiscoveryListener(listener);

        assertEquals(2, vehicleList.size(), "Vehicles not found");
        ArgumentCaptor<DiscoveryResult> discoveries = ArgumentCaptor.forClass(DiscoveryResult.class);
        ArgumentCaptor<DiscoveryService> services = ArgumentCaptor.forClass(DiscoveryService.class);

        // call the discovery
        vehicleDiscovery.startScan();

        Mockito.verify(listener, Mockito.times(2)).thingDiscovered(services.capture(), discoveries.capture());
        List<DiscoveryResult> results = discoveries.getAllValues();
        assertEquals(2, results.size(), "Vehicles Not Found");

        assertEquals("mybmw:conv:abc:VIN1234567", results.get(0).getThingUID().getAsString(), "Thing UID");
        assertEquals("mybmw:conv:abc:VIN1234568", results.get(1).getThingUID().getAsString(), "Thing UID");

        // call the discovery again to check if the vehicle is already known -> no newly created vehicles should be
        // found
        when(bridge.getThings()).thenReturn(things);

        ArgumentCaptor<DiscoveryResult> discoveries2 = ArgumentCaptor.forClass(DiscoveryResult.class);
        ArgumentCaptor<DiscoveryService> services2 = ArgumentCaptor.forClass(DiscoveryService.class);

        // call the discovery
        vehicleDiscovery.startScan();

        Mockito.verify(listener, Mockito.times(2)).thingDiscovered(services2.capture(), discoveries2.capture());
        results = discoveries2.getAllValues();

        vehicleDiscovery.deactivate();
        assertEquals(2, results.size(), "Vehicles Not Found");
    }

    private Configuration createConfiguration(String vin) {
        Configuration configuration = new Configuration();
        configuration.put(MyBMWConstants.VIN, vin);

        return configuration;
    }
}
