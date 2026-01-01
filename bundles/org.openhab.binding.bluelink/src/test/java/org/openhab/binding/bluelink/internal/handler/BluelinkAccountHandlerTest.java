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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.openhab.binding.bluelink.internal.MockApiData.ENROLLMENT_RESPONSE;
import static org.openhab.binding.bluelink.internal.MockApiData.TOKEN_RESPONSE;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.bluelink.internal.MockApiData;
import org.openhab.binding.bluelink.internal.api.BluelinkApiException;
import org.openhab.binding.bluelink.internal.dto.VehicleInfo;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Integration tests for the Bluelink binding using WireMock to mock the API.
 *
 * @author Marcus Better - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class BluelinkAccountHandlerTest extends JavaTest {

    private static final WireMockServer WIREMOCK_SERVER = new WireMockServer(
            WireMockConfiguration.options().dynamicPort());
    private static final HttpClient HTTP_CLIENT = new HttpClient();

    private final TimeZoneProvider timeZoneProvider = () -> ZoneId.of("America/New_York");
    private final LocaleProvider localeProvider = () -> Locale.US;

    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private @NonNullByDefault({}) BluelinkAccountHandler handler;

    @BeforeAll
    static void setUpHttpServer() throws Exception {
        WIREMOCK_SERVER.start();
        WireMock.configureFor("localhost", WIREMOCK_SERVER.port());
        stubFor(post(urlEqualTo("/v2/ac/oauth/token")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(TOKEN_RESPONSE)));
        stubFor(get(urlPathMatching("/ac/v2/enrollment/details/.*")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(ENROLLMENT_RESPONSE)));

        HTTP_CLIENT.start();
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

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    @AfterAll
    static void tearDownHttpServer() throws Exception {
        WIREMOCK_SERVER.stop();
        HTTP_CLIENT.stop();
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
        final VehicleInfo vehicle = handler.getVehicles().getFirst();
        assertNotNull(vehicle);
        assertEquals("IONIQ 6", vehicle.modelCode());
        assertEquals("E", vehicle.evStatus());
        assertEquals("2", vehicle.vehicleGeneration());
        assertTrue(vehicle.isElectric());
    }
}
