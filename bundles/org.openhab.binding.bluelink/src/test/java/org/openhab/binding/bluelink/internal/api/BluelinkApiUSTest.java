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
import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluelink.internal.dto.CommonVehicleStatus;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.Vehicle;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.binding.bluelink.internal.model.IVehicle.EngineType;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;

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
        stubFor(post(urlEqualTo("/v2/ac/oauth/token")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(TOKEN_RESPONSE_US)));
        stubFor(get(urlPathMatching("/ac/v2/enrollment/details/.*")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(ENROLLMENT_RESPONSE)));
        stubFor(get(urlEqualTo("/ac/v2/rcs/rvs/vehicleStatus")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(VEHICLE_STATUS_RESPONSE_US)));

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
        final AbstractBluelinkApi<?> api = new BluelinkApiUS(HTTP_CLIENT, Optional.of(baseUrl), timeZoneProvider,
                TEST_USERNAME, TEST_PASSWORD, null);
        assertTrue(api.login());

        final IVehicle vehicle = new Vehicle("123", "VIN1234", "Car", EngineType.EV, "IONIQ5", 2022, "2", 100.0);
        final AtomicReference<@Nullable CommonVehicleStatus> aStatus = new AtomicReference<>();
        final AtomicReference<@Nullable Instant> aLastUpdate = new AtomicReference<>();
        final AtomicReference<@Nullable PointType> aLocation = new AtomicReference<>();
        final AtomicBoolean aSmartKeyBattery = new AtomicBoolean();
        final AtomicReference<@Nullable QuantityType<Length>> aOdometer = new AtomicReference<>();

        final boolean res = api.getVehicleStatus(vehicle, false, new VehicleStatusCallback() {
            @Override
            public void acceptStatus(final CommonVehicleStatus data) {
                aStatus.set(data);
            }

            @Override
            public void acceptLastUpdateTimestamp(final Instant lastUpdated) {
                aLastUpdate.set(lastUpdated);
            }

            @Override
            public void acceptSmartKeyBatteryWarning(final boolean smartKeyBattery) {
                aSmartKeyBattery.set(smartKeyBattery);
            }

            @Override
            public void acceptLocation(final PointType location) {
                aLocation.set(location);
            }

            @Override
            public void acceptOdometer(final QuantityType<Length> odometer) {
                aOdometer.set(odometer);
            }
        });

        assertTrue(res);
        final var status = aStatus.get();
        assertNotNull(status);
        assertTrue(status.doorLock());
        assertFalse(status.engine());
        assertEquals(75, status.battery().stateOfCharge());
        final var airTemp = status.airTemp();
        assertNotNull(airTemp);
        assertEquals(new QuantityType<>(70, FAHRENHEIT), airTemp.getTemperature(vehicle));

        final var evStatus = status.evStatus();
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

        final var lastUpdate = aLastUpdate.get();
        assertNotNull(lastUpdate);
        assertEquals(Instant.parse("2025-12-18T04:09:28Z"), lastUpdate);
        assertFalse(aSmartKeyBattery.get());
        final var loc = aLocation.get();
        assertNotNull(loc);
        assertEquals(55.0, loc.getAltitude().doubleValue());

        final var odometer = aOdometer.get();
        assertEquals(new QuantityType<>(6942.0, ImperialUnits.MILE), odometer);
    }
}
