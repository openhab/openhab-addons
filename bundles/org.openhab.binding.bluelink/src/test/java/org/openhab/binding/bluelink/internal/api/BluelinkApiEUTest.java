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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bluelink.internal.MockApiData.*;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
import org.openhab.binding.bluelink.internal.dto.eu.Vehicle;
import org.openhab.binding.bluelink.internal.dto.eu.ccs2.Ccs2VehicleStatusResponse;
import org.openhab.binding.bluelink.internal.model.Brand;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.binding.bluelink.internal.model.PlugType;
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
    private static final String TEST_VEHICLE_ID = "test-vehicle-id";

    private static final WireMockServer WIREMOCK_SERVER = new WireMockServer(
            WireMockConfiguration.options().dynamicPort());
    private static final HttpClient HTTP_CLIENT = new HttpClient();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

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
        stubFor(get(urlPathEqualTo("/api/v1/spa/vehicles/test-vehicle-id/ccs2/carstatus/latest"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(VEHICLE_STATUS_RESPONSE_EU_CCS2)));

        HTTP_CLIENT.start();
    }

    @AfterAll
    static void tearDown() throws Exception {
        WIREMOCK_SERVER.stop();
        HTTP_CLIENT.stop();
        SCHEDULER.shutdownNow();
    }

    @Test
    void testLoginAndGetVehicleStatus() throws BluelinkApiException {
        final String baseUrl = "http://localhost:" + WIREMOCK_SERVER.port();
        final BluelinkApiEU api = new BluelinkApiEU(HTTP_CLIENT, SCHEDULER, Brand.HYUNDAI, Map.of(), baseUrl,
                timeZoneProvider, MockApiData.TEST_REFRESH_TOKEN);
        assertTrue(api.login());

        // Verify device ID was obtained
        assertFalse(api.getProperties().isEmpty());
        assertEquals("122c2e30-d642-4d34-ba07-7ce7d787349a", api.getProperties().get("deviceId"));

        final IVehicle vehicle = new Vehicle(TEST_VEHICLE_ID, "KMHXX00XXXX000000", "My Car", IVehicle.EngineType.EV,
                "IONIQ 5", 2022, false);
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
            if (targetSoC.plugType() == PlugType.DC) {
                assertEquals(90, targetSoC.targetSocLevel());
            } else if (targetSoC.plugType() == PlugType.AC) {
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
    void testLoginAndGetVehicleStatusForCcs2Protocol() throws BluelinkApiException {
        final String baseUrl = "http://localhost:" + WIREMOCK_SERVER.port();
        final BluelinkApiEU api = new BluelinkApiEU(HTTP_CLIENT, SCHEDULER, Brand.HYUNDAI, Map.of(), baseUrl,
                timeZoneProvider, MockApiData.TEST_REFRESH_TOKEN);
        assertTrue(api.login());

        // Verify device ID was obtained
        assertFalse(api.getProperties().isEmpty());
        assertEquals("122c2e30-d642-4d34-ba07-7ce7d787349a", api.getProperties().get("deviceId"));

        final IVehicle vehicle = new Vehicle(TEST_VEHICLE_ID, "KMHXX00XXXX000000", "My Car", IVehicle.EngineType.EV,
                "EV9", 2024, true);
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
        assertTrue(status.engine());
        assertFalse(status.trunkOpen());
        assertTrue(status.hoodOpen());
        assertNotNull(status.battery());
        assertEquals(88.5, status.battery().stateOfCharge(), 0.0001);
        assertTrue(status.airCtrlOn());
        assertTrue(status.defrost());

        final var evStatus = status.evStatus();
        assertNotNull(evStatus);
        assertEquals(53.5, evStatus.batteryStatus(), 0.0001);
        assertFalse(evStatus.batteryCharge());
        assertEquals(0, evStatus.rawBatteryPlugin());
        assertFalse(evStatus.batteryPlugin());

        final var drvDistance = evStatus.drvDistance();
        assertNotNull(drvDistance);
        assertFalse(drvDistance.isEmpty());
        final var rangeByFuel = drvDistance.getFirst().rangeByFuel();
        assertNotNull(rangeByFuel);
        assertNotNull(rangeByFuel.evModeRange());
        assertEquals(new QuantityType<>(247, KILO(METRE)), rangeByFuel.evModeRange().getRange());
        assertEquals(new QuantityType<>(247, KILO(METRE)), rangeByFuel.totalAvailableRange().getRange());

        final var chargeInfos = evStatus.reservChargeInfos();
        assertNotNull(chargeInfos);
        final var targetSocList = chargeInfos.targetSocList();
        assertNotNull(targetSocList);
        assertEquals(2, targetSocList.size());
        for (final var targetSoC : targetSocList) {
            if (targetSoC.plugType() == PlugType.DC) {
                assertEquals(95, targetSoC.targetSocLevel());
            } else if (targetSoC.plugType() == PlugType.AC) {
                assertEquals(90, targetSoC.targetSocLevel());
            }
        }

        final var lastUpdate = aLastUpdate.get();
        assertNotNull(lastUpdate);

        final var doorOpen = status.doorOpen();
        assertNotNull(doorOpen);
        assertFalse(doorOpen.frontLeft()); // driver
        assertTrue(doorOpen.frontRight()); // passenger
        assertFalse(doorOpen.backLeft());
        assertFalse(doorOpen.backRight());

        final var windowOpen = status.windowOpen();
        assertNotNull(windowOpen);
        assertTrue(windowOpen.frontLeft()); // driver vented window (Open: 0, OpenLevel: 1)
        assertFalse(windowOpen.frontRight());
        assertFalse(windowOpen.backLeft());
        assertFalse(windowOpen.backRight());

        final var location = aLocation.get();
        assertNotNull(location);
        assertEquals(9.9, location.getLatitude().doubleValue(), 0.0001);
        assertEquals(10.10, location.getLongitude().doubleValue(), 0.0001);
        assertEquals(13.4, location.getAltitude().doubleValue(), 0.0001);

        final var odometer = aOdometer.get();
        assertNotNull(odometer);
        assertEquals(new QuantityType<>(33628.9, KILO(METRE)), odometer);

        final var smartKey = aSmartKey.get();
        assertNotNull(smartKey);
        assertFalse(smartKey);
    }

    @Test
    void testCcs2IgnitionOnlyEngineStatus() {
        final var resp = new com.google.gson.Gson().fromJson(VEHICLE_STATUS_RESPONSE_EU_CCS2_IGNITION,
                Ccs2VehicleStatusResponse.class);
        assertNotNull(resp);

        final IVehicle ev = new Vehicle(TEST_VEHICLE_ID, "VIN123", "EV", IVehicle.EngineType.EV, "EV6", 2024, true);
        final var status = resp.toCommonVehicleStatus(ev);

        assertNotNull(status);
        assertFalse(status.engine());
    }

    @Test
    void testCcs2BatterySentinel() {
        final var resp = new com.google.gson.Gson().fromJson(VEHICLE_STATUS_RESPONSE_EU_CCS2_SENTINEL,
                Ccs2VehicleStatusResponse.class);
        assertNotNull(resp);

        final IVehicle ev = new Vehicle(TEST_VEHICLE_ID, "VIN123", "EV", IVehicle.EngineType.EV, "EV6", 2024, true);
        final var status = resp.toCommonVehicleStatus(ev);

        assertNotNull(status);
        assertNull(status.battery());
    }

    @Test
    void testCcs2PhevRangesAndUnits() {
        final var resp = new com.google.gson.Gson().fromJson(VEHICLE_STATUS_RESPONSE_EU_CCS2_PHEV,
                Ccs2VehicleStatusResponse.class);
        assertNotNull(resp);

        final IVehicle phev = new Vehicle(TEST_VEHICLE_ID, "VIN123", "PHEV", IVehicle.EngineType.PHEV, "Sportage", 2025,
                true);
        final var status = resp.toCommonVehicleStatus(phev);

        assertNotNull(status);
        assertEquals(new QuantityType<>(600.0, KILO(METRE)), status.dte().getRange());

        final var evStatus = status.evStatus();
        assertNotNull(evStatus);

        final var drvDistance = evStatus.drvDistance();
        assertNotNull(drvDistance);
        assertFalse(drvDistance.isEmpty());

        final var rangeByFuel = drvDistance.getFirst().rangeByFuel();
        assertNotNull(rangeByFuel);
        assertEquals(new QuantityType<>(600.0, KILO(METRE)), rangeByFuel.totalAvailableRange().getRange());
        assertNotNull(rangeByFuel.evModeRange());
        assertEquals(new QuantityType<>(47.0, KILO(METRE)), rangeByFuel.evModeRange().getRange());
    }

    @Test
    @SuppressWarnings("null")
    void testForceRefreshForCcs2ProtocolAndDisposal() throws Exception {
        resetAllRequests();
        stubFor(get(urlPathEqualTo("/api/v1/spa/vehicles/test-vehicle-id/ccs2/carstatus"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"retCode\":\"S\",\"resCode\":\"0000\"}")));

        final ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);
        final ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        final AtomicReference<Runnable> scheduledTask = new AtomicReference<>();

        when(mockScheduler.schedule(any(Runnable.class), eq(BluelinkApiEU.CCS2_FORCE_REFRESH_DELAY_SECONDS),
                eq(TimeUnit.SECONDS))).thenAnswer(invocation -> {
                    scheduledTask.set(invocation.getArgument(0));
                    return mockFuture;
                });

        final String baseUrl = "http://localhost:" + WIREMOCK_SERVER.port();
        final BluelinkApiEU api = new BluelinkApiEU(HTTP_CLIENT, mockScheduler, Brand.HYUNDAI, Map.of(), baseUrl,
                timeZoneProvider, MockApiData.TEST_REFRESH_TOKEN);
        assertTrue(api.login());

        final IVehicle vehicle = new Vehicle(TEST_VEHICLE_ID, "KMHXX00XXXX000000", "My Car", IVehicle.EngineType.EV,
                "EV9", 2024, true);

        final AtomicReference<@Nullable CommonVehicleStatus> aStatus = new AtomicReference<>();
        final VehicleStatusCallback cb = new VehicleStatusCallback() {
            @Override
            public void acceptStatus(final CommonVehicleStatus data) {
                aStatus.set(data);
            }

            @Override
            public void acceptLastUpdateTimestamp(final Instant lastUpdated) {
            }

            @Override
            public void acceptSmartKeyBatteryWarning(final boolean smartKeyBattery) {
            }

            @Override
            public void acceptLocation(final PointType location) {
            }

            @Override
            public void acceptOdometer(final QuantityType<Length> odometer) {
            }
        };

        // 1. Initial forced refresh: sends wake request and schedules delayed /latest fetch
        final boolean forceRefreshResult = api.getVehicleStatus(vehicle, true, cb);
        assertTrue(forceRefreshResult);

        // Verify wake endpoint was requested
        verify(1, getRequestedFor(urlEqualTo("/api/v1/spa/vehicles/test-vehicle-id/ccs2/carstatus")));

        // Verify task was scheduled with 25s delay
        assertNotNull(scheduledTask.get());

        // 2. Execute the scheduled task (simulating time passing and scheduler running the job)
        scheduledTask.get().run();

        // Verify non-forced /latest fetch was executed and received status
        verify(1, getRequestedFor(urlEqualTo("/api/v1/spa/vehicles/test-vehicle-id/ccs2/carstatus/latest")));
        assertNotNull(aStatus.get());

        // 3. Test disposal cancels task
        api.getVehicleStatus(vehicle, true, cb);
        api.dispose();
        verify(mockFuture, times(1)).cancel(true);
    }
}
