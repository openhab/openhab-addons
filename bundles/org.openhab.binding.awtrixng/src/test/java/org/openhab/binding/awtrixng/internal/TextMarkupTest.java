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

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Test cases for the {@link TextMarkup} conversion.
 *
 * @author Thomas Lauterbach - Initial contribution (test cases of the AWTRIX Light binding)
 * @author Keith T. Garner - Adapted to the AWTRIX NG fragment format
 */
class TextMarkupTest {

    private static void assertFragment(JsonArray fragments, int index, String text, String color) {
        var fragment = fragments.get(index).getAsJsonObject();
        assertEquals(text, fragment.get("text").getAsString());
        if (color == null) {
            assertFalse(fragment.has("color"));
        } else {
            assertEquals(color, fragment.get("color").getAsString());
        }
    }

    @Test
    void mixedContent() {
        JsonElement json = TextMarkup.toJson(
                "This is a <font color=\"#cc33cc\">Multi</font> Colored <font color=\"#34ab12\">Text</font>!",
                "#FFFFFF");

        JsonArray fragments = json.getAsJsonArray();
        assertEquals(5, fragments.size());
        assertFragment(fragments, 0, "This is a ", "#FFFFFF");
        assertFragment(fragments, 1, "Multi", "#cc33cc");
        assertFragment(fragments, 2, " Colored ", "#FFFFFF");
        assertFragment(fragments, 3, "Text", "#34ab12");
        assertFragment(fragments, 4, "!", "#FFFFFF");
    }

    @Test
    void withoutDefaultColorLeavesColorToTheClock() {
        JsonArray fragments = TextMarkup.toJson("a <font color=\"#ff0000\">b</font>", null).getAsJsonArray();

        assertEquals(2, fragments.size());
        assertFragment(fragments, 0, "a ", null);
        assertFragment(fragments, 1, "b", "#ff0000");
    }

    @Test
    void startingWithTag() {
        JsonArray fragments = TextMarkup.toJson("<font color=\"#ff0000\">Red</font> text at start", null)
                .getAsJsonArray();

        assertEquals(2, fragments.size());
        assertFragment(fragments, 0, "Red", "#ff0000");
        assertFragment(fragments, 1, " text at start", null);
    }

    @Test
    void angleBracketInsideTag() {
        JsonArray fragments = TextMarkup.toJson("<font color=\"#ff0000\">Red <- Nice!</font>", null).getAsJsonArray();

        assertEquals(1, fragments.size());
        assertFragment(fragments, 0, "Red <- Nice!", "#ff0000");
    }

    @Test
    void adjacentTags() {
        JsonArray fragments = TextMarkup
                .toJson("<font color=\"#ff0000\">Red</font><font color=\"#0000ff\">Blue</font>", null).getAsJsonArray();

        assertEquals(2, fragments.size());
        assertFragment(fragments, 0, "Red", "#ff0000");
        assertFragment(fragments, 1, "Blue", "#0000ff");
    }

    @Test
    void adjacentTagsWithSpace() {
        JsonArray fragments = TextMarkup
                .toJson("<font color=\"#ff0000\">Red</font> <font color=\"#0000ff\">Blue</font>", null)
                .getAsJsonArray();

        assertEquals(3, fragments.size());
        assertFragment(fragments, 1, " ", null);
    }

    @Test
    void tagWithoutColorUsesDefault() {
        JsonArray fragments = TextMarkup.toJson("<font>x</font>", "#123456").getAsJsonArray();

        assertEquals(1, fragments.size());
        assertFragment(fragments, 0, "x", "#123456");
    }

    @Test
    void missingClosingTagColorsTheRest() {
        JsonArray fragments = TextMarkup.toJson("a <font color=\"#ff0000\">rest", null).getAsJsonArray();

        assertEquals(2, fragments.size());
        assertFragment(fragments, 1, "rest", "#ff0000");
    }

    @Test
    void malformedTagIsKeptAsText() {
        JsonArray fragments = TextMarkup.toJson("a <font color", null).getAsJsonArray();

        assertEquals(1, fragments.size());
        assertFragment(fragments, 0, "a <font color", null);
    }

    @Test
    void plainTextStaysAString() {
        assertEquals("Just plain text", TextMarkup.toJson("Just plain text", "#FFFFFF").getAsString());
        assertEquals("", TextMarkup.toJson("", null).getAsString());
        assertEquals("   ", TextMarkup.toJson("   ", null).getAsString());
    }

    @Test
    void markupRoundTrip() {
        String markup = "plain <font color=\"#FF0000\">red</font> and <font color=\"#00FF00\">green</font>";

        assertEquals(markup, TextMarkup.toMarkup(TextMarkup.toJson(markup, null)));
        assertEquals("plain", TextMarkup.toMarkup(TextMarkup.toJson("plain", null)));
    }

    @Test
    void piecesThatAreNoFragmentAreLeftOutOfTheMarkup() {
        JsonElement stored = JsonParser.parseString(
                "[\"a\",{\"text\":\"b\",\"color\":\"#FF0000\"},{\"text\":{\"x\":1}},null,{\"color\":\"#00FF00\"}]");

        assertEquals("<font color=\"#FF0000\">b</font>", TextMarkup.toMarkup(stored));
    }

    @Test
    void markupOfFragmentsFromTheClock() {
        JsonElement fragments = JsonParser.parseString("[{\"text\":\"CPU \",\"color\":\"#888888\"},{\"text\":\"87\"}]");

        assertEquals("<font color=\"#888888\">CPU </font>87", TextMarkup.toMarkup(fragments));
    }
}
