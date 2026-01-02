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

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkApiUSTest {

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
                .withHeader("Content-Type", "application/json").withBody(US_VEHICLE_STATUS_RESPONSE)));

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
        final BluelinkApiUS api = new BluelinkApiUS(HTTP_CLIENT, baseUrl, timeZoneProvider, TEST_USERNAME,
                TEST_PASSWORD, null);
        assertTrue(api.login());

        final Vehicle vehicle = new Vehicle("123", "", "", "VIN1234", "", false, 2, 0.0, null);
        final VehicleStatus status = api.getVehicleStatus(vehicle, false);

        assertNotNull(status);
        assertTrue(status.doorsLocked());
        assertFalse(status.engineOn());
        assertFalse(status.hoodOpen());
        assertEquals(75, status.batterySoC());

        final var evStatus = status.evStatus();
        assertNotNull(evStatus);
        assertEquals(42, evStatus.batterySoC());
        assertTrue(evStatus.isCharging());
        assertTrue(evStatus.isPluggedIn());

        final var rangeByFuel = evStatus.range();
        assertNotNull(rangeByFuel);
        assertEquals(new QuantityType<>(184.0, KILO(METRE)), rangeByFuel.ev());
        assertEquals(new QuantityType<>(184.0, KILO(METRE)), rangeByFuel.total());

        final var location = status.location();
        assertNotNull(location);
        assertEquals(55.0, location.altitude());
        assertEquals(43.23436388, location.latitude());
        assertEquals(-99.15509166666666, location.longitude());

        final var windowOpen = status.windowOpen();
        assertNotNull(windowOpen);
        assertFalse(windowOpen.frontLeft());
        assertFalse(windowOpen.frontRight());
        assertTrue(windowOpen.rearLeft());
        assertTrue(windowOpen.rearRight());
    }
}
