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
package org.openhab.binding.icalendar.internal.logic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A single event.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Andrew Fiddian-Green - Added support for event description
 */
@NonNullByDefault
public class Event implements Comparable<Event> {
    private static final Pattern HTML_TOKEN = Pattern.compile(
            "(?<quoted>(?<=:)(?:\"[^\"\\r\\n]*\"|'[^'\\r\\n]*'))" //
                    + "|(?<br><br\\s*/?>|</(?:p|div|li)\\s*>)" //
                    + "|(?<tag></?[a-zA-Z][\\w:-]*(?:\\s[^>]*)?/?>)" //
                    + "|(?<entity>&(?:nbsp|lt|gt|quot|amp|#39);)",
            Pattern.CASE_INSENSITIVE);
    private static final Map<String, String> ENTITIES = Map.of("&nbsp;", " ", "&lt;", "<", "&gt;", ">", "&quot;",
            "\"", "&amp;", "&", "&#39;", "'");

    public final List<CommandTag> commandTags = new ArrayList<>();
    public final Instant end;
    public final Instant start;
    public final String title;
    public final String location;

    public Event(String title, Instant start, Instant end, String description, String location) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.location = location;

        if (description.isEmpty()) {
            return;
        }

        String[] lines = stripHtml(description).split("\\R");
        for (String line : lines) {
            CommandTag tag = CommandTag.createCommandTag(line.trim());
            if (tag != null) {
                commandTags.add(tag);
            }
        }
    }

    /**
     * Converts a (possibly HTML formatted) event description to plain text. Calendar providers like Google Calendar
     * insert HTML markup into the description, which would otherwise break the Command Tags.
     * Line breaking elements are converted to line breaks, all other tags are removed and the most common entities are
     * decoded.
     * <p>
     * Quoted String values of a Command Tag (e.g. <code>BEGIN:Item:"&lt;b&gt;text&lt;/b&gt;"</code>) are passed
     * through literally. Descriptions without any markup outside of such values are returned unchanged.
     *
     * @param description the raw description of the event
     * @return the description as plain text
     */
    static String stripHtml(String description) {
        StringBuilder result = new StringBuilder();
        boolean hasMarkup = false;
        int last = 0;
        Matcher matcher = HTML_TOKEN.matcher(description);
        while (matcher.find()) {
            result.append(description, last, matcher.start());
            last = matcher.end();
            if (matcher.group("quoted") != null) {
                result.append(matcher.group());
            } else if (matcher.group("br") != null) {
                hasMarkup = true;
                result.append('\n');
            } else if (matcher.group("tag") != null) {
                hasMarkup = true;
            } else {
                String entity = matcher.group();
                result.append(ENTITIES.getOrDefault(entity.toLowerCase(Locale.ROOT), entity));
            }
        }
        if (!hasMarkup) {
            return description;
        }
        result.append(description, last, description.length());
        return result.toString();
    }

    @Override
    public String toString() {
        String[] tagStrings = new String[this.commandTags.size()];
        for (int i = 0; i < tagStrings.length; i++) {
            tagStrings[i] = this.commandTags.get(i).toString();
        }
        return "Event(title: " + this.title + ", start: " + this.start.toString() + ", end: " + this.end.toString()
                + ", commandTags: List(" + String.join(", ", tagStrings) + ")";
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        final Event otherEvent = (Event) other;
        return (this.title.equals(otherEvent.title) && this.start.equals(otherEvent.start)
                && this.end.equals(otherEvent.end) && this.location.equals(otherEvent.location));
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + location.hashCode();
        return result;
    }

    @Override
    public int compareTo(Event o) {
        return start.compareTo(o.start);
    }
}
