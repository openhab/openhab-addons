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
package org.openhab.binding.tapocontrol.internal.devices.wifi.lightswitch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;

/**
 * Tests for {@link TapoDimmerSwitchData}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class TapoDimmerSwitchDataTest {

    @Test
    void parsesHs220DeviceInfo() {
        TapoDimmerSwitchData data = Objects.requireNonNull(GSON.fromJson("""
                {
                  "brightness": 51,
                  "device_on": true,
                  "model": "HS220",
                  "nickname": "RGluaW5nIFJvb20gRGltbWVy"
                }
                """, TapoDimmerSwitchData.class));

        assertThat(data.getBrightness(), is(51));
        assertThat(data.isOn(), is(true));
        assertThat(data.getNickname(), is("Dining Room Dimmer"));
    }

    @Test
    void brightnessCommandTurnsDimmerOn() {
        TapoDimmerSwitchData data = new TapoDimmerSwitchData();

        data.setBrightness(42);

        var json = JsonParser.parseString(data.toJson()).getAsJsonObject();
        assertThat(json.get("brightness").getAsInt(), is(42));
        assertThat(json.get("device_on").getAsBoolean(), is(true));
    }

    @Test
    void zeroBrightnessTurnsDimmerOffWithoutChangingLevel() {
        TapoDimmerSwitchData data = new TapoDimmerSwitchData();

        data.setBrightness(0);

        var json = JsonParser.parseString(data.toJson()).getAsJsonObject();
        assertThat(json.has("brightness"), is(false));
        assertThat(json.get("device_on").getAsBoolean(), is(false));
    }
}
