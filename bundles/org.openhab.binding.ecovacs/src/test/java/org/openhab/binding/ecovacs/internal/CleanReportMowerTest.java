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
package org.openhab.binding.ecovacs.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.CleanReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.LastTimeStatsReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.StatsReport;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

/**
 * Tests for CleanReport mower-specific behavior, including the determineCleanMode logic,
 * StatsReport mowedArea deserialization, and LastTimeStatsReport deserialization.
 *
 * @author Stefan Höhn - Initial contribution
 */
class CleanReportMowerTest {

    private final Gson gson = new Gson();

    // --- CleanReport.determineCleanMode tests ---

    @Test
    void testDetermineCleanModeWorkingWithTypeAuto() {
        CleanReport report = new CleanReport();
        CleanReport.CleanStateReport cleanState = new CleanReport.CleanStateReport();
        cleanState.motionState = "working";
        cleanState.type = "auto";
        report.cleanState = cleanState;

        CleanMode mode = report.determineCleanMode(gson);
        assertEquals(CleanMode.AUTO, mode);
    }

    @Test
    void testDetermineCleanModeWorkingWithTypeNull() {
        // Bug fix: mowers may send type=null when working, should default to AUTO
        CleanReport report = new CleanReport();
        CleanReport.CleanStateReport cleanState = new CleanReport.CleanStateReport();
        cleanState.motionState = "working";
        cleanState.type = null;
        report.cleanState = cleanState;

        CleanMode mode = report.determineCleanMode(gson);
        assertEquals(CleanMode.AUTO, mode);
    }

    @Test
    void testDetermineCleanModePause() {
        CleanReport report = new CleanReport();
        CleanReport.CleanStateReport cleanState = new CleanReport.CleanStateReport();
        cleanState.motionState = "pause";
        cleanState.type = "auto";
        report.cleanState = cleanState;

        CleanMode mode = report.determineCleanMode(gson);
        assertEquals(CleanMode.PAUSE, mode);
    }

    @Test
    void testDetermineCleanModeGoCharging() {
        CleanReport report = new CleanReport();
        CleanReport.CleanStateReport cleanState = new CleanReport.CleanStateReport();
        cleanState.motionState = "goCharging";
        cleanState.type = "auto";
        report.cleanState = cleanState;

        CleanMode mode = report.determineCleanMode(gson);
        assertEquals(CleanMode.RETURNING, mode);
    }

    @Test
    void testDetermineCleanModeIdleNoCleanState() {
        CleanReport report = new CleanReport();
        report.state = "idle";
        report.cleanState = null;

        CleanMode mode = report.determineCleanMode(gson);
        assertEquals(CleanMode.IDLE, mode);
    }

    @Test
    void testDetermineCleanModeContentAsJsonObject() {
        // Mowers send content as a JSON object, not a string
        CleanReport report = new CleanReport();
        CleanReport.CleanStateReport cleanState = new CleanReport.CleanStateReport();
        cleanState.motionState = "working";
        cleanState.type = "auto";
        cleanState.content = JsonParser.parseString("{\"type\":\"auto\",\"value\":\"zone1\"}");
        report.cleanState = cleanState;

        CleanMode mode = report.determineCleanMode(gson);
        assertEquals(CleanMode.AUTO, mode);
        // getAreaDefinition should return null for JSON object content
        assertNull(cleanState.getAreaDefinition());
    }

    @Test
    void testGetAreaDefinitionWithStringContent() {
        CleanReport.CleanStateReport cleanState = new CleanReport.CleanStateReport();
        cleanState.content = JsonParser.parseString("\"1,2,3,4\"");

        assertEquals("1,2,3,4", cleanState.getAreaDefinition());
    }

    @Test
    void testGetAreaDefinitionWithNullContent() {
        CleanReport.CleanStateReport cleanState = new CleanReport.CleanStateReport();
        cleanState.content = null;

        assertNull(cleanState.getAreaDefinition());
    }

    @Test
    void testDetermineCleanModeWorkingWithTypeBorder() {
        CleanReport report = new CleanReport();
        CleanReport.CleanStateReport cleanState = new CleanReport.CleanStateReport();
        cleanState.motionState = "working";
        cleanState.type = "border";
        report.cleanState = cleanState;

        CleanMode mode = report.determineCleanMode(gson);
        assertEquals(CleanMode.EDGE, mode);
    }

    // --- CleanReport deserialization from JSON (simulating GetMowerStateCommand response) ---

    @Test
    void testCleanReportDeserializationWorkingAuto() {
        String json = "{\"trigger\":\"app\",\"state\":\"clean\","
                + "\"cleanState\":{\"router\":\"plan\",\"type\":\"auto\",\"motionState\":\"working\"}}";
        CleanReport report = gson.fromJson(json, CleanReport.class);

        assertNotNull(report.cleanState);
        assertEquals("working", report.cleanState.motionState);
        assertEquals(CleanMode.AUTO, report.determineCleanMode(gson));
    }

    @Test
    void testCleanReportDeserializationPause() {
        String json = "{\"trigger\":\"app\",\"state\":\"clean\","
                + "\"cleanState\":{\"router\":\"plan\",\"type\":\"auto\",\"motionState\":\"pause\"}}";
        CleanReport report = gson.fromJson(json, CleanReport.class);

        assertNotNull(report.cleanState);
        assertEquals("pause", report.cleanState.motionState);
        assertEquals(CleanMode.PAUSE, report.determineCleanMode(gson));
    }

    @Test
    void testCleanReportDeserializationGoCharging() {
        String json = "{\"trigger\":\"workComplete\",\"state\":\"clean\","
                + "\"cleanState\":{\"router\":\"plan\",\"type\":\"auto\",\"motionState\":\"goCharging\"}}";
        CleanReport report = gson.fromJson(json, CleanReport.class);

        assertNotNull(report.cleanState);
        assertEquals("goCharging", report.cleanState.motionState);
        assertEquals(CleanMode.RETURNING, report.determineCleanMode(gson));
    }

    @Test
    void testCleanReportDeserializationIdleNoCleanState() {
        String json = "{\"trigger\":\"app\",\"state\":\"idle\"}";
        CleanReport report = gson.fromJson(json, CleanReport.class);

        assertNull(report.cleanState);
        assertEquals("idle", report.state);
        assertEquals(CleanMode.IDLE, report.determineCleanMode(gson));
    }

    @Test
    void testCleanReportDeserializationWithObjectContent() {
        // Mowers send content as a JSON object rather than a string
        String json = "{\"trigger\":\"app\",\"state\":\"clean\","
                + "\"cleanState\":{\"router\":\"plan\",\"type\":\"auto\",\"motionState\":\"working\","
                + "\"content\":{\"type\":\"auto\",\"value\":\"zone1\"}}}";
        CleanReport report = gson.fromJson(json, CleanReport.class);

        assertNotNull(report.cleanState);
        assertNotNull(report.cleanState.content);
        assertTrue(report.cleanState.content.isJsonObject());
        assertNull(report.cleanState.getAreaDefinition());
        assertEquals(CleanMode.AUTO, report.determineCleanMode(gson));
    }

    // --- StatsReport tests ---

    @Test
    void testStatsReportMowedAreaDeserialization() {
        String json = "{\"area\":150,\"mowedArea\":120,\"time\":3600,\"cid\":\"session123\","
                + "\"start\":1700000000,\"type\":\"auto\"}";
        StatsReport report = gson.fromJson(json, StatsReport.class);

        assertEquals(150, report.area);
        assertEquals(120, report.mowedArea);
        assertEquals(3600, report.timeInSeconds);
        assertEquals("session123", report.cid);
        assertEquals(1700000000L, report.startTimestamp);
        assertEquals("auto", report.type);
    }

    @Test
    void testStatsReportMowedAreaZero() {
        String json = "{\"area\":0,\"mowedArea\":0,\"time\":0,\"cid\":\"abc\",\"start\":0,\"type\":\"auto\"}";
        StatsReport report = gson.fromJson(json, StatsReport.class);

        assertEquals(0, report.mowedArea);
    }

    // --- LastTimeStatsReport tests ---

    @Test
    void testLastTimeStatsReportDeserialization() {
        String json = "{\"start\":\"1700000000\",\"time\":1800,\"area\":250}";
        LastTimeStatsReport report = gson.fromJson(json, LastTimeStatsReport.class);

        assertEquals("1700000000", report.startTimestamp);
        assertEquals(1800, report.durationSeconds);
        assertEquals(250, report.areaSqCm);
    }

    @Test
    void testLastTimeStatsReportDefaultValues() {
        String json = "{}";
        LastTimeStatsReport report = gson.fromJson(json, LastTimeStatsReport.class);

        // Gson will not use the field initializer for missing fields when deserializing to an existing class,
        // but startTimestamp has a default of "0" in the class definition
        assertEquals(0, report.durationSeconds);
        assertEquals(0, report.areaSqCm);
    }

    @Test
    void testLastTimeStatsReportWithMowerData() {
        // Simulates typical GOAT mower last time stats
        String json = "{\"start\":\"1720000000\",\"time\":4500,\"area\":800}";
        LastTimeStatsReport report = gson.fromJson(json, LastTimeStatsReport.class);

        assertEquals("1720000000", report.startTimestamp);
        assertEquals(4500, report.durationSeconds);
        assertEquals(800, report.areaSqCm);
    }
}
