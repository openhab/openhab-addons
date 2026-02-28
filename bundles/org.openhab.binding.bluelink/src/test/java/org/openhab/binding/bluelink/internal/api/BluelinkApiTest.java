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

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluelink.internal.dto.VehicleInfo;
import org.openhab.binding.bluelink.internal.dto.VehicleStatus;
import org.openhab.core.i18n.TimeZoneProvider;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkApiTest {

    private static final WireMockServer WIREMOCK_SERVER = new WireMockServer(
            WireMockConfiguration.options().dynamicPort());
    private static final HttpClient HTTP_CLIENT = new HttpClient();

    private final TimeZoneProvider timeZoneProvider = () -> ZoneId.of("America/New_York");

    @BeforeAll
    static void setUp() throws Exception {
        WIREMOCK_SERVER.start();
        WireMock.configureFor("localhost", WIREMOCK_SERVER.port());
        stubFor(post(urlEqualTo("/v2/ac/oauth/token")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(TOKEN_RESPONSE)));
        stubFor(get(urlEqualTo("/ac/v2/rcs/rvs/vehicleStatus")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(VEHICLE_STATUS_RESPONSE)));

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
        final BluelinkApi api = new BluelinkApi(HTTP_CLIENT, baseUrl, timeZoneProvider, TEST_USERNAME, TEST_PASSWORD,
                null);
        assertTrue(api.login());

        final VehicleInfo vehicle = new VehicleInfo("123", "", "VIN1234", "", "", "2", 0);
        final VehicleStatus status = api.getVehicleStatus(vehicle, false);

        assertNotNull(status);
        assertNotNull(status.vehicleStatus());
        assertTrue(status.vehicleStatus().doorLock());
        assertFalse(status.vehicleStatus().engine());
        assertEquals(75, status.vehicleStatus().battery().stateOfCharge());

        final var evStatus = status.vehicleStatus().evStatus();
        assertNotNull(evStatus);
        assertEquals(42, evStatus.batteryStatus());
        assertTrue(evStatus.batteryCharge());
        assertEquals(2, evStatus.batteryPlugin());

        final var drvDistance = evStatus.drvDistance();
        assertNotNull(drvDistance);
        assertFalse(drvDistance.isEmpty());
        final var rangeByFuel = drvDistance.getFirst().rangeByFuel();
        assertNotNull(rangeByFuel);
        assertEquals(184.0, rangeByFuel.evModeRange().value());
        assertEquals(184.0, rangeByFuel.totalAvailableRange().value());
    }
}
