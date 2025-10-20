/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.awtrixlight.internal.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Test cases for the {@link AwtrixApp} object.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
class AwtrixAppTest {

    @Test
    void testTextWithMixedContent() {
        AwtrixApp app = new AwtrixApp();
        app.setText("This is a <font color=\"#cc33cc\">Multi</font> Colored <font color=\"#34ab12\">Text</font>!");
        app.setColor(new int[] { 255, 255, 255 }); // Default white color

        String config = app.getAppConfig();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();
        JsonArray text = json.get("text").getAsJsonArray();

        // Should be 5 segments: "This is a ", "Multi", " Colored ", "Text", "!"
        assertEquals(5, text.size());

        // Verify each segment's text
        assertEquals("This is a ", text.get(0).getAsJsonObject().get("t").getAsString());
        assertEquals("Multi", text.get(1).getAsJsonObject().get("t").getAsString());
        assertEquals(" Colored ", text.get(2).getAsJsonObject().get("t").getAsString());
        assertEquals("Text", text.get(3).getAsJsonObject().get("t").getAsString());
        assertEquals("!", text.get(4).getAsJsonObject().get("t").getAsString());

        // Optionally, verify colors
        assertEquals("ffffff", text.get(0).getAsJsonObject().get("c").getAsString()); // Default color
        assertEquals("cc33cc", text.get(1).getAsJsonObject().get("c").getAsString());
        assertEquals("ffffff", text.get(2).getAsJsonObject().get("c").getAsString()); // Default color
        assertEquals("34ab12", text.get(3).getAsJsonObject().get("c").getAsString());
        assertEquals("ffffff", text.get(4).getAsJsonObject().get("c").getAsString()); // Default color
    }

    @Test
    void testTextStartingWithColor() {
        AwtrixApp app = new AwtrixApp();
        app.setText("<font color=\"#ff0000\">Red</font> text at start");
        app.setColor(new int[] { 0, 0, 0 }); // Default black color

        String config = app.getAppConfig();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();

        assertTrue(json.has("text"));
        JsonArray text = json.get("text").getAsJsonArray();
        assertEquals(2, text.size());
        assertEquals("Red", text.get(0).getAsJsonObject().get("t").getAsString());
        assertEquals(" text at start", text.get(1).getAsJsonObject().get("t").getAsString());
    }

    @Test
    void testTextWithBrackets() {
        AwtrixApp app = new AwtrixApp();
        app.setText("<font color=\"#ff0000\">Red <- Nice!</font>");
        app.setColor(new int[] { 0, 0, 0 }); // Default black color

        String config = app.getAppConfig();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();

        assertTrue(json.has("text"));
        JsonArray text = json.get("text").getAsJsonArray();
        assertEquals(1, text.size());
        assertEquals("Red <- Nice!", text.get(0).getAsJsonObject().get("t").getAsString());
    }

    @Test
    void testAdjacentColorSegments() {
        AwtrixApp app = new AwtrixApp();
        app.setText("<font color=\"#ff0000\">Red</font><font color=\"#0000ff\">Blue</font>");
        app.setColor(new int[] { 0, 0, 0 });

        String config = app.getAppConfig();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();

        assertTrue(json.has("text"));
        JsonArray text = json.get("text").getAsJsonArray();
        assertEquals(2, text.size());
        assertEquals("Red", text.get(0).getAsJsonObject().get("t").getAsString());
        assertEquals("Blue", text.get(1).getAsJsonObject().get("t").getAsString());
    }

    @Test
    void testAdjacentColorSegmentsWithSpace() {
        AwtrixApp app = new AwtrixApp();
        app.setText("<font color=\"#ff0000\">Red</font> <font color=\"#0000ff\">Blue</font>");
        app.setColor(new int[] { 0, 0, 0 });

        String config = app.getAppConfig();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();

        assertTrue(json.has("text"));
        JsonArray text = json.get("text").getAsJsonArray();
        assertEquals(3, text.size());
        assertEquals("Red", text.get(0).getAsJsonObject().get("t").getAsString());
        assertEquals(" ", text.get(1).getAsJsonObject().get("t").getAsString());
        assertEquals("Blue", text.get(2).getAsJsonObject().get("t").getAsString());
    }

    @Test
    void testPlainText() {
        AwtrixApp app = new AwtrixApp();
        app.setText("Just plain text with no colors");
        app.setColor(new int[] { 18, 52, 86 }); // Some default color

        String config = app.getAppConfig();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();

        assertTrue(json.has("text"));
        String text = json.get("text").getAsString();
        assertEquals("Just plain text with no colors", text);
    }

    @Test
    void testEmptyString() {
        AwtrixApp app = new AwtrixApp();
        app.setText("");
        app.setColor(new int[] { 0, 0, 0 });

        String config = app.getAppConfig();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();

        // Depending on implementation, the text field might be empty or not present
        if (json.has("text")) {
            assertTrue(json.get("text").getAsString().isEmpty());
        }
    }

    @Test
    void testOnlySpaces() {
        AwtrixApp app = new AwtrixApp();
        app.setText("   ");
        app.setColor(new int[] { 0, 0, 0 });

        String config = app.getAppConfig();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();

        assertTrue(json.has("text"));
        assertEquals("   ", json.get("text").getAsString());
    }
}
