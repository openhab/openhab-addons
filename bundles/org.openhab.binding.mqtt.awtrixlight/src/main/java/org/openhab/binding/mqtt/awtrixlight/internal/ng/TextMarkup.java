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
package org.openhab.binding.mqtt.awtrixlight.internal.ng;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.TextFragment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * The {@link TextMarkup} converts the {@code <font color="#rrggbb">} markup users can put into the text channel into
 * the fragment array AWTRIX NG expects.
 *
 * @author Thomas Lauterbach - Initial contribution (AWTRIX 3 font markup parsing this class is based on)
 * @author Keith T. Garner - Adapted to the AWTRIX NG fragment format
 */
@NonNullByDefault
public class TextMarkup {

    private static final String OPEN_TAG = "<font";
    private static final String CLOSE_TAG = "</font>";
    private static final Pattern COLOR_PATTERN = Pattern.compile("color=\"(#[0-9A-Fa-f]{6})\"");
    private static final Gson GSON = new Gson();

    private TextMarkup() {
    }

    /**
     * @param markup the text, possibly containing font tags
     * @param defaultColor color for the text outside of tags, or null to leave it to the clock
     * @return the plain string if the text has no tag, otherwise the array of colored fragments
     */
    public static JsonElement toJson(String markup, @Nullable String defaultColor) {
        if (indexOfOpenTag(markup) < 0) {
            return new JsonPrimitive(markup);
        }
        return GSON.toJsonTree(toFragments(markup, defaultColor));
    }

    /**
     * The reverse of {@link #toJson}: shows the text of an app as the user would have entered it.
     */
    public static String toMarkup(JsonElement text) {
        if (text.isJsonPrimitive()) {
            return text.getAsString();
        }
        StringBuilder markup = new StringBuilder();
        if (text.isJsonArray()) {
            for (JsonElement element : text.getAsJsonArray()) {
                TextFragment fragment = fragmentOf(element);
                if (fragment == null || fragment.text == null) {
                    continue;
                }
                String color = fragment.color;
                if (color == null) {
                    markup.append(fragment.text);
                } else {
                    markup.append(OPEN_TAG).append(" color=\"").append(color).append("\">").append(fragment.text)
                            .append(CLOSE_TAG);
                }
            }
        }
        return markup.toString();
    }

    /** A stored app comes from the broker, so a piece that is no fragment is left out and does not fail the rest. */
    private static @Nullable TextFragment fragmentOf(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }
        try {
            return GSON.fromJson(element, TextFragment.class);
        } catch (JsonParseException e) {
            return null;
        }
    }

    /**
     * The start of the next font tag. Text such as {@code <fontawesome>} is no tag, so a space or {@code >} must
     * follow.
     */
    private static int indexOfOpenTag(String text) {
        int start = text.indexOf(OPEN_TAG);
        while (start >= 0) {
            int next = start + OPEN_TAG.length();
            if (next < text.length() && (Character.isWhitespace(text.charAt(next)) || text.charAt(next) == '>')) {
                return start;
            }
            start = text.indexOf(OPEN_TAG, next);
        }
        return -1;
    }

    static List<TextFragment> toFragments(String markup, @Nullable String defaultColor) {
        List<TextFragment> fragments = new ArrayList<>();
        String remaining = markup;
        while (!remaining.isEmpty()) {
            int tagStart = indexOfOpenTag(remaining);
            int tagEnd = tagStart < 0 ? -1 : remaining.indexOf('>', tagStart);
            if (tagEnd < 0) {
                // No tag left, or a malformed one that is kept as plain text
                fragments.add(new TextFragment(remaining, defaultColor));
                break;
            }
            if (tagStart > 0) {
                fragments.add(new TextFragment(remaining.substring(0, tagStart), defaultColor));
            }
            Matcher color = COLOR_PATTERN.matcher(remaining.substring(tagStart, tagEnd + 1));
            String tagColor = color.find() ? color.group(1) : defaultColor;
            int close = remaining.indexOf(CLOSE_TAG, tagEnd);
            if (close < 0) {
                fragments.add(new TextFragment(remaining.substring(tagEnd + 1), tagColor));
                break;
            }
            fragments.add(new TextFragment(remaining.substring(tagEnd + 1, close), tagColor));
            remaining = remaining.substring(close + CLOSE_TAG.length());
        }
        return fragments;
    }
}
