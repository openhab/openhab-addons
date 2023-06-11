/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        String servicesSuppoertedReference = "RemoteHistory;ChargingHistory;ScanAndCharge;DCSContractManagement;BmwCharging;ChargeNowForBusiness;ChargingPlan";
        String servicesUnsuppoertedReference = "MiniCharging;EvGoCharging;CustomerEsim;CarSharing;EasyCharge";
        String servicesEnabledReference = "FindCharging;";
        String servicesDisabledReference = "DataPrivacy;ChargingSettings;ChargingHospitality;ChargingPowerLimit;ChargingTargetSoc;ChargingLoudness";
        assertEquals(servicesSuppoertedReference,
                VehicleDiscovery.getServices(vehicle, VehicleDiscovery.SUPPORTED_SUFFIX, true), "Services supported");
        assertEquals(servicesUnsuppoertedReference,
                VehicleDiscovery.getServices(vehicle, VehicleDiscovery.SUPPORTED_SUFFIX, false),
                "Services unsupported");

        String servicesEnabled = VehicleDiscovery.getServices(vehicle, VehicleDiscovery.ENABLED_SUFFIX, true)
                + Constants.SEMICOLON + VehicleDiscovery.getServices(vehicle, VehicleDiscovery.ENABLE_SUFFIX, true);
        assertEquals(servicesEnabledReference, servicesEnabled, "Services enabled");
        String servicesDisabled = VehicleDiscovery.getServices(vehicle, VehicleDiscovery.ENABLED_SUFFIX, false)
                + Constants.SEMICOLON + VehicleDiscovery.getServices(vehicle, VehicleDiscovery.ENABLE_SUFFIX, false);
        assertEquals(servicesDisabledReference, servicesDisabled, "Services disabled");
    }

    @Test
    public void testAnonymousFingerPrint() {
        String content = FileReader.readFileInString("src/test/resources/responses/fingerprint-raw.json");
        String anonymous = Converter.anonymousFingerprint(content);
        assertFalse(anonymous.contains("ABC45678"), "VIN deleted");

        anonymous = Converter.anonymousFingerprint(Constants.EMPTY);
        assertEquals(Constants.EMPTY, anonymous, "Equal Fingerprint if Empty");

        anonymous = Converter.anonymousFingerprint(Constants.EMPTY_JSON);
        assertEquals(Constants.EMPTY_JSON, anonymous, "Equal Fingerprint if Empty JSon");
    }

    @Test
    public void testRawVehicleData() {
        String content = FileReader.readFileInString("src/test/resources/responses/TwoVehicles/two-vehicles.json");
        String anonymousVehicle = Converter.getRawVehicleContent("anonymous", content);
        String contentAnon = FileReader.readFileInString("src/test/resources/responses/TwoVehicles/anonymous-raw.json");
        // remove formatting
        JsonObject jo = JsonParser.parseString(contentAnon).getAsJsonObject();
        assertEquals(jo.toString(), anonymousVehicle, "Anonymous VIN raw data");
        String contentF11 = FileReader.readFileInString("src/test/resources/responses/TwoVehicles/f11-raw.json");
        String f11Vehicle = Converter.getRawVehicleContent("some_vin_F11", content);
        jo = JsonParser.parseString(contentF11).getAsJsonObject();
        assertEquals(jo.toString(), f11Vehicle, "F11 VIN raw data");
    }
}
