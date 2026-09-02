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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.handler.AccountHandlerMock;
import org.openhab.binding.mercedesme.internal.handler.ThingCallbackListener;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * {@link StatusTests} sequences for testing ThingStatus
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class StatusTests {
    public static final String JUNIT_EMAIL = "test@junit.org";
    public static final String JUNIT_PASSWORD = "junitPassword";
    public static final String JUNIT_TOKEN = "junitTestToken";
    public static final String JUNIT_REFRESH_TOKEN = "junitRefreshToken";

    public static void tearDown(AccountHandlerMock ahm) {
        ahm.dispose();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail();
        }
    }

    public static HttpClient getHttpClient(int tokenResponseCode) {
        Utils.initialize(Utils.timeZoneProvider, Utils.localeProvider);
        HttpClient httpClient = mock(HttpClient.class);
        try {
            Request clientRequest = mock(Request.class);
            when(httpClient.newRequest(anyString())).thenReturn(clientRequest);
            when(httpClient.POST(anyString())).thenReturn(clientRequest);
            when(clientRequest.followRedirects(anyBoolean())).thenReturn(clientRequest);
            when(clientRequest.header(anyString(), anyString())).thenReturn(clientRequest);
            when(clientRequest.content(any())).thenReturn(clientRequest);
            when(clientRequest.timeout(anyLong(), any())).thenReturn(clientRequest);
            when(clientRequest.getURI()).thenReturn(null);
            ContentResponse response = mock(ContentResponse.class);
            when(response.getRequest()).thenReturn(clientRequest);
            when(response.getStatus()).thenReturn(tokenResponseCode);
            String tokenResponse = FileReader.readFileInString("src/test/resources/json/TokenResponse.json");
            when(response.getContentAsString()).thenReturn(tokenResponse);
            when(clientRequest.send()).thenReturn(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(e.getMessage());
        } catch (TimeoutException | ExecutionException e) {
            fail(e.getMessage());
        }
        return httpClient;
    }

    private static AccountHandlerMock createAccountHandler(Map<String, Object> config, int tokenResponseCode,
            ThingCallbackListener tcl) {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        bi.setConfiguration(new Configuration(config));
        AccountHandlerMock ahm = new AccountHandlerMock(bi, null, getHttpClient(tokenResponseCode));
        ahm.setCallback(tcl);
        return ahm;
    }

    private static void assertOfflineStatus(ThingStatusInfo tsi, ThingStatusDetail expectedDetail,
            @Nullable String expectedDescription, String messagePrefix) {
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), messagePrefix + " offline");
        assertEquals(expectedDetail, tsi.getStatusDetail(), messagePrefix + " detail");
        if (expectedDescription != null) {
            assertEquals(expectedDescription, tsi.getDescription(), messagePrefix + " text");
        }
    }

    @Test
    void testInvalidConfigEmailMissing() {
        Map<String, Object> config = new HashMap<>();
        ThingCallbackListener tcl = new ThingCallbackListener();
        AccountHandlerMock ahm = createAccountHandler(config, 404, tcl);
        ahm.initialize();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertOfflineStatus(tsi, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/mercedesme.account.status.config.email-missing", "EMail");
        tearDown(ahm);
    }

    @Test
    void testInvalidConfigPasswordMissing() {
        Map<String, Object> config = new HashMap<>();
        config.put("email", JUNIT_EMAIL);
        ThingCallbackListener tcl = new ThingCallbackListener();
        AccountHandlerMock ahm = createAccountHandler(config, 404, tcl);
        ahm.initialize();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertOfflineStatus(tsi, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/mercedesme.account.status.config.password-missing", "Password");
        tearDown(ahm);
    }

    @Test
    void testInvalidConfigRegionMissing() {
        Map<String, Object> config = new HashMap<>();
        config.put("email", JUNIT_EMAIL);
        config.put("password", JUNIT_PASSWORD);
        ThingCallbackListener tcl = new ThingCallbackListener();
        AccountHandlerMock ahm = createAccountHandler(config, 404, tcl);
        ahm.initialize();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertOfflineStatus(tsi, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/mercedesme.account.status.config.region-missing", "Region");
        tearDown(ahm);
    }

    @Test
    void testInvalidConfigAuthFailure() {
        Map<String, Object> config = new HashMap<>();
        config.put("email", JUNIT_EMAIL);
        config.put("password", JUNIT_PASSWORD);
        config.put("region", "row");
        ThingCallbackListener tcl = new ThingCallbackListener();
        AccountHandlerMock ahm = createAccountHandler(config, 404, tcl);
        ahm.initialize();
        ahm.refresh();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertOfflineStatus(tsi, ThingStatusDetail.COMMUNICATION_ERROR, null, "Auth");
        tearDown(ahm);
    }

    @Test
    void testInvalidConfigRefreshIntervalInvalid() {
        Map<String, Object> config = new HashMap<>();
        config.put("email", JUNIT_EMAIL);
        config.put("password", JUNIT_PASSWORD);
        config.put("region", "row");
        config.put("refreshInterval", 0);
        ThingCallbackListener tcl = new ThingCallbackListener();
        AccountHandlerMock ahm = createAccountHandler(config, 404, tcl);
        ahm.initialize();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertOfflineStatus(tsi, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/mercedesme.account.status.config.refresh-invalid[\"0\"]", "Refresh");
        tearDown(ahm);
    }

    @Test
    void testNoTokenStored() {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        Map<String, Object> config = new HashMap<>();
        config.put("refreshInterval", 15);
        config.put("region", "row");
        config.put("email", JUNIT_EMAIL);
        config.put("password", JUNIT_PASSWORD);
        bi.setConfiguration(new Configuration(config));
        AccountHandlerMock ahm = new AccountHandlerMock(bi, null, getHttpClient(404));
        ThingCallbackListener tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        ahm.refresh();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Auth Offline");
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, tsi.getStatusDetail(), "Auth details");
        String statusDescription = tsi.getDescription();
        assertNotNull(statusDescription);
        assertTrue(statusDescription.contains("@text/mercedesme.account.status.auth-failure"),
                "Auth text: " + statusDescription);
        tearDown(ahm);
        AccessTokenResponse token = new AccessTokenResponse();
        token.setExpiresIn(3000);
        token.setAccessToken(JUNIT_TOKEN);
        token.setRefreshToken(JUNIT_REFRESH_TOKEN);
        ahm.onAccessTokenResponse(token);
        ahm.connect();
        tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.ONLINE, tsi.getStatus(), "Auth Online");
        tearDown(ahm);
    }

    @Test
    void testTokenStored() {
        BridgeImpl bi = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        Map<String, Object> config = new HashMap<>();
        config.put("refreshInterval", Integer.MAX_VALUE);
        config.put("region", "row");
        config.put("email", JUNIT_EMAIL);
        config.put("password", JUNIT_PASSWORD);
        bi.setConfiguration(new Configuration(config));
        String tokenResponse = FileReader.readFileInString("src/test/resources/json/TokenResponse.json");
        AccountHandlerMock ahm = new AccountHandlerMock(bi, tokenResponse, getHttpClient(200));
        ThingCallbackListener tcl = new ThingCallbackListener();
        ahm.setCallback(tcl);
        ahm.initialize();
        ThingStatusInfo tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(),
                "OFFLINE " + tsi.getStatusDetail() + " " + tsi.getDescription());
        ahm.connect();
        tsi = tcl.getThingStatus();
        assertEquals(ThingStatus.ONLINE, tsi.getStatus(), "Socket Online");
        tearDown(ahm);
    }
}
