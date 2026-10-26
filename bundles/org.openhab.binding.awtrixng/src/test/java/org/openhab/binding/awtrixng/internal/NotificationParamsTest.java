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
package org.openhab.binding.awtrixng.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.binding.awtrixng.internal.dto.NotificationPayload;

import com.google.gson.JsonParser;

/**
 * Test cases for the {@link NotificationParams}.
 *
 * @author Keith T. Garner - Initial contribution
 */
class NotificationParamsTest {

    private static String json(Map<String, ?> params) {
        return Helper.toJson(NotificationParams.toPayload(params));
    }

    private static void assertJson(String expected, String actual) {
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(actual), actual);
    }

    /** A rule can pass null where the signature allows none, which the compiler would not let a test write out. */
    @SuppressWarnings("null")
    private static <T> T nullFromARule() {
        return null;
    }

    @Test
    void notificationThatIsNullIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> NotificationParams.toPayload(nullFromARule()));
    }

    @Test
    void ngKeysArePassedOn() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("text", "Doorbell");
        params.put("textColor", "#FF0000");
        params.put("durationMs", 10000);
        params.put("hold", true);
        params.put("stack", false);
        params.put("wakeup", true);
        params.put("name", "door");
        params.put("icon", "1234");
        params.put("repeat", 2);

        assertJson("""
                {"text":"Doorbell","textColor":"#FF0000","durationMs":10000,"hold":true,"stack":false,
                 "wakeup":true,"name":"door","icon":"1234","repeat":2}""", json(params));
    }

    @Test
    void nestedObjectsAndArraysAreKept() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("scroll", Map.of("mode", "bounce", "speed", 50));
        params.put("palette", java.util.List.of("#FFFF00", "#FF0000"));
        params.put("textColor", "palette");
        params.put("barChart", java.util.List.of(1, 2, 3));

        assertJson("""
                {"scroll":{"mode":"bounce","speed":50},"palette":["#FFFF00","#FF0000"],"textColor":"palette",
                 "barChart":[1,2,3]}""", json(params));
    }

    @Test
    void soundCanBeGivenByName() {
        assertJson("{\"sound\":{\"file\":\"ding\"}}", json(Map.of("sound", "ding")));
        assertJson("{\"sound\":{\"rtttl\":\"x:d=4:c\",\"loop\":true}}",
                json(Map.of("sound", Map.of("rtttl", "x:d=4:c", "loop", true))));
    }

    @Test
    void textWithFontTagsBecomesFragments() {
        assertJson("{\"text\":[{\"text\":\"a \"},{\"text\":\"b\",\"color\":\"#00FF00\"}]}",
                json(Map.of("text", "a <font color=\"#00FF00\">b</font>")));
    }

    @Test
    void unknownKeysAreRejectedAndNamed() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("text", "x");
        params.put("color", "#FF0000");
        params.put("duration", 5);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> NotificationParams.toPayload(params));

        // The keys of AWTRIX 3 are not those of NG
        assertEquals("Unknown notification keys: color, duration", e.getMessage());
    }

    @Test
    void keyWithoutValueIsLeftOut() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("text", "x");
        params.put("icon", null);

        assertJson("{\"text\":\"x\"}", json(params));
    }

    @Test
    void unknownKeyInsideASoundIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> NotificationParams.toPayload(Map.of("sound", Map.of("file", "ding", "bogus", 1))));

        assertEquals("A value does not fit its key: sound", e.getMessage());
    }

    @Test
    void valueThatOnlyLooksLikeTheTypeIsRejected() {
        // Gson would make false of "yes" and 5000 of "5000", NG ignores the first and rejects the second
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> NotificationParams.toPayload(Map.of("hold", "yes")));
        assertEquals("A value does not fit its key: hold", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> NotificationParams.toPayload(Map.of("durationMs", "5000")));
    }

    @Test
    void valueOfTheWrongTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> NotificationParams.toPayload(Map.of("hold", "yes")));
        assertThrows(IllegalArgumentException.class, () -> NotificationParams.toPayload(Map.of("durationMs", "long")));
        assertThrows(IllegalArgumentException.class, () -> NotificationParams.toPayload(Map.of("scroll", "bounce")));
    }

    @Test
    void invalidNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> NotificationParams.toPayload(Map.of("name", "has space")));
        assertEquals("door-1", NotificationParams.toPayload(Map.of("name", "door-1")).name);
    }

    @Test
    void emptyParametersGiveAnEmptyNotification() {
        NotificationPayload payload = NotificationParams.toPayload(Map.of());

        assertEquals("{}", Helper.toJson(payload));
    }
}
