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
package org.openhab.binding.tapocontrol.internal.api.camera;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoLastAlarmInfo;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoMsgAlarmInfo;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoPresets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Verifies command payload shapes against the reference implementation.
 *
 * @author Kai Kreuzer - Initial contribution
 */
class TapoCameraCommandsTest {

    private static JsonObject json(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    @Test
    void getModuleBuildsReferenceShape() {
        JsonObject cmd = TapoCameraCommands.getModule("lens_mask", "lens_mask_info");
        assertEquals(json("{\"method\":\"get\",\"lens_mask\":{\"name\":[\"lens_mask_info\"]}}"), cmd);
    }

    @Test
    void setSectionBuildsReferenceShape() {
        JsonObject props = json("{\"enabled\":\"on\"}");
        assertEquals(json("{\"method\":\"set\",\"lens_mask\":{\"lens_mask_info\":{\"enabled\":\"on\"}}}"),
                TapoCameraCommands.setSection("lens_mask", "lens_mask_info", props));
    }

    @Test
    void setLensMaskUsesReferenceFunction() {
        assertEquals(json(
                "{\"method\":\"multipleRequest\",\"params\":{\"requests\":[{\"method\":\"setLensMaskConfig\",\"params\":{\"lens_mask\":{\"lens_mask_info\":{\"enabled\":\"on\"}}}}]}}"),
                TapoCameraCommands.setLensMaskEnabled(true));
    }

    @Test
    void manualAlarmUsesDoMethod() {
        assertEquals(json("{\"method\":\"do\",\"msg_alarm\":{\"manual_msg_alarm\":{\"action\":\"on\"}}}"),
                TapoCameraCommands.manualAlarm(true));
    }

    @Test
    void moveToPresetUsesDoMethodWithId() {
        assertEquals(json("{\"method\":\"do\",\"preset\":{\"goto_preset\":{\"id\":\"2\"}}}"),
                TapoCameraCommands.moveToPreset(2));
    }

    @Test
    void alertConfigSetterCombinesEnabledAndModes() {
        JsonObject expected = json(
                "{\"method\":\"set\",\"msg_alarm\":{\"chn1_msg_alarm_info\":{\"enabled\":\"on\",\"alarm_mode\":[\"sound\",\"light\"]}}}");
        assertEquals(expected, TapoCameraCommands.setAlertConfig(true, List.of("sound", "light")));
    }

    @Test
    void lastAlarmInfoParsing() {
        var info = TapoLastAlarmInfo.fromJson(extract(
                "{\"result\":{\"system\":{\"last_alarm_info\":{\"last_alarm_type\":\"motion\",\"last_alarm_time\":1689317707}}}}",
                "system", "last_alarm_info"));
        assertEquals("motion", info.type());
        assertEquals(1689317707L, info.timeEpochSeconds());
    }

    @Test
    void presetsParseParallelArrays() {
        var presets = TapoPresets
                .fromJson(extract("{\"result\":{\"preset\":{\"preset\":{\"id\":[1,2],\"name\":[\"Home\",\"Door\"]}}}}",
                        "preset", "preset"));
        assertEquals(List.of(1, 2), presets.ids());
        assertEquals(List.of("Home", "Door"), presets.names());
    }

    @Test
    void msgAlarmParsesModes() {
        var info = TapoMsgAlarmInfo.fromJson(extract(
                "{\"result\":{\"msg_alarm\":{\"chn1_msg_alarm_info\":{\"enabled\":\"on\",\"alarm_mode\":[\"sound\"]}}}}",
                "msg_alarm", "chn1_msg_alarm_info"));
        assertTrue(info.enabled());
        assertEquals(List.of("sound"), info.alarmModes());
    }

    private static JsonObject extract(String fullResponse, String module, String section) {
        return JsonParser.parseString(fullResponse).getAsJsonObject().getAsJsonObject("result").getAsJsonObject(module)
                .getAsJsonObject(section);
    }
}
