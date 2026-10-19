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
package org.openhab.binding.salus.internal.aws.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.HttpSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

import com.google.gson.JsonParser;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class AwsSalusApiTest {
    private static final Instant NOW = Instant.parse("2026-10-19T10:00:00Z");
    private static final String PROPERTY = "ep9:sIT600TH:SetHeatingSetpoint_x100";
    private static final String HOST = "example-ats.iot.eu-central-1.amazonaws.com";
    private static final String URL = "https://" + HOST + "/things/device-123/shadow";
    private static final String PUBLISH_PATH = "/topics/%24aws%2Fthings%2Fdevice-123%2Fshadow%2Fupdate?qos=1";
    private static final String PUBLISH_URL = "https://" + HOST + PUBLISH_PATH;
    private static final String BODY = """
            {"state":{"desired":{"11":{"properties":{"ep9:sIT600TH:SetHeatingSetpoint_x100":2150}}}}}
            """;
    private final RestClient client = mock(RestClient.class);
    private final AuthenticationHelper authenticationHelper = mock(AuthenticationHelper.class);
    private final Clock clock = mock(Clock.class);
    private final AwsSalusApi api = new AwsSalusApi("user", new byte[0], "https://example.com", client,
            GsonMapper.INSTANCE, clock, authenticationHelper, "salus-eu", "example-ats", "eu-central-1");

    @BeforeEach
    void setupAuthentication() throws Exception {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(NOW);
        var auth = mock(AuthenticationResultResponse.class);
        when(auth.getAccessToken()).thenReturn("access-token");
        when(auth.getIdToken()).thenReturn("id-token");
        when(auth.getRefreshToken()).thenReturn("refresh-token");
        when(auth.getTokenType()).thenReturn("Bearer");
        when(auth.getExpiresIn()).thenReturn(3600);
        when(authenticationHelper.performSrpAuthentication(anyString(), anyString())).thenReturn(auth);
        var identity = new GetIdResponse();
        identity.setIdentityId("identity");
        when(authenticationHelper.getId(auth)).thenReturn(identity);
        var result = new GetCredentialsForIdentityResponse();
        var credentials = result.getCredentials();
        credentials.setAccessKeyId("example-access-key");
        credentials.setSecretKey("example-secret-key");
        credentials.setSessionToken("example-session-token");
        credentials.setExpiration(NOW.plusSeconds(7200));
        when(authenticationHelper.getCredentialsForIdentity(auth, "identity")).thenReturn(result);
    }

    @Test
    void publishesOnlyRequestedPropertyToShadowUpdateTopic() throws Exception {
        when(client.post(eq(PUBLISH_URL), any(), any(RestClient.Header[].class))).thenReturn("");

        assertThat(api.setValueForProperty("device-123", PROPERTY, 2150L)).isEqualTo(2150L);

        var content = ArgumentCaptor.forClass(RestClient.Content.class);
        var headers = ArgumentCaptor.forClass(RestClient.Header[].class);
        verify(client).post(eq(PUBLISH_URL), content.capture(), headers.capture());
        assertThat(JsonParser.parseString(content.getValue().body())).isEqualTo(JsonParser.parseString(BODY));
        assertThat(content.getValue().type()).isEqualTo("application/json");
        Map<String, String> actual = Arrays.stream(headers.getValue())
                .collect(Collectors.toMap(RestClient.Header::name, h -> h.values().getFirst()));
        assertThat(actual)
                .isEqualTo(AwsSigner.sign("POST", HOST, PUBLISH_PATH, ZonedDateTime.ofInstant(NOW, ZoneOffset.UTC),
                        new CogitoCredentials("example-access-key", "example-secret-key", "example-session-token"),
                        "eu-central-1", "iotdevicegateway", content.getValue().body()));
        verify(client, never()).get(anyString(), any(RestClient.Header[].class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void acceptsEmptyPublishResponse(@org.eclipse.jdt.annotation.Nullable String response) throws Exception {
        when(client.post(eq(PUBLISH_URL), any(), any(RestClient.Header[].class))).thenReturn(response);
        assertThat(api.setValueForProperty("device-123", PROPERTY, 2150L)).isEqualTo(2150L);
    }

    @Test
    void publishesModeCommandWithEncodedDeviceName() throws Exception {
        var url = "https://" + HOST + "/topics/%24aws%2Fthings%2Fdevice%3A123%2Fshadow%2Fupdate?qos=1";
        assertThat(api.setValueForProperty("device:123", "ep9:sIT600TH:SetHoldType", 0L)).isEqualTo(0L);
        var content = ArgumentCaptor.forClass(RestClient.Content.class);
        verify(client).post(eq(url), content.capture(), any(RestClient.Header[].class));
        assertThat(JsonParser.parseString(content.getValue().body())).isEqualTo(JsonParser.parseString("""
                {"state":{"desired":{"11":{"properties":{"ep9:sIT600TH:SetHoldType":0}}}}}
                """));
    }

    @Test
    void rejectsUnsupportedCommandsBeforePublishing() {
        assertThatThrownBy(() -> api.setValueForProperty("device-123", "ep9:sIT600TH:SetFactoryDefaultReset", 1L))
                .isInstanceOf(SalusApiException.class);
        assertThatThrownBy(() -> api.setValueForProperty("device-123", PROPERTY, 21.5))
                .isInstanceOf(SalusApiException.class);
        verifyNoInteractions(client, authenticationHelper);
    }

    @Test
    void readsReportedStateWithSignedGet() throws Exception {
        when(client.get(eq(URL), any(RestClient.Header[].class))).thenReturn("""
                {"state":{"reported":{"11":{"properties":{"ep9:sIT600TH:HeatingSetpoint_x100":2100}}}}}
                """);
        assertThat(api.findDeviceProperties("device-123")).hasSize(2);
        var headers = ArgumentCaptor.forClass(RestClient.Header[].class);
        verify(client).get(eq(URL), headers.capture());
        assertThat(Arrays.stream(headers.getValue()).filter(h -> h.name().equals("host")).findFirst())
                .contains(new RestClient.Header("host", HOST));
        verify(client, never()).post(anyString(), any(), any(RestClient.Header[].class));
    }

    @Test
    void propagatesWriteFailureAndAllowsNextCommand() throws Exception {
        when(client.post(eq(PUBLISH_URL), any(), any(RestClient.Header[].class)))
                .thenThrow(new HttpSalusApiException(403, "Forbidden")).thenReturn("");
        assertThatThrownBy(() -> api.setValueForProperty("device-123", PROPERTY, 2150L))
                .isInstanceOf(HttpSalusApiException.class);
        assertThat(api.setValueForProperty("device-123", PROPERTY, 2150L)).isEqualTo(2150L);
    }

    @Test
    void retriesAuthenticationAfterLoginFailure() throws Exception {
        var auth = authenticationHelper.performSrpAuthentication("user", "");
        clearInvocations(authenticationHelper);
        when(authenticationHelper.performSrpAuthentication(anyString(), anyString()))
                .thenThrow(new AuthSalusApiException("Authentication failed")).thenReturn(auth);
        when(client.post(eq(PUBLISH_URL), any(), any(RestClient.Header[].class))).thenReturn("");
        assertThatThrownBy(() -> api.setValueForProperty("device-123", PROPERTY, 2150L))
                .isInstanceOf(AuthSalusApiException.class);
        verifyNoInteractions(client);
        assertThat(api.setValueForProperty("device-123", PROPERTY, 2150L)).isEqualTo(2150L);
    }

    @ParameterizedTest
    @ValueSource(strings = { "UTC", "Europe/Warsaw", "America/New_York" })
    void refreshesExpiredAuthenticationBeforeSigning(String zone) throws Exception {
        when(clock.getZone()).thenReturn(ZoneId.of(zone));
        when(client.post(eq(PUBLISH_URL), any(), any(RestClient.Header[].class))).thenReturn("");
        api.setValueForProperty("device-123", PROPERTY, 2150L);
        when(clock.instant()).thenReturn(NOW.plusSeconds(3601));
        api.setValueForProperty("device-123", PROPERTY, 2150L);
        verify(authenticationHelper, times(2)).performSrpAuthentication("user", "");
    }

    @Test
    void rejectsInvalidDeviceNameBeforeSendingAnything() {
        assertThatThrownBy(() -> api.setValueForProperty("device/shadow?name=other", PROPERTY, 2150L))
                .isInstanceOf(SalusApiException.class);
        verifyNoInteractions(client, authenticationHelper);
    }
}
