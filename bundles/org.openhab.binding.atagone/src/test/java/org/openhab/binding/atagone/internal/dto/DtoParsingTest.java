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
package org.openhab.binding.atagone.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Verifies Gson DTO parsing against captured fixture JSON (structure matches real device).
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
class DtoParsingTest {

    private static final Gson GSON = new GsonBuilder().create();

    private String loadFixture(String name) throws IOException {
        try (@Nullable
        InputStream in = getClass().getResourceAsStream(name)) {
            assertNotNull(in, "Fixture not found: " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private RetrieveReplyDTO loadRetrieveReply() throws IOException {
        String json = loadFixture("retrieve_reply.json");
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        RetrieveReplyDTO reply = GSON.fromJson(root.getAsJsonObject("retrieve_reply"), RetrieveReplyDTO.class);
        assertNotNull(reply);
        return reply;
    }

    @Test
    void retrieveReplyTopLevelParsesCorrectly() throws IOException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        assertEquals(2, reply.acc_status);
        assertEquals(0, reply.seqnr);
    }

    @Test
    void statusBlockParsesCorrectly() throws IOException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        assertEquals("6808-1401-3109_15-30-001-544", reply.status.device_id);
        assertEquals(766123456L, reply.status.date_time);
        assertEquals(16385, reply.status.device_status);
    }

    @Test
    void reportTemperaturesParsedCorrectly() throws IOException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        assertEquals(21.3, reply.report.room_temp, 0.001);
        // Negative outside temperature must remain negative — kozmoz/atag-one-api issue #36
        assertEquals(-3.5, reply.report.outside_temp, 0.001);
        assertEquals(1.52, reply.report.ch_water_pres, 0.001);
        assertEquals(3521.75, reply.report.burning_hours, 0.001);
    }

    @Test
    void reportFieldsRelocatedFromDetailsAreParsedCorrectly() throws IOException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        // These fields are in the report root, not in report.details
        assertEquals(0.0, reply.report.dhw_flow_rate, 0.001);
        assertEquals(3, reply.report.resets);
        assertEquals(6485, reply.report.memory_allocation);
        assertEquals(-24, reply.report.current);
        assertEquals(3914, reply.report.voltage);
        assertEquals(27, reply.report.rssi);
    }

    @Test
    void reportDetailsParsedCorrectly() throws IOException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        assertEquals(45, reply.report.details.rel_mod_level);
        assertEquals(15, reply.report.details.min_mod_level);
        assertEquals(60.0, reply.report.details.max_boiler_temp, 0.001);
        // boiler_return_temp — renamed from ret_temp
        assertEquals(52.1, reply.report.details.boiler_return_temp, 0.001);
        assertEquals(1, reply.report.details.regulation_state);
    }

    @Test
    void controlBlockParsedCorrectly() throws IOException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        assertEquals(2, reply.control.ch_mode); // automatic
        // 0=room control, 1=weather-compensated control — independent of ch_mode, see ControlDTO's
        // javadoc. This fixture's device is on room control.
        assertEquals(0, reply.control.ch_control_mode);
        assertEquals(60.0, reply.control.dhw_temp_setp, 0.001);
        // weather fields are in control, not report
        assertEquals(5.0, reply.control.weather_temp, 0.001);
        assertEquals(3, reply.control.weather_status);
        assertEquals(3600L, reply.control.fireplace_duration);
    }

    @Test
    void configurationBlockParsedCorrectly() throws IOException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        // Setpoints are double, not int
        assertEquals(20.0, reply.configuration.ch_min_set, 0.001);
        assertEquals(85.0, reply.configuration.ch_max_set, 0.001);
        assertEquals(10.0, reply.configuration.dhw_min_set, 0.001);
        assertEquals(65.0, reply.configuration.dhw_max_set, 0.001);
        assertEquals(15.0, reply.configuration.ch_vacation_temp, 0.001);
        // Renamed fields
        assertEquals(1, reply.configuration.frost_prot_enabled);
        assertEquals(4.0, reply.configuration.frost_prot_temp_room, 0.001);
        assertEquals(1, reply.configuration.dhw_legion_enabled);
        assertEquals(30, reply.configuration.disp_brightness);
        assertEquals(604800L, reply.configuration.ch_mode_vacation);
        assertEquals("P153530934", reply.configuration.boiler_id);
    }

    @Test
    void pairReplyParsesCorrectly() throws IOException {
        String json = loadFixture("pair_reply.json");
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        PairReplyDTO reply = GSON.fromJson(root.getAsJsonObject("pair_reply"), PairReplyDTO.class);
        assertNotNull(reply);
        assertEquals(1, reply.acc_status); // pending — user must press Accept
        assertEquals(0, reply.seqnr);
    }

    @Test
    void controlUpdateOmitsNullFields() {
        ControlUpdateDTO update = new ControlUpdateDTO();
        update.ch_mode = 3;
        update.vacation_duration = 604800L;

        String json = GSON.toJson(update);
        assertFalse(json.contains("ch_control_mode"), "null fields must not appear in JSON");
        assertTrue(json.contains("\"ch_mode\":3"));
        assertTrue(json.contains("\"vacation_duration\":604800"));
    }

    @Test
    void configUpdateOmitsNullFields() {
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.start_vacation = 830995200L;
        update.ch_vacation_temp = 15.0;

        String json = GSON.toJson(update);
        assertFalse(json.contains("frost_prot_enabled"), "null fields must not appear in JSON");
        assertTrue(json.contains("\"start_vacation\":830995200"));
        assertTrue(json.contains("\"ch_vacation_temp\":15.0"));
    }
}
