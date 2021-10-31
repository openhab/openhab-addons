/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.net.ssl.SSLSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.evnotify.api.CarChargingData;
import org.openhab.binding.evnotify.api.EVNotifyClient;

/**
 * Test cases for the {@link EVNotifyClientImpl} class
 *
 * @author Michael Schmidt - Initial contribution
 */
class EVNotifyClientImplTest {

    @Mock
    HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
    }

    @Test
    void shouldGiveAValidCarCHargingDataForValidResponse() throws IOException, InterruptedException {
        // given
        HttpResponse<String> response = getResponse(200, getResponseBody());
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        EVNotifyClient evNotifyClient = new EVNotifyClientImpl("aKey", "token", httpClient);

        // when
        CarChargingData carChargingData = evNotifyClient.getCarChargingData();

        // then
        assertEquals(true, carChargingData.isCharging());
        assertEquals(false, carChargingData.isRapidChargePort());
        assertEquals(true, carChargingData.isNormalChargePort());
        assertEquals(false, carChargingData.isSlowChargePort());
        assertEquals(Float.valueOf(100.0f), carChargingData.getStateOfHealth());
        assertEquals(Float.valueOf(14.5f), carChargingData.getAuxBatteryVoltage());
        assertEquals(Float.valueOf(362.1f), carChargingData.getDcBatteryVoltage());
        assertEquals(Float.valueOf(-8.7f), carChargingData.getDcBatteryCurrent());
        assertEquals(Float.valueOf(-3.15027f), carChargingData.getDcBatteryPower());
        assertEquals(Float.valueOf(3881.5f), carChargingData.getCumulativeEnergyCharged());
        assertEquals(Float.valueOf(3738.8f), carChargingData.getCumulativeEnergyDischarged());
        assertEquals(Float.valueOf(25f), carChargingData.getBatteryMinTemperature());
        assertEquals(Float.valueOf(26f), carChargingData.getBatteryMaxTemperature());
        assertEquals(Float.valueOf(24f), carChargingData.getBatteryInletTemperature());
        assertNull(carChargingData.getExternalTemperature());
        assertEquals(1631220014L, carChargingData.getLastExtended().toInstant().toEpochMilli());
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

    private String getResponseBody() {
        return "{" + "\"soh\": 100," + "\"charging\": 1," + "\"rapid_charge_port\": 0," + "\"normal_charge_port\": 1,"
                + "\"slow_charge_port\": null," + "\"aux_battery_voltage\": 14.5," + "\"dc_battery_voltage\": 362.1,"
                + "\"dc_battery_current\": -8.7," + "\"dc_battery_power\": -3.15027,"
                + "\"cumulative_energy_charged\": 3881.5," + "\"cumulative_energy_discharged\": 3738.8,"
                + "\"battery_min_temperature\": 25," + "\"battery_max_temperature\": 26,"
                + "\"battery_inlet_temperature\": 24," + "\"external_temperature\": null," + "\"odo\": null,"
                + "\"last_extended\": 1631220014" + "}";
    }
}
