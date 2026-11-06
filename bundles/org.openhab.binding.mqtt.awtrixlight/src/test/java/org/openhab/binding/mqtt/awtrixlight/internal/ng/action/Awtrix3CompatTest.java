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
package org.openhab.binding.mqtt.awtrixlight.internal.ng.action;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/**
 * Test cases for the {@link Awtrix3Compat}.
 *
 * @author Keith T. Garner - Initial contribution
 */
class Awtrix3CompatTest {

    private final Set<String> withoutEquivalent = new TreeSet<>();

    private Map<String, Object> translate(Map<String, ?> params) {
        return Awtrix3Compat.toNotification(params, withoutEquivalent);
    }

    @Test
    void rgbBecomesHex() {
        assertEquals("#0A0B0C", Awtrix3Compat.toHex(new int[] { 10, 11, 12 }));
    }

    @Test
    void rgbThatIsNoColorIsRefused() {
        assertNull(Awtrix3Compat.toHex(new int[] { 1, 2 }));
        assertNull(Awtrix3Compat.toHex(new int[] { 1, 2, 3, 4 }));
        assertNull(Awtrix3Compat.toHex(new int[] { 0, -1, 0 }));
        assertNull(Awtrix3Compat.toHex(new int[] { 0, 0, 256 }));
    }

    @Test
    void renamedKeysKeepTheirValues() {
        Map<String, Object> notification = translate(Map.of("textOffset", 3, "blinkText", 500, "fadeText", 700, "bar",
                List.of(1, 2), "line", List.of(3, 4), "autoscale", true, "progress", 40));

        assertEquals(Map.of("textOffsetX", 3, "textBlinkMs", 500, "textFadeMs", 700, "barChart", List.of(1, 2),
                "lineChart", List.of(3, 4), "chartAutoscale", true, "progress", 40), notification);
    }

    @Test
    void colorsBecomeHex() {
        Map<String, Object> notification = translate(Map.of("color", List.of(255, 0, 0), "background",
                new int[] { 0, 0, 255 }, "progressC", "#00FF00", "progressBC", List.of(1, 2, 3)));

        assertEquals("#FF0000", notification.get("textColor"));
        assertEquals("#0000FF", notification.get("backgroundColor"));
        assertEquals("#00FF00", notification.get("progressColor"));
        assertEquals("#010203", notification.get("progressTrackColor"));
    }

    @Test
    void secondsBecomeMilliseconds() {
        Map<String, Object> notification = translate(Map.of("duration", 7, "lifetime", 1.5));

        assertEquals(7000L, notification.get("durationMs"));
        assertEquals(1500L, notification.get("lifetimeMs"));
    }

    @Test
    void numbersBecomeWords() {
        Map<String, Object> notification = translate(
                Map.of("textCase", 2, "pushIcon", 1, "lifetimeMode", 1, "center", false));

        assertEquals("asTyped", notification.get("textCase"));
        assertEquals("pushOnce", notification.get("iconMode"));
        assertEquals("mark", notification.get("lifetimeExpiry"));
        assertEquals("start", notification.get("textAlign"));
    }

    @Test
    void numberThatIsNoWordIsPassedOnForTheClockToReject() {
        assertEquals(7, translate(Map.of("textCase", 7)).get("textCase"));
    }

    @Test
    void centerBecomesTextAlign() {
        assertEquals("center", translate(Map.of("center", true)).get("textAlign"));
    }

    @Test
    void overlayClearBecomesEmpty() {
        assertEquals("", translate(Map.of("overlay", "Clear")).get("overlay"));
        assertEquals("rain", translate(Map.of("overlay", "rain")).get("overlay"));
    }

    @Test
    void scrollKeysBecomeTheScrollObject() {
        Map<String, Object> notification = translate(Map.of("noScroll", true, "scrollSpeed", 50));

        assertEquals(Map.of("mode", "static", "speed", 50), notification.get("scroll"));
    }

    @Test
    void scrollThatStaysOnLeavesNoScrollObjectWithoutSpeed() {
        assertFalse(translate(Map.of("noScroll", false)).containsKey("scroll"));
    }

    @Test
    void rainbowBecomesThePalette() {
        Map<String, Object> notification = translate(Map.of("rainbow", true, "color", List.of(1, 2, 3)));

        assertEquals("Rainbow", notification.get("palette"));
        assertEquals("palette", notification.get("textColor"));
    }

    @Test
    void gradientBecomesThePalette() {
        Map<String, Object> notification = translate(
                Map.of("gradient", List.of(List.of(255, 0, 0), new int[] { 0, 0, 255 })));

        assertEquals("#FF0000,#0000FF", notification.get("palette"));
        assertEquals("palette", notification.get("textColor"));
    }

    @Test
    void effectSettingsBecomeTopLevelKeys() {
        Map<String, Object> notification = translate(
                Map.of("effect", "Fireworks", "effectSettings", Map.of("speed", 3, "palette", "Ocean", "blend", true)));

        assertEquals("Fireworks", notification.get("effect"));
        assertEquals(3, notification.get("effectSpeed"));
        assertEquals("Ocean", notification.get("palette"));
        assertEquals(true, notification.get("paletteBlend"));
    }

    @Test
    void palettePreferenceGoesToTheText() {
        Map<String, Object> notification = translate(
                Map.of("rainbow", true, "effectSettings", Map.of("palette", "Ocean")));

        assertEquals("Rainbow", notification.get("palette"));
    }

    @Test
    void effectPaletteNoneIsNoPalette() {
        assertFalse(translate(Map.of("effectSettings", Map.of("palette", "None"))).containsKey("palette"));
    }

    @Test
    void textPartsAreRenamedAndGetTheHash() {
        Map<String, Object> notification = translate(
                Map.of("text", List.of(Map.of("t", "Hello ", "c", "ff0000"), Map.of("t", "World", "c", "#00ff00"))));

        assertEquals(List.of(Map.of("text", "Hello ", "color", "#ff0000"), Map.of("text", "World", "color", "#00ff00")),
                notification.get("text"));
    }

    @Test
    void textAsStringIsPassedOn() {
        assertEquals("Hi <font color=\"#FF0000\">there</font>",
                translate(Map.of("text", "Hi <font color=\"#FF0000\">there</font>")).get("text"));
    }

    @Test
    void wakeUpBecomesWakeup() {
        assertEquals(true, translate(Map.of("wakeUp", true)).get("wakeup"));
    }

    @Test
    void keysWithoutEquivalentAreLeftOutAndReported() {
        Map<String, Object> notification = translate(Map.of("text", "Hi", "topText", true, "pos", 1, "barBC",
                List.of(1, 2, 3), "clients", List.of("a"), "save", true, "draw", List.of()));

        assertEquals(Map.of("text", "Hi"), notification);
        assertEquals(Set.of("topText", "pos", "barBC", "clients", "save", "draw"), withoutEquivalent);
    }

    @Test
    void otherKeysArePassedOn() {
        assertEquals(Map.of("icon", "bell", "textColor", "#FFFFFF"),
                translate(Map.of("icon", "bell", "textColor", "#FFFFFF")));
    }

    @Test
    void rtttlWinsOverSound() {
        assertEquals(Map.of("rtttl", "a:d=4,o=5,b=100:c"), Awtrix3Compat.toSound("a:d=4,o=5,b=100:c", "ding", false));
    }

    @Test
    void soundFileLoops() {
        assertEquals(Map.of("file", "ding", "loop", true), Awtrix3Compat.toSound(null, "ding", true));
    }

    @Test
    void noSoundWithoutRtttlAndFile() {
        assertNull(Awtrix3Compat.toSound("", " ", true));
        assertNull(Awtrix3Compat.toSound(null, null, false));
    }
}
