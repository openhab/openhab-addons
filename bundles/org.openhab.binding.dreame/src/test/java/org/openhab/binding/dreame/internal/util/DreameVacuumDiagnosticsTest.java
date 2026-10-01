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
package org.openhab.binding.dreame.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameDevice;

import com.google.gson.JsonParser;

/**
 * Verifies that vacuum diagnostics expose only approved metadata.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumDiagnosticsTest {
    @Test
    void acceptsDryingTimeAndAutoEmptyAvailabilityProperties() {
        var values = JsonParser.parseString("[{\"siid\":4,\"piid\":40,\"code\":0,\"value\":2},"
                + "{\"siid\":15,\"piid\":3,\"code\":0,\"value\":1}]").getAsJsonArray();

        assertEquals(2, DreameVacuumDiagnostics.readPropertyResults(values).get("4/40"));
        assertEquals(1, DreameVacuumDiagnostics.readPropertyResults(values).get("15/3"));
    }

    @Test
    void omitsIdentifiersNamesFirmwareBrokerAndEmbeddedSecrets() {
        DreameDevice device = new DreameDevice("secret-id", "Private room", "dreame.vacuum.example", "secret-version",
                "secret-owner", "private-host:8883", "{\"unknownSecret\":\"secret-token\"}");
        assertEquals("model=dreame.vacuum.example, firmwarePresent=true, ownerPresent=true, brokerPresent=true",
                DreameVacuumDiagnostics.describe(device));
    }

    @Test
    void rejectsUnvalidatedModelTextAndReportsMissingMetadata() {
        DreameDevice device = new DreameDevice("", "", "dreame.vacuum.example\nprivate-data", "", "", "", "");
        assertFalse(device.isVacuum());
        assertEquals("model=<unknown>, firmwarePresent=false, ownerPresent=false, brokerPresent=false",
                DreameVacuumDiagnostics.describe(device));
    }

    @Test
    void messageSummaryOmitsValuesAndUnknownFields() {
        String payload = """
                {"did":"private-id","data":{"method":"properties_changed","params":[
                  {"siid":3,"piid":1,"value":81},
                  {"siid":4,"piid":2,"value":{"room":"private-room","token":"secret"}},
                  {"siid":"private-address","piid":1.5,"value":"private-value"}
                ],"unknown-secret":"secret"}}
                """;
        assertEquals(
                "method=properties_changed, params=3, properties=[siid=3/piid=1/type=number,"
                        + "siid=4/piid=2/type=object,siid=unknown/piid=unknown/type=string], truncated=false",
                DreameVacuumDiagnostics.describeMessage(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void unknownAndMalformedMessagesDoNotLeakContents() {
        assertEquals("method=unknown, paramsType=null", DreameVacuumDiagnostics.describeMessage(
                "{\"method\":\"private-method\",\"secret\":\"private-value\"}".getBytes(StandardCharsets.UTF_8)));
        assertEquals("message=invalid-json",
                DreameVacuumDiagnostics.describeMessage("{private-secret".getBytes(StandardCharsets.UTF_8)));
        assertEquals("message=omitted-size", DreameVacuumDiagnostics.describeMessage(new byte[65537]));
        assertEquals("message=omitted-depth", DreameVacuumDiagnostics
                .describeMessage(("[".repeat(33) + "0" + "]".repeat(33)).getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void limitsPropertySummaryLength() {
        String payload = "{\"params\":[" + "{\"siid\":1,\"piid\":2},".repeat(40) + "{}]}";
        String summary = DreameVacuumDiagnostics.describeMessage(payload.getBytes(StandardCharsets.UTF_8));
        assertEquals(32, summary.split("siid=", -1).length - 1);
        assertEquals(true, summary.endsWith("truncated=true"));
    }

    @Test
    void logsOnlyAllowlistedNumericStatusValuesForConfirmedModel() {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":2,"piid":1,"value":3},
                  {"siid":2,"piid":2,"value":0,"code":0},
                  {"siid":3,"piid":1,"value":81},
                  {"siid":3,"piid":2,"value":1},
                  {"siid":4,"piid":1,"value":2},
                  {"siid":4,"piid":7,"value":6},
                  {"siid":4,"piid":58,"value":12345},
                  {"siid":6,"piid":1,"value":"private!map"}
                ]}}
                """;
        String summary = DreameVacuumDiagnostics.describeMessage(payload.getBytes(StandardCharsets.UTF_8),
                "dreame.vacuum.r9445d");
        assertEquals(
                "method=properties_changed, params=8, properties=[siid=2/piid=1/type=number/value=3,"
                        + "siid=2/piid=2/type=number/value=0,siid=3/piid=1/type=number/value=81,"
                        + "siid=3/piid=2/type=number/value=1,siid=4/piid=1/type=number/value=2,"
                        + "siid=4/piid=7/type=number/value=6,siid=4/piid=58/type=number,"
                        + "siid=6/piid=1/type=string/chars=11/keyPresent=false/base64=false], truncated=false",
                summary);
        assertFalse(DreameVacuumDiagnostics
                .describeMessage(payload.getBytes(StandardCharsets.UTF_8), "dreame.vacuum.other").contains("/value="));
    }

    @Test
    void rejectsInvalidStatusTypesRangesAndFailureCodes() {
        String payload = """
                {"method":"properties_changed","params":[
                  {"siid":2,"piid":1,"value":"private-value"},
                  {"siid":2,"piid":1,"value":true},
                  {"siid":2,"piid":1,"value":{"secret":"private"}},
                  {"siid":2,"piid":1,"value":1.5},
                  {"siid":2,"piid":1,"value":-1},
                  {"siid":2,"piid":1,"value":256},
                  {"siid":2,"piid":2,"value":65536},
                  {"siid":3,"piid":1,"value":101},
                  {"siid":4,"piid":1,"value":2,"code":-1},
                  {"siid":4,"piid":1,"value":2,"code":"0"},
                  {"siid":4,"piid":1,"value":2,"code":null},
                  {"siid":4,"piid":7,"value":1e100},
                  {"siid":4,"piid":7}
                ]}
                """;
        String summary = DreameVacuumDiagnostics.describeMessage(payload.getBytes(StandardCharsets.UTF_8),
                "dreame.vacuum.r9445d");
        assertFalse(summary.contains("/value="));
        assertFalse(summary.contains("private"));
    }

    @Test
    void neverLogsValuesForEventsOrUnknownMethods() {
        for (String method : java.util.List.of("event_occured", "unknown")) {
            String payload = "{\"method\":\"" + method + "\",\"params\":[{\"siid\":2,\"piid\":1,\"value\":3}]}";
            assertFalse(DreameVacuumDiagnostics
                    .describeMessage(payload.getBytes(StandardCharsets.UTF_8), "dreame.vacuum.r9445d")
                    .contains("/value="));
        }
    }

    @Test
    void describesMapEncodingWithoutExposingMapKeysOrObjectNames() {
        String payload = """
                {"method":"properties_changed","params":[
                  {"siid":6,"piid":1,"value":"eJw=,secret-key"},
                  {"siid":6,"piid":3,"value":"https://private/map"},
                  {"siid":6,"piid":8,"value":"private-list"}
                ]}
                """;
        assertEquals("method=properties_changed, params=3, properties=["
                + "siid=6/piid=1/type=string/chars=15/keyPresent=true/base64=true/decodedBytes=2/zlibHeader=true,"
                + "siid=6/piid=3/type=string/chars=19,siid=6/piid=8/type=string/chars=12], truncated=false",
                DreameVacuumDiagnostics.describeMessage(payload.getBytes(StandardCharsets.UTF_8),
                        "dreame.vacuum.r9445d"));
        assertFalse(DreameVacuumDiagnostics
                .describeMessage(payload.getBytes(StandardCharsets.UTF_8), "dreame.vacuum.other").contains("/chars="));
        assertFalse(DreameVacuumDiagnostics.describeMessage(
                payload.replace("properties_changed", "event_occured").getBytes(StandardCharsets.UTF_8),
                "dreame.vacuum.r9445d").contains("/chars="));
    }

    @Test
    void skipsFailedMapPropertiesAndRejectsMalformedEncodingWithoutLeakingIt() {
        String payload = """
                {"method":"properties_changed","params":[
                  {"siid":6,"piid":1,"code":-1,"value":"secret"},
                  {"siid":6,"piid":1,"value":"!secret,private-key"},
                  {"siid":6,"piid":1,"value":"-_8="},
                  {"siid":6,"piid":8,"value":{"private":"secret"}}
                ]}
                """;
        assertEquals("method=properties_changed, params=4, properties=[siid=6/piid=1/type=string,"
                + "siid=6/piid=1/type=string/chars=19/keyPresent=true/base64=false,"
                + "siid=6/piid=1/type=string/chars=4/keyPresent=false/base64=true/decodedBytes=2/zlibHeader=false,"
                + "siid=6/piid=8/type=object], truncated=false",
                DreameVacuumDiagnostics.describeMessage(payload.getBytes(StandardCharsets.UTF_8),
                        "dreame.vacuum.r9445d"));
    }

    @Test
    void checksInflatedStructureWithoutExposingContents() throws IOException {
        byte[] map = new byte[40];
        map[19] = 2;
        map[21] = 3;
        System.arraycopy("secret".getBytes(StandardCharsets.UTF_8), 0, map, 30, 6);
        String summary = mapSummary(compress(map));
        assertTrue(summary.contains("/inflate=ok/inflatedBytes=40/trailingBytes=0/mapHeader=true/pixelBlockFits=true"));
        assertFalse(summary.contains("secret"));
        map[20] = (byte) 0xFF;
        assertTrue(mapSummary(compress(map)).contains("pixelBlockFits=false"));
        map[20] = 0;
        map[19] = 100;
        assertTrue(mapSummary(compress(map)).contains("pixelBlockFits=false"));
        assertTrue(mapSummary(compress(new byte[26])).contains("mapHeader=false"));
    }

    @Test
    void boundsInflationAndRejectsTruncatedOrCorruptStreams() throws IOException {
        assertTrue(mapSummary(compress(new byte[1024 * 1024])).contains("/inflate=ok/"));
        assertTrue(mapSummary(compress(new byte[1024 * 1024 + 1])).contains("/inflate=omitted-size"));
        byte[] compressed = compress(new byte[40]);
        assertTrue(mapSummary(Arrays.copyOf(compressed, compressed.length - 1)).contains("/inflate=incomplete"));
        byte[] trailing = Arrays.copyOf(compressed, compressed.length + 1);
        assertTrue(mapSummary(trailing).contains("/trailingBytes=1/"));
        compressed[compressed.length - 1] ^= 1;
        assertTrue(mapSummary(compressed).contains("/inflate=invalid"));
    }

    private static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DeflaterOutputStream stream = new DeflaterOutputStream(bytes)) {
            stream.write(data);
        }
        return bytes.toByteArray();
    }

    private static String mapSummary(byte[] compressed) {
        String payload = "{\"method\":\"properties_changed\",\"params\":[{\"siid\":6,\"piid\":1,\"value\":\""
                + Base64.getEncoder().encodeToString(compressed) + "\"}]}";
        return DreameVacuumDiagnostics.describeMessage(payload.getBytes(StandardCharsets.UTF_8),
                "dreame.vacuum.r9445d");
    }
}
