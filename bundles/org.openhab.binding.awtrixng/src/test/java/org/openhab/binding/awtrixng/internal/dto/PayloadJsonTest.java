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
package org.openhab.binding.awtrixng.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.awtrixng.internal.Helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Test cases for the serialization and parsing of the payload DTOs. The fixtures are the examples of the AWTRIX NG
 * reference documentation.
 *
 * @author Keith T. Garner - Initial contribution
 */
class PayloadJsonTest {

    private static JsonObject json(Object payload) {
        return JsonParser.parseString(Helper.toJson(payload)).getAsJsonObject();
    }

    @Test
    void emptyPayloadHasNoKeys() {
        assertEquals("{}", Helper.toJson(new AppPayload()));
        assertEquals("{}", Helper.toJson(new NotificationPayload()));
        assertEquals("{}", Helper.toJson(new Settings()));
    }

    @Test
    void appPayloadUsesNgKeysAndUnits() {
        AppPayload app = new AppPayload();
        app.text = new JsonPrimitive("OVERHEAT");
        app.textColor = "palette";
        app.palette = new JsonArray();
        app.palette.getAsJsonArray().add("#FFFF00");
        app.palette.getAsJsonArray().add("#FF0000");
        app.durationMs = 10000L;
        app.lifetimeMs = 300000L;
        app.lifetimeExpiry = "mark";
        app.iconMode = "pushOnce";
        app.textCase = "asTyped";
        app.barChart = List.of(1, 2, 3);
        app.scroll = new ScrollConfig();
        app.scroll.mode = "static";
        app.scroll.speed = 50;

        JsonObject json = json(app);

        assertEquals("palette", json.get("textColor").getAsString());
        assertEquals(2, json.getAsJsonArray("palette").size());
        assertEquals(10000L, json.get("durationMs").getAsLong());
        assertEquals(300000L, json.get("lifetimeMs").getAsLong());
        assertEquals("mark", json.get("lifetimeExpiry").getAsString());
        assertEquals("pushOnce", json.get("iconMode").getAsString());
        assertEquals("asTyped", json.get("textCase").getAsString());
        assertEquals(3, json.getAsJsonArray("barChart").size());
        assertEquals("static", json.getAsJsonObject("scroll").get("mode").getAsString());
        assertEquals(50, json.getAsJsonObject("scroll").get("speed").getAsInt());
        assertFalse(json.getAsJsonObject("scroll").has("gap"));
        assertEquals(10, json.size());
    }

    @Test
    void notificationPayloadWithSound() {
        NotificationPayload notification = new NotificationPayload();
        notification.text = new JsonPrimitive("ALARM");
        notification.hold = true;
        notification.sound = new SoundSpec();
        notification.sound.rtttl = "siren:d=8,o=5,b=200:c,g,c,g";
        notification.sound.loop = true;

        JsonObject json = json(notification);

        assertTrue(json.get("hold").getAsBoolean());
        assertFalse(json.has("stack"));
        assertEquals("siren:d=8,o=5,b=200:c,g,c,g", json.getAsJsonObject("sound").get("rtttl").getAsString());
        assertTrue(json.getAsJsonObject("sound").get("loop").getAsBoolean());
        assertFalse(json.getAsJsonObject("sound").has("track"));
    }

    @Test
    void specialCharactersAreNotEscaped() {
        NotificationPayload notification = new NotificationPayload();
        notification.text = new JsonPrimitive("R&D <5> it's");
        notification.sound = new SoundSpec();
        notification.sound.rtttl = "two:d=4,o=5,b=200:c,e";

        String json = Helper.toJson(notification);

        assertTrue(json.contains("\"R&D <5> it's\""), json);
        assertTrue(json.contains("\"two:d=4,o=5,b=200:c,e\""), json);
        assertEquals("R&D <5> it's", JsonParser.parseString(json).getAsJsonObject().get("text").getAsString());
    }

    @Test
    void appPayloadRoundTrip() {
        String stored = """
                {"text":[{"text":"CPU ","color":"#888888"},{"text":"87","color":"#FF0000"}],
                 "icon":"2422","durationMs":5000,"effectSpeed":1.5,"draw":[["pixel",1,2,"#FF0000"]]}
                """;

        AppPayload app = Helper.fromJson(stored, AppPayload.class);

        assertNotNull(app);
        assertEquals(2, app.text.getAsJsonArray().size());
        assertEquals("2422", app.icon);
        assertEquals(5000L, app.durationMs);
        assertEquals(1.5, app.effectSpeed);
        assertEquals(1, app.draw.size());
        assertEquals(JsonParser.parseString(stored), JsonParser.parseString(Helper.toJson(app)));
    }

    @Test
    void settingsPartialUpdate() {
        Settings settings = new Settings();
        settings.brightness = 120;
        settings.autoBrightness = false;

        assertEquals(JsonParser.parseString("{\"brightness\":120,\"autoBrightness\":false}"), json(settings));
    }

    @Test
    void deviceStateFromReferenceExample() {
        String payload = """
                {"version":"1.0.12","uid":"a4cf12ab34cd","boardType":"awtrixng","soc":"esp32",
                 "ipAddress":"192.168.1.42","macAddress":"A4:CF:12:0B:3C:7D","hostname":"awtrixng-ab34cd",
                 "wifiRssi":-58,"uptimeSeconds":4213,"freeHeapBytes":118234,"fps":41,"brightness":120,
                 "lightLevel":29.3,"batteryPercent":88,"batteryVoltage":4.1,"lowBattery":false,
                 "temperature":21.5,"humidity":42,"pressureHpa":1013.2,"matrixPower":true,"currentApp":"Time",
                 "indicators":[{"on":true,"color":"#FF0000","blinkMs":500,"fadeMs":0},
                  {"on":false,"color":"#000000","blinkMs":0,"fadeMs":0},
                  {"on":false,"color":"#000000","blinkMs":0,"fadeMs":0}],
                 "messageCount":0,"wifi":{"enabled":true},"mqtt":{"enabled":true}}
                """;

        DeviceState state = Helper.fromJson(payload, DeviceState.class);

        assertNotNull(state);
        assertEquals("a4cf12ab34cd", state.uid);
        assertEquals("awtrixng", state.boardType);
        assertEquals(-58, state.wifiRssi);
        assertEquals(29.3, state.lightLevel);
        assertEquals(42.0, state.humidity);
        assertEquals(Boolean.TRUE, state.matrixPower);
        assertEquals(3, state.indicators.size());
        assertTrue(state.indicators.get(0).on);
        assertEquals("#FF0000", state.indicators.get(0).color);
        assertEquals(500, state.indicators.get(0).blinkMs);
    }

    @Test
    void deviceStateWithoutOptionalHardware() {
        DeviceState state = Helper.fromJson("{\"uid\":\"a4cf12ab34cd\",\"boardType\":\"awtrixng\"}", DeviceState.class);

        assertNotNull(state);
        assertNull(state.temperature);
        assertNull(state.batteryPercent);
        assertNull(state.lightLevel);
    }

    @Test
    void capabilitiesFromReferenceExample() {
        String payload = """
                {"effects":["None","Matrix"],"paletteEffects":["Plasma"],"transitions":["Random","Slide"],
                 "overlays":["rain","snow"],"palettes":["Cloud","Lava"],"audio":{"buzzer":true,"dfplayer":false},
                 "fonts":[{"name":"small","ascent":5,"descent":0,"lineHeight":6},{"name":"large"}],
                 "sensors":{"light":true}}
                """;

        Capabilities capabilities = Helper.fromJson(payload, Capabilities.class);

        assertNotNull(capabilities);
        assertEquals(List.of("None", "Matrix"), capabilities.effects);
        assertEquals(List.of("Random", "Slide"), capabilities.transitions);
        assertEquals(List.of("rain", "snow"), capabilities.overlays);
        assertEquals("large", capabilities.fonts.get(1).name);
    }

    @Test
    void screenFrameFromReferenceFormat() {
        ScreenFrame frame = Helper.fromJson("{\"width\":2,\"height\":2,\"pixels\":[16711680,65280,255,0]}",
                ScreenFrame.class);

        assertNotNull(frame);
        assertEquals(2, frame.width);
        assertEquals(2, frame.height);
        assertArrayEquals(new int[] { 0xFF0000, 0x00FF00, 0x0000FF, 0 }, frame.pixels);
    }

    @Test
    void screenFrameWithoutPixelsIsEmpty() {
        ScreenFrame frame = Helper.fromJson("{\"width\":32,\"height\":8}", ScreenFrame.class);

        assertNotNull(frame);
        assertEquals(0, frame.pixels.length);
    }

    @Test
    void invalidJsonIsRejected() {
        assertNull(Helper.fromJson("{not json", DeviceState.class));
        assertNull(Helper.fromJson("", DeviceState.class));
    }
}
