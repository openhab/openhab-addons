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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Builders for the module-style command payloads of the Tapo camera local API.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public final class TapoCameraCommands {
    public static final String MODULE_LENS_MASK = "lens_mask";
    public static final String SECTION_LENS_MASK_INFO = "lens_mask_info";
    public static final String MODULE_MSG_ALARM = "msg_alarm";
    public static final String SECTION_MSG_ALARM_INFO = "chn1_msg_alarm_info";
    public static final String SECTION_MANUAL_ALARM = "manual_msg_alarm";
    public static final String MODULE_SYSTEM = "system";
    public static final String SECTION_LAST_ALARM_INFO = "last_alarm_info";
    public static final String MODULE_MOTION_DETECTION = "motion_detection";
    public static final String SECTION_MOTION_DET = "motion_det";
    public static final String MODULE_LED = "led";
    public static final String SECTION_LED_CONFIG = "config";
    public static final String MODULE_PRESET = "preset";
    public static final String SECTION_PRESET = "preset";
    public static final String SECTION_GOTO_PRESET = "goto_preset";
    public static final String MODULE_DEVICE_INFO = "device_info";
    public static final String SECTION_BASIC_INFO = "basic_info";

    private TapoCameraCommands() {
    }

    /** {"method":"get","<module>":{"name":["<section>"]}} */
    public static JsonObject getModule(String module, String section) {
        JsonObject body = new JsonObject();
        JsonArray sections = new JsonArray();
        sections.add(section);
        body.add("name", sections);
        JsonObject cmd = new JsonObject();
        cmd.addProperty("method", "get");
        cmd.add(module, body);
        return cmd;
    }

    /** {"method":"set","<module>":{"<section>":{...props}}} */
    public static JsonObject setSection(String module, String section, JsonObject properties) {
        JsonObject sectionBody = new JsonObject();
        sectionBody.add(section, properties);
        JsonObject cmd = new JsonObject();
        cmd.addProperty("method", "set");
        cmd.add(module, sectionBody);
        return cmd;
    }

    /** {"method":"do","<module>":{"<action>":{...props}}} */
    public static JsonObject doAction(String module, String action, JsonObject properties) {
        JsonObject actionBody = new JsonObject();
        actionBody.add(action, properties);
        JsonObject cmd = new JsonObject();
        cmd.addProperty("method", "do");
        cmd.add(module, actionBody);
        return cmd;
    }

    public static JsonObject getLensMaskInfo() {
        return getModule(MODULE_LENS_MASK, SECTION_LENS_MASK_INFO);
    }

    public static JsonObject setLensMaskEnabled(boolean enabled) {
        JsonObject props = new JsonObject();
        props.addProperty("enabled", onOff(enabled));
        JsonObject request = new JsonObject();
        request.addProperty("method", "setLensMaskConfig");
        JsonObject params = new JsonObject();
        JsonObject lensMask = new JsonObject();
        lensMask.add(SECTION_LENS_MASK_INFO, props);
        params.add(MODULE_LENS_MASK, lensMask);
        request.add("params", params);
        JsonArray requests = new JsonArray();
        requests.add(request);
        JsonObject multipleParams = new JsonObject();
        multipleParams.add("requests", requests);
        JsonObject command = new JsonObject();
        command.addProperty("method", "multipleRequest");
        command.add("params", multipleParams);
        return command;
    }

    public static JsonObject getAlertConfig() {
        return getModule(MODULE_MSG_ALARM, SECTION_MSG_ALARM_INFO);
    }

    public static JsonObject setAlertConfig(boolean enabled, List<String> alarmModes) {
        JsonObject props = new JsonObject();
        props.addProperty("enabled", onOff(enabled));
        JsonArray modes = new JsonArray();
        alarmModes.forEach(modes::add);
        props.add("alarm_mode", modes);
        return setSection(MODULE_MSG_ALARM, SECTION_MSG_ALARM_INFO, props);
    }

    public static JsonObject manualAlarm(boolean on) {
        JsonObject props = new JsonObject();
        props.addProperty("action", onOff(on));
        return doAction(MODULE_MSG_ALARM, SECTION_MANUAL_ALARM, props);
    }

    public static JsonObject getLastAlarmInfo() {
        return getModule(MODULE_SYSTEM, SECTION_LAST_ALARM_INFO);
    }

    public static JsonObject getDetectionConfig() {
        return getModule(MODULE_MOTION_DETECTION, SECTION_MOTION_DET);
    }

    public static JsonObject setDetectionConfig(@Nullable Boolean enabled, @Nullable Integer sensitivity) {
        JsonObject props = new JsonObject();
        if (enabled != null) {
            props.addProperty("enabled", onOff(enabled));
        }
        if (sensitivity != null) {
            props.addProperty("digital_sensitivity", sensitivity.toString());
        }
        return setSection(MODULE_MOTION_DETECTION, SECTION_MOTION_DET, props);
    }

    public static JsonObject getLedConfig() {
        return getModule(MODULE_LED, SECTION_LED_CONFIG);
    }

    public static JsonObject setLedEnabled(boolean enabled) {
        JsonObject props = new JsonObject();
        props.addProperty("enabled", onOff(enabled));
        return setSection(MODULE_LED, SECTION_LED_CONFIG, props);
    }

    public static JsonObject getPresets() {
        return getModule(MODULE_PRESET, SECTION_PRESET);
    }

    public static JsonObject moveToPreset(int presetId) {
        JsonObject props = new JsonObject();
        props.addProperty("id", Integer.toString(presetId));
        return doAction(MODULE_PRESET, SECTION_GOTO_PRESET, props);
    }

    public static JsonObject getDeviceInfo() {
        return getModule(MODULE_DEVICE_INFO, SECTION_BASIC_INFO);
    }

    public static String onOff(boolean value) {
        return value ? "on" : "off";
    }
}
