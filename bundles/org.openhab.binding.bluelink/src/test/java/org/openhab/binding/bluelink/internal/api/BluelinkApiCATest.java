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
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.bluelink.internal.MockApiData.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluelink.internal.dto.CommonVehicleStatus;
import org.openhab.binding.bluelink.internal.dto.ca.Vehicle;
import org.openhab.binding.bluelink.internal.model.Brand;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkApiCATest {

    private static final WireMockServer WIREMOCK_SERVER = new WireMockServer(
            WireMockConfiguration.options().dynamicPort());
    private static final HttpClient HTTP_CLIENT = new HttpClient();

    private final TimeZoneProvider timeZoneProvider = () -> ZoneId.of("America/Toronto");

    @BeforeAll
    static void setUp() throws Exception {
        WIREMOCK_SERVER.start();
        WireMock.configureFor("localhost", WIREMOCK_SERVER.port());
        stubFor(post(urlEqualTo("/v2/login")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(TOKEN_RESPONSE_CA)));
        stubFor(post(urlEqualTo("/lstvhclsts")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(VEHICLE_STATUS_RESPONSE_CA)));
        stubFor(post(urlEqualTo("/rltmvhclsts")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(VEHICLE_STATUS_RESPONSE_CA)));

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
        final AbstractBluelinkApi<?> api = new BluelinkApiCA(HTTP_CLIENT, Brand.KIA, Map.of(), baseUrl,
                timeZoneProvider, TEST_USERNAME, TEST_PASSWORD, null);
        assertTrue(api.login());

        final IVehicle vehicle = new Vehicle("123", "VIN1234", "Car", IVehicle.EngineType.EV, "EV9", 2022);
        final AtomicReference<@Nullable CommonVehicleStatus> aStatus = new AtomicReference<>();
        final AtomicReference<@Nullable Instant> aLastUpdate = new AtomicReference<>();

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
            }

            @Override
            public void acceptLocation(final PointType location) {
            }

            @Override
            public void acceptOdometer(final QuantityType<Length> odometer) {
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
        assertEquals(new QuantityType<>(23, CELSIUS), airTemp.getTemperature(vehicle));

        final var evStatus = status.evStatus();
        assertNotNull(evStatus);
        assertEquals(42, evStatus.batteryStatus());
        assertTrue(evStatus.batteryCharge());
        assertEquals(2, evStatus.rawBatteryPlugin());
        assertTrue(evStatus.batteryPlugin());

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
    }

    @Test
    void testGetVehicles() throws Exception {
        stubFor(post(urlEqualTo("/vhcllst")).willReturn(json("""
                {"responseHeader": {"responseCode": 0},
                 "result": {"vehicles": [{"vehicleId": "123", "vin": "VIN1234", "nickName": "Car",
                                          "modelName": "IONIQ 5", "modelYear": "2023", "fuelKindCode": "E"}]}}
                """)));
        final BluelinkApiCA api = new BluelinkApiCA(HTTP_CLIENT, Brand.HYUNDAI, Map.of(),
                "http://localhost:" + WIREMOCK_SERVER.port(), timeZoneProvider, TEST_USERNAME, TEST_PASSWORD, null);

        final var vehicles = api.getVehicles();

        assertEquals(1, vehicles.size());
        assertEquals("VIN1234", vehicles.getFirst().vin());
        assertEquals(IVehicle.EngineType.EV, vehicles.getFirst().engineType());
    }

    @Test
    void testExpiredTokenLogsInAgain() throws Exception {
        final String user = "expired@example.com";
        stubFor(post(urlEqualTo("/v2/login")).withRequestBody(matchingJsonPath("$.loginId", equalTo(user)))
                .willReturn(json(TOKEN_RESPONSE_CA)));
        stubFor(post(urlEqualTo("/lstvhclsts")).inScenario("expired").whenScenarioStateIs(STARTED).willReturn(json("""
                {"responseHeader": {"responseCode": 1},
                 "error": {"errorCode": "7403", "errorDesc": "Your authentication has expired."}}
                """)).willSetStateTo("renewed"));
        final BluelinkApiCA api = new BluelinkApiCA(HTTP_CLIENT, Brand.HYUNDAI, Map.of(),
                "http://localhost:" + WIREMOCK_SERVER.port(), timeZoneProvider, user, TEST_PASSWORD, null);
        final IVehicle vehicle = new Vehicle("123", "VIN1234", "Car", IVehicle.EngineType.EV, "IONIQ 5", 2023);

        assertTrue(api.getVehicleStatus(vehicle, false, new NoOpCallback()));

        verify(2, postRequestedFor(urlEqualTo("/v2/login"))
                .withRequestBody(matchingJsonPath("$.loginId", equalTo(user))));
    }

    @Test
    void testValidTokenIsNotReplaced() throws Exception {
        final String user = "once@example.com";
        stubFor(post(urlEqualTo("/v2/login")).withRequestBody(matchingJsonPath("$.loginId", equalTo(user)))
                .willReturn(json(TOKEN_RESPONSE_CA)));
        final BluelinkApiCA api = new BluelinkApiCA(HTTP_CLIENT, Brand.HYUNDAI, Map.of(),
                "http://localhost:" + WIREMOCK_SERVER.port(), timeZoneProvider, user, TEST_PASSWORD, null);

        // a second login would delete the token of the first one
        assertTrue(api.login());
        assertTrue(api.login());

        verify(1, postRequestedFor(urlEqualTo("/v2/login"))
                .withRequestBody(matchingJsonPath("$.loginId", equalTo(user))));
    }

    @Test
    void testLoginWithVerificationCode() throws Exception {
        final String user = "otp@example.com";
        stubFor(post(urlEqualTo("/v2/login")).withRequestBody(matchingJsonPath("$.loginId", equalTo(user)))
                .willReturn(json("""
                        {"responseHeader": {"responseCode": 1},
                         "error": {"errorCode": "7110", "errorDesc": "OTP required"}}
                        """)));
        stubFor(post(urlEqualTo("/mfa/selverifmeth")).willReturn(json("""
                {"responseHeader": {"responseCode": 0},
                 "result": {"userInfoUuid": "user-uuid", "emailList": ["%s"]}}
                """.formatted(user))));
        stubFor(post(urlEqualTo("/mfa/sendotp"))
                .withRequestBody(matchingJsonPath("$.userInfoUuid", equalTo("user-uuid"))).willReturn(json("""
                        {"responseHeader": {"responseCode": 0}, "result": {"otpKey": "otp-key"}}
                        """)));
        stubFor(post(urlEqualTo("/mfa/validateotp")).withRequestBody(matchingJsonPath("$.otpNo", equalTo("123456")))
                .withRequestBody(matchingJsonPath("$.otpKey", equalTo("otp-key"))).willReturn(json("""
                        {"responseHeader": {"responseCode": 0},
                         "result": {"verifiedOtp": true, "otpValidationKey": "validation-key"}}
                        """)));
        stubFor(post(urlEqualTo("/mfa/genmfatkn"))
                .withRequestBody(matchingJsonPath("$.otpValidationKey", equalTo("validation-key"))).willReturn(json("""
                        {"responseHeader": {"responseCode": 0},
                         "result": {"token": {"accessToken": "verified-token", "expireIn": "3600"}}}
                        """)));

        final String baseUrl = "http://localhost:" + WIREMOCK_SERVER.port();
        final BluelinkApiCA api = new BluelinkApiCA(HTTP_CLIENT, Brand.KIA, Map.of("deviceId", "known-device"), baseUrl,
                timeZoneProvider, user, TEST_PASSWORD, null);

        LoginRejectedException e = assertThrows(LoginRejectedException.class, api::login);
        assertEquals("@text/account-handler.login.verification-required", e.getStatusDescription());
        // nothing is sent until the user requests the code
        assertThrows(LoginRejectedException.class, api::login);
        verify(0, postRequestedFor(urlEqualTo("/mfa/sendotp")));
        verify(1, postRequestedFor(urlEqualTo("/v2/login"))
                .withRequestBody(matchingJsonPath("$.loginId", equalTo(user))));

        assertEquals(user, api.requestVerificationCode());
        e = assertThrows(LoginRejectedException.class, api::login);
        assertEquals("@text/account-handler.login.verification-code-sent [\"" + user + "\"]", e.getStatusDescription());
        verify(1, postRequestedFor(urlEqualTo("/mfa/sendotp")));

        assertTrue(api.verifyCode(" 123456 "));

        assertEquals("verified-token", api.accessToken);
        verify(postRequestedFor(urlEqualTo("/v2/login")).withHeader("Deviceid", equalTo("known-device")));
        verify(postRequestedFor(urlEqualTo("/mfa/genmfatkn")).withHeader("Deviceid", equalTo("known-device")));
        assertEquals("known-device", api.getProperties().get("deviceId"));
    }

    @Test
    void testNoVerificationCodeForKnownDevice() throws Exception {
        final String user = "known@example.com";
        stubFor(post(urlEqualTo("/v2/login")).withRequestBody(matchingJsonPath("$.loginId", equalTo(user)))
                .willReturn(json(TOKEN_RESPONSE_CA)));
        final BluelinkApiCA api = new BluelinkApiCA(HTTP_CLIENT, Brand.HYUNDAI, Map.of(),
                "http://localhost:" + WIREMOCK_SERVER.port(), timeZoneProvider, user, TEST_PASSWORD, null);

        assertTrue(api.login());
        assertNull(api.requestVerificationCode());
        assertFalse(api.verifyCode("123456"));

        api.accessTokenExpiry = Instant.now();
        assertTrue(api.login());
        verify(0, postRequestedFor(urlEqualTo("/mfa/selverifmeth"))
                .withRequestBody(matchingJsonPath("$.userAccount", equalTo(user))));
    }

    @Test
    void testLoginReportsInvalidCredentials() {
        final String user = "wrong@example.com";
        stubFor(post(urlEqualTo("/v2/login")).withRequestBody(matchingJsonPath("$.loginId", equalTo(user)))
                .willReturn(json("""
                        {"responseHeader": {"responseCode": 1},
                         "error": {"errorCode": "7404", "errorDesc": "Invalid username or password"}}
                        """)));
        final BluelinkApiCA api = new BluelinkApiCA(HTTP_CLIENT, Brand.KIA, Map.of(),
                "http://localhost:" + WIREMOCK_SERVER.port(), timeZoneProvider, user, TEST_PASSWORD, null);

        final LoginRejectedException e = assertThrows(LoginRejectedException.class, api::login);
        assertEquals(LoginRejectedException.Reason.INVALID_CREDENTIALS, e.getReason());
    }

    private static class NoOpCallback implements VehicleStatusCallback {
        @Override
        public void acceptStatus(final CommonVehicleStatus data) {
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
    }

    private static ResponseDefinitionBuilder json(final String body) {
        return aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body);
    }
}
