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
package org.openhab.binding.mybmw.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link DiscoveryTest} Test Discovery Results
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DiscoveryTest {

    @Test
    public void testDiscovery() {
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/vehicles.json");
        Bridge b = mock(Bridge.class);
        MyBMWBridgeHandler bh = new MyBMWBridgeHandler(b, mock(HttpClientFactory.class), "en");
        when(b.getUID()).thenReturn(new ThingUID("mybmw", "account", "abc"));
        VehicleDiscovery discovery = new VehicleDiscovery();
        discovery.setThingHandler(bh);
        DiscoveryListener listener = mock(DiscoveryListener.class);
        discovery.addDiscoveryListener(listener);
        List<Vehicle> vl = Converter.getVehicleList(content);
        assertEquals(1, vl.size(), "Vehicles found");
        ArgumentCaptor<DiscoveryResult> discoveries = ArgumentCaptor.forClass(DiscoveryResult.class);
        ArgumentCaptor<DiscoveryService> services = ArgumentCaptor.forClass(DiscoveryService.class);
        bh.onResponse(content);
        verify(listener, times(1)).thingDiscovered(services.capture(), discoveries.capture());
        List<DiscoveryResult> results = discoveries.getAllValues();
        assertEquals(1, results.size(), "Found Vehicles");
        DiscoveryResult result = results.get(0);
        assertEquals("mybmw:bev_rex:abc:anonymous", result.getThingUID().getAsString(), "Thing UID");
    }

    @Test
    public void testProperties() {
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/vehicles.json");
        Vehicle vehicle = Converter.getVehicle(Constants.ANONYMOUS, content);
        String servicesSuppoertedReference = "RemoteHistory ChargingHistory ScanAndCharge DCSContractManagement BmwCharging ChargeNowForBusiness ChargingPlan";
        String servicesUnsuppoertedReference = "MiniCharging EvGoCharging CustomerEsim CarSharing EasyCharge";
        String servicesEnabledReference = "FindCharging ";
        String servicesDisabledReference = "DataPrivacy ChargingSettings ChargingHospitality ChargingPowerLimit ChargingTargetSoc ChargingLoudness";
        assertEquals(servicesSuppoertedReference,
                VehicleDiscovery.getServices(vehicle, VehicleDiscovery.SUPPORTED_SUFFIX, true), "Services supported");
        assertEquals(servicesUnsuppoertedReference,
                VehicleDiscovery.getServices(vehicle, VehicleDiscovery.SUPPORTED_SUFFIX, false),
                "Services unsupported");

        String servicesEnabled = VehicleDiscovery.getServices(vehicle, VehicleDiscovery.ENABLED_SUFFIX, true)
                + Constants.SPACE + VehicleDiscovery.getServices(vehicle, VehicleDiscovery.ENABLE_SUFFIX, true);
        assertEquals(servicesEnabledReference, servicesEnabled, "Services enabled");
        String servicesDisabled = VehicleDiscovery.getServices(vehicle, VehicleDiscovery.ENABLED_SUFFIX, false)
                + Constants.SPACE + VehicleDiscovery.getServices(vehicle, VehicleDiscovery.ENABLE_SUFFIX, false);
        assertEquals(servicesDisabledReference, servicesDisabled, "Services disabled");
    }
}
