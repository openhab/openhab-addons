/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal.cloud.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Optional;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.HttpSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@SuppressWarnings("DataFlowIssue")
@NonNullByDefault
public class HttpSalusApiTest {

    // Find devices returns sorted set of devices
    @Test
    @DisplayName("Find devices returns sorted set of devices")
    public void testFindDevicesReturnsSortedSetOfDevices() throws Exception {
        // Given
        var username = "correct_username";
        var password = "correct_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = "devices_json";
        when(restClient.get(anyString(), any())).thenReturn(response);

        var devices = new ArrayList<Device>();
        when(mapper.parseDevices(anyString())).thenReturn(devices);

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var result = salusApi.findDevices();

        // Then
        assertThat(result).containsExactlyInAnyOrderElementsOf(devices);
    }

    // Find device properties returns sorted set of device properties
    @Test
    @DisplayName("Find device properties returns sorted set of device properties")
    public void testFindDevicePropertiesReturnsSortedSetOfDeviceProperties() throws Exception {
        // Given
        var username = "correct_username";
        var password = "correct_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = "device_properties_json";
        when(restClient.get(anyString(), any())).thenReturn(response);

        var deviceProperties = new ArrayList<DeviceProperty<?>>();
        when(mapper.parseDeviceProperties(anyString())).thenReturn(deviceProperties);

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var result = salusApi.findDeviceProperties("dsn");

        // Then
        assertThat(result).containsExactlyInAnyOrderElementsOf(deviceProperties);
    }

    // Set value for property returns OK response with datapoint value
    @Test
    @DisplayName("Set value for property returns OK response with datapoint value")
    public void testSetValueForPropertyReturnsOkResponseWithDatapointValue() throws Exception {
        // Given
        var username = "correct_username";
        var password = "correct_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = "datapoint_value_json";
        when(restClient.post(anyString(), any(), any())).thenReturn(response);

        var datapointValue = new Object();
        when(mapper.datapointValue(anyString())).thenReturn(Optional.of(datapointValue));

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var result = salusApi.setValueForProperty("dsn", "property_name", "value");

        // Then
        assertThat(result).isEqualTo(datapointValue);
    }

    // Login with incorrect credentials throws HttpUnauthorizedException
    @Test
    @DisplayName("Login with incorrect credentials throws HttpUnauthorizedException")
    public void testLoginWithIncorrectCredentialsThrowsHttpUnauthorizedException() throws Exception {
        // Given
        var username = "incorrect_username";
        var password = "incorrect_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        when(restClient.post(anyString(), any(), any()))
                .thenThrow(new HttpSalusApiException(401, "unauthorized_error_json"));

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);

        // When
        ThrowingCallable findDevicesResponse = salusApi::findDevices;

        // Then
        assertThatThrownBy(findDevicesResponse).isInstanceOf(AuthSalusApiException.class)
                .hasMessage("Could not log in, for user incorrect_username");
    }

    // Find devices with invalid auth token throws HttpUnauthorizedException
    @Test
    @DisplayName("Find devices with invalid auth token throws HttpUnauthorizedException")
    public void testFindDevicesWithInvalidAuthTokenThrowsHttpUnauthorizedException() throws Exception {
        // Given
        var username = "correct_username";
        var password = "correct_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        when(restClient.get(anyString(), any())).thenThrow(new HttpSalusApiException(401, "unauthorized_error_json"));

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        ThrowingCallable objectApiResponse = salusApi::findDevices;

        // Then
        assertThatThrownBy(objectApiResponse).isInstanceOf(HttpSalusApiException.class)
                .hasMessage("HTTP Error 401: unauthorized_error_json");
    }

    // Find device properties with invalid auth token throws HttpUnauthorizedException
    @Test
    @DisplayName("Find device properties with invalid auth token throws HttpUnauthorizedException")
    public void testFindDevicePropertiesWithInvalidAuthTokenThrowsHttpUnauthorizedException() throws Exception {
        // Given
        var username = "correct_username";
        var password = "correct_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        when(restClient.get(anyString(), any())).thenThrow(new HttpSalusApiException(401, "unauthorized_error_json"));

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        ThrowingCallable objectApiResponse = () -> salusApi.findDeviceProperties("dsn");

        // Given
        assertThatThrownBy(objectApiResponse).isInstanceOf(HttpSalusApiException.class)
                .hasMessage("HTTP Error 401: unauthorized_error_json");
    }

    // Set value for property with invalid auth token throws HttpUnauthorizedException
    @Test
    @DisplayName("Set value for property with invalid auth token throws HttpUnauthorizedException")
    public void testSetValueForPropertyWithInvalidAuthTokenThrowsHttpUnauthorizedException() throws Exception {
        // Given
        var username = "correct_username";
        var password = "correct_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);

        // When
        ThrowingCallable objectApiResponse = () -> salusApi.setValueForProperty("dsn", "property_name", "value");

        // given

        assertThatThrownBy(objectApiResponse).isInstanceOf(AuthSalusApiException.class)
                .hasMessage("Could not log in, for user correct_username");
    }

    // Find device properties with invalid DSN returns ApiResponse with error
    @Test
    @DisplayName("Find device properties with invalid DSN returns ApiResponse with error")
    public void testFindDevicePropertiesWithInvalidDsnReturnsApiResponseWithError() throws Exception {
        // Given
        var username = "correct_username";
        var password = "correct_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        when(restClient.get(anyString(), any())).thenThrow(new HttpSalusApiException(404, "not found"));

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        ThrowingCallable result = () -> salusApi.findDeviceProperties("invalid_dsn");

        // Then
        assertThatThrownBy(result).isInstanceOf(HttpSalusApiException.class).hasMessage("HTTP Error 404: not found");
    }

    // Login with incorrect credentials 3 times throws HttpForbiddenException
    @Test
    @DisplayName("Login with incorrect credentials 3 times throws HttpForbiddenException")
    public void testLoginWithIncorrectCredentials3TimesThrowsHttpForbiddenException() throws Exception {
        // Given
        var username = "incorrect_username";
        var password = "incorrect_password".getBytes(UTF_8);
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        when(restClient.post(anyString(), any(), any()))
                .thenThrow(new HttpSalusApiException(403, "forbidden_error_json"));

        var salusApi = new HttpSalusApi(username, password, baseUrl, restClient, mapper, clock);

        // When
        ThrowingCallable findDevicesResponse = salusApi::findDevices;

        // Then
        assertThatThrownBy(findDevicesResponse).isInstanceOf(AuthSalusApiException.class)
                .hasMessage("Could not log in, for user incorrect_username");
    }

    private void setAuthToken(HttpSalusApi salusApi, RestClient restClient, GsonMapper mapper, AuthToken authToken)
            throws SalusApiException {
        var username = "correct_username";
        var password = "correct_password".getBytes(UTF_8);
        var inputBody = "login_param_json";
        when(mapper.loginParam(username, password)).thenReturn(inputBody);
        var authTokenJson = "auth_token";
        when(mapper.authToken(authTokenJson)).thenReturn(authToken);

        when(restClient.post(endsWith("/users/sign_in.json"), eq(new RestClient.Content(inputBody, "application/json")),
                any())).thenReturn(authTokenJson);
    }
}
