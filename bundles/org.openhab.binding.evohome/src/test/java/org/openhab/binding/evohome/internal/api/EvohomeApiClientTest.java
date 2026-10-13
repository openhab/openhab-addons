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
package org.openhab.binding.evohome.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.HeatSetpointStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.SystemModeStatus;
import org.openhab.binding.evohome.internal.configuration.EvohomeAccountConfiguration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests authentication, installation loading and status recovery.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class EvohomeApiClientTest {
    private static final String TOKEN = """
            {"access_token":"test-token","refresh_token":"test-refresh","expires_in":3600}
            """;
    private static final String ACCOUNT = "{\"userId\":\"user\"}";
    private static final String LOCATIONS = "[{\"locationInfo\":{\"locationId\":\"location\"},\"gateways\":[]}]";
    private static final String STATUS = "{\"locationId\":\"location\",\"gateways\":[]}";
    private final HttpClient http = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);

    private static final String CAPABILITIES = """
            {"minHeatSetpoint":10,"maxHeatSetpoint":25,"valueResolution":0.25,
             "allowedSetpointModes":["PermanentOverride","TemporaryOverride","FollowSchedule"],
             "maxDuration":"1.00:00:00","timingResolution":"00:10:00"}
            """;

    private String installation(String capabilities, String modes, String extraSystem, String zoneTimeZone) {
        return """
                [{"locationInfo":{"locationId":"location","timeZone":{"timeZoneId":"%s"}},
                  "gateways":[{"temperatureControlSystems":[
                    {"systemId":"system","allowedSystemModes":%s,
                     "zones":[{"zoneId":"zone","heatSetpointCapabilities":%s},
                              {"zoneId":"second-zone"}],"dhw":{"dhwId":"water"}}%s]}]}]
                """.formatted(zoneTimeZone, modes, capabilities, extraSystem);
    }

    private EvohomeApiClient installed(String installation) throws Exception {
        EvohomeApiClient client = client();
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, installation),
                response(201, "{}"));
        client.login();
        return client;
    }

    private List<JsonObject> commandBodies() {
        ArgumentCaptor<ContentProvider> content = ArgumentCaptor.forClass(ContentProvider.class);
        verify(request, atLeastOnce()).content(content.capture(), eq("application/json"));
        return content.getAllValues().stream().map(provider -> {
            StringBuilder body = new StringBuilder();
            for (ByteBuffer buffer : provider) {
                body.append(StandardCharsets.UTF_8.decode(buffer.duplicate()));
            }
            return JsonParser.parseString(body.toString()).getAsJsonObject();
        }).toList();
    }

    private EvohomeApiClient client() throws Exception {
        when(http.newRequest(anyString())).thenReturn(request);
        EvohomeAccountConfiguration configuration = new EvohomeAccountConfiguration();
        configuration.username = "test-user";
        configuration.password = "test-password";
        configuration.refreshInterval = 3000;
        return new EvohomeApiClient(configuration, http);
    }

    private ContentResponse response(int code, String json) {
        return mock(ContentResponse.class, invocation -> switch (invocation.getMethod().getName()) {
            case "getStatus" -> code;
            case "getContentAsString" -> json;
            case "getHeaders" -> new HttpFields();
            default -> RETURNS_DEFAULTS.answer(invocation);
        });
    }

    @Test
    void failedInstallationReadCanRecoverWithoutFalseLoginSuccess() throws Exception {
        EvohomeApiClient client = client();
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(500, "{}"),
                response(200, ACCOUNT), response(200, LOCATIONS), response(200, STATUS));
        assertThrows(EvohomeApiClientException.class, client::login);
        assertFalse(client.isLoggedIn());
        client.login();
        client.update();
        assertTrue(client.isLoggedIn());
        assertEquals(1, client.getInstallationInfo().size());
        verify(http, times(1)).newRequest(EvohomeApiConstants.URL_V2_AUTH);
    }

    @Test
    void timeoutDoesNotReportSuccessfulUpdateAndCanRecover() throws Exception {
        EvohomeApiClient client = client();
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(200, STATUS)).thenThrow(new TimeoutException()).thenReturn(response(200, STATUS));
        client.login();
        client.update();
        Object previous = client.getInstallationStatus();
        assertThrows(TimeoutException.class, client::update);
        assertSame(previous, client.getInstallationStatus());
        client.update();
        assertNotSame(previous, client.getInstallationStatus());
    }

    @Test
    void renewsAuthenticationOnceAfterUnauthorizedRead() throws Exception {
        EvohomeApiClient client = client();
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(401, "{}"), response(200, TOKEN), response(200, STATUS));
        client.login();
        client.update();
        verify(http, times(2)).newRequest(EvohomeApiConstants.URL_V2_AUTH);
    }

    @Test
    void repeatedUnauthorizedResponseDoesNotLoop() throws Exception {
        EvohomeApiClient client = client();
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(401, "{}"), response(200, TOKEN), response(401, "{}"));
        client.login();
        assertThrows(EvohomeApiClientException.class, client::update);
        verify(request, times(6)).send();
    }

    @Test
    void reportsRejectedCommandWithoutRetryingServerFailure() throws Exception {
        EvohomeApiClient client = client();
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(500, "{}"));
        client.login();
        assertThrows(EvohomeApiClientException.class, () -> client.setHeatingZoneOverride("zone", 21));
        verify(request, times(4)).send();
    }

    @Test
    void longPollingIntervalDoesNotRefreshNewTokenOnEveryPoll() throws Exception {
        EvohomeApiClient client = client();
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(200, STATUS));
        client.login();
        client.update();
        client.update();
        client.setTcsMode("system", "Auto");
        verify(http, times(1)).newRequest(EvohomeApiConstants.URL_V2_AUTH);
    }

    @Test
    void missingAccountIsNotAnEmptySuccessfulInstallation() throws Exception {
        EvohomeApiClient client = client();
        when(request.send()).thenReturn(response(200, TOKEN), response(200, "{}"));
        assertThrows(EvohomeApiClientException.class, client::login);
        assertFalse(client.isLoggedIn());
    }

    @Test
    void closedClientCannotSendCommandsOrRestart() throws Exception {
        EvohomeApiClient client = client();
        client.close();
        assertThrows(EvohomeApiClientException.class, client::login);
        assertThrows(EvohomeApiClientException.class, () -> client.setHeatingZoneOverride("zone", 21));
        verifyNoInteractions(request);
    }

    @Test
    void temporaryOverrideUsesUtcTimeUntilAndAdvertisedTemperatureResolution() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        Instant until = Instant.now().plusSeconds(3600).truncatedTo(ChronoUnit.SECONDS);
        client.setHeatingZoneTemporaryOverride("zone", 20.37, until);
        JsonObject body = commandBodies().getFirst();
        assertEquals("TemporaryOverride", body.get("setpointMode").getAsString());
        assertEquals(20.25, body.get("heatSetpointValue").getAsDouble());
        assertEquals(until.toString(), body.get("timeUntil").getAsString());
        assertFalse(body.has("until"));
        verify(http).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureZone/zone/heatSetpoint");
    }

    @Test
    void temperatureLimitsAreValidatedWithoutSendingCommands() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        for (double value : new double[] { 9.9, 25.1, Double.NaN, Double.POSITIVE_INFINITY }) {
            assertThrows(IllegalArgumentException.class, () -> client.setHeatingZoneOverride("zone", value));
        }
        verify(request, times(3)).send();
    }

    @Test
    void missingCapabilitiesRetainHalfDegreeResolution() throws Exception {
        EvohomeApiClient client = installed(installation("{}", "[]", "", "Europe/Budapest"));
        client.setHeatingZoneOverride("zone", 21.2);
        assertEquals(21.0, commandBodies().getFirst().get("heatSetpointValue").getAsDouble());
    }

    @Test
    void unavailableOverrideModeIsRejected() throws Exception {
        EvohomeApiClient client = installed(
                installation("{\"allowedSetpointModes\":[\"PermanentOverride\"]}", "[]", "", "Europe/Budapest"));
        assertThrows(IllegalArgumentException.class,
                () -> client.setHeatingZoneTemporaryOverride("zone", 20, Instant.now().plusSeconds(3600)));
        assertThrows(IllegalArgumentException.class, () -> client.cancelHeatingZoneOverride("zone"));
        verify(request, times(3)).send();
    }

    @Test
    void overrideExpiryMustBeFutureAndWithinAdvertisedMaximum() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        assertThrows(IllegalArgumentException.class,
                () -> client.setHeatingZoneTemporaryOverride("zone", 20, Instant.now().minusSeconds(1)));
        assertThrows(IllegalArgumentException.class,
                () -> client.setHeatingZoneTemporaryOverride("zone", 20, Instant.now().plusSeconds(172800)));
        verify(request, times(3)).send();
    }

    @Test
    void controllerModesRespectAdvertisedPermanentCapability() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, """
                [{"systemMode":"Auto","canBePermanent":true},
                 {"systemMode":"Away","canBePermanent":false,"canBeTemporary":true}]
                """, "", "Europe/Budapest"));
        assertThrows(IllegalArgumentException.class, () -> client.setTcsMode("system", "Custom"));
        assertThrows(IllegalArgumentException.class, () -> client.setTcsMode("system", "Away"));
        client.setTcsMode("system", "Auto");
        assertEquals("Auto", commandBodies().getFirst().get("systemMode").getAsString());
        assertTrue(commandBodies().getFirst().get("permanent").getAsBoolean());
    }

    @Test
    void nativeSystemResetUsesOnlySelectedController() throws Exception {
        EvohomeApiClient client = installed(
                installation(CAPABILITIES, "[{\"systemMode\":\"AutoWithReset\",\"canBePermanent\":true}]",
                        ",{\"systemId\":\"other\",\"zones\":[{\"zoneId\":\"other-zone\"}]}", "Europe/Budapest"));
        client.resetTcs("system");
        assertEquals("AutoWithReset", commandBodies().getFirst().get("systemMode").getAsString());
        verify(request, times(4)).send();
        verify(http, never()).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureZone/zone/heatSetpoint");
        verify(http, never()).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureControlSystem/other/mode");
    }

    @Test
    void fallbackResetCancelsOnlySelectedControllerZonesAndDhw() throws Exception {
        EvohomeApiClient client = installed(
                installation(CAPABILITIES, "[{\"systemMode\":\"Auto\",\"canBePermanent\":true}]",
                        ",{\"systemId\":\"other\",\"zones\":[{\"zoneId\":\"other-zone\"}]}", "Europe/Budapest"));
        client.resetTcs("system");
        List<JsonObject> bodies = commandBodies();
        assertEquals(4, bodies.size());
        assertEquals("Auto", bodies.get(0).get("systemMode").getAsString());
        assertEquals("FollowSchedule", bodies.get(1).get("setpointMode").getAsString());
        assertEquals("FollowSchedule", bodies.get(2).get("setpointMode").getAsString());
        assertEquals(JsonParser.parseString("{\"mode\":\"FollowSchedule\"}"), bodies.get(3));
        verify(http).newRequest(EvohomeApiConstants.URL_V2_BASE + "domesticHotWater/water/state");
        verify(http, never()).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureZone/other-zone/heatSetpoint");
    }

    @Test
    void fallbackResetPropagatesFailureAndStopsRemainingCommands() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES,
                "[{\"systemMode\":\"Auto\",\"canBePermanent\":true}]", "", "Europe/Budapest"));
        when(request.send()).thenReturn(response(201, "{}"), response(400, "{}"));
        EvohomeApiClientException error = assertThrows(EvohomeApiClientException.class,
                () -> client.resetTcs("system"));
        assertEquals(400, error.getStatusCode());
        verify(http, never()).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureZone/second-zone/heatSetpoint");
        verify(http, never()).newRequest(EvohomeApiConstants.URL_V2_BASE + "domesticHotWater/water/state");
    }

    @Test
    void fallbackResetValidatesAllLocalCapabilitiesBeforeChangingController() throws Exception {
        EvohomeApiClient client = installed(installation("{\"allowedSetpointModes\":[\"PermanentOverride\"]}",
                "[{\"systemMode\":\"Auto\",\"canBePermanent\":true}]", "", "Europe/Budapest"));
        assertThrows(IllegalArgumentException.class, () -> client.resetTcs("system"));
        verify(request, times(3)).send();
    }

    @ParameterizedTest
    @ValueSource(strings = { "Central Europe Standard Time", "CentralEuropeStandardTime", "GMTStandardTime",
            "WEuropeStandardTime" })
    void defaultTemporaryOverrideReadsCurrentScheduleAndReturnsAcceptedExpiry(String timeZone) throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", timeZone));
        String schedule = scheduleAt("23:59:00", "00:00:00");
        when(request.send()).thenReturn(response(200, schedule), response(201, "{}"));
        Instant until = client.setHeatingZoneTemporaryOverrideUntilNextSwitchpoint("zone", 21);
        assertTrue(until.isAfter(Instant.now()));
        assertEquals(until.toString(), commandBodies().getFirst().get("timeUntil").getAsString());
        verify(http).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureZone/zone/schedule");
    }

    @ParameterizedTest
    @ValueSource(strings = { "Unknown Windows Time Zone", "UnknownWindowsTimeZone" })
    void unknownLocationTimezoneCannotCreateAnAccidentalPermanentOverride(String timeZone) throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", timeZone));
        assertThrows(IllegalArgumentException.class,
                () -> client.setHeatingZoneTemporaryOverrideUntilNextSwitchpoint("zone", 21));
        verify(request, times(3)).send();
    }

    @Test
    void overrideStatusReadsUntilRatherThanRequestTimeUntil() {
        HeatSetpointStatus status = Objects.requireNonNull(new Gson().fromJson("""
                {"targetHeatTemperature":21.5,"setpointMode":"TemporaryOverride","until":"2026-10-08T18:00:00Z"}
                """, HeatSetpointStatus.class));
        assertEquals("2026-10-08T18:00:00Z", status.getUntil());
    }

    @Test
    void temporarySystemModeUsesUtcSecondsAndControllerRequestFieldNames() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Unknown Windows Time Zone"));
        Instant until = OffsetDateTime.parse("2099-07-13T12:34:56.987+02:00").toInstant();
        client.setTemporaryTcsMode("system", "AutoWithEco", until);
        assertEquals(JsonParser.parseString("""
                {"systemMode":"AutoWithEco","permanent":false,"timeUntil":"2099-07-13T10:34:56Z"}
                """), commandBodies().getFirst());
        verify(http).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureControlSystem/system/mode");
        verify(http, never()).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureControlSystem/other/mode");
    }

    @Test
    void exactPeriodExpiryIsPreservedWithoutAssumingMidnightOrWholeDayResolution() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, """
                [{"systemMode":"Away","canBePermanent":true,"canBeTemporary":true,
                  "timingMode":"Period","timingResolution":"1.00:00:00","maxDuration":"99.00:00:00"}]
                """, "", "Europe/Budapest"));
        Instant until = Instant.now().plusSeconds(3600).truncatedTo(ChronoUnit.SECONDS);
        client.setTemporaryTcsMode("system", "Away", until);
        JsonObject body = commandBodies().getFirst();
        assertEquals("Away", body.get("systemMode").getAsString());
        assertFalse(body.get("permanent").getAsBoolean());
        assertEquals(until.toString(), body.get("timeUntil").getAsString());
    }

    @Test
    void permanentSystemModeCommandDoesNotReuseTemporaryExpiry() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        client.setTemporaryTcsMode("system", "AutoWithEco", Instant.parse("2099-07-13T10:34:56Z"));
        client.setTcsMode("system", "AutoWithEco");
        List<JsonObject> bodies = commandBodies();
        assertEquals(2, bodies.size());
        assertFalse(bodies.getFirst().get("permanent").getAsBoolean());
        assertEquals(JsonParser.parseString("{\"systemMode\":\"AutoWithEco\",\"permanent\":true}"), bodies.getLast());
    }

    @Test
    void temporarySystemModeRejectsUnsupportedModesAndCapabilitiesBeforeSending() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, """
                [{"systemMode":"Auto","canBePermanent":true,"canBeTemporary":false},
                 {"systemMode":"Away","canBePermanent":true,"canBeTemporary":false}]
                """, "", "Europe/Budapest"));
        Instant until = Instant.now().plusSeconds(3600);
        for (String mode : List.of("Auto", "Away", "Custom")) {
            assertThrows(IllegalArgumentException.class, () -> client.setTemporaryTcsMode("system", mode, until));
        }
        verify(request, times(3)).send();
        verify(http, never()).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureControlSystem/system/mode");
    }

    @Test
    void temporarySystemModeRejectsPastExpiryAndAdvertisedMaximumDuration() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, """
                [{"systemMode":"AutoWithEco","canBePermanent":true,"canBeTemporary":true,
                  "timingMode":"Duration","timingResolution":"01:00:00","maxDuration":"1.00:00:00"}]
                """, "", "Europe/Budapest"));
        assertThrows(IllegalArgumentException.class,
                () -> client.setTemporaryTcsMode("system", "AutoWithEco", Instant.now().minusSeconds(1)));
        assertThrows(IllegalArgumentException.class,
                () -> client.setTemporaryTcsMode("system", "AutoWithEco", Instant.now().plusSeconds(172800)));
        verify(request, times(3)).send();
        verify(http, never()).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureControlSystem/system/mode");
    }

    @Test
    void missingSystemModeCapabilitiesAllowOnlyKnownTemporaryModes() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        Instant until = Instant.parse("2099-07-13T10:34:56Z");
        for (String mode : List.of("AutoWithEco", "Away", "DayOff", "Custom")) {
            client.setTemporaryTcsMode("system", mode, until);
        }
        for (String mode : List.of("Auto", "AutoWithReset", "HeatingOff", "Unknown")) {
            assertThrows(IllegalArgumentException.class, () -> client.setTemporaryTcsMode("system", mode, until));
        }
        assertEquals(List.of("AutoWithEco", "Away", "DayOff", "Custom"),
                commandBodies().stream().map(body -> body.get("systemMode").getAsString()).toList());
        verify(request, times(7)).send();
    }

    @Test
    void temporarySystemModePreservesStructuredCloudFailureWithoutRetrying() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        when(request.send()).thenReturn(response(422, "{}"));
        EvohomeApiClientException failure = assertThrows(EvohomeApiClientException.class,
                () -> client.setTemporaryTcsMode("system", "AutoWithEco", Instant.parse("2099-07-13T10:34:56Z")));
        assertEquals(422, failure.getStatusCode());
        verify(request, times(4)).send();
    }

    @Test
    void systemModeReadbackUsesTimeUntilAndDistinguishesMissingPermanentStatus() {
        Gson gson = new Gson();
        SystemModeStatus temporary = Objects.requireNonNull(gson.fromJson("""
                {"mode":"AutoWithEco","isPermanent":false,"timeUntil":"2026-10-09T18:00:00Z"}
                """, SystemModeStatus.class));
        assertEquals(Boolean.FALSE, temporary.getIsPermanent());
        assertEquals("2026-10-09T18:00:00Z", temporary.getTimeUntil());
        SystemModeStatus permanent = Objects
                .requireNonNull(gson.fromJson("{\"mode\":\"Auto\",\"isPermanent\":true}", SystemModeStatus.class));
        assertEquals(Boolean.TRUE, permanent.getIsPermanent());
        assertNull(permanent.getTimeUntil());
        SystemModeStatus unknown = Objects.requireNonNull(gson.fromJson("{\"mode\":\"Auto\"}", SystemModeStatus.class));
        assertNull(unknown.getIsPermanent());
    }

    @Test
    void scheduleReadCachesValidatedProgramAndRefreshesChangedPayloadWithoutWriting() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        String firstProgram = scheduleAt("06:00:00", "18:00:00");
        String changedProgram = firstProgram.replace("\"heatSetpoint\":18", "\"heatSetpoint\":18.125");
        when(request.send()).thenReturn(response(200, firstProgram), response(200, changedProgram));
        assertNull(client.getCachedZoneSchedule("zone"));
        client.readZoneSchedule("zone");
        EvohomeApiClient.CachedSchedule first = Objects.requireNonNull(client.getCachedZoneSchedule("zone"));
        assertEquals(ZoneId.of("Europe/Budapest"), first.timeZone());
        assertEquals(JsonParser.parseString(firstProgram), JsonParser.parseString(first.schedule().toJson()));
        client.readZoneSchedule("zone");
        EvohomeApiClient.CachedSchedule changed = Objects.requireNonNull(client.getCachedZoneSchedule("zone"));
        assertNotSame(first, changed);
        assertFalse(changed.updatedAt().isBefore(first.updatedAt()));
        assertEquals(JsonParser.parseString(changedProgram), JsonParser.parseString(changed.schedule().toJson()));
        assertEquals(18.125,
                changed.schedule()
                        .nextScheduledSwitchpoint(Instant.parse("2026-10-12T03:00:00Z"), ZoneId.of("Europe/Budapest"))
                        .temperature());
        verify(http, times(2)).newRequest(EvohomeApiConstants.URL_V2_BASE + "temperatureZone/zone/schedule");
        verify(request, never()).method(HttpMethod.PUT);
    }

    @ParameterizedTest
    @CsvSource({ "GMT Standard Time,Europe/London,2026-01-12T06:00:00Z,2026-07-12T05:00:00Z",
            "GMTStandardTime,Europe/London,2026-01-12T06:00:00Z,2026-07-12T05:00:00Z",
            "Central Europe Standard Time,Europe/Budapest,2026-01-12T05:00:00Z,2026-07-12T04:00:00Z",
            "CentralEuropeStandardTime,Europe/Budapest,2026-01-12T05:00:00Z,2026-07-12T04:00:00Z",
            "W. Europe Standard Time,Europe/Berlin,2026-01-12T05:00:00Z,2026-07-12T04:00:00Z",
            "WEuropeStandardTime,Europe/Berlin,2026-01-12T05:00:00Z,2026-07-12T04:00:00Z",
            "Europe/Budapest,Europe/Budapest,2026-01-12T05:00:00Z,2026-07-12T04:00:00Z",
            "Etc/GMT-1,Etc/GMT-1,2026-01-12T05:00:00Z,2026-07-12T05:00:00Z" })
    void scheduleTimezonePreservesRegionalSummerAndWinterRules(String reportedTimeZone, String expectedTimeZone,
            String winterSwitchpoint, String summerSwitchpoint) throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", reportedTimeZone));
        when(request.send()).thenReturn(response(200, scheduleAt("06:00:00", "18:00:00")));
        EvohomeApiClient.CachedSchedule cached = client.readZoneSchedule("zone");
        ZoneId timeZone = Objects.requireNonNull(cached.timeZone());
        assertEquals(ZoneId.of(expectedTimeZone), timeZone);
        var winter = cached.schedule().nextScheduledSwitchpoint(Instant.parse("2026-01-12T00:00:00Z"), timeZone);
        assertEquals(Instant.parse(winterSwitchpoint), winter.time());
        assertEquals(18.0, winter.temperature());
        var summer = cached.schedule().nextScheduledSwitchpoint(Instant.parse("2026-07-12T00:00:00Z"), timeZone);
        assertEquals(Instant.parse(summerSwitchpoint), summer.time());
        assertEquals(18.0, summer.temperature());
        verify(request, never()).method(HttpMethod.PUT);
    }

    @Test
    void locationWithDaylightSavingDisabledUsesStandardOffsetInSummer() throws Exception {
        String installation = installation(CAPABILITIES, "[]", "", "CentralEuropeStandardTime").replace(
                "\"timeZoneId\":\"CentralEuropeStandardTime\"",
                "\"timeZoneId\":\"CentralEuropeStandardTime\",\"offsetMinutes\":60,\"currentOffsetMinutes\":120")
                .replace("\"locationId\":\"location\"",
                        "\"locationId\":\"location\",\"useDaylightSaveSwitching\":false");
        EvohomeApiClient client = installed(installation);
        when(request.send()).thenReturn(response(200, scheduleAt("06:00:00", "18:00:00")));
        EvohomeApiClient.CachedSchedule cached = client.readZoneSchedule("zone");
        ZoneId timeZone = Objects.requireNonNull(cached.timeZone());
        assertEquals(ZoneId.of("+01:00"), timeZone);
        var next = cached.schedule().nextScheduledSwitchpoint(Instant.parse("2026-07-12T00:00:00Z"), timeZone);
        assertEquals(Instant.parse("2026-07-12T05:00:00Z"), next.time());
        assertEquals(18.0, next.temperature());
        verify(request, never()).method(HttpMethod.PUT);
    }

    @Test
    void invalidScheduleOrCloudFailureCannotReplaceLastSuccessfulProgramAndTimestamp() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        String valid = scheduleAt("06:00:00", "18:00:00");
        String missingTemperature = valid.replaceFirst("\"heatSetpoint\":18", "\"heatSetpoint\":null");
        when(request.send()).thenReturn(response(200, valid), response(200, missingTemperature), response(500, "{}"));
        client.readZoneSchedule("zone");
        EvohomeApiClient.CachedSchedule lastGood = Objects.requireNonNull(client.getCachedZoneSchedule("zone"));
        assertThrows(IllegalArgumentException.class, () -> client.readZoneSchedule("zone"));
        assertSame(lastGood, client.getCachedZoneSchedule("zone"));
        EvohomeApiClientException failure = assertThrows(EvohomeApiClientException.class,
                () -> client.readZoneSchedule("zone"));
        assertEquals(500, failure.getStatusCode());
        assertSame(lastGood, client.getCachedZoneSchedule("zone"));
        verify(request, never()).method(HttpMethod.PUT);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Unknown Windows Time Zone", "UnknownWindowsTimeZone" })
    void unknownLocationTimezoneStillAllowsRawWeeklyProgramReadback(String timeZone) throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", timeZone));
        String valid = scheduleAt("06:00:00", "18:00:00");
        when(request.send()).thenReturn(response(200, valid));
        client.readZoneSchedule("zone");
        EvohomeApiClient.CachedSchedule cached = Objects.requireNonNull(client.getCachedZoneSchedule("zone"));
        assertNull(cached.timeZone());
        assertEquals(JsonParser.parseString(valid), JsonParser.parseString(cached.schedule().toJson()));
        verify(request, never()).method(HttpMethod.PUT);
    }

    @Test
    void invalidInitialScheduleIsNotCachedAsAnEmptyOrSuccessfulProgram() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        when(request.send()).thenReturn(response(200, "{\"dailySchedules\":[]}"),
                response(200, scheduleAt("06:00:00", "18:00:00")));
        assertThrows(IllegalArgumentException.class, () -> client.readZoneSchedule("zone"));
        assertNull(client.getCachedZoneSchedule("zone"));
        client.readZoneSchedule("zone");
        assertNotNull(client.getCachedZoneSchedule("zone"));
    }

    @Test
    void scheduleRefreshUsesSuccessfulReadTimestampAndIncludesExactTtlBoundary() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        assertTrue(client.isZoneScheduleDue("zone", Instant.now(), 60));
        when(request.send()).thenReturn(response(200, scheduleAt("06:00:00", "18:00:00")));
        client.readZoneSchedule("zone");
        Instant updatedAt = Objects.requireNonNull(client.getCachedZoneSchedule("zone")).updatedAt();
        assertFalse(client.isZoneScheduleDue("zone", updatedAt, 60));
        assertFalse(client.isZoneScheduleDue("zone", updatedAt.plusSeconds(3599), 60));
        assertTrue(client.isZoneScheduleDue("zone", updatedAt.plusSeconds(3600), 60));
        verify(request, times(4)).send();
    }

    @Test
    void scheduleFailureBackoffDoesNotCreateASuccessfulCacheEntry() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        when(request.send()).thenReturn(response(500, "{}"));
        assertThrows(EvohomeApiClientException.class, () -> client.readZoneSchedule("zone"));
        client.deferZoneScheduleRefresh("zone", 900);
        assertNull(client.getCachedZoneSchedule("zone"));
        assertFalse(client.isZoneScheduleDue("zone", Instant.now(), 60));
        assertTrue(client.isZoneScheduleDue("zone", Instant.now().plusSeconds(1800), 60));
    }

    @Test
    void scheduleResponseArrivingAfterCloseCannotRepopulateCache() throws Exception {
        EvohomeApiClient client = installed(installation(CAPABILITIES, "[]", "", "Europe/Budapest"));
        String valid = scheduleAt("06:00:00", "18:00:00");
        when(request.send()).thenReturn(response(200, valid));
        client.readZoneSchedule("zone");
        assertNotNull(client.getCachedZoneSchedule("zone"));
        when(request.send()).thenAnswer(invocation -> {
            client.close();
            return response(200, valid);
        });
        assertThrows(EvohomeApiClientException.class, () -> client.readZoneSchedule("zone"));
        assertNull(client.getCachedZoneSchedule("zone"));
        verify(request, times(5)).send();
    }

    @Test
    void loginInvalidatesPreviousInstallationScheduleCache() throws Exception {
        String installation = installation(CAPABILITIES, "[]", "", "Europe/Budapest");
        EvohomeApiClient client = installed(installation);
        when(request.send()).thenReturn(response(200, scheduleAt("06:00:00", "18:00:00")));
        client.readZoneSchedule("zone");
        assertNotNull(client.getCachedZoneSchedule("zone"));
        when(request.send()).thenReturn(response(200, ACCOUNT), response(200, installation));
        client.login();
        assertNull(client.getCachedZoneSchedule("zone"));
        assertTrue(client.isZoneScheduleDue("zone", Instant.now(), 60));
    }

    static String scheduleAt(String first, String second) {
        return "{\"dailySchedules\":[" + List
                .of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").stream()
                .map(day -> """
                        {"dayOfWeek":"%s","switchpoints":[{"timeOfDay":"%s","heatSetpoint":18},
                        {"timeOfDay":"%s","heatSetpoint":20}]}
                        """.formatted(day, first, second)).reduce((left, right) -> left + "," + right).orElseThrow()
                + "]}";
    }
}
