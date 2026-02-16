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
import static javax.measure.MetricPrefix.KILO;
import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.bluelink.internal.MockApiData.*;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluelink.internal.MockApiData;
import org.openhab.binding.bluelink.internal.dto.CommonVehicleStatus;
import org.openhab.binding.bluelink.internal.dto.EvStatus;
import org.openhab.binding.bluelink.internal.dto.eu.Vehicle;
import org.openhab.binding.bluelink.internal.model.Brand;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.PointType;
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

    private final TimeZoneProvider timeZoneProvider = () -> ZoneId.of("Europe/Berlin");

    @BeforeAll
    static void setUp() throws Exception {
        WIREMOCK_SERVER.start();
        WireMock.configureFor("localhost", WIREMOCK_SERVER.port());

        stubFor(post(urlEqualTo("/auth/api/v2/user/oauth2/token")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(TOKEN_RESPONSE_EU)));
        stubFor(post(urlEqualTo("/api/v1/spa/notifications/register")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(DEVICE_REGISTRATION_RESPONSE_EU)));
        stubFor(get(urlPathEqualTo("/api/v1/spa/vehicles/test-vehicle-id/status/latest")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json").withBody(VEHICLE_STATUS_RESPONSE_EU)));

        HTTP_CLIENT.start();
    }

    @AfterAll
    static void tearDown() throws Exception {
        WIREMOCK_SERVER.stop();
        HTTP_CLIENT.stop();
    }

    @Test
    void testLoginAndGetVehicleStatus() throws BluelinkApiException {
        final String baseUrl = "http://localhost:" + WIREMOCK_SERVER.port();
        final BluelinkApiEU api = new BluelinkApiEU(HTTP_CLIENT, Brand.HYUNDAI, Map.of(), Optional.of(baseUrl),
                timeZoneProvider, MockApiData.TEST_REFRESH_TOKEN);
        assertTrue(api.login());

        // Verify device ID was obtained
        assertFalse(api.getProperties().isEmpty());
        assertEquals("122c2e30-d642-4d34-ba07-7ce7d787349a", api.getProperties().get("deviceId"));

        final IVehicle vehicle = new Vehicle("test-vehicle-id", "KMHXX00XXXX000000", "My Car", IVehicle.EngineType.EV,
                "IONIQ 5", 0, false);
        final AtomicReference<@Nullable CommonVehicleStatus> aStatus = new AtomicReference<>();
        final AtomicReference<@Nullable Instant> aLastUpdate = new AtomicReference<>();
        final AtomicReference<@Nullable PointType> aLocation = new AtomicReference<>();
        final AtomicReference<@Nullable QuantityType<Length>> aOdometer = new AtomicReference<>();
        final AtomicReference<@Nullable Boolean> aSmartKey = new AtomicReference<>();

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
                aSmartKey.set(smartKeyBattery);
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
        assertFalse(status.doorLock());
        assertFalse(status.engine());
        assertFalse(status.trunkOpen());
        assertTrue(status.hoodOpen());
        assertEquals(82, status.battery().stateOfCharge());
        assertFalse(status.airCtrlOn());
        assertFalse(status.defrost());

        final var evStatus = status.evStatus();
        assertNotNull(evStatus);
        assertEquals(35, evStatus.batteryStatus());
        assertFalse(evStatus.batteryCharge());
        assertEquals(0, evStatus.rawBatteryPlugin());
        assertFalse(evStatus.batteryPlugin());

        final var drvDistance = evStatus.drvDistance();
        assertNotNull(drvDistance);
        assertFalse(drvDistance.isEmpty());
        final var rangeByFuel = drvDistance.getFirst().rangeByFuel();
        assertNotNull(rangeByFuel);
        assertNotNull(rangeByFuel.evModeRange());
        assertEquals(new QuantityType<>(129.0, KILO(METRE)), rangeByFuel.evModeRange().getRange());
        assertEquals(new QuantityType<>(129.0, KILO(METRE)), rangeByFuel.totalAvailableRange().getRange());

        final var chargeInfos = evStatus.reservChargeInfos();
        assertNotNull(chargeInfos);
        final var targetSocList = chargeInfos.targetSocList();
        assertNotNull(targetSocList);
        assertEquals(2, targetSocList.size());
        for (final var targetSoC : targetSocList) {
            if (targetSoC.plugType() == EvStatus.ReserveChargeInfo.PlugType.DC) {
                assertEquals(90, targetSoC.targetSocLevel());
            } else if (targetSoC.plugType() == EvStatus.ReserveChargeInfo.PlugType.AC) {
                assertEquals(80, targetSoC.targetSocLevel());
            }
        }

        final var lastUpdate = aLastUpdate.get();
        assertNotNull(lastUpdate);

        final var doorOpen = status.doorOpen();
        assertNotNull(doorOpen);
        assertTrue(doorOpen.frontLeft());
        assertFalse(doorOpen.frontRight());
        assertFalse(doorOpen.backLeft());
        assertFalse(doorOpen.backRight());

        final var location = aLocation.get();
        assertNotNull(location);
        assertEquals(49.01395, location.getLatitude().doubleValue(), 0.0001);
        assertEquals(8.40448, location.getLongitude().doubleValue(), 0.0001);
        assertEquals(34.0, location.getAltitude().doubleValue(), 0.0001);

        final var odometer = aOdometer.get();
        assertNotNull(odometer);
        assertEquals(new QuantityType<>(39505.5, KILO(METRE)), odometer);

        final var smartKey = aSmartKey.get();
        assertNotNull(smartKey);
        assertFalse(smartKey);
    }

    @Test
    void testControlActionsThrow() throws Exception {
        final String baseUrl = "http://localhost:" + WIREMOCK_SERVER.port();
        final BluelinkApiEU api = new BluelinkApiEU(HTTP_CLIENT, Brand.HYUNDAI, Map.of(), Optional.of(baseUrl),
                timeZoneProvider, MockApiData.TEST_REFRESH_TOKEN);
        assertTrue(api.login());

        final IVehicle vehicle = new Vehicle("test-vehicle-id", "KMHXX00XXXX000000", "My Car", IVehicle.EngineType.EV,
                "IONIQ 5", 0, false);

        assertThrows(UnsupportedOperationException.class, () -> api.lockVehicle(vehicle));
        assertThrows(UnsupportedOperationException.class, () -> api.unlockVehicle(vehicle));
        assertThrows(UnsupportedOperationException.class, () -> api.startCharging(vehicle));
        assertThrows(UnsupportedOperationException.class, () -> api.stopCharging(vehicle));
        assertThrows(UnsupportedOperationException.class, () -> api.climateStop(vehicle));
        assertThrows(UnsupportedOperationException.class, () -> api.setChargeLimitDC(vehicle, 80));
        assertThrows(UnsupportedOperationException.class, () -> api.setChargeLimitAC(vehicle, 80));
    }
}
