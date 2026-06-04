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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2CCTStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2RGBCCTStatus;

import com.google.gson.Gson;

/**
 * Tests for the Shelly Duo Bulb Gen3 profile detection and RGBCCT DTO mapping.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2DuoBulbProfileTest {

    private final Gson gson = new Gson();

    // --- Shelly.GetStatus deserialization ---

    @Test
    void getCCTStatusDeserializes() {
        String json = """
                {"id":0,"output":false,"brightness":50.0,"ct":3200}
                """;
        Shelly2CCTStatus status = Objects.requireNonNull(gson.fromJson(json, Shelly2CCTStatus.class));
        assertThat(status.output, is(false));
        assertThat(status.brightness, is(50.0));
        assertThat(status.ct, is(3200));
    }

    @Test
    void getCCTStatusNullCtWhenOff() {
        // Device may omit ct when off — binding must default to minTemp
        String json = """
                {"id":0,"output":false,"brightness":0}
                """;
        Shelly2CCTStatus status = Objects.requireNonNull(gson.fromJson(json, Shelly2CCTStatus.class));
        assertThat(status.output, is(false));
        assertThat(status.ct, is(nullValue()));
    }

    @Test
    void getRGBCCTStatusDeserializesInCctMode() {
        String json = """
                {"id":0,"mode":"cct","output":true,"brightness":75.0,"ct":4000}
                """;
        Shelly2RGBCCTStatus status = Objects.requireNonNull(gson.fromJson(json, Shelly2RGBCCTStatus.class));
        assertThat(status.mode, is("cct"));
        assertThat(status.output, is(true));
        assertThat(status.brightness, is(75.0));
        assertThat(status.ct, is(4000));
        assertThat(status.rgb, is(nullValue()));
    }

    @Test
    void getRGBCCTStatusDeserializesInRgbMode() {
        String json = """
                {"id":0,"mode":"rgb","output":true,"brightness":60.0,"rgb":[255,128,0]}
                """;
        Shelly2RGBCCTStatus status = Objects.requireNonNull(gson.fromJson(json, Shelly2RGBCCTStatus.class));
        assertThat(status.mode, is("rgb"));
        assertThat(status.output, is(true));
        assertThat(status.brightness, is(60.0));
        assertThat(status.ct, is(nullValue()));
        Integer[] rgb = status.rgb;
        assertThat(rgb, is(notNullValue()));
        if (rgb != null) {
            assertThat(rgb.length, is(3));
            assertThat(rgb[0], is(255));
            assertThat(rgb[1], is(128));
            assertThat(rgb[2], is(0));
        }
    }

    @Test
    void getRGBCCTStatusNullModeDefaultsToCct() {
        // Missing mode field — binding must treat as CCT (safe default)
        String json = """
                {"id":0,"output":false,"brightness":50.0,"ct":3200}
                """;
        Shelly2RGBCCTStatus status = Objects.requireNonNull(gson.fromJson(json, Shelly2RGBCCTStatus.class));
        assertThat(status.mode, is(nullValue()));
        // inColor = "rgb".equals(null) → false → CCT mode
        boolean inColor = "rgb".equals(status.mode);
        assertThat(inColor, is(false));
    }

    // --- Shelly.GetStatus result wrapper deserialization ---

    @Test
    void deviceStatusResultHasCct0Field() {
        // Wraps the result payload — verifies @SerializedName("cct:0") mapping
        String json = """
                {"cct:0":{"id":0,"output":true,"brightness":80.0,"ct":3500}}
                """;
        Shelly2DeviceStatus.Shelly2DeviceStatusResult result = Objects
                .requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));
        Shelly2CCTStatus cct = result.cct0;
        assertThat(cct, is(notNullValue()));
        if (cct != null) {
            assertThat(cct.output, is(true));
            assertThat(cct.brightness, is(80.0));
            assertThat(cct.ct, is(3500));
        }
    }

    @Test
    void deviceStatusResultHasRgbcct0Field() {
        // Verifies @SerializedName("rgbcct:0") mapping for RGBCCT-capable Duo devices
        String json = """
                {"rgbcct:0":{"id":0,"mode":"cct","output":true,"brightness":80.0,"ct":3500}}
                """;
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));
        Shelly2RGBCCTStatus rgbcct = result.rgbcct0;
        assertThat(rgbcct, is(notNullValue()));
        if (rgbcct != null) {
            assertThat(rgbcct.mode, is("cct"));
            assertThat(rgbcct.output, is(true));
            assertThat(rgbcct.ct, is(3500));
        }
    }

    @Test
    void deviceStatusResultCct0NullWhenAbsent() {
        // CCT-only device in RGBCCT result — cct0 is null, rgbcct0 is populated
        String json = """
                {"rgbcct:0":{"id":0,"mode":"rgb","output":true,"brightness":50.0,"rgb":[0,255,0]}}
                """;
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));
        assertThat(result.cct0, is(nullValue()));
        assertThat(result.rgbcct0, is(notNullValue()));
    }

    // --- Shelly.GetConfig deserialization ---

    @Test
    void getConfigResultHasCct0WhenCctOnly() {
        String json = """
                {"cct:0":{"id":0,"auto_on":false,"auto_off":false}}
                """;
        Shelly2GetConfigResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));
        assertThat(result.cct0, is(notNullValue()));
        assertThat(result.rgbcct0, is(nullValue()));
    }

    @Test
    void getConfigResultHasRgbcct0WhenRgbcct() {
        // RGBCCT config — presence of rgbcct0 triggers isRGBCCT=true in fillDuoBulbSettings
        String json = """
                {"rgbcct:0":{"id":0,"auto_on":false,"auto_off":false}}
                """;
        Shelly2GetConfigResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));
        assertThat(result.rgbcct0, is(notNullValue()));
        assertThat(result.cct0, is(nullValue()));
    }

    @Test
    void getConfigResultCct0NullWhenAbsent() {
        // Switching device config — no cct0 and no rgbcct0
        String json = """
                {"switch:0":{"id":0}}
                """;
        Shelly2GetConfigResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));
        assertThat(result.cct0, is(nullValue()));
        assertThat(result.rgbcct0, is(nullValue()));
    }
}
