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
package org.openhab.binding.airgradient.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.airgradient.internal.communication.AirGradientCommunicationException;
import org.openhab.binding.airgradient.internal.communication.RemoteAPIController;
import org.openhab.binding.airgradient.internal.config.AirGradientAPIConfiguration;

import com.google.gson.Gson;

/**
 * @author Jørgen Austvik - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class RemoteApiControllerTest {

    private static final AirGradientAPIConfiguration TEST_CONFIG = new AirGradientAPIConfiguration() {
        {
            hostname = "abc123";
            token = "def456";
        }
    };

    private static final String SINGLE_CONFIG = """
             {"country":"NO","pmStandard":"ugm3","ledBarMode":"off","abcDays":8,"tvocLearningOffset":12,"noxLearningOffset":12,"mqttBrokerUrl":"https://192.168.1.1/mqtt","temperatureUnit":"c","configurationControl":"both","postDataToAirGradient":true,"ledBarBrightness":100,"displayBrightness":100,"offlineMode":false,"model":"I-9PSL"}
            """;

    private static final String SINGLE_CONTENT = """
             {"locationId":4321,"locationName":"Some other name","pm01":1,"pm02":2,"pm10":2,"pm003Count":536,"atmp":20.45,"rhum":16.61,"rco2":456,"tvoc":51.644928,"wifi":-54,"timestamp":"2024-01-07T13:00:20.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"serial","firmwareVersion":null,"tvocIndex":null,"noxIndex":null}
            """;

    private static final String MULTI_CONTENT = """
            [
             {"locationId":1234,"locationName":"Some Name","pm01":1,"pm02":2,"pm10":2,"pm003Count":536,"atmp":20.45,"rhum":16.61,"rco2":null,"tvoc":null,"wifi":-54,"timestamp":"2024-01-07T13:00:20.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"serial","firmwareVersion":null,"tvocIndex":null,"noxIndex":null},
             {"locationId":4321,"locationName":"Some other name","pm01":1,"pm02":2,"pm10":2,"pm003Count":536,"atmp":20.45,"rhum":16.61,"rco2":null,"tvoc":null,"wifi":-54,"timestamp":"2024-01-07T13:00:20.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"serial","firmwareVersion":null,"tvocIndex":null,"noxIndex":null}
            ]""";
    private static final String MULTI_CONTENT2 = """
            [{"locationId":654321,"locationName":"xxxx","pm01":0,"pm02":1,"pm10":1,"pm003Count":null,"atmp":24.2,"rhum":18,"rco2":519,"tvoc":50.793266,"wifi":-62,"timestamp":"2024-02-01T19:15:37.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"ecda3b1a2a50","firmwareVersion":null,"tvocIndex":52,"noxIndex":1},{"locationId":123456,"locationName":"yyyy","pm01":0,"pm02":0,"pm10":0,"pm003Count":105,"atmp":22.33,"rhum":24,"rco2":468,"tvoc":130.95694,"wifi":-50,"timestamp":"2024-02-01T19:15:34.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"84fce612e644","firmwareVersion":null,"tvocIndex":137,"noxIndex":1}]
            """;

    private static final String PROMETHEUS_CONTENT = """
            # HELP pm02 Particulate Matter PM2.5 value
            # TYPE pm02 gauge
            pm02{id="Airgradient"}6
            # HELP rco2 CO2 value, in ppm
            # TYPE rco2 gauge
            rco2{id="Airgradient"}862
            # HELP atmp Temperature, in degrees Celsius
            # TYPE atmp gauge
            atmp{id="Airgradient"}31.6
            # HELP rhum Relative humidity, in percent
            # TYPE rhum gauge
            rhum{id="Airgradient"}38
            # HELP tvoc Total volatile organic components, in μg/m³
            # TYPE tvoc gauge
            tvoc{id="Airgradient"}51.644928
            # HELP nox, in μg/m³
            # TYPE nox gauge
            nox{id="Airgradient"}1
            """;

    private static final String OPEN_METRICS_CONTENT = """
            # HELP airgradient_info AirGradient device information
            # TYPE airgradient_info info
            airgradient_info{airgradient_serial_number="4XXXXXXXXXXc",airgradient_device_type="ONE_I-9PSL",airgradient_library_version="3.0.4"} 1
            # HELP airgradient_config_ok 1 if the AirGradient device was able to successfully fetch its configuration from the server
            # TYPE airgradient_config_ok gauge
            airgradient_config_ok{} 1
            # HELP airgradient_post_ok 1 if the AirGradient device was able to successfully send to the server
            # TYPE airgradient_post_ok gauge
            airgradient_post_ok{} 1
            # HELP airgradient_wifi_rssi_dbm WiFi signal strength from the AirGradient device perspective, in dBm
            # TYPE airgradient_wifi_rssi_dbm gauge
            # UNIT airgradient_wifi_rssi_dbm dbm
            airgradient_wifi_rssi_dbm{} -51
            # HELP airgradient_co2_ppm Carbon dioxide concentration as measured by the AirGradient S8 sensor, in parts per million
            # TYPE airgradient_co2_ppm gauge
            # UNIT airgradient_co2_ppm ppm
            airgradient_co2_ppm{} 589
            # HELP airgradient_pm1_ugm3 PM1.0 concentration as measured by the AirGradient PMS sensor, in micrograms per cubic meter
            # TYPE airgradient_pm1_ugm3 gauge
            # UNIT airgradient_pm1_ugm3 ugm3
            airgradient_pm1_ugm3{} 3
            # HELP airgradient_pm2d5_ugm3 PM2.5 concentration as measured by the AirGradient PMS sensor, in micrograms per cubic meter
            # TYPE airgradient_pm2d5_ugm3 gauge
            # UNIT airgradient_pm2d5_ugm3 ugm3
            airgradient_pm2d5_ugm3{} 3
            # HELP airgradient_pm10_ugm3 PM10 concentration as measured by the AirGradient PMS sensor, in micrograms per cubic meter
            # TYPE airgradient_pm10_ugm3 gauge
            # UNIT airgradient_pm10_ugm3 ugm3
            airgradient_pm10_ugm3{} 3
            # HELP airgradient_pm0d3_p100ml PM0.3 concentration as measured by the AirGradient PMS sensor, in number of particules per 100 milliliters
            # TYPE airgradient_pm0d3_p100ml gauge
            # UNIT airgradient_pm0d3_p100ml p100ml
            airgradient_pm0d3_p100ml{} 594
            # HELP airgradient_tvoc_index The processed Total Volatile Organic Compounds (TVOC) index as measured by the AirGradient SGP sensor
            # TYPE airgradient_tvoc_index gauge
            airgradient_tvoc_index{} 220
            # HELP airgradient_tvoc_raw_index The raw input value to the Total Volatile Organic Compounds (TVOC) index as measured by the AirGradient SGP sensor
            # TYPE airgradient_tvoc_raw_index gauge
            airgradient_tvoc_raw_index{} 30801
            # HELP airgradient_nox_index The processed Nitrous Oxide (NOx) index as measured by the AirGradient SGP sensor
            # TYPE airgradient_nox_index gauge
            airgradient_nox_index{} 1
            # HELP airgradient_temperature_degc The ambient temperature as measured by the AirGradient SHT sensor, in degrees Celsius
            # TYPE airgradient_temperature_degc gauge
            # UNIT airgradient_temperature_degc degc
            airgradient_temperature_degc{} 23.69
            # HELP airgradient_humidity_percent The relative humidity as measured by the AirGradient SHT sensor
            # TYPE airgradient_humidity_percent gauge
            # UNIT airgradient_humidity_percent percent
            airgradient_humidity_percent{} 39
            # EOF
            """;

    @Nullable
    private RemoteAPIController sut;

    @Nullable
    HttpClient httpClientMock;

    @Nullable
    Request requestMock;

    @BeforeEach
    public void setUp() {
        httpClientMock = Mockito.mock(HttpClient.class);
        requestMock = Mockito.mock(Request.class);

        sut = new RemoteAPIController(requireNonNull(httpClientMock), new Gson(), TEST_CONFIG);
    }

    @Test
    public void testGetMeasuresNone() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        Mockito.when(requestMock.send()).thenReturn(response);
        Mockito.when(response.getMediaType()).thenReturn("application/json");
        Mockito.when(response.getStatus()).thenReturn(500);

        AirGradientCommunicationException agce = Assertions.assertThrows(AirGradientCommunicationException.class,
                () -> sut.getMeasures());
        assertThat(agce.getMessage(), is("Returned status code: 500"));
    }

    @Test
    public void testGetMeasuresSingle() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        Mockito.when(requestMock.send()).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getMediaType()).thenReturn("application/json");
        Mockito.when(response.getContentAsString()).thenReturn(SINGLE_CONTENT);

        var res = sut.getMeasures();
        assertThat(res, is(not(empty())));
        assertThat(res.size(), is(1));
        assertThat(res.get(0).locationName, is("Some other name"));
    }

    @Test
    public void testGetMeasuresMulti() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        Mockito.when(requestMock.send()).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getMediaType()).thenReturn("application/json");
        Mockito.when(response.getContentAsString()).thenReturn(MULTI_CONTENT);

        var res = sut.getMeasures();
        assertThat(res, is(not(empty())));
        assertThat(res.size(), is(2));
        assertThat(res.get(0).locationName, is("Some Name"));
        assertThat(res.get(1).locationName, is("Some other name"));
    }

    @Test
    public void testGetMeasuresMulti2() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        Mockito.when(requestMock.send()).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getMediaType()).thenReturn("application/json");
        Mockito.when(response.getContentAsString()).thenReturn(MULTI_CONTENT2);

        var res = sut.getMeasures();
        assertThat(res, is(not(empty())));
        assertThat(res.size(), is(2));
        assertThat(res.get(0).locationName, is("xxxx"));
        assertThat(res.get(1).locationName, is("yyyy"));
    }

    @Test
    public void testGetMeasuresPrometheus() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        Mockito.when(requestMock.send()).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getMediaType()).thenReturn("text/plain");
        Mockito.when(response.getContentAsString()).thenReturn(PROMETHEUS_CONTENT);

        var res = sut.getMeasures();
        assertThat(res, is(not(empty())));
        assertThat(res.size(), is(1));
        assertThat(res.get(0).pm02, closeTo(6, 0.1));
        assertThat(res.get(0).rco2, closeTo(862, 0.1));
        assertThat(res.get(0).atmp, closeTo(31.6, 0.1));
        assertThat(res.get(0).rhum, closeTo(38, 0.1));
        assertThat(res.get(0).tvoc, closeTo(51.644928, 0.1));
        assertThat(res.get(0).noxIndex, closeTo(1, 0.1));
        assertThat(res.get(0).locationId, is("Airgradient"));
        assertThat(res.get(0).locationName, is("Airgradient"));
        assertThat(res.get(0).serialno, is("Airgradient"));
    }

    @Test
    public void testGetMeasuresOpenMetrics() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        Mockito.when(requestMock.send()).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getMediaType()).thenReturn("application/openmetrics-text");
        Mockito.when(response.getContentAsString()).thenReturn(OPEN_METRICS_CONTENT);

        var res = sut.getMeasures();
        assertThat(res, is(not(empty())));
        assertThat(res.size(), is(1));
        assertThat(res.get(0).pm01, closeTo(3, 0.1));
        assertThat(res.get(0).pm02, closeTo(3, 0.1));
        assertThat(res.get(0).pm10, closeTo(3, 0.1));
        assertThat(res.get(0).rco2, closeTo(589, 0.1));
        assertThat(res.get(0).atmp, closeTo(23.69, 0.1));
        assertThat(res.get(0).rhum, closeTo(39, 0.1));
        assertThat(res.get(0).tvoc, closeTo(220, 0.1));
        assertThat(res.get(0).noxIndex, closeTo(1, 0.1));
        assertThat(res.get(0).serialno, is("4XXXXXXXXXXc"));
    }

    @Test
    public void testGetConfig() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        Mockito.when(requestMock.send()).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getMediaType()).thenReturn("application/json");
        Mockito.when(response.getContentAsString()).thenReturn(SINGLE_CONFIG);

        var res = sut.getConfig();
        assertThat(res.abcDays, is(8L));
        assertThat(res.configurationControl, is("both"));
        assertThat(res.country, is("NO"));
        assertThat(res.displayBrightness, is(100L));
        assertThat(res.ledBarBrightness, is(100L));
        assertThat(res.ledBarMode, is("off"));
        assertThat(res.model, is("I-9PSL"));
        assertThat(res.mqttBrokerUrl, is("https://192.168.1.1/mqtt"));
        assertThat(res.noxLearningOffset, is(12L));
        assertThat(res.pmStandard, is("ugm3"));
        assertThat(res.postDataToAirGradient, is(true));
        assertThat(res.temperatureUnit, is("c"));
        assertThat(res.tvocLearningOffset, is(12L));
    }
}
