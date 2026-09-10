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
package org.openhab.binding.eyeonwater.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient.EyeOnWaterMeterData;

/**
 * Tests for {@link EyeOnWaterClient}.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
class EyeOnWaterClientTest {

    private final EyeOnWaterClient client = new EyeOnWaterClient("eyeonwater.com", "testuser", "testpassword",
            Mockito.mock(HttpClient.class));

    private Request mockRequest() {
        Request request = mock(Request.class);
        when(request.method((HttpMethod) any())).thenReturn(request);
        when(request.timeout(anyLong(), any())).thenReturn(request);
        when(request.header((HttpHeader) any(), anyString())).thenReturn(request);
        when(request.header((String) any(), anyString())).thenReturn(request);
        when(request.followRedirects(anyBoolean())).thenReturn(request);
        when(request.content(any())).thenReturn(request);
        return request;
    }

    private ContentResponse mockResponse(int status, String body) {
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(status);
        when(response.getContentAsString()).thenReturn(body);
        return response;
    }

    @Test
    void testDiscoverMetersNewSearchSuccess() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        EyeOnWaterClient testClient = new EyeOnWaterClient("eyeonwater.com", "testuser", "testpassword", httpClient);

        // Mock Sign-in response
        Request signinRequest = mockRequest();
        ContentResponse signinResponse = mockResponse(HttpStatus.FOUND_302, "");
        when(signinRequest.send()).thenReturn(signinResponse);
        when(httpClient.newRequest(contains("signin"))).thenReturn(signinRequest);

        // Mock Discovery Search response
        Request searchRequest = mockRequest();
        String jsonPayload = "{" + "  \"elastic_results\": {" + "    \"hits\": {" + "      \"hits\": [" + "        {"
                + "          \"_source\": {" + "            \"meter\": {"
                + "              \"meter_uuid\": \"uuid-123\"," + "              \"meter_id\": \"id-123\""
                + "            }" + "          }" + "        }" + "      ]" + "    }" + "  }" + "}";
        ContentResponse searchResponse = mockResponse(HttpStatus.OK_200, jsonPayload);
        when(searchRequest.send()).thenReturn(searchResponse);
        when(httpClient.newRequest(contains("new_search"))).thenReturn(searchRequest);

        List<EyeOnWaterMeterData> meters = testClient.discoverMeters(true);
        assertEquals(1, meters.size());
        assertEquals("uuid-123", meters.get(0).getMeterUuid());
        assertEquals("id-123", meters.get(0).getMeterId());
    }

    @Test
    void testPollMeterSuccess() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        EyeOnWaterClient testClient = new EyeOnWaterClient("eyeonwater.com", "testuser", "testpassword", httpClient);

        // Mock Sign-in response
        Request signinRequest = mockRequest();
        ContentResponse signinResponse = mockResponse(HttpStatus.FOUND_302, "");
        when(signinRequest.send()).thenReturn(signinResponse);
        when(httpClient.newRequest(contains("signin"))).thenReturn(signinRequest);

        // Mock Poll response
        Request pollRequest = mockRequest();
        String jsonPayload = "{" + "  \"elastic_results\": {" + "    \"hits\": {" + "      \"hits\": [" + "        {"
                + "          \"_source\": {" + "            \"register_0\": {" + "              \"latest_read\": {"
                + "                \"full_read\": 125.5," + "                \"units\": \"GAL\","
                + "                \"read_time\": \"2026-09-10T12:00:00Z\"" + "              },"
                + "              \"flags\": {" + "                \"Leak\": true,"
                + "                \"LowBattery\": false," + "                \"ReverseFlow\": true"
                + "              }," + "              \"leak\": {" + "                \"rate\": 0.25"
                + "              }" + "            }" + "          }" + "        }" + "      ]" + "    }" + "  }" + "}";
        ContentResponse pollResponse = mockResponse(HttpStatus.OK_200, jsonPayload);
        when(pollRequest.send()).thenReturn(pollResponse);
        when(httpClient.newRequest(contains("new_search"))).thenReturn(pollRequest);

        EyeOnWaterMeterData data = testClient.pollMeter("uuid-123", "id-123");
        assertEquals("uuid-123", data.getMeterUuid());
        assertEquals("id-123", data.getMeterId());
        assertEquals(125.5, data.getReadingValue());
        assertEquals("GAL", data.getReadingUnit());
        assertEquals("2026-09-10T12:00:00Z", data.getReadTime());
        assertTrue(data.isLeakAlert());
        assertFalse(data.isLowBatteryAlert());
        assertTrue(data.isReverseFlowAlert());
        assertEquals(0.25, data.getLeakRate());
    }

    @Test
    void testPollMeterMalformedThrows() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        EyeOnWaterClient testClient = new EyeOnWaterClient("eyeonwater.com", "testuser", "testpassword", httpClient);

        // Mock Sign-in response
        Request signinRequest = mockRequest();
        ContentResponse signinResponse = mockResponse(HttpStatus.FOUND_302, "");
        when(signinRequest.send()).thenReturn(signinResponse);
        when(httpClient.newRequest(contains("signin"))).thenReturn(signinRequest);

        // Mock Poll malformed response (missing register_0)
        Request pollRequest = mockRequest();
        String jsonPayload = "{" + "  \"elastic_results\": {" + "    \"hits\": {" + "      \"hits\": [" + "        {"
                + "          \"_source\": {}" + "        }" + "      ]" + "    }" + "  }" + "}";
        ContentResponse pollResponse = mockResponse(HttpStatus.OK_200, jsonPayload);
        when(pollRequest.send()).thenReturn(pollResponse);
        when(httpClient.newRequest(contains("new_search"))).thenReturn(pollRequest);

        assertThrows(IOException.class, () -> testClient.pollMeter("uuid-123", "id-123"));
    }

    @Test
    void testPollMeterWith401UnauthorizedRetry() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        EyeOnWaterClient testClient = new EyeOnWaterClient("eyeonwater.com", "testuser", "testpassword", httpClient);

        // Mock Sign-in response (used first and upon retry)
        Request signinRequest = mockRequest();
        ContentResponse signinResponse = mockResponse(HttpStatus.FOUND_302, "");
        when(signinRequest.send()).thenReturn(signinResponse);
        when(httpClient.newRequest(contains("signin"))).thenReturn(signinRequest);

        // Mock Poll request that returns 401 Unauthorized first, then 200 OK upon retry
        Request pollRequest = mockRequest();
        ContentResponse pollResponse401 = mockResponse(HttpStatus.UNAUTHORIZED_401, "Session Expired");
        String jsonPayload = "{" + "  \"elastic_results\": {" + "    \"hits\": {" + "      \"hits\": [" + "        {"
                + "          \"_source\": {" + "            \"register_0\": {" + "              \"latest_read\": {"
                + "                \"full_read\": 100.0," + "                \"units\": \"GAL\","
                + "                \"read_time\": \"2026-09-10T12:00:00Z\"" + "              }" + "            }"
                + "          }" + "        }" + "      ]" + "    }" + "  }" + "}";
        ContentResponse pollResponse200 = mockResponse(HttpStatus.OK_200, jsonPayload);

        // Return 401 on first call, 200 on second
        when(pollRequest.send()).thenReturn(pollResponse401).thenReturn(pollResponse200);
        when(httpClient.newRequest(contains("new_search"))).thenReturn(pollRequest);

        EyeOnWaterMeterData data = testClient.pollMeter("uuid-123", "id-123");
        assertEquals(100.0, data.getReadingValue());

        // Verify that sign-in was executed twice (once initially in poll, once upon 401 reauth)
        verify(signinRequest, times(2)).send();
    }

    @Test
    void testParseMetersFromDashboardStandard() {
        String html = "<html><head><script>\n" + "AQ.Views.MeterPicker.meters = [\n" + "  {\n"
                + "    \"meter_uuid\": \"12345678-abcd-1234-abcd-123456789012\",\n"
                + "    \"meter_id\": \"METER-123\"\n" + "  }\n" + "];\n" + "</script></head><body></body></html>";

        List<EyeOnWaterMeterData> meters = client.parseMetersFromDashboard(html);
        assertEquals(1, meters.size());
        assertEquals("12345678-abcd-1234-abcd-123456789012", meters.get(0).getMeterUuid());
        assertEquals("METER-123", meters.get(0).getMeterId());
    }

    @Test
    void testParseMetersFromDashboardMinified() {
        String html = "<html><body><script>AQ.Views.MeterPicker.meters=[{\"meter_uuid\":\"uuid-999\",\"meter_id\":\"id-999\"}];var x=10;</script></body></html>";

        List<EyeOnWaterMeterData> meters = client.parseMetersFromDashboard(html);
        assertEquals(1, meters.size());
        assertEquals("uuid-999", meters.get(0).getMeterUuid());
        assertEquals("id-999", meters.get(0).getMeterId());
    }

    @Test
    void testParseMetersFromDashboardNoMatch() {
        String html = "<html><body>No meters here!</body></html>";

        List<EyeOnWaterMeterData> meters = client.parseMetersFromDashboard(html);
        assertTrue(meters.isEmpty());
    }

    @Test
    void testParseMetersFromDashboardIncompleteData() {
        String html = "<html><body><script>AQ.Views.MeterPicker.meters=[{\"meter_id\":\"missing-uuid\"}];</script></body></html>";

        List<EyeOnWaterMeterData> meters = client.parseMetersFromDashboard(html);
        assertTrue(meters.isEmpty());
    }
}
