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
package org.openhab.binding.groheondus.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpCookieStore;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 * Tests for {@link GroheOndusApi}.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
class GroheOndusApiTest {
    private static final ZoneId ZONE = ZoneOffset.ofHours(1);
    private static final ApplianceRef SENSE = new ApplianceRef(1001, 2001, "11111111-1111-1111-1111-111111111111");
    private static final ApplianceRef GUARD = new ApplianceRef(1001, 2002, "22222222-2222-2222-2222-222222222222");
    private static final String TOKEN_JSON = "{\"access_token\":\"access\",\"refresh_token\":\"refresh\",\"expires_in\":3600}";

    private final HttpClient httpClient = mock(HttpClient.class);
    private final List<String> requestedUrls = new ArrayList<>();
    // URL fragment -> response; the first matching entry is used
    private final Map<String, Object> responses = new LinkedHashMap<>();
    private @Nullable GroheOndusApi api;

    @BeforeEach
    void setUp() {
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZONE);
        when(httpClient.getHttpCookieStore()).thenReturn(mock(HttpCookieStore.class));
        when(httpClient.newRequest(anyString())).thenAnswer(invocation -> createRequest(invocation.getArgument(0)));
        api = new GroheOndusApi(httpClient, timeZoneProvider);
    }

    private GroheOndusApi api() {
        GroheOndusApi api = this.api;
        assertNotNull(api);
        return api;
    }

    private Request createRequest(String url) throws Exception {
        requestedUrls.add(url);
        Request request = mock(Request.class, RETURNS_SELF);
        Object response = responses.entrySet().stream().filter(e -> url.contains(e.getKey())).findFirst()
                .map(Map.Entry::getValue).orElseThrow(() -> new AssertionError("Unexpected request " + url));
        if (response instanceof Exception exception) {
            when(request.send()).thenThrow(exception);
        } else {
            when(request.send()).thenReturn((ContentResponse) response);
        }
        return request;
    }

    private static ContentResponse response(int status, String body) {
        return response(status, body, HttpFields.EMPTY);
    }

    private static ContentResponse response(int status, String body, HttpFields headers) {
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(status);
        when(response.getContentAsString()).thenReturn(body);
        when(response.getHeaders()).thenReturn(headers);
        return response;
    }

    private static String fixture(String name) throws IOException {
        try (InputStream stream = GroheOndusApiTest.class.getResourceAsStream(name)) {
            assertNotNull(stream, name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void authenticate() throws GroheOndusApiException {
        responses.put("oidc/refresh", response(200, TOKEN_JSON));
        api().authenticateWithRefreshToken("stored");
    }

    @Test
    void requestWithoutAuthenticationIsRejected() {
        responses.put("dashboard", response(200, "{}"));
        assertThrows(GroheOndusAuthenticationException.class, () -> api().getAppliances());
    }

    @Test
    void refreshTokenRejected() {
        responses.put("oidc/refresh", response(401, ""));
        assertThrows(GroheOndusAuthenticationException.class, () -> api().authenticateWithRefreshToken("old"));
        assertFalse(api().isAuthenticated());
    }

    @Test
    void tokenResponseWithoutTokenIsCommunicationError() {
        responses.put("oidc/refresh", response(200, "{\"expires_in\":3600}"));
        assertThrows(GroheOndusCommunicationException.class, () -> api().authenticateWithRefreshToken("old"));
    }

    @Test
    void webFormLogin() throws Exception {
        HttpFields redirect = HttpFields.build().put("Location",
                "ondus://idp2-apigw.cloud.grohe.com/v3/iot/oidc/token?code=xyz");
        responses.put("oidc/login", response(200, fixture("login-page.html")));
        responses.put("login-actions/authenticate", response(302, "", redirect));
        responses.put("oidc/token", response(200, TOKEN_JSON));

        api().authenticateWithCredentials("user", "pass");

        assertTrue(api().isAuthenticated());
        assertEquals("https://idp2-apigw.cloud.grohe.com/v1/sso/auth/realms/idm-apigw/login-actions/authenticate"
                + "?session_code=abc&execution=def&client_id=iot&tab_id=ghi", requestedUrls.get(1));
        assertEquals("https://idp2-apigw.cloud.grohe.com/v3/iot/oidc/token?code=xyz", requestedUrls.get(2));
    }

    @Test
    void webFormLoginWithWrongCredentials() throws Exception {
        responses.put("oidc/login", response(200, fixture("login-page.html")));
        responses.put("login-actions/authenticate", response(200, "<span>Invalid email address or password.</span>"));

        assertThrows(GroheOndusAuthenticationException.class, () -> api().authenticateWithCredentials("user", "wrong"));
    }

    @Test
    void loginPageWithoutFormIsCommunicationError() {
        assertThrows(GroheOndusCommunicationException.class,
                () -> GroheOndusApi.extractFormAction("<html>maintenance</html>"));
    }

    @Test
    void getAppliancesFlattensDashboard() throws Exception {
        authenticate();
        responses.put("dashboard", response(200, fixture("dashboard.json")));

        List<ApplianceInfo> appliances = api().getAppliances();

        assertEquals(3, appliances.size());
        assertEquals(new ApplianceInfo(SENSE, "Sense kitchen", ApplianceInfo.TYPE_SENSE, null, null),
                appliances.get(0));
        assertEquals(new ApplianceInfo(GUARD, "Guard", ApplianceInfo.TYPE_SENSE_GUARD, 900, null), appliances.get(2));
    }

    @Test
    void getAppliance() throws Exception {
        authenticate();
        responses.put("appliances/22222222", response(200, fixture("appliance-guard.json")));

        ApplianceInfo appliance = api().getAppliance(GUARD);

        assertEquals(ApplianceInfo.TYPE_SENSE_GUARD, appliance.type());
        assertEquals(900, appliance.measurementTransmissionInterval());
        assertEquals(Instant.parse("2026-11-18T09:00:00Z"), appliance.lastTransmission());
    }

    @Test
    void missingApplianceIsNotFound() throws Exception {
        authenticate();
        responses.put("appliances/22222222", response(200, "[]"));
        assertThrows(GroheOndusNotFoundException.class, () -> api().getAppliance(GUARD));

        responses.clear();
        responses.put("appliances/22222222", response(404, ""));
        assertThrows(GroheOndusNotFoundException.class, () -> api().getAppliance(GUARD));
    }

    @Test
    void expiredAccessTokenIsAuthenticationError() throws Exception {
        authenticate();
        responses.put("appliances/22222222", response(401, ""));
        assertThrows(GroheOndusAuthenticationException.class, () -> api().getAppliance(GUARD));
    }

    @Test
    void serverErrorIsCommunicationErrorWithStatus() throws Exception {
        authenticate();
        responses.put("appliances/22222222", response(503, ""));
        GroheOndusCommunicationException e = assertThrows(GroheOndusCommunicationException.class,
                () -> api().getAppliance(GUARD));
        assertEquals(503, e.getHttpStatus());
    }

    @Test
    void timeoutIsCommunicationError() throws Exception {
        authenticate();
        responses.put("appliances/22222222", new TimeoutException());
        assertThrows(GroheOndusCommunicationException.class, () -> api().getAppliance(GUARD));
    }

    @Test
    void malformedJsonIsCommunicationError() throws Exception {
        authenticate();
        responses.put("appliances/22222222", response(200, "<html>"));
        assertThrows(GroheOndusCommunicationException.class, () -> api().getAppliance(GUARD));
    }

    @Test
    void senseSamplesAreHourlySortedAndValid() throws Exception {
        authenticate();
        responses.put("data/aggregated", response(200, fixture("sense-data-hourly.json")));

        List<SenseSample> samples = api().getSenseSamples(SENSE, LocalDate.of(2026, 11, 16),
                LocalDate.of(2026, 11, 19));

        assertTrue(requestedUrls.get(requestedUrls.size() - 1)
                .endsWith("/data/aggregated?from=2026-11-16&to=2026-11-19&groupBy=hour"));
        assertEquals(3, samples.size());
        assertEquals(new SenseSample(Instant.parse("2026-11-17T08:00:00Z"), 20.5f, 41f), samples.get(0));
        assertEquals(new SenseSample(Instant.parse("2026-11-17T09:00:00Z"), 20.75f, null), samples.get(1));
        assertEquals(new SenseSample(Instant.parse("2026-11-17T10:00:00Z"), 21f, 40f), samples.get(2));
    }

    @Test
    void fallsBackToDailyDataWhenHourlyIsRejected() throws Exception {
        authenticate();
        responses.put("groupBy=hour", response(400, ""));
        responses.put("data/aggregated", response(200, fixture("sense-data-hourly.json")));

        api().getSenseSamples(SENSE, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 19));
        api().getSenseSamples(SENSE, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 19));

        // The fallback applies per request: every request tries hourly data first
        List<String> dataRequests = requestedUrls.stream().filter(url -> url.contains("data/aggregated")).toList();
        assertEquals(4, dataRequests.size());
        assertTrue(dataRequests.get(0).endsWith("&groupBy=hour"));
        assertTrue(dataRequests.get(1).endsWith("to=2026-11-19"));
        assertTrue(dataRequests.get(2).endsWith("&groupBy=hour"));
        assertTrue(dataRequests.get(3).endsWith("to=2026-11-19"));
    }

    @Test
    void guardData() throws Exception {
        authenticate();
        responses.put("data/aggregated", response(200, fixture("guard-data.json")));

        GuardData data = api().getGuardData(GUARD, LocalDate.of(2026, 11, 17), LocalDate.of(2026, 11, 19));

        assertEquals(2, data.measurements().size());
        assertEquals(Instant.parse("2026-11-17T23:00:00Z"), data.measurements().get(0).timestamp());
        assertEquals(4.4f, data.measurements().get(1).pressure());
        assertEquals(3, data.withdrawals().size(), "Withdrawal without consumption must be skipped");
    }

    @Test
    void status() throws Exception {
        authenticate();
        responses.put("/status", response(200, fixture("status.json")));
        assertEquals(new ApplianceStatus(87, false), api().getStatus(SENSE));

        responses.clear();
        responses.put("/status", response(200, "[{\"type\":\"connection\",\"value\":\"1\"}]"));
        assertEquals(new ApplianceStatus(null, true), api().getStatus(SENSE));

        responses.clear();
        responses.put("/status", response(200, "[{\"type\":\"battery\",\"value\":\"n/a\"}]"));
        assertEquals(new ApplianceStatus(null, null), api().getStatus(SENSE));
    }

    @Test
    void valveState() throws Exception {
        authenticate();
        responses.put("/command", response(200, fixture("command.json")));
        assertEquals(Boolean.TRUE, api().isValveOpen(GUARD));
    }

    @Test
    void parseTimestampFormats() {
        assertEquals(Instant.parse("2026-11-16T23:00:00Z"), GroheOndusApi.parseTimestamp("2026-11-17", ZONE));
        assertEquals(Instant.parse("2026-11-17T08:05:03Z"), GroheOndusApi.parseTimestamp("2026-11-17 09:05:03", ZONE));
        assertEquals(Instant.parse("2026-11-17T08:05:03Z"), GroheOndusApi.parseTimestamp("2026-11-17 9:5:3", ZONE));
        assertEquals(Instant.parse("2026-11-17T08:00:00Z"), GroheOndusApi.parseTimestamp("2026-11-17T09:00:00", ZONE));
        assertEquals(Instant.parse("2026-11-17T07:00:00Z"),
                GroheOndusApi.parseTimestamp("2026-11-17T09:00:00.000+02:00", ZONE));
        assertNull(GroheOndusApi.parseTimestamp("garbage", ZONE));
        assertNull(GroheOndusApi.parseTimestamp("", ZONE));
        assertNull(GroheOndusApi.parseTimestamp(null, ZONE));
    }
}
