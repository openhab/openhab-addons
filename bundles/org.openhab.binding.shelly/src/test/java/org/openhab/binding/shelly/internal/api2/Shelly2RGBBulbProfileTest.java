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
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2RGBStatus;

import com.google.gson.Gson;

/**
 * Tests for the Shelly Multicolor Bulb Gen3 {@code rgb:0} component DTO mapping.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2RGBBulbProfileTest {

    private final Gson gson = new Gson();

    // --- RGB.GetStatus deserialization ---

    @Test
    void getRGBStatusDeserializes() {
        String json = """
                {"id":0,"output":true,"brightness":70.0,"rgb":[255,100,0]}
                """;
        Shelly2RGBStatus status = Objects.requireNonNull(gson.fromJson(json, Shelly2RGBStatus.class));
        assertThat(status.output, is(true));
        assertThat(status.brightness, is(70.0));
        Integer[] rgb = status.rgb;
        assertThat(rgb, is(notNullValue()));
        if (rgb != null) {
            assertThat(rgb.length, is(3));
            assertThat(rgb[0], is(255));
            assertThat(rgb[1], is(100));
            assertThat(rgb[2], is(0));
        }
    }

    @Test
    void getRGBStatusNullRgbWhenOff() {
        String json = """
                {"id":0,"output":false,"brightness":0}
                """;
        Shelly2RGBStatus status = Objects.requireNonNull(gson.fromJson(json, Shelly2RGBStatus.class));
        assertThat(status.output, is(false));
        assertThat(status.rgb, is(nullValue()));
    }

    @Test
    void getRGBStatusHasMeterFields() {
        // Confirms the DTO carries meter fields even though the binding does not
        // currently wire them into emeter channels for this device (known limitation)
        String json = """
                {"id":0,"output":true,"brightness":50.0,"rgb":[0,0,255],"apower":4.2,"voltage":230.1,"current":0.02}
                """;
        Shelly2RGBStatus status = Objects.requireNonNull(gson.fromJson(json, Shelly2RGBStatus.class));
        assertThat(status.apower, is(4.2));
        assertThat(status.voltage, is(230.1));
        assertThat(status.current, is(0.02));
    }

    // --- Shelly.GetStatus result wrapper deserialization ---

    @Test
    void deviceStatusResultHasRgb0Field() {
        // Verifies @SerializedName("rgb:0") mapping used exclusively by Multicolor Bulb G3
        String json = """
                {"rgb:0":{"id":0,"output":true,"brightness":80.0,"rgb":[10,20,30]}}
                """;
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));
        Shelly2RGBStatus rgb = result.rgb0;
        assertThat(rgb, is(notNullValue()));
        if (rgb != null) {
            assertThat(rgb.output, is(true));
            assertThat(rgb.brightness, is(80.0));
        }
    }

    @Test
    void deviceStatusResultRgb0NullWhenAbsent() {
        String json = """
                {"switch:0":{"id":0}}
                """;
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));
        assertThat(result.rgb0, is(nullValue()));
    }

    // --- Shelly.GetConfig deserialization ---

    @Test
    void getConfigResultHasRgb0WhenRgbBulb() {
        String json = """
                {"rgb:0":{"id":0,"auto_on":false,"auto_off":false}}
                """;
        Shelly2GetConfigResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));
        assertThat(result.rgb0, is(notNullValue()));
    }

    @Test
    void getConfigResultRgb0NullWhenAbsent() {
        String json = """
                {"switch:0":{"id":0}}
                """;
        Shelly2GetConfigResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));
        assertThat(result.rgb0, is(nullValue()));
    }
}
