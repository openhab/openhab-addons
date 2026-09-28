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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameVacuumAction;
import org.openhab.binding.dreame.internal.model.DreameVacuumProperties;
import org.openhab.binding.dreame.internal.model.DreameVacuumSetting;

import com.google.gson.JsonParser;

/**
 * Verifies vacuum command wire format, acknowledgements and cancellation without accessing a device.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumCommandTest {
    @Test
    void preservesGroupedL50SettingsWhenChangingCleaningMode() throws Exception {
        Fixture f = new Fixture();
        List<String> bodies = new ArrayList<>();
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response("{\"code\":0,\"data\":{\"result\":[{\"siid\":4,\"piid\":23,\"code\":0,\"value\":5120}]}}"),
                response("{\"code\":0,\"data\":{\"result\":[{\"siid\":4,\"piid\":23,\"code\":0}]}}")).when(f.request)
                .send();

        f.client.getVacuumProperties(device("dreame.vacuum.r9445d"), () -> true);
        f.client.setVacuumSetting(device("dreame.vacuum.r9445d"), DreameVacuumSetting.CLEANING_MODE, 0, () -> true);

        var property = JsonParser.parseString(bodies.getLast()).getAsJsonObject().getAsJsonObject("data")
                .getAsJsonArray("params").get(0).getAsJsonObject();
        assertEquals(5122, property.get("value").getAsInt());
    }

    @Test
    void sendsRoomCleaningWithCurrentSettings() throws Exception {
        Fixture f = new Fixture();
        List<String> bodies = new ArrayList<>();
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response("{\"code\":0,\"data\":{\"result\":{\"code\":0}}}")).when(f.request).send();

        f.client.cleanVacuumRooms(device("dreame.vacuum.r9445d"), List.of(3, 7), 2, 1, () -> true);

        var params = JsonParser.parseString(bodies.getFirst()).getAsJsonObject().getAsJsonObject("data")
                .getAsJsonObject("params");
        assertEquals(4, params.get("siid").getAsInt());
        assertEquals(1, params.get("aiid").getAsInt());
        var inputs = params.getAsJsonArray("in");
        assertEquals(18, inputs.get(0).getAsJsonObject().get("value").getAsInt());
        assertEquals("{\"selects\":[[3,1,2,1,1],[7,1,2,1,1]]}",
                inputs.get(1).getAsJsonObject().get("value").getAsString());
    }

    @Test
    void writesValidatedVacuumSettingAsProperty() throws Exception {
        Fixture f = new Fixture();
        List<String> bodies = new ArrayList<>();
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response("{\"code\":0,\"data\":{\"result\":[{\"did\":\"test\",\"siid\":4,\"piid\":4,\"code\":0}]}}"))
                .when(f.request).send();

        f.client.setVacuumSetting(device("dreame.vacuum.r9445d"), DreameVacuumSetting.SUCTION_LEVEL, 3, () -> true);

        var data = JsonParser.parseString(bodies.getFirst()).getAsJsonObject().getAsJsonObject("data");
        assertEquals("set_properties", data.get("method").getAsString());
        var property = data.getAsJsonArray("params").get(0).getAsJsonObject();
        assertEquals("test", property.get("did").getAsString());
        assertEquals(4, property.get("siid").getAsInt());
        assertEquals(4, property.get("piid").getAsInt());
        assertEquals(3, property.get("value").getAsInt());
    }

    @Test
    void writesDryingTimeAndAutoSwitchSettings() throws Exception {
        Fixture f = new Fixture();
        List<String> bodies = new ArrayList<>();
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response("{\"code\":0,\"data\":{\"result\":[{\"code\":0}]}}")).when(f.request).send();

        f.client.setVacuumSetting(device("dreame.vacuum.r9445d"), DreameVacuumSetting.DRYING_TIME, 3, () -> true);
        f.client.setVacuumSetting(device("dreame.vacuum.r9445d"), DreameVacuumSetting.CLEAN_GENIUS, 2, () -> true);
        f.client.setVacuumSetting(device("dreame.vacuum.r9445d"), DreameVacuumSetting.CLEANING_ROUTE, 4, () -> true);

        var drying = property(bodies.get(0));
        assertEquals(40, drying.get("piid").getAsInt());
        assertEquals(3, drying.get("value").getAsInt());
        var genius = property(bodies.get(1));
        assertEquals(50, genius.get("piid").getAsInt());
        assertEquals("{\"k\":\"SmartHost\",\"v\":2}", genius.get("value").getAsString());
        var route = property(bodies.get(2));
        assertEquals(50, route.get("piid").getAsInt());
        assertEquals("{\"k\":\"CleanRoute\",\"v\":4}", route.get("value").getAsString());
    }

    @Test
    void sendsVacuumActionIdsAndOptionalInputWithCloudDeviceId() throws Exception {
        Fixture f = new Fixture();
        List<String> bodies = new ArrayList<>();
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response("{\"code\":0,\"data\":{\"result\":{\"code\":0}}}")).when(f.request).send();
        int[][] expected = { { 2, 1 }, { 2, 2 }, { 3, 1 }, { 4, 2 }, { 7, 1 }, { 15, 1 }, { 4, 4 }, { 4, 4 }, { 4, 4 },
                { 4, 4 } };
        int previousId = -1;
        for (DreameVacuumAction action : DreameVacuumAction.values()) {
            f.client.callVacuumAction(device("dreame.vacuum.r9445d"), action, () -> true);
            var root = JsonParser.parseString(bodies.getLast()).getAsJsonObject();
            var data = root.getAsJsonObject("data");
            var params = data.getAsJsonObject("params");
            assertEquals("action", data.get("method").getAsString());
            assertEquals("test", params.get("did").getAsString());
            assertEquals(expected[action.ordinal()][0], params.get("siid").getAsInt());
            assertEquals(expected[action.ordinal()][1], params.get("aiid").getAsInt());
            var inputs = params.getAsJsonArray("in");
            if (action.inputValue() == null) {
                assertTrue(inputs.isEmpty());
            } else {
                assertEquals(1, inputs.size());
                assertEquals(10, inputs.get(0).getAsJsonObject().get("piid").getAsInt());
                assertEquals(action.inputValue(), inputs.get(0).getAsJsonObject().get("value").getAsString());
            }
            assertTrue(root.get("id").getAsInt() > previousId);
            previousId = root.get("id").getAsInt();
            assertEquals(root.get("id"), data.get("id"));
        }
        verify(f.request, times(DreameVacuumAction.values().length)).send();
    }

    @Test
    void rejectsNonzeroMissingAndMalformedDeviceResults() throws Exception {
        Fixture f = new Fixture();
        for (String result : List.of("{\"code\":-1}", "{}", "null", "[]", "{\"code\":\"0\"}", "{\"code\":0.5}",
                "{\"code\":4294967296}", "{\"code\":true}")) {
            doReturn(response("{\"code\":0,\"data\":{\"result\":" + result + "}}")).when(f.request).send();
            assertThrows(DreameCloudException.class, () -> f.client.callVacuumAction(device("dreame.vacuum.r9445d"),
                    DreameVacuumAction.START, () -> true));
        }
    }

    @Test
    void rejectsCloudFailureAndDoesNotRetryTimeout() throws Exception {
        Fixture f = new Fixture();
        doReturn(response("{\"code\":-1}")).when(f.request).send();
        assertThrows(DreameCloudException.class,
                () -> f.client.callVacuumAction(device("dreame.vacuum.r9445d"), DreameVacuumAction.START, () -> true));
        when(f.request.send()).thenThrow(new TimeoutException("private-details"));
        assertThrows(DreameCloudException.class,
                () -> f.client.callVacuumAction(device("dreame.vacuum.r9445d"), DreameVacuumAction.PAUSE, () -> true));
        verify(f.request, times(2)).send();
    }

    @Test
    void requestsCompleteMapAndReadsMapOutput() throws Exception {
        Fixture f = new Fixture();
        List<String> bodies = new ArrayList<>();
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response(
                "{\"code\":0,\"data\":{\"result\":{\"code\":0,\"out\":[{\"piid\":1,\"value\":\"map-data\"}]}}}"))
                .when(f.request).send();

        assertEquals("map-data", f.client.getVacuumMap(device("dreame.vacuum.r9445d"), () -> true));
        var data = JsonParser.parseString(bodies.getFirst()).getAsJsonObject().getAsJsonObject("data");
        var parameters = data.getAsJsonObject("params");
        assertEquals("action", data.get("method").getAsString());
        assertEquals(6, parameters.get("siid").getAsInt());
        assertEquals(1, parameters.get("aiid").getAsInt());
        var input = parameters.getAsJsonArray("in").get(0).getAsJsonObject();
        assertEquals(2, input.get("piid").getAsInt());
        var request = JsonParser.parseString(input.get("value").getAsString()).getAsJsonObject();
        assertEquals(1, request.get("req_type").getAsInt());
        assertEquals("I", request.get("frame_type").getAsString());
        assertEquals(1, request.get("force_type").getAsInt());
    }

    @Test
    void downloadsMapFromObjectNameOutput() throws Exception {
        Fixture f = new Fixture();
        List<String> urls = new ArrayList<>();
        List<String> bodies = new ArrayList<>();
        when(f.http.newRequest(anyString())).thenAnswer(invocation -> {
            urls.add(invocation.getArgument(0));
            return f.request;
        });
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response(
                "{\"code\":0,\"data\":{\"result\":{\"code\":0,\"out\":[{\"piid\":3,\"value\":\"ali_dreame/LN997047/2089995067/1\"}]}}}"),
                response(
                        "{\"code\":0,\"data\":\"https://dreame-eu.oss-eu-central-1.aliyuncs.com/iot/tmp/map?signature=test\"}"),
                response("base-map-data")).when(f.request).send();

        assertEquals("base-map-data", f.client.getVacuumMap(device("dreame.vacuum.r9445d"), () -> true));
        assertEquals(3, urls.size());
        assertTrue(urls.get(1).endsWith("/dreame-user-iot/iotfile/getDownloadUrl"));
        assertEquals("https://dreame-eu.oss-eu-central-1.aliyuncs.com/iot/tmp/map?signature=test", urls.get(2));
        var fileRequest = JsonParser.parseString(bodies.get(1)).getAsJsonObject();
        assertEquals("test", fileRequest.get("did").getAsString());
        assertEquals("dreame.vacuum.r9445d", fileRequest.get("model").getAsString());
        assertEquals("ali_dreame/LN997047/2089995067/1", fileRequest.get("filename").getAsString());
        assertEquals("eu", fileRequest.get("region").getAsString());
    }

    @Test
    void downloadsMapFromLegacyObjectNameOutput() throws Exception {
        Fixture f = new Fixture();
        doReturn(response(
                "{\"code\":0,\"data\":{\"result\":{\"code\":0,\"out\":[{\"piid\":13,\"value\":\"1,ali_dreame/LN997047/2089995067/0\"}]}}}"),
                response("{\"code\":0,\"data\":\"https://dreame-eu.oss-eu-central-1.aliyuncs.com/map\"}"),
                response("legacy-base-map")).when(f.request).send();

        assertEquals("legacy-base-map", f.client.getVacuumMap(device("dreame.vacuum.r9445d"), () -> true));
    }

    @Test
    void downloadsMapFromDreameIotHost() throws Exception {
        Fixture f = new Fixture();
        doReturn(response(
                "{\"code\":0,\"data\":{\"result\":{\"code\":0,\"out\":[{\"piid\":3,\"value\":\"ali_dreame/LN997047/2089995067/1\"}]}}}"),
                response("{\"code\":0,\"data\":\"https://dreame-eu.iot.dreame.tech/iot/tmp/map\"}"),
                response("dreame-map")).when(f.request).send();

        assertEquals("dreame-map", f.client.getVacuumMap(device("dreame.vacuum.r9445d"), () -> true));
    }

    @Test
    void rejectsUntrustedMapDownloadUrl() throws Exception {
        Fixture f = new Fixture();
        doReturn(response(
                "{\"code\":0,\"data\":{\"result\":{\"code\":0,\"out\":[{\"piid\":3,\"value\":\"ali_dreame/LN997047/2089995067/1\"}]}}}"),
                response("{\"code\":0,\"data\":\"https://example.org/map\"}")).when(f.request).send();

        assertThrows(DreameCloudException.class,
                () -> f.client.getVacuumMap(device("dreame.vacuum.r9445d"), () -> true));
        verify(f.request, times(2)).send();
    }

    @Test
    void rejectsMalformedCancelledAndUnsupportedMapResponses() throws Exception {
        Fixture f = new Fixture();
        for (String result : List.of("null", "[]", "{}", "{\"code\":0.5,\"out\":[]}",
                "{\"code\":0,\"out\":[{\"piid\":1.5,\"value\":\"map\"}]}", "{\"code\":-1,\"out\":[]}")) {
            doReturn(response("{\"code\":0,\"data\":{\"result\":" + result + "}}")).when(f.request).send();
            if (result.contains("piid")) {
                assertNull(f.client.getVacuumMap(device("dreame.vacuum.r9445d"), () -> true));
            } else {
                assertThrows(DreameCloudException.class,
                        () -> f.client.getVacuumMap(device("dreame.vacuum.r9445d"), () -> true));
            }
        }
        clearInvocations(f.request);
        assertThrows(DreameCloudException.class,
                () -> f.client.getVacuumMap(device("dreame.vacuum.r9445d"), () -> false));
        assertThrows(DreameCloudException.class,
                () -> f.client.getVacuumMap(device("dreame.vacuum.other"), () -> true));
        verify(f.request, never()).send();
    }

    @Test
    void cancelledOrUnsupportedCommandsNeverSend() throws Exception {
        Fixture f = new Fixture();
        assertThrows(DreameCloudException.class,
                () -> f.client.callVacuumAction(device("dreame.vacuum.r9445d"), DreameVacuumAction.START, () -> false));
        for (String model : List.of("dreame.vacuum.other", "dreame.mower.g2540d", "mova.mower.g2584d")) {
            assertThrows(DreameCloudException.class,
                    () -> f.client.callVacuumAction(device(model), DreameVacuumAction.START, () -> true));
        }
        verify(f.request, never()).send();
    }

    @Test
    void readsOnlySuccessfulNumericSnapshotValuesAndSendsSupportedAddresses() throws Exception {
        Fixture f = new Fixture();
        List<String> bodies = new ArrayList<>();
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response("""
                {"code":0,"data":{"result":[
                {"siid":3,"piid":1,"code":0,"value":99},
                {"siid":2,"piid":2,"code":0,"value":0},
                {"siid":2,"piid":1,"code":-1,"value":6},
                {"siid":3,"piid":2,"value":1},
                {"siid":4,"piid":1,"code":0,"value":"6"},
                {"siid":4,"piid":7,"code":0,"value":1},
                {"siid":9,"piid":2,"code":0,"value":87},
                {"siid":12,"piid":3,"code":0,"value":42}]}}
                """)).when(f.request).send();
        assertEquals(new DreameVacuumProperties(Map.of("3/1", 99, "2/2", 0, "4/7", 1, "9/2", 87, "12/3", 42), Map.of()),
                f.client.getVacuumProperties(device("dreame.vacuum.r9445d"), () -> true));
        var data = JsonParser.parseString(bodies.getFirst()).getAsJsonObject().getAsJsonObject("data");
        assertEquals("get_properties", data.get("method").getAsString());
        var parameters = data.getAsJsonArray("params");
        int[][] expected = { { 0, 2, 1 }, { 1, 2, 2 }, { 2, 3, 1 }, { 3, 3, 2 }, { 4, 4, 1 }, { 5, 4, 2 }, { 6, 4, 3 },
                { 7, 4, 4 }, { 8, 4, 5 }, { 9, 4, 7 }, { 10, 4, 23 }, { 11, 4, 25 }, { 12, 4, 40 }, { 13, 4, 50 },
                { 14, 9, 1 }, { 15, 9, 2 }, { 16, 10, 1 }, { 17, 10, 2 }, { 18, 11, 1 }, { 19, 11, 2 }, { 20, 12, 2 },
                { 21, 12, 3 }, { 22, 12, 4 }, { 23, 16, 1 }, { 24, 16, 2 }, { 25, 18, 1 }, { 26, 18, 2 }, { 27, 20, 1 },
                { 28, 20, 2 } };
        assertEquals(expected.length, parameters.size());
        for (int i = 0; i < expected.length; i++) {
            var parameter = parameters.get(i).getAsJsonObject();
            assertEquals(Integer.toString(expected[i][0]), parameter.get("did").getAsString());
            assertEquals(expected[i][1], parameter.get("siid").getAsInt());
            assertEquals(expected[i][2], parameter.get("piid").getAsInt());
        }
    }

    @Test
    void readsMapListReferenceFromPropertySixEight() throws Exception {
        Fixture f = new Fixture();
        List<String> bodies = new ArrayList<>();
        doAnswer(invocation -> {
            ContentProvider content = invocation.getArgument(0);
            StringBuilder body = new StringBuilder();
            content.forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer.duplicate())));
            bodies.add(body.toString());
            return f.request;
        }).when(f.request).content(any(ContentProvider.class));
        doReturn(response("""
                {"code":0,"data":{"result":[{"siid":6,"piid":8,"code":0,
                "value":"{\\"object_name\\":\\"ali_dreame/device/map-list\\",\\"md5\\":\\"private\\"}"}]}}
                """)).when(f.request).send();

        assertEquals("ali_dreame/device/map-list",
                f.client.getVacuumMapListObjectName(device("dreame.vacuum.r9445d"), () -> true));
        var parameter = JsonParser.parseString(bodies.getFirst()).getAsJsonObject().getAsJsonObject("data")
                .getAsJsonArray("params").get(0).getAsJsonObject();
        assertEquals(6, parameter.get("siid").getAsInt());
        assertEquals(8, parameter.get("piid").getAsInt());
    }

    @Test
    void rejectsEmptyMalformedCancelledAndUnsupportedSnapshots() throws Exception {
        Fixture f = new Fixture();
        for (String value : List.of("[]", "null", "{}", "[{\"siid\":3,\"piid\":1,\"code\":0,\"value\":101}]",
                "[{\"siid\":2,\"piid\":2,\"code\":\"0\",\"value\":0}]")) {
            doReturn(response("{\"code\":0,\"data\":{\"result\":" + value + "}}")).when(f.request).send();
            assertThrows(DreameCloudException.class,
                    () -> f.client.getVacuumProperties(device("dreame.vacuum.r9445d"), () -> true));
        }
        clearInvocations(f.request);
        assertThrows(DreameCloudException.class,
                () -> f.client.getVacuumProperties(device("dreame.vacuum.r9445d"), () -> false));
        assertThrows(DreameCloudException.class,
                () -> f.client.getVacuumProperties(device("dreame.vacuum.other"), () -> true));
        verify(f.request, never()).send();
    }

    private static DreameDevice device(String model) {
        return new DreameDevice("test", "test", model, "", "", "eu.host:8883", "");
    }

    private static com.google.gson.JsonObject property(String body) {
        return JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("params").get(0)
                .getAsJsonObject();
    }

    private static ContentResponse response(String json) {
        ContentResponse response = Objects.requireNonNull(mock(ContentResponse.class));
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(json);
        return response;
    }

    private static class Fixture {
        final HttpClient http = Objects.requireNonNull(mock(HttpClient.class));
        final Request request = Objects.requireNonNull(mock(Request.class, RETURNS_SELF));
        final DreameApiClient client;

        Fixture() throws Exception {
            when(http.newRequest(anyString())).thenReturn(request);
            doReturn(response(
                    "{\"access_token\":\"token\",\"refresh_token\":\"refresh\",\"expires_in\":7200,\"region\":\"eu\"}"))
                    .when(request).send();
            client = new DreameApiClient(http);
            client.login("test@example.com", "secret", "eu");
            clearInvocations(request);
        }
    }
}
