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
package org.openhab.binding.dreame.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameMapData;
import org.openhab.binding.dreame.internal.model.DreameMowingStatistics;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;

import com.google.gson.JsonParser;

/**
 * Tests Dreamehome request encoding and device response parsing.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameApiClientTest {

    @Test
    void expiredAccessTokenIsRefreshedBeforeAuthenticatedRequest() throws Exception {
        HttpClient httpClient = Objects.requireNonNull(mock(HttpClient.class));
        Request request = Objects.requireNonNull(mock(Request.class, RETURNS_SELF));
        ContentResponse passwordLogin = response("""
                {"access_token":"old-token","refresh_token":"refresh-old","expires_in":1,"region":"eu"}
                """);
        ContentResponse refreshLogin = response("""
                {"access_token":"new-token","refresh_token":"refresh-new","expires_in":7200,"region":"eu"}
                """);
        ContentResponse devices = response("""
                {"code":0,"success":true,"data":{"page":{"records":[]}}}
                """);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(passwordLogin, refreshLogin, devices);
        DreameApiClient client = new DreameApiClient(httpClient,
                Clock.fixed(Instant.parse("2026-08-07T18:00:00Z"), ZoneOffset.UTC));

        client.login("user@example.com", "secret", "eu");
        assertEquals(List.of(), client.getDevices());

        verify(request, times(3)).send();
        verify(request).header("Dreame-Auth", "new-token");
    }

    private static ContentResponse response(String json) {
        ContentResponse response = Objects.requireNonNull(mock(ContentResponse.class));
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(json);
        return response;
    }

    @Test
    void passwordLoginBodyMatchesReferenceProtocol() throws DreameCloudException {
        assertEquals(
                "platform=IOS&scope=all&grant_type=password&username=user@example.com&password=ab51518dc498dcac64b000f288be8ea6&type=account",
                DreameAuthenticationService.createPasswordRequestBody("user@example.com", "secret", ""));
        assertEquals(
                "platform=IOS&scope=all&grant_type=password&username=user@example.com&password=ab51518dc498dcac64b000f288be8ea6&type=account&country=DE&lang=de",
                DreameAuthenticationService.createPasswordRequestBody("user@example.com", "secret", "de"));
    }

    @Test
    void mapsEuropeanCountriesToEuCloud() throws DreameCloudException {
        assertEquals("eu", DreameAuthenticationService.cloudRegion("de"));
        assertEquals("eu", DreameAuthenticationService.cloudRegion("EU"));
        assertEquals("us", DreameAuthenticationService.cloudRegion("us"));
    }

    @Test
    void deviceParserKeepsOnlyMowers() throws DreameCloudException {
        String json = """
                {"code":0,"data":{"page":{"records":[
                  {"did":"123","customName":"","model":"dreame.mower.g2422","masterUid":"42","bindDomain":"host:8883","property":"{}",
                   "deviceInfo":{"displayName":"A1 Pro 2000"}},
                  {"did":"456","name":"Vacuum","model":"dreame.vacuum.r2228o"}
                ]}}}
                """;

        List<DreameDevice> devices = new DreameApiResponseParser()
                .parseDevices(JsonParser.parseString(json).getAsJsonObject());

        assertEquals(List.of(new DreameDevice("123", "A1 Pro 2000", "dreame.mower.g2422", "", "42", "host:8883", "{}")),
                devices);
    }

    @Test
    void propertyParserMapsSuccessfulValues() {
        String json = """
                [
                  {"did":"0","siid":2,"piid":1,"code":0,"value":6},
                  {"did":"2","siid":3,"piid":1,"code":0,"value":87},
                  {"did":"70","siid":5,"piid":1,"code":0,"value":true},
                  {"did":"1","siid":2,"piid":2,"code":-1}
                ]
                """;

        DreameStatus status = new DreameApiResponseParser().parseProperties(
                JsonParser.parseString(json).getAsJsonArray(),
                List.of(DreameProperty.STATE, DreameProperty.BATTERY_LEVEL, DreameProperty.DND, DreameProperty.ERROR));

        assertEquals(6, status.integer(DreameProperty.STATE, -1));
        assertEquals(87, status.integer(DreameProperty.BATTERY_LEVEL, -1));
        assertEquals(true, status.bool(DreameProperty.DND, false));
        assertEquals(-1, status.integer(DreameProperty.ERROR, -1));
        assertTrue(status.contains(DreameProperty.STATE));
        assertTrue(status.contains(DreameProperty.BATTERY_LEVEL));
        assertFalse(status.contains(DreameProperty.ERROR));
        assertEquals(3, status.properties().size());
    }

    @Test
    void historyParserAggregatesMissionCompletionEvents() throws DreameCloudException {
        DreameMowingStatistics statistics = new DreameApiResponseParser()
                .parseMowingStatistics(JsonParser.parseString("""
                        {"code":0,"data":{"list":[
                          {"history":"[{\\\"piid\\\":2,\\\"value\\\":47},{\\\"piid\\\":3,\\\"value\\\":1234}]"},
                          {"value":[{"piid":2,"value":13},{"piid":3,"value":566}]}
                        ]}}
                        """).getAsJsonObject());

        assertEquals(2, statistics.sessions());
        assertEquals(60, statistics.totalMinutes());
        assertEquals(new java.math.BigDecimal("18.00"), statistics.totalArea());
    }

    @Test
    void historyParserIgnoresEmptyHistory() throws DreameCloudException {
        DreameMowingStatistics statistics = new DreameApiResponseParser()
                .parseMowingStatistics(JsonParser.parseString("""
                        {"code":0,"data":{"list":[]}}
                        """).getAsJsonObject());

        assertEquals(0, statistics.sessions());
        assertEquals(0, statistics.totalMinutes());
        assertEquals(new java.math.BigDecimal("0.00"), statistics.totalArea());
    }

    @Test
    void commandPathUsesMqttRegionPrefix() {
        DreameDevice device = new DreameDevice("123", "Garden", "dreame.mower.g2422", "1.0", "42",
                "eu.iot.dreame.tech:8883", "{}");

        assertEquals("/dreame-iot-com-eu/device/sendCommand", DreameApiClient.commandPath(device));
    }

    @Test
    void mapParserReassemblesChunksAndExtractsDescriptors() throws DreameCloudException {
        String map = "{\"mapIndex\":0,\"name\":\"Garden\",\"totalArea\":152.5,"
                + "\"boundary\":{\"x1\":0,\"y1\":0,\"x2\":100,\"y2\":200},"
                + "\"mowingAreas\":{\"dataType\":\"Map\",\"value\":[[1,{\"name\":\"Front\",\"area\":42.25,"
                + "\"path\":[{\"x\":0,\"y\":0},{\"x\":100,\"y\":0},{\"x\":100,\"y\":200}]}]]}}";
        String encoded = new com.google.gson.Gson().toJson(List.of(map));
        int split = encoded.length() / 2;
        com.google.gson.JsonObject batch = new com.google.gson.JsonObject();
        batch.addProperty("MAP.1", encoded.substring(split));
        batch.addProperty("MAP.info", encoded.length());
        batch.addProperty("MAP.0", encoded.substring(0, split));

        DreameMapData data = new DreameApiResponseParser().parseMapData(batch, JsonParser.parseString("""
                {"code":0,"out":[{"r":0,"d":[[0,1],[1,0]]}]}
                """));

        assertEquals(1, data.currentMapId());
        assertEquals("Garden", data.maps().get(0).name());
        assertEquals(new java.math.BigDecimal("152.5"), data.maps().get(0).area());
        assertEquals("Front", data.zones().get(0).name());
        assertEquals(new java.math.BigDecimal("42.25"), data.zones().get(0).area());
        assertEquals(3, data.geometries().get(0).zones().get(0).points().size());
    }

    @Test
    void parsesCuttingHeightFromMowingPreferenceRecord() throws DreameCloudException {
        assertEquals(new java.math.BigDecimal("5.0"), DreameApiClient.parseCuttingHeight(JsonParser.parseString("""
                {"aiid":50,"code":0,"out":[{"m":"r","r":0,"d":[7,0,0,1,50,2,3]}],"siid":2}
                """)));
    }

    @Test
    void updatesOnlyCuttingHeightAddressAndVersionSlots() throws DreameCloudException {
        com.google.gson.JsonArray source = JsonParser.parseString("[9,4,7,1,60,2,3,4,5]").getAsJsonArray();
        com.google.gson.JsonArray updated = DreameApiClient.updatedCuttingHeightRecord(source, 2,
                new java.math.BigDecimal("5.5"));

        assertEquals("[0,2,0,1,55,2,3,4,5]", updated.toString());
        assertEquals("[9,4,7,1,60,2,3,4,5]", source.toString());
    }
}
