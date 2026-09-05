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
package org.openhab.binding.millheat.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.millheat.internal.client.MillheatCloudApiClient;
import org.openhab.binding.millheat.internal.config.MillheatAccountConfiguration;
import org.openhab.binding.millheat.internal.dto.VacationModeRequest;
import org.openhab.binding.millheat.internal.handler.MillheatAccountHandler;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.ModeType;
import org.openhab.binding.millheat.internal.model.Room;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.osgi.framework.BundleContext;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

/**
 * Exercises the account handler against a stubbed Mill cloud service.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Rewritten for the MillNorway cloud API
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MillHeatAccountHandlerTest {

    private static final String HOUSE_ID = "11111111-1111-4111-8111-111111111111";
    private static final String ROOM_ID = "22222222-2222-4222-8222-222222222222";
    private static final String DEVICE_ID = "33333333-3333-4333-8333-333333333333";

    private WireMockServer wireMockServer;
    private HttpClient httpClient;

    private @Mock BundleContext bundleContext;
    private @Mock Configuration configuration;
    private @Mock Bridge millheatAccountMock;
    private @Mock ThingHandlerCallback callbackMock;

    @BeforeEach
    public void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        httpClient = new HttpClient();
        httpClient.start();

        MillheatCloudApiClient.endpoint = "http://localhost:" + wireMockServer.port();
    }

    @AfterEach
    public void shutdown() throws Exception {
        httpClient.stop();
        wireMockServer.stop();
        wireMockServer.resetAll();
    }

    private static String fixture(final String name) throws IOException {
        try (var stream = MillHeatAccountHandlerTest.class.getResourceAsStream(name)) {
            assertNotNull(stream, "Missing test fixture " + name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void stubJson(final String url, final String fixtureName) throws IOException {
        stubFor(get(urlEqualTo(url)).willReturn(okJson(fixture(fixtureName))));
    }

    private void stubSignIn(final String fixtureName) throws IOException {
        stubFor(post(urlEqualTo("/customer/auth/sign-in")).willReturn(okJson(fixture(fixtureName))));
    }

    private void stubModelEndpoints() throws IOException {
        stubJson("/houses", "/houses_ok.json");
        stubJson("/houses/" + HOUSE_ID + "/devices", "/house_devices_ok.json");
        stubJson("/houses/" + HOUSE_ID + "/devices/independent", "/house_independent_ok.json");
        stubJson("/rooms/" + ROOM_ID + "/devices", "/room_info_ok.json");
    }

    private MillheatAccountHandler newHandler() {
        when(millheatAccountMock.getConfiguration()).thenReturn(configuration);
        when(millheatAccountMock.getUID()).thenReturn(new ThingUID("millheat:account:thinguid"));

        final MillheatAccountConfiguration accountConfig = new MillheatAccountConfiguration();
        accountConfig.username = "username";
        accountConfig.password = "password";
        when(configuration.as(eq(MillheatAccountConfiguration.class))).thenReturn(accountConfig);

        when(millheatAccountMock.getThings()).thenReturn(List.of());
        final MillheatAccountHandler handler = new MillheatAccountHandler(millheatAccountMock, httpClient,
                bundleContext);
        handler.setCallback(callbackMock);
        return handler;
    }

    @Test
    public void testRefreshModel() throws Exception {
        stubSignIn("/sign_in_ok.json");
        stubModelEndpoints();

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();
        final MillheatModel model = subject.refreshModel();

        assertEquals(1, model.getHomes().size());
        final Home home = model.getHomes().get(0);
        assertEquals(HOUSE_ID, home.getId());
        assertEquals("Test House", home.getName());
        assertFalse(home.isVacationModeActive());

        assertEquals(1, home.getRooms().size());
        final Room room = home.getRooms().get(0);
        assertEquals(ROOM_ID, room.getId());
        // The captured room runs a weekly program, so the effective mode is the one it selected
        // rather than the literal "weekly_program".
        assertNotEquals(ModeType.WEEKLY_PROGRAM, room.getMode());

        assertEquals(1, room.getHeaters().size());
        final Heater heater = room.getHeaters().get(0);
        assertEquals(DEVICE_ID, heater.getId());
        assertFalse(heater.isIndependent());
        assertTrue(heater.isOnline());
        // Measured power comes from telemetry now, with no nominal value configured.
        assertNotNull(heater.getCurrentPower());

        verify(postRequestedFor(urlEqualTo("/customer/auth/sign-in")));
        verify(getRequestedFor(urlEqualTo("/houses")));
        verify(getRequestedFor(urlEqualTo("/houses/" + HOUSE_ID + "/devices")));
        verify(getRequestedFor(urlEqualTo("/rooms/" + ROOM_ID + "/devices")));
    }

    @Test
    public void testHeaterIsFoundByMacAddress() throws Exception {
        stubSignIn("/sign_in_ok.json");
        stubModelEndpoints();

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();
        final MillheatModel model = subject.refreshModel();

        assertTrue(model.findHeaterByMacOrId("AA:BB:CC:DD:EE:FF", null).isPresent());
        assertTrue(model.findHeaterByMacOrId(null, DEVICE_ID).isPresent());
        assertTrue(model.findHeaterByMacOrId("no-such-mac", null).isEmpty());

        // The cloud API separates the MAC with colons while the old service did not, and existing
        // configurations use the bare form, so both must resolve to the same heater.
        assertTrue(model.findHeaterByMacOrId("AABBCCDDEEFF", null).isPresent());
        assertTrue(model.findHeaterByMacOrId("aa:bb:cc:dd:ee:ff", null).isPresent());
    }

    @Test
    public void testStagedVacationTimesSurviveARefresh() throws Exception {
        stubSignIn("/sign_in_ok.json");
        stubModelEndpoints();

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();
        subject.updateModelFromServerWithRetry(true);

        // The cloud API cannot store vacation times without also enabling vacation mode, so they
        // are staged locally until the mode is switched on. A poll in between must not discard
        // them, or enabling afterwards fails for want of a start and end time.
        final Home home = subject.getModel().getHomes().get(0);
        final Instant start = Instant.parse("2026-12-01T00:00:00Z");
        final Instant end = Instant.parse("2026-12-14T00:00:00Z");
        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_START, new DateTimeType(start));
        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_END, new DateTimeType(end));
        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_TEMP,
                new QuantityType<>(12, SIUnits.CELSIUS));
        assertFalse(home.isVacationModeActive());

        subject.updateModelFromServerWithRetry(true);

        final Home refreshed = subject.getModel().getHomes().get(0);
        assertNotSame(home, refreshed, "the poll should have rebuilt the home");
        assertEquals(start, refreshed.getVacationModeStart());
        assertEquals(end, refreshed.getVacationModeEnd());
        assertEquals(12.0, refreshed.getVacationTemperature());
    }

    @Test
    public void testEnablingVacationModePostsTheStagedTimes() throws Exception {
        stubSignIn("/sign_in_ok.json");
        stubModelEndpoints();
        stubFor(post(urlEqualTo("/houses/" + HOUSE_ID + "/mode/vacation")).willReturn(aResponse().withStatus(200)));

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();
        subject.updateModelFromServerWithRetry(true);

        final Home home = subject.getModel().getHomes().get(0);
        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_START,
                new DateTimeType(Instant.ofEpochSecond(1796083200L)));
        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_END,
                new DateTimeType(Instant.ofEpochSecond(1797292800L)));
        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_MODE, OnOffType.ON);

        assertTrue(home.isVacationModeActive());
        verify(postRequestedFor(urlEqualTo("/houses/" + HOUSE_ID + "/mode/vacation"))
                .withRequestBody(matchingJsonPath("$.startDate", equalTo("1796083200")))
                .withRequestBody(matchingJsonPath("$.endDate", equalTo("1797292800"))));
    }

    @Test
    public void testVacationModeIsNotEnabledWithoutTimes() throws Exception {
        stubSignIn("/sign_in_ok.json");
        stubModelEndpoints();

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();
        subject.updateModelFromServerWithRetry(true);

        final Home home = subject.getModel().getHomes().get(0);
        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_MODE, OnOffType.ON);

        assertFalse(home.isVacationModeActive());
        verify(0, postRequestedFor(urlEqualTo("/houses/" + HOUSE_ID + "/mode/vacation")));
    }

    @Test
    public void testAdvancedVacationModeSelectsRoomAwayTemperatures() throws Exception {
        stubSignIn("/sign_in_ok.json");
        stubModelEndpoints();

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();
        subject.updateModelFromServerWithRetry(true);

        // The channel has always meant "use each room's away temperature", so ON must map to
        // use_away_temperature rather than the house-wide vacation temperature.
        final Home home = subject.getModel().getHomes().get(0);
        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_ADVANCED, OnOffType.ON);
        assertTrue(home.isAdvancedVacationMode());
        assertEquals(VacationModeRequest.TYPE_AWAY_TEMPERATURE, home.getVacationModeType());

        subject.updateVacationProperty(home, MillheatAccountHandler.VACATION_PROP_ADVANCED, OnOffType.OFF);
        assertFalse(home.isAdvancedVacationMode());
        assertEquals(VacationModeRequest.TYPE_VACATION_TEMPERATURE, home.getVacationModeType());
    }

    @Test
    public void testExpiredTokenIsRefreshedBeforeUse() throws Exception {
        // Signing in yields a token that expired long ago, so the client must renew it before the
        // first authenticated call rather than waiting for a 401.
        stubSignIn("/sign_in_expired.json");
        stubFor(post(urlEqualTo("/customer/auth/refresh")).willReturn(okJson(fixture("/sign_in_ok.json"))));
        stubModelEndpoints();

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();
        subject.refreshModel();

        verify(postRequestedFor(urlEqualTo("/customer/auth/refresh")).withHeader("Authorization",
                equalTo("Bearer test-refresh-token")));
    }

    @Test
    public void testUnauthorizedTriggersRefreshAndOneRetry() throws Exception {
        stubSignIn("/sign_in_ok.json");
        stubFor(post(urlEqualTo("/customer/auth/refresh")).willReturn(okJson(fixture("/sign_in_ok.json"))));

        // The first call to /houses is rejected; after a refresh the retry succeeds.
        stubFor(get(urlEqualTo("/houses")).inScenario("expiry").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(401)).willSetStateTo("refreshed"));
        stubFor(get(urlEqualTo("/houses")).inScenario("expiry").whenScenarioStateIs("refreshed")
                .willReturn(okJson(fixture("/houses_ok.json"))));
        stubJson("/houses/" + HOUSE_ID + "/devices", "/house_devices_ok.json");
        stubJson("/houses/" + HOUSE_ID + "/devices/independent", "/house_independent_ok.json");
        stubJson("/rooms/" + ROOM_ID + "/devices", "/room_info_ok.json");

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();
        final MillheatModel model = subject.refreshModel();

        assertEquals(1, model.getHomes().size());
        verify(2, getRequestedFor(urlEqualTo("/houses")));
        verify(postRequestedFor(urlEqualTo("/customer/auth/refresh")));
    }

    @Test
    public void testRateLimitIsReportedAndNotRetried() throws Exception {
        stubSignIn("/sign_in_ok.json");
        stubFor(get(urlEqualTo("/houses")).willReturn(aResponse().withStatus(429)));

        final MillheatAccountHandler subject = newHandler();
        subject.signIn();

        final MillheatCommunicationException failure = assertThrows(MillheatCommunicationException.class,
                subject::refreshModel);
        assertTrue(failure.isRateLimited());
        // A 429 must not be retried, or the account digs itself deeper into the limit.
        verify(1, getRequestedFor(urlEqualTo("/houses")));
    }
}
