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
package org.openhab.binding.salus.internal.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class SalusApiTest {

    // Find devices returns sorted set of devices
    @Test
    @DisplayName("Find devices returns sorted set of devices")
    public void testFindDevicesReturnsSortedSetOfDevices() {
        // Given
        var username = "correct_username";
        var password = "correct_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = new RestClient.Response<>(200, "devices_json");
        when(restClient.get(anyString(), any())).thenReturn(response);

        var devices = new ArrayList<Device>();
        when(mapper.parseDevices(anyString())).thenReturn(devices);

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var result = salusApi.findDevices();

        // Then
        assertThat(result.succeed()).isTrue();
        assertThat(result.body()).containsExactlyInAnyOrderElementsOf(devices);
    }

    // Find device properties returns sorted set of device properties
    @Test
    @DisplayName("Find device properties returns sorted set of device properties")
    public void testFindDevicePropertiesReturnsSortedSetOfDeviceProperties() {
        // Given
        var username = "correct_username";
        var password = "correct_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = new RestClient.Response<>(200, "device_properties_json");
        when(restClient.get(anyString(), any())).thenReturn(response);

        var deviceProperties = new ArrayList<DeviceProperty<?>>();
        when(mapper.parseDeviceProperties(anyString())).thenReturn(deviceProperties);

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var result = salusApi.findDeviceProperties("dsn");

        // Then
        assertThat(result.succeed()).isTrue();
        assertThat(result.body()).containsExactlyInAnyOrderElementsOf(deviceProperties);
    }

    // Set value for property returns OK response with datapoint value
    @Test
    @DisplayName("Set value for property returns OK response with datapoint value")
    public void testSetValueForPropertyReturnsOkResponseWithDatapointValue() {
        // Given
        var username = "correct_username";
        var password = "correct_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = new RestClient.Response<>(200, "datapoint_value_json");
        when(restClient.post(anyString(), any(), any())).thenReturn(response);

        var datapointValue = new Object();
        when(mapper.datapointValue(anyString())).thenReturn(Optional.of(datapointValue));

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var result = salusApi.setValueForProperty("dsn", "property_name", "value");

        // Then
        assertThat(result.succeed()).isTrue();
        assertThat(result.body()).isEqualTo(datapointValue);
    }

    // Login with incorrect credentials throws HttpUnauthorizedException
    @Test
    @DisplayName("Login with incorrect credentials throws HttpUnauthorizedException")
    public void testLoginWithIncorrectCredentialsThrowsHttpUnauthorizedException() {
        // Given
        var username = "incorrect_username";
        var password = "incorrect_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var response = new RestClient.Response<>(401, "unauthorized_error_json");
        when(restClient.post(anyString(), any(), any())).thenReturn(response);

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);

        // When
        var findDevicesResponse = salusApi.findDevices();

        // Then
        assertThat(findDevicesResponse.succeed()).isFalse();
        assertThat(findDevicesResponse.error()).isNotNull();
        assertThat(findDevicesResponse.error().code()).isEqualTo("401");
        assertThat(findDevicesResponse.error().message()).isEqualTo("unauthorized_error_json");
    }

    // Find devices with invalid auth token throws HttpUnauthorizedException
    @Test
    @DisplayName("Find devices with invalid auth token throws HttpUnauthorizedException")
    public void testFindDevicesWithInvalidAuthTokenThrowsHttpUnauthorizedException() {
        // Given
        var username = "correct_username";
        var password = "correct_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        when(mapper.parseError(any())).thenReturn(new Error("401", "error_message"));
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = new RestClient.Response<>(401, "unauthorized_error_json");
        when(restClient.get(anyString(), any())).thenReturn(response);

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var objectApiResponse = salusApi.findDevices();

        // Then
        assertThat(objectApiResponse.succeed()).isFalse();
        assertThat(objectApiResponse.failed()).isTrue();
        assertThat(objectApiResponse.error().code()).isEqualTo("401");
    }

    // Find device properties with invalid auth token throws HttpUnauthorizedException
    @Test
    @DisplayName("Find device properties with invalid auth token throws HttpUnauthorizedException")
    public void testFindDevicePropertiesWithInvalidAuthTokenThrowsHttpUnauthorizedException() {
        // Given
        var username = "correct_username";
        var password = "correct_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        when(mapper.parseError(any())).thenReturn(new Error("error_code", "error_message"));
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var unauthResponse = new RestClient.Response<>(401, "unauthorized_error_json");
        when(restClient.get(anyString(), any())).thenReturn(unauthResponse);

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        salusApi.findDeviceProperties("dsn");

        // Given
    }

    // Set value for property with invalid auth token throws HttpUnauthorizedException
    @Test
    @DisplayName("Set value for property with invalid auth token throws HttpUnauthorizedException")
    public void testSetValueForPropertyWithInvalidAuthTokenThrowsHttpUnauthorizedException() {
        // Given
        var username = "correct_username";
        var password = "correct_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        when(mapper.parseError(any())).thenReturn(new Error("401", "error_message"));
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = new RestClient.Response<>(401, "unauthorized_error_json");
        when(restClient.post(anyString(), any(), any())).thenReturn(response);

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var objectApiResponse = salusApi.setValueForProperty("dsn", "property_name", "value");

        // given
        assertThat(objectApiResponse.succeed()).isFalse();
        assertThat(objectApiResponse.failed()).isTrue();
        assertThat(objectApiResponse.error().code()).isEqualTo("401");
    }

    // Find device properties with invalid DSN returns ApiResponse with error
    @Test
    @DisplayName("Find device properties with invalid DSN returns ApiResponse with error")
    public void testFindDevicePropertiesWithInvalidDsnReturnsApiResponseWithError() {
        // Given
        var username = "correct_username";
        var password = "correct_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var authToken = new AuthToken("access_token", "refresh_token", 3600L, "role");
        var response = new RestClient.Response<>(404, "not_found_error_json");
        when(restClient.get(anyString(), any())).thenReturn(response);

        var error = new Error(404, "Not found");
        when(mapper.parseError(any())).thenReturn(error);

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);
        setAuthToken(salusApi, restClient, mapper, authToken);

        // When
        var result = salusApi.findDeviceProperties("invalid_dsn");

        // Then
        assertThat(result.succeed()).isFalse();
        assertThat(result.failed()).isTrue();
        assertThat(result.error()).isEqualTo(error);
    }

    // Login with incorrect credentials 3 times throws HttpForbiddenException
    @Test
    @DisplayName("Login with incorrect credentials 3 times throws HttpForbiddenException")
    public void testLoginWithIncorrectCredentials3TimesThrowsHttpForbiddenException() {
        // Given
        var username = "incorrect_username";
        var password = "incorrect_password".toCharArray();
        var baseUrl = "https://example.com";
        var restClient = mock(RestClient.class);
        var mapper = mock(GsonMapper.class);
        var clock = Clock.systemDefaultZone();

        var response = new RestClient.Response<>(403, "forbidden_error_json");
        when(restClient.post(anyString(), any(), any())).thenReturn(response);

        var salusApi = new SalusApi(username, password, baseUrl, restClient, mapper, clock);

        // When
        var findDevicesResponse = salusApi.findDevices();

        // Then
        assertThat(findDevicesResponse.succeed()).isFalse();
        assertThat(findDevicesResponse.error()).isNotNull();
        assertThat(findDevicesResponse.error().code()).isEqualTo("403");
        assertThat(findDevicesResponse.error().message()).isEqualTo("forbidden_error_json");
    }

    private void setAuthToken(SalusApi salusApi, RestClient restClient, GsonMapper mapper, AuthToken authToken) {
        var username = "correct_username";
        var password = "correct_password".toCharArray();
        var inputBody = "login_param_json";
        when(mapper.loginParam(username, password)).thenReturn(inputBody);
        var authTokenJson = "auth_token";
        when(mapper.authToken(authTokenJson)).thenReturn(authToken);

        var response = new RestClient.Response<>(200, authTokenJson);
        when(restClient.post(endsWith("/users/sign_in.json"), eq(new RestClient.Content(inputBody, "application/json")),
                any())).thenReturn(response);
    }
}
