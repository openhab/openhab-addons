/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.api.v2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.net.ssl.SSLSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.openhab.binding.evnotify.api.ApiException;
import org.openhab.binding.evnotify.api.ChargingData;
import org.openhab.binding.evnotify.api.EVNotifyClient;

/**
 * Test cases for the {@link EVNotifyClientImpl} class
 *
 * @author Michael Schmidt - Initial contribution
 */
class EVNotifyClientImplTest {

    @Mock
    HttpClient httpClient;

    private final URI basicUri = new URI("https://app.evnotify.de/soc");

    private final URI extendedUri = new URI("https://app.evnotify.de/extended");

    private final HttpRequestUriMatcher basicUriMatcher = new HttpRequestUriMatcher(basicUri);

    private final HttpRequestUriMatcher extendedUriMatcher = new HttpRequestUriMatcher(extendedUri);

    EVNotifyClientImplTest() throws URISyntaxException {
    }

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
    }

    @Test
    void shouldGiveAValidChargingDataForValidResponse() throws IOException, InterruptedException, ApiException {
        // given
        HttpResponse<String> basicResponse = getResponse(200, getValidBasicResponseBody());
        mockResponse(basicUriMatcher, basicResponse);

        HttpResponse<String> extendedResponse = getResponse(200, getValidExtendedResponseBody());
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when
        ChargingData chargingData = evNotifyClient.getCarChargingData();

        // then
        assertEquals(Float.valueOf(93.0f), chargingData.getStateOfChargeDisplay());
        assertEquals(Float.valueOf(88.5f), chargingData.getStateOfChargeBms());
        assertEquals(1631220014L, chargingData.getLastStateOfCharge().toEpochSecond());
        assertTrue(chargingData.isCharging());
        assertFalse(chargingData.isRapidChargePort());
        assertTrue(chargingData.isNormalChargePort());
        assertFalse(chargingData.isSlowChargePort());
        assertEquals(Float.valueOf(100.0f), chargingData.getStateOfHealth());
        assertEquals(Float.valueOf(14.5f), chargingData.getAuxBatteryVoltage());
        assertEquals(Float.valueOf(362.1f), chargingData.getDcBatteryVoltage());
        assertEquals(Float.valueOf(-8.7f), chargingData.getDcBatteryCurrent());
        assertEquals(Float.valueOf(-3.15027f), chargingData.getDcBatteryPower());
        assertEquals(Float.valueOf(3881.5f), chargingData.getCumulativeEnergyCharged());
        assertEquals(Float.valueOf(3738.8f), chargingData.getCumulativeEnergyDischarged());
        assertEquals(Float.valueOf(25f), chargingData.getBatteryMinTemperature());
        assertEquals(Float.valueOf(26f), chargingData.getBatteryMaxTemperature());
        assertEquals(Float.valueOf(24f), chargingData.getBatteryInletTemperature());
        assertNull(chargingData.getExternalTemperature());
        assertEquals(1631220014L, chargingData.getLastExtended().toEpochSecond());
    }

    @Test
    void shouldGiveAEmptyChargingDataForEmptyResponse() throws IOException, InterruptedException, ApiException {
        // given
        HttpResponse<String> basicResponse = getResponse(200, getEmptyResponseBody());
        mockResponse(basicUriMatcher, basicResponse);

        HttpResponse<String> extendedResponse = getResponse(200, getEmptyResponseBody());
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when
        ChargingData chargingData = evNotifyClient.getCarChargingData();

        // then
        assertNull(chargingData.getStateOfChargeDisplay());
        assertNull(chargingData.getStateOfChargeBms());
        assertNull(chargingData.getLastStateOfCharge());
        assertFalse(chargingData.isCharging());
        assertFalse(chargingData.isRapidChargePort());
        assertFalse(chargingData.isNormalChargePort());
        assertFalse(chargingData.isSlowChargePort());
        assertNull(chargingData.getStateOfHealth());
        assertNull(chargingData.getAuxBatteryVoltage());
        assertNull(chargingData.getDcBatteryVoltage());
        assertNull(chargingData.getDcBatteryCurrent());
        assertNull(chargingData.getDcBatteryPower());
        assertNull(chargingData.getCumulativeEnergyCharged());
        assertNull(chargingData.getCumulativeEnergyDischarged());
        assertNull(chargingData.getBatteryMinTemperature());
        assertNull(chargingData.getBatteryMaxTemperature());
        assertNull(chargingData.getBatteryInletTemperature());
        assertNull(chargingData.getExternalTemperature());
        assertNull(chargingData.getLastExtended());
    }

    @Test
    void shouldThrowApiExceptionForNullBasicResponse() throws IOException, InterruptedException {
        // given
        mockResponse(basicUriMatcher, null);

        HttpResponse<String> extendedResponse = getResponse(200, getValidExtendedResponseBody());
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when then
        ApiException exception = assertThrows(ApiException.class, evNotifyClient::getCarChargingData);
        assertEquals("Response was null", exception.getMessage());
    }

    @Test
    void shouldThrowApiExceptionForNullExtendedResponse() throws IOException, InterruptedException {
        // given
        HttpResponse<String> basicResponse = getResponse(200, getValidBasicResponseBody());
        mockResponse(basicUriMatcher, basicResponse);

        mockResponse(extendedUriMatcher, null);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when then
        ApiException exception = assertThrows(ApiException.class, evNotifyClient::getCarChargingData);
        assertEquals("Response was null", exception.getMessage());
    }

    @Test
    void shouldThrowApiExceptionForInvalidBasicResponseStatus() throws IOException, InterruptedException {
        // given
        HttpResponse<String> basicResponse = getResponse(401, getErrorResponseBody());
        mockResponse(basicUriMatcher, basicResponse);

        HttpResponse<String> extendedResponse = getResponse(200, getValidExtendedResponseBody());
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when then
        ApiException exception = assertThrows(ApiException.class, evNotifyClient::getCarChargingData);
        assertEquals(
                "Request failed with status 401 with error code 1900 and message 'Provided token is invalid or no longer valid.'",
                exception.getMessage());
    }

    @Test
    void shouldThrowApiExceptionForInvalidExtendedResponseStatus() throws IOException, InterruptedException {
        // given
        HttpResponse<String> basicResponse = getResponse(200, getValidBasicResponseBody());
        mockResponse(basicUriMatcher, basicResponse);

        HttpResponse<String> extendedResponse = getResponse(401, getErrorResponseBody());
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when then
        ApiException exception = assertThrows(ApiException.class, evNotifyClient::getCarChargingData);
        assertEquals(
                "Request failed with status 401 with error code 1900 and message 'Provided token is invalid or no longer valid.'",
                exception.getMessage());
    }

    @Test
    void shouldThrowApiExceptionForNullBasicResponseBody() throws IOException, InterruptedException {
        // given
        HttpResponse<String> basicResponse = getResponse(200, null);
        mockResponse(basicUriMatcher, basicResponse);

        HttpResponse<String> extendedResponse = getResponse(200, getValidExtendedResponseBody());
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when then
        ApiException exception = assertThrows(ApiException.class, evNotifyClient::getCarChargingData);
        assertEquals("Request failed with null response body", exception.getMessage());
    }

    @Test
    void shouldThrowApiExceptionForNullExtendedResponseBody() throws IOException, InterruptedException {
        // given
        HttpResponse<String> basicResponse = getResponse(200, getValidBasicResponseBody());
        mockResponse(basicUriMatcher, basicResponse);

        HttpResponse<String> extendedResponse = getResponse(200, null);
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when then
        ApiException exception = assertThrows(ApiException.class, evNotifyClient::getCarChargingData);
        assertEquals("Request failed with null response body", exception.getMessage());
    }

    @Test
    void shouldThrowApiExceptionForInvalidBasicResponseBody() throws IOException, InterruptedException {
        // given
        HttpResponse<String> basicResponse = getResponse(200, getInvalidResponseBody());
        mockResponse(basicUriMatcher, basicResponse);

        HttpResponse<String> extendedResponse = getResponse(200, getValidExtendedResponseBody());
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when then
        ApiException exception = assertThrows(ApiException.class, evNotifyClient::getCarChargingData);
        assertEquals("Request failed with invalid response body", exception.getMessage());
    }

    @Test
    void shouldThrowApiExceptionForInvalidExtendedResponseBody() throws IOException, InterruptedException {
        // given
        HttpResponse<String> basicResponse = getResponse(200, getValidBasicResponseBody());
        mockResponse(basicUriMatcher, basicResponse);

        HttpResponse<String> extendedResponse = getResponse(200, getInvalidResponseBody());
        mockResponse(extendedUriMatcher, extendedResponse);

        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when then
        ApiException exception = assertThrows(ApiException.class, evNotifyClient::getCarChargingData);
        assertEquals("Request failed with invalid response body", exception.getMessage());
    }

    private HttpResponse<String> getResponse(Integer statusCode, String body) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }

    private String getValidBasicResponseBody() {
        return "{\"soc_display\": 93,\"soc_bms\": 88.5,\"last_soc\": 1631220014}";
    }

    private String getErrorResponseBody() {
        return "{\"error\":{\"code\":1900,\"message\":\"Provided token is invalid or no longer valid.\"}}";
    }

    private String getEmptyResponseBody() {
        return "{}";
    }

    private String getInvalidResponseBody() {
        return "{999}";
    }

    private String getValidExtendedResponseBody() {
        return "{\"soh\": 100,\"charging\": 1,\"rapid_charge_port\": 0,\"normal_charge_port\": 1,"
                + "\"slow_charge_port\": null,\"aux_battery_voltage\": 14.5,\"dc_battery_voltage\": 362.1,"
                + "\"dc_battery_current\": -8.7,\"dc_battery_power\": -3.15027,"
                + "\"cumulative_energy_charged\": 3881.5,\"cumulative_energy_discharged\": 3738.8,"
                + "\"battery_min_temperature\": 25,\"battery_max_temperature\": 26,"
                + "\"battery_inlet_temperature\": 24,\"external_temperature\": null,\"odo\": null,"
                + "\"last_extended\": 1631220014}";
    }

    @SuppressWarnings("unchecked")
    private void mockResponse(HttpRequestUriMatcher httpRequestUriMatcher, HttpResponse<String> response)
            throws IOException, InterruptedException {
        when(httpClient.send(argThat(httpRequestUriMatcher), any(HttpResponse.BodyHandler.class))).thenReturn(response);
    }

    private static class HttpRequestUriMatcher implements ArgumentMatcher<HttpRequest> {

        private final URI uri;

        protected HttpRequestUriMatcher(URI uri) {
            this.uri = uri;
        }

        @Override
        public boolean matches(HttpRequest httpRequest) {
            return httpRequest != null && this.uri.getScheme().equals(httpRequest.uri().getScheme())
                    && this.uri.getHost().equals(httpRequest.uri().getHost())
                    && this.uri.getPath().equals(httpRequest.uri().getPath());
        }
    }
}
