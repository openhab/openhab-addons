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
import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api2.ShellyBluJsonDTO.Shelly2NotifyBluEventData;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

/**
 * Tests for BLU ZB device support: model registration, BTHome field deserialization,
 * and scalar/array adapter handling.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBluZbTest {

    private final Gson gson = new Gson();

    @Test
    void bluRcButton4ZbMapsToRcButton4ThingType() {
        ThingTypeUID uid = THING_TYPE_BY_DEVICE_TYPE.get(SHELLYDT_BLURCBUTTON4ZB);
        assertNotNull(uid);
        assertThat(uid, is(THING_TYPE_SHELLYBLURCBUTTON4));
    }

    @Test
    void bluMotionZbMapsToMotionThingType() {
        ThingTypeUID uid = THING_TYPE_BY_DEVICE_TYPE.get(SHELLYDT_BLUMOTIONZB);
        assertNotNull(uid);
        assertThat(uid, is(THING_TYPE_SHELLYBLUMOTION));
    }

    @Test
    void bluDwZbMapsToDwThingType() {
        ThingTypeUID uid = THING_TYPE_BY_DEVICE_TYPE.get(SHELLYDT_BLUDWZB);
        assertNotNull(uid);
        assertThat(uid, is(THING_TYPE_SHELLYBLUDW));
    }

    @Test
    void bluHtDisplayZbMapsToHtThingType() {
        ThingTypeUID uid = THING_TYPE_BY_DEVICE_TYPE.get(SHELLYDT_BLUHTDISPLAYZB);
        assertNotNull(uid);
        assertThat(uid, is(THING_TYPE_SHELLYBLUHT));
    }

    @Test
    void lightLevelBrightDeserializes() {
        Shelly2NotifyBluEventData data = gson.fromJson("{\"LightLevel\":2,\"Battery\":85}",
                Shelly2NotifyBluEventData.class);
        assertNotNull(data);
        assertThat(data.lightLevel, is(2));
        assertThat(data.battery, is(85));
    }

    @Test
    void lightLevelDarkDeserializes() {
        Shelly2NotifyBluEventData data = gson.fromJson("{\"LightLevel\":0}", Shelly2NotifyBluEventData.class);
        assertNotNull(data);
        assertThat(data.lightLevel, is(0));
    }

    @Test
    void batteryLowSetDeserializes() {
        Shelly2NotifyBluEventData data = gson.fromJson("{\"Battery\":10,\"BatteryLow\":1}",
                Shelly2NotifyBluEventData.class);
        assertNotNull(data);
        assertThat(data.battery, is(10));
        assertThat(data.batteryLow, is(1));
    }

    @Test
    void batteryLowClearedDeserializes() {
        Shelly2NotifyBluEventData data = gson.fromJson("{\"Battery\":80,\"BatteryLow\":0}",
                Shelly2NotifyBluEventData.class);
        assertNotNull(data);
        assertThat(data.batteryLow, is(0));
    }

    @Test
    void scalarButtonDeserializesToSingleElementArray() {
        Shelly2NotifyBluEventData data = gson.fromJson("{\"Button\":1}", Shelly2NotifyBluEventData.class);
        assertNotNull(data);
        Integer[] buttons = data.buttons;
        assertNotNull(buttons);
        assertThat(buttons.length, is(1));
        assertThat(buttons[0], is(1));
    }

    @Test
    void arrayButtonDeserializesToMultiElementArray() {
        Shelly2NotifyBluEventData data = gson.fromJson("{\"Button\":[1,0,1,0]}", Shelly2NotifyBluEventData.class);
        assertNotNull(data);
        Integer[] buttons = data.buttons;
        assertNotNull(buttons);
        assertThat(buttons.length, is(4));
        assertThat(buttons[0], is(1));
        assertThat(buttons[2], is(1));
    }

    @Test
    void scalarTemperatureDeserializesToSingleElementArray() {
        Shelly2NotifyBluEventData data = gson.fromJson("{\"Temperature\":21.5}", Shelly2NotifyBluEventData.class);
        assertNotNull(data);
        Double[] temperatures = data.temperatures;
        assertNotNull(temperatures);
        assertThat(temperatures.length, is(1));
        assertThat(temperatures[0], is(21.5));
    }

    @Test
    void arrayTemperatureDeserializesToMultiElementArray() {
        Shelly2NotifyBluEventData data = gson.fromJson("{\"Temperature\":[21.5,22.0]}",
                Shelly2NotifyBluEventData.class);
        assertNotNull(data);
        Double[] temperatures = data.temperatures;
        assertNotNull(temperatures);
        assertThat(temperatures.length, is(2));
        assertThat(temperatures[0], is(21.5));
        assertThat(temperatures[1], is(22.0));
    }
}
