/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal;

import static java.time.DayOfWeek.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.HttpURLConnection;
import java.time.DayOfWeek;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.myenergi.internal.dto.DaysOfWeekMap;
import org.openhab.binding.myenergi.internal.dto.EddiSummary;
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimeSlot;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimes;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.exception.AuthenticationException;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;
import org.openhab.binding.myenergi.internal.util.ZappiChargingMode;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * The {@link MyenergiApiClientTest} is a test class for {@link MyEnergiApiClient}.
 *
 * @author Rene Scherer - Initial contribution
 * @author Stephen Cook - Eddi Support
 */
@NonNullByDefault
class MyenergiApiClientTest {

    public static class HttpClientFactoryMock implements HttpClientFactory {

        private SslContextFactory sslContextFactory = new SslContextFactory.Client();

        @Override
        public HttpClient createHttpClient(String consumerName) {
            return new HttpClient(sslContextFactory);
        }

        @Override
        public HttpClient getCommonHttpClient() {
            return new HttpClient(sslContextFactory);
        }

        @Override
        public HttpClient createHttpClient(String consumerName, @Nullable SslContextFactory sslContextFactory) {
            return createHttpClient(consumerName);
        }

        @Override
        public HTTP2Client createHttp2Client(String consumerName) {
            return new HTTP2Client();
        }

        @Override
        public HTTP2Client createHttp2Client(String consumerName, @Nullable SslContextFactory sslContextFactory) {
            return new HTTP2Client();
        }
    }

    private static final String TEST_USERNAME = "12345678";
    private static final String TEST_PASSWORD_VALID = "validPassword";
    private static final String TEST_PASSWORD_INVALID = "invalidPassword";

    private static final int RESPONSE_200_STATUS = HttpURLConnection.HTTP_OK;
    private static final String RESPONSE_200_REASON = "OK";
    private static final int RESPONSE_401_STATUS = HttpURLConnection.HTTP_UNAUTHORIZED;
    private static final String RESPONSE_401_REASON = "NOT AUTHORIZED";
    private static final String RESPONSE_401_CONTENT = "";

    private static final int HARVI_SERIAL_NUMBER = 87263212;
    private static final int ZAPPI_SERIAL_NUMBER = 21287642;
    private static final int EDDI_SERIAL_NUMBER = 21364287;

    private static MyenergiApiClient api = new MyenergiApiClient();
    private static HttpFields responseFields = mock(HttpFields.class);

    private static AuthenticationStore authenticationStore = mock(AuthenticationStore.class);
    private static HttpClientFactory httpClientFactory = mock(HttpClientFactoryMock.class);
    private static HttpClient httpClient = mock(HttpClient.class);
    private static HttpRequest request = mock(HttpRequest.class);
    private static HttpContentResponse response = mock(HttpContentResponse.class);

    @BeforeAll
    static void setUp() throws Exception {
        when(httpClientFactory.createHttpClient(anyString())).thenReturn(httpClient);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        when(httpClient.getAuthenticationStore()).thenReturn(authenticationStore);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(httpClient.isStarted()).thenReturn(true);

        when(request.method(HttpMethod.GET)).thenReturn(request);

        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(RESPONSE_200_STATUS);
        when(response.getReason()).thenReturn(RESPONSE_200_REASON);
        when(responseFields.get(MyenergiGetHostFromDirector.MY_ENERGI_RESPONSE_FIELD)).thenReturn("SomeHost");
        when(response.getHeaders()).thenReturn(responseFields);
        api.setHttpClientFactory(httpClientFactory);
        api.initialize(TEST_USERNAME, TEST_PASSWORD_VALID);
    }

    @Test
    void testUpdateTopologyCacheInvalidPassword() throws Exception {
        when(response.getStatus()).thenReturn(RESPONSE_401_STATUS);
        when(response.getReason()).thenReturn(RESPONSE_401_REASON);
        when(response.getContentAsString()).thenReturn(RESPONSE_401_CONTENT);
        api.initialize(TEST_USERNAME, TEST_PASSWORD_INVALID);

        // then:
        // we want an authentication exception
        assertThrows(AuthenticationException.class, () -> {
            api.updateTopologyCache();
        });

        when(response.getStatus()).thenReturn(RESPONSE_200_STATUS);
        when(response.getReason()).thenReturn(RESPONSE_200_REASON);
    }

    @Test
    void testUpdateTopologyCache() {
        when(response.getContentAsString()).thenReturn(
                "[{\"eddi\":[{\"sno\":21364287,\"dat\":\"03-03-2021\",\"tim\":\"16:06:12\",\"ectp1\":502,\"ectt1\":\"Internal Load\",\"ectt2\":\"None\",\"ectt3\":\"None\"}\n"
                        + "]},{\"zappi\":[{\"dat\":\"03-03-2021\",\"tim\":\"20:00:57\",\"ectp2\":836,\"ectp3\":4,\"ectt1\":\"Internal Load\",\"ectt2\":\"Grid\",\"ectt3\":\"None\",\"frq\":50.8,\"grd\":819,\"pha\":1,\"sno\":21287642,\"sta\":1,\"vol\":2319,\"pri\":1,\"cmt\":254,\"zmo\":3,\"tbk\":5,\"che\":7.35,\"pst\":\"A\",\"mgl\":100,\"sbh\":17,\"sbk\":5,\"ectp4\":-224,\"ectt4\":\"None\",\"ectt5\":\"None\",\"ectt6\":\"None\",\"fwv\":\"3560S3.054\",\"dst\":1,\"lck\":16}\n"
                        + "]},{\"harvi\":[{\"sno\":87263212,\"dat\":\"03-03-2021\",\"tim\":\"16:06:12\",\"ectp1\":1,\"ectt1\":\"Generation\",\"ectt2\":\"None\",\"ectt3\":\"None\",\"ect1p\":1,\"ect2p\":1,\"ect3p\":1,\"fwv\":\"\"}\n"
                        + "]},{\"asn\":\"s0.myenergi.net\",\"fwv\":\"3401S3021\"}]");
        // when:
        try {
            api.updateTopologyCache();
            // then:
            assertEquals(1, api.getData().getEddis().size());
            assertEquals(1, api.getData().getZappis().size());
            assertEquals(1, api.getData().getHarvis().size());
            assertEquals("s0.myenergi.net", api.getData().getActiveServer());
            assertEquals("3401S3021", api.getData().getFirmwareVersion());
            assertEquals(EDDI_SERIAL_NUMBER, api.getData().getEddis().get(0).serialNumber);
            assertEquals(ZAPPI_SERIAL_NUMBER, api.getData().getZappis().get(0).serialNumber);
            assertEquals(HARVI_SERIAL_NUMBER, api.getData().getHarvis().get(0).serialNumber);
        } catch (ApiException e) {
            fail(e);
        }
    }

    @Test
    void testUpdateZappiSummary() {
        when(response.getContentAsString()).thenReturn(
                "{\"zappi\":[{\"dat\":\"03-03-2021\",\"tim\":\"21:14:13\",\"ectp2\":723,\"ectp3\":3,\"ectt1\":\"Internal Load\",\"ectt2\":\"Grid\",\"ectt3\":\"None\",\"frq\":50.4,\"grd\":711,\"pha\":1,\"sno\":21287642,\"sta\":1,\"vol\":2345,\"pri\":1,\"cmt\":254,\"zmo\":3,\"tbk\":5,\"che\":7.35,\"pst\":\"A\",\"mgl\":100,\"sbh\":17,\"sbk\":5,\"ectp4\":3,\"ectt4\":\"None\",\"ectt5\":\"None\",\"ectt6\":\"None\",\"fwv\":\"3560S3.054\",\"dst\":1,\"lck\":16}]}");
        // when:
        try {
            ZappiSummary sum = api.updateZappiSummary(ZAPPI_SERIAL_NUMBER);
            // then:
            assertEquals(ZAPPI_SERIAL_NUMBER, sum.serialNumber);
        } catch (ApiException | RecordNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void testUpdateZappiSummaryInvalidSerialNumber() {
        when(response.getContentAsString())
                .thenReturn("{\"status\":-19,\"statustext\":\"\",\"asn\":\"s0.myenergi.net\",\"fwv\":\"3401S3021\"}");
        // when:
        assertThrows(RecordNotFoundException.class, () -> {
            api.updateZappiSummary(ZAPPI_SERIAL_NUMBER + 1);
        });
    }

    @Test
    void testUpdateHarviSummary() {
        when(response.getContentAsString()).thenReturn(
                "{\"harvi\":[{\"sno\":87263212,\"dat\":\"03-03-2021\",\"tim\":\"16:06:12\",\"ectp1\":1,\"ectt1\":\"Generation\",\"ectt2\":\"None\",\"ectt3\":\"None\",\"ect1p\":1,\"ect2p\":1,\"ect3p\":1,\"fwv\":\"\"}]}");
        // when:
        try {
            HarviSummary sum = api.updateHarviSummary(HARVI_SERIAL_NUMBER);
            // then:
            assertEquals(HARVI_SERIAL_NUMBER, sum.serialNumber);
        } catch (ApiException | RecordNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void testUpdateHarviSummaryInvalidSerialNumber() {
        when(response.getContentAsString())
                .thenReturn("{\"status\":-19,\"statustext\":\"\",\"asn\":\"s0.myenergi.net\",\"fwv\":\"3401S3021\"}");
        // when:
        assertThrows(RecordNotFoundException.class, () -> {
            api.updateHarviSummary(HARVI_SERIAL_NUMBER + 1);
        });
    }

    @Test
    void testUpdateEddiSummary() {
        when(response.getContentAsString()).thenReturn(
                "{\"eddi\":[{\"sno\":21364287,\"dat\":\"13-11-2022\",\"tim\":\"16:50:57\",\"ectp1\":502,\"ectt1\":\"Internal Load\",\"ectt2\":\"None\",\"ectt3\":\"None\",\"bsm\":1,\"bst\":0,\"cmt\":254,\"dst\":1,\"div\":502,\"frq\":49.92,\"fwv\":\"3202S4.097\",\"grd\":3302,\"pha\":1,\"pri\":1,\"sta\":4,\"tz\":0,\"vol\":2341,\"che\":3.05,\"hpri\":1,\"hno\":1,\"ht1\":\"Tank 1\",\"ht2\":\"Tank 2\",\"r1a\":0,\"r2a\":0,\"r1b\":0,\"r2b\":0,\"rbc\":1,\"rbt\":1188,\"tp1\":56,\"tp2\":127}]}");
        // when:
        try {
            EddiSummary sum = api.updateEddiSummary(EDDI_SERIAL_NUMBER);
            // then:
            assertEquals(EDDI_SERIAL_NUMBER, sum.serialNumber);
        } catch (ApiException | RecordNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void testUpdateEddiSummaryInvalidSerialNumber() {
        when(response.getContentAsString())
                .thenReturn("{\"status\":-19,\"statustext\":\"\",\"asn\":\"s0.myenergi.net\",\"fwv\":\"3401S3021\"}");
        // when:
        assertThrows(RecordNotFoundException.class, () -> {
            api.updateEddiSummary(EDDI_SERIAL_NUMBER + 1);
        });
    }

    @Test
    public void testGetZappiBoostTimes() {
        when(response.getContentAsString()).thenReturn(
                "{\"boost_times\":[{\"slt\":11,\"bsh\":2,\"bsm\":0,\"bdh\":8,\"bdm\":0,\"bdd\":\"00101010\"},{\"slt\":12,\"bsh\":0,\"bsm\":0,\"bdh\":0,\"bdm\":0,\"bdd\":\"00000000\"},{\"slt\":13,\"bsh\":0,\"bsm\":0,\"bdh\":0,\"bdm\":0,\"bdd\":\"00000000\"},{\"slt\":14,\"bsh\":0,\"bsm\":0,\"bdh\":0,\"bdm\":0,\"bdd\":\"00000000\"}]}");
        // when:
        try {
            ZappiBoostTimes bt = api.getZappiBoostTimes(ZAPPI_SERIAL_NUMBER);
            // then:
            assertEquals(4, bt.boostTimes.size());
            ZappiBoostTimeSlot slot = bt.boostTimes.get(0);
            assertEquals(2, slot.startHour);
            assertEquals(0, slot.startMinute);
            assertEquals(8, slot.durationHour);
            assertEquals(0, slot.durationMinute);
            assertEquals("00101010", slot.daysOfTheWeekMap);
        } catch (ApiException e) {
            fail(e);
        }
    }

    @Test
    public void testSetZappiBoostTimes() {
        when(response.getContentAsString()).thenReturn(
                "{\"boost_times\":[{\"slt\":11,\"bsh\":2,\"bsm\":0,\"bdh\":6,\"bdm\":45,\"bdd\":\"00101010\"},{\"slt\":12,\"bsh\":0,\"bsm\":0,\"bdh\":0,\"bdm\":0,\"bdd\":\"00000000\"},{\"slt\":13,\"bsh\":0,\"bsm\":0,\"bdh\":0,\"bdm\":0,\"bdd\":\"00000000\"},{\"slt\":14,\"bsh\":0,\"bsm\":0,\"bdh\":0,\"bdm\":0,\"bdd\":\"00000000\"}]}");
        // when:
        try {
            DayOfWeek[] days = { TUESDAY, THURSDAY, SATURDAY };
            ZappiBoostTimeSlot slot1 = new ZappiBoostTimeSlot(11, 2, 0, 6, 45, new DaysOfWeekMap(days));
            ZappiBoostTimes bt = api.setZappiBoostTimes(ZAPPI_SERIAL_NUMBER, slot1);
            // then:
            assertEquals(4, bt.boostTimes.size());
            ZappiBoostTimeSlot slot = bt.boostTimes.get(0);
            assertEquals(2, slot.startHour);
            assertEquals(0, slot.startMinute);
            assertEquals(6, slot.durationHour);
            assertEquals(45, slot.durationMinute);
            assertEquals("00101010", slot.daysOfTheWeekMap);
        } catch (ApiException e) {
            fail(e);
        }
    }

    // @Test
    public void testSetZappiChargingMode() {
        try {
            if (api.getData().getZappiBySerialNumber(ZAPPI_SERIAL_NUMBER).chargingMode == ZappiChargingMode.ECO_PLUS
                    .getIntValue()) {
                api.setZappiChargingMode(ZAPPI_SERIAL_NUMBER, ZappiChargingMode.FAST);
                api.setZappiChargingMode(ZAPPI_SERIAL_NUMBER, ZappiChargingMode.ECO_PLUS);
            } else {
                api.setZappiChargingMode(ZAPPI_SERIAL_NUMBER, ZappiChargingMode.ECO_PLUS);
            }
        } catch (RecordNotFoundException | ApiException e) {
            fail(e);
        }
    }
}
