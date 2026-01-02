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
package org.openhab.binding.bluelink.internal.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.bluelink.internal.MockApiData.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.METRE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.QuantityType;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class BluelinkApiEUTest {

    private static final WireMockServer WIREMOCK_SERVER = new WireMockServer(
            WireMockConfiguration.options().dynamicPort());
    private static final HttpClient HTTP_CLIENT = new HttpClient();

    @BeforeAll
    static void setUp() throws Exception {
        WIREMOCK_SERVER.start();
        WireMock.configureFor("localhost", WIREMOCK_SERVER.port());
        stubFor(post(urlEqualTo("/auth/api/v2/user/oauth2/token")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(TOKEN_RESPONSE)));
        stubFor(post(urlEqualTo("/api/v1/spa/notifications/register")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(EU_DEVICE_REGISTRATION_RESPONSE)));
        stubFor(get(urlEqualTo("/api/v1/spa/vehicles/1234/status/latest")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(EU_VEHICLE_STATUS_RESPONSE)));

        HTTP_CLIENT.start();
    }

    @AfterAll
    static void tearDown() throws Exception {
        WIREMOCK_SERVER.stop();
        HTTP_CLIENT.stop();
    }

    @Test
    void testGetVehicleStatus() throws Exception {
        final String baseUrl = "http://localhost:" + WIREMOCK_SERVER.port();
        final BluelinkApiEU.BrandConfig config = new BluelinkApiEU.BrandConfig(baseUrl, baseUrl,
                "6d477c38-3ca4-4cf3-9557-2a1929a94654", "014d2225-8495-4735-812d-2616334fd15d",
                "KUy49XxPzLpLuoK0xhBC77W6VXhmtQR9iQhmIFjjoY4IpxsV",
                "RFtoRq/vDXJmRndoZaZQyfOot7OrIqGVFj96iY2WL3yyH5Z/pUvlUhqmCxD2t+D65SQ=", "GCM");

        final BluelinkApiEU api = new BluelinkApiEU(HTTP_CLIENT, config, TEST_REFRESH_TOKEN);
        assertTrue(api.login());

        final Vehicle vehicle = new Vehicle("1234", "IONIQ 5", "IONIQ 5", "VIN1234", "E", true, null, null, false);
        final VehicleStatus status = api.getVehicleStatus(vehicle, false);

        assertNotNull(status);
        assertFalse(status.doorsLocked());
        assertFalse(status.engineOn());
        assertFalse(status.trunkOpen());
        assertTrue(status.hoodOpen());
        assertEquals(82, status.batterySoC());

        final var evStatus = status.evStatus();
        assertNotNull(evStatus);
        assertEquals(35, evStatus.batterySoC());
        assertFalse(evStatus.isCharging());
        assertFalse(evStatus.isPluggedIn());
        assertEquals(90, evStatus.targetSoCs().stream().filter(t -> "DC".equals(t.plugType()))
                .map(VehicleStatus.TargetSoC::level).findFirst().orElseThrow());
        assertEquals(80, evStatus.targetSoCs().stream().filter(t -> "AC".equals(t.plugType()))
                .map(VehicleStatus.TargetSoC::level).findFirst().orElseThrow());

        final var rangeByFuel = evStatus.range();
        assertNotNull(rangeByFuel);
        assertEquals(new QuantityType<>(129.0, KILO(METRE)), rangeByFuel.ev());
        assertEquals(new QuantityType<>(129.0, KILO(METRE)), rangeByFuel.total());

        final var doorOpen = status.doorOpen();
        assertNotNull(doorOpen);
        assertTrue(doorOpen.frontLeft());
        assertFalse(doorOpen.frontRight());
        assertFalse(doorOpen.rearLeft());
        assertFalse(doorOpen.rearRight());

        final var location = status.location();
        assertNotNull(location);
        assertEquals(34.0, location.altitude());
        assertEquals(49.01395, location.latitude());
        assertEquals(8.40448, location.longitude());

        assertFalse(status.airControlOn());
        assertFalse(status.defrost());

        assertEquals(new QuantityType<>(39505.5, KILO(METRE)), status.odometer());
    }
}
