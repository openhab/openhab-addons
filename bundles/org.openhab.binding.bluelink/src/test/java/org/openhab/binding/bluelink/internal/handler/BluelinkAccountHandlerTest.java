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
package org.openhab.binding.bluelink.internal.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openhab.binding.bluelink.internal.MockApiData.DEVICE_REGISTRATION_RESPONSE_EU;
import static org.openhab.binding.bluelink.internal.MockApiData.ENROLLMENT_RESPONSE_US;
import static org.openhab.binding.bluelink.internal.MockApiData.TOKEN_RESPONSE_US;
import static org.openhab.binding.bluelink.internal.MockApiData.VEHICLES_RESPONSE_EU;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.bluelink.internal.CciLoginStubs;
import org.openhab.binding.bluelink.internal.MockApiData;
import org.openhab.binding.bluelink.internal.api.BluelinkApiException;
import org.openhab.binding.bluelink.internal.api.Region;
import org.openhab.binding.bluelink.internal.dto.eu.Vehicle;
import org.openhab.binding.bluelink.internal.model.Brand;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

/**
 * Integration tests for the Bluelink binding using WireMock to mock the API.
 *
 * @author Marcus Better - Initial contribution
 * @author Florian Hotze - Added tests for EU account
 * @author Carlo Dischler - Added tests for EU password login
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class BluelinkAccountHandlerTest extends JavaTest {

    private static final ResponseGate GATE = new ResponseGate();
    private static final WireMockServer WIREMOCK_SERVER = new WireMockServer(
            WireMockConfiguration.options().dynamicPort().extensions(GATE));
    private static final HttpClient HTTP_CLIENT = new HttpClient();

    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private @NonNullByDefault({}) BluelinkAccountHandler handler;

    @BeforeAll
    static void setUpHttpServer() throws Exception {
        WIREMOCK_SERVER.start();
        HTTP_CLIENT.start();
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    @AfterAll
    static void tearDownHttpServer() throws Exception {
        WIREMOCK_SERVER.stop();
        HTTP_CLIENT.stop();
    }

    @Nested
    class US {
        private final TimeZoneProvider timeZoneProvider = () -> ZoneId.of("America/New_York");
        private final LocaleProvider localeProvider = () -> Locale.US;

        @BeforeAll
        static void setUpStubs() {
            WireMock.configureFor("localhost", WIREMOCK_SERVER.port());
            WireMock.reset();
            stubFor(post(urlEqualTo("/v2/ac/oauth/token")).willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json").withBody(TOKEN_RESPONSE_US)));
            stubFor(get(urlPathMatching("/ac/v2/enrollment/details/.*")).willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json").withBody(ENROLLMENT_RESPONSE_US)));
        }

        @BeforeEach
        void setUp() {
            final Configuration config = new Configuration(Map.of("username", MockApiData.TEST_USERNAME, "password",
                    MockApiData.TEST_PASSWORD, "apiBaseUrl", "http://localhost:" + WIREMOCK_SERVER.port()));
            when(bridge.getConfiguration()).thenReturn(config);
            when(bridge.getUID()).thenReturn(new ThingUID("bluelink:account:testaccount"));

            handler = new BluelinkAccountHandler(bridge, HTTP_CLIENT, timeZoneProvider, localeProvider);
            handler.setCallback(callback);
            handler.initialize();
        }

        @Test
        void testLoginAndGetVehicles() throws BluelinkApiException {
            waitForAssert(() -> {
                try {
                    assertThat(handler.getVehicles(), is(not(empty())));
                } catch (final BluelinkApiException e) {
                    throw new IllegalStateException(e);
                }
            });
            final var vehicle = handler.getVehicles().getFirst();
            assertNotNull(vehicle);
            assertEquals("IONIQ 6", vehicle.model());
            assertEquals(IVehicle.EngineType.EV, vehicle.engineType());
            assertTrue(vehicle.isElectric());
        }

        @Test
        void testOutdatedLoginIsIgnoredAfterRecovery() throws Exception {
            waitForAssert(() -> Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.ONLINE)));
            handler.dispose();
            Mockito.clearInvocations(bridge, callback);
            GATE.reset();
            stubFor(post(urlEqualTo("/v2/ac/oauth/token")).inScenario("gate").whenScenarioStateIs(STARTED)
                    .willReturn(aResponse().withStatus(503).withTransformers(GATE.getName()))
                    .willSetStateTo("released"));

            handler = new BluelinkAccountHandler(bridge, HTTP_CLIENT, timeZoneProvider, localeProvider);
            handler.setCallback(callback);
            handler.initialize();
            assertTrue(GATE.arrived.await(10, TimeUnit.SECONDS));

            // a vehicle request logs in on its own and brings the bridge online while the first login still runs
            assertThat(handler.getVehicles(), is(not(empty())));
            Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.ONLINE));

            // the first login fails afterwards, but its result is outdated and must not take the bridge offline
            GATE.released.countDown();
            Mockito.verify(callback, Mockito.after(2000).never()).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        }
    }

    @Nested
    class EU {
        private final TimeZoneProvider timeZoneProvider = () -> ZoneId.of("Europe/Berlin");
        private final LocaleProvider localeProvider = () -> Locale.GERMAN;

        @BeforeAll
        static void setUpStubs() {
            WireMock.configureFor("localhost", WIREMOCK_SERVER.port());
            WireMock.reset();
            new CciLoginStubs(WIREMOCK_SERVER).stubLogin();
            stubFor(post(urlEqualTo("/api/v1/spa/notifications/register")).willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json").withBody(DEVICE_REGISTRATION_RESPONSE_EU)));
            stubFor(get(urlPathMatching("/api/v1/spa/vehicles")).willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json").withBody(VEHICLES_RESPONSE_EU)));
        }

        @BeforeEach
        void setUp() {
            final Configuration config = new Configuration(Map.of("username", MockApiData.TEST_USERNAME, "password",
                    MockApiData.TEST_PASSWORD, "region", Region.EU.name(), "brand", Brand.HYUNDAI.name(), "apiBaseUrl",
                    "http://localhost:" + WIREMOCK_SERVER.port()));
            when(bridge.getConfiguration()).thenReturn(config);
            when(bridge.getUID()).thenReturn(new ThingUID("bluelink:account:testaccount"));

            handler = new BluelinkAccountHandler(bridge, HTTP_CLIENT, timeZoneProvider, localeProvider);
            handler.setCallback(callback);
            handler.initialize();
        }

        @Test
        void testLoginAndGetVehicles() throws BluelinkApiException {
            final var vehicles = handler.getVehicles();

            assertEquals(2, vehicles.size());
            final var first = vehicles.getFirst();
            assertNotNull(first);
            assertInstanceOf(Vehicle.class, first);
            assertEquals("aa6c0ca6-48eb-430c-9eea-902bb6cd281c", first.id());
            assertEquals("VIN1234", first.vin());
            assertEquals("IONIQ 5", first.model());
            assertEquals(2022, first.modelYear());
            assertEquals(IVehicle.EngineType.EV, first.engineType());
            assertEquals("My Car", first.getDisplayName());
            assertTrue(first.isElectric());
            assertInstanceOf(Vehicle.class, first);
            final var firstEu = (Vehicle) first;
            assertFalse(firstEu.ccs2ProtocolSupport());

            final var second = vehicles.getLast();
            assertNotNull(second);
            assertInstanceOf(Vehicle.class, second);
            assertEquals("60392c08-c747-40fe-9de9-7c36e7e24da5", second.id());
            assertEquals("VIN5678", second.vin());
            assertEquals("EV9", second.model());
            assertEquals(2024, second.modelYear());
            assertEquals(IVehicle.EngineType.EV, second.engineType());
            assertEquals("My Other Car", second.getDisplayName());
            assertTrue(second.isElectric());
            final var secondEu = (Vehicle) second;
            assertTrue(secondEu.ccs2ProtocolSupport());

            verify(getRequestedFor(urlEqualTo("/api/v1/spa/vehicles")).withHeader("Authorization",
                    equalTo("Bearer " + CciLoginStubs.CCS_ACCESS_TOKEN)));
        }

        @Test
        void testSetChargeLimitDC() throws BluelinkApiException {
            final var vehicleId = "aa6c0ca6-48eb-430c-9eea-902bb6cd281c";
            stubFor(post(urlEqualTo("/api/v1/spa/vehicles/" + vehicleId + "/charge/target"))
                    .withRequestBody(equalToJson("{\"targetSOClist\":[{\"plugType\":0,\"targetSOClevel\":80}]}"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody("{\"retCode\":\"S\"}")));

            final var vehicle = handler.getVehicles().getFirst();
            assertTrue(handler.setChargeLimitDC(vehicle, 80));
        }

        @Test
        void testSetChargeLimitAC() throws BluelinkApiException {
            final var vehicleId = "aa6c0ca6-48eb-430c-9eea-902bb6cd281c";
            stubFor(post(urlEqualTo("/api/v1/spa/vehicles/" + vehicleId + "/charge/target"))
                    .withRequestBody(equalToJson("{\"targetSOClist\":[{\"plugType\":1,\"targetSOClevel\":90}]}"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody("{\"retCode\":\"S\"}")));

            final var vehicle = handler.getVehicles().getFirst();
            assertTrue(handler.setChargeLimitAC(vehicle, 90));
        }

        @Test
        void testVehicleRequestBringsBridgeOnlineAfterFailedLogin() throws BluelinkApiException {
            waitForAssert(() -> Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.ONLINE)));
            handler.dispose();
            Mockito.clearInvocations(bridge, callback);
            stubFor(get(urlPathEqualTo("/auth/api/v2/user/oauth2/authorize")).inScenario("login")
                    .whenScenarioStateIs(STARTED).willReturn(aResponse().withStatus(503)).willSetStateTo("up"));

            handler = new BluelinkAccountHandler(bridge, HTTP_CLIENT, timeZoneProvider, localeProvider);
            handler.setCallback(callback);
            handler.initialize();
            waitForAssert(() -> Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                            && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)));

            assertEquals(2, handler.getVehicles().size());

            Mockito.verify(bridge).setProperty("deviceId", "122c2e30-d642-4d34-ba07-7ce7d787349a");
            Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        }

        @Test
        void testLegacyRefreshTokenIsReported() {
            waitForAssert(() -> Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.ONLINE)));
            handler.dispose();
            stubFor(post(urlEqualTo("/auth/account/signin")).withFormParam("username", equalTo("token@example.com"))
                    .willReturn(aResponse().withStatus(302).withHeader("Location",
                            "http://localhost:" + WIREMOCK_SERVER.port()
                                    + "/auth/api/v2/user/oauth2/authorize?error_description=Invalid+password")));
            final Configuration config = new Configuration(Map.of("username", "token@example.com", "password",
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJKL", "region", Region.EU.name(), "brand",
                    Brand.HYUNDAI.name(), "apiBaseUrl", "http://localhost:" + WIREMOCK_SERVER.port()));
            when(bridge.getConfiguration()).thenReturn(config);
            handler = new BluelinkAccountHandler(bridge, HTTP_CLIENT, timeZoneProvider, localeProvider);
            handler.setCallback(callback);

            handler.initialize();

            waitForAssert(() -> Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                            && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR
                            && "@text/account-handler.login.refresh-token".equals(status.getDescription()))));
        }

        @Test
        void testMissingUsernameIsRejected() {
            waitForAssert(() -> Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.ONLINE)));
            handler.dispose();
            resetAllRequests();
            final Configuration config = new Configuration(
                    Map.of("password", MockApiData.TEST_PASSWORD, "region", Region.EU.name(), "brand",
                            Brand.HYUNDAI.name(), "apiBaseUrl", "http://localhost:" + WIREMOCK_SERVER.port()));
            when(bridge.getConfiguration()).thenReturn(config);
            handler = new BluelinkAccountHandler(bridge, HTTP_CLIENT, timeZoneProvider, localeProvider);
            handler.setCallback(callback);

            handler.initialize();

            Mockito.verify(callback).statusUpdated(eq(bridge),
                    argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                            && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
            verify(0, getRequestedFor(urlPathEqualTo("/auth/api/v2/user/oauth2/authorize")));
        }
    }

    // holds a response until the test releases it, so that a login can overlap with a vehicle request
    @NonNullByDefault({})
    private static final class ResponseGate implements ResponseDefinitionTransformerV2 {
        private volatile CountDownLatch arrived = new CountDownLatch(1);
        private volatile CountDownLatch released = new CountDownLatch(1);

        void reset() {
            arrived = new CountDownLatch(1);
            released = new CountDownLatch(1);
        }

        @Override
        public ResponseDefinition transform(final ServeEvent serveEvent) {
            arrived.countDown();
            try {
                released.await(10, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return serveEvent.getResponseDefinition();
        }

        @Override
        public boolean applyGlobally() {
            return false;
        }

        @Override
        public String getName() {
            return "gate";
        }
    }
}
