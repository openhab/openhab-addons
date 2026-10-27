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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;

/**
 * Tests of Command Tag extraction from (HTML formatted) event descriptions in {@link Event}.
 *
 * @author Contributors - Initial contribution
 */
@NonNullByDefault
public class EventTest {

    private Event createEvent(String description) {
        return new Event("Test", Instant.parse("2026-03-19T06:00:00Z"), Instant.parse("2026-03-19T07:00:00Z"),
                description, "");
    }

    @Test
    public void testPlainDescription() {
        Event event = createEvent("BEGIN:Office_Lights:ON\nEND:Office_Lights:OFF");
        assertEquals(2, event.commandTags.size());
        assertEquals(OnOffType.ON, event.commandTags.get(0).getCommand());
        assertEquals(OnOffType.OFF, event.commandTags.get(1).getCommand());
    }

    @Test
    public void testHtmlLineBreaks() {
        Event event = createEvent("<span>BEGIN:Office_Lights:50%</span><br>END:Office_Lights:0%<br/>");
        assertEquals(2, event.commandTags.size());
        assertEquals(CommandTagType.BEGIN, event.commandTags.get(0).getTagType());
        assertEquals(new PercentType(50), event.commandTags.get(0).getCommand());
        assertEquals(CommandTagType.END, event.commandTags.get(1).getTagType());
        assertEquals(new PercentType(0), event.commandTags.get(1).getCommand());
    }

    @Test
    public void testHtmlBlob() {
        Event event = createEvent(
                "<html-blob><pre><code>BEGIN:ExteriorLightsColorScheme:\"green\"</code></pre></html-blob>");
        assertEquals(1, event.commandTags.size());
        assertEquals("ExteriorLightsColorScheme", event.commandTags.get(0).getItemName());
        assertEquals(new StringType("green"), event.commandTags.get(0).getCommand());
    }

    @Test
    public void testParagraphsWithAttributesAndNamespacedTags() {
        Event event = createEvent(
                "<p>BEGIN:A:ON</p><p class=MsoNormal><span class=SpellE>END:A:OFF</span><o:p></o:p></p>");
        assertEquals(2, event.commandTags.size());
        assertEquals(OnOffType.ON, event.commandTags.get(0).getCommand());
        assertEquals(OnOffType.OFF, event.commandTags.get(1).getCommand());
    }

    @Test
    public void testQuotedStringWithAngleBracketsIsPreserved() {
        Event event = createEvent("BEGIN:Notification:\"<b>Meeting</b>\"");
        assertEquals(1, event.commandTags.size());
        assertEquals(new StringType("<b>Meeting</b>"), event.commandTags.get(0).getCommand());
    }

    @Test
    public void testQuotedStringWithAngleBracketsIsPreservedInHtml() {
        Event event = createEvent("<p>BEGIN:Notification:\"<b>Meeting</b>\"</p><p>END:Notification:'a &amp; b'</p>");
        assertEquals(2, event.commandTags.size());
        assertEquals(new StringType("<b>Meeting</b>"), event.commandTags.get(0).getCommand());
        assertEquals(new StringType("a &amp; b"), event.commandTags.get(1).getCommand());
    }

    @Test
    public void testEntitiesInQuotedStringArePreserved() {
        Event event = createEvent("<span>BEGIN:Notification:\"Tom &amp; Jerry\"</span><br>END:Notification:\"x\"");
        assertEquals(2, event.commandTags.size());
        assertEquals(new StringType("Tom &amp; Jerry"), event.commandTags.get(0).getCommand());
    }

    @Test
    public void testPlainTextWithoutMarkupIsUnchanged() {
        assertEquals("BEGIN:Notification:Tom &amp; Jerry", Event.stripHtml("BEGIN:Notification:Tom &amp; Jerry"));
    }

    @Test
    public void testTagAttributesWithQuotesAreStripped() {
        Event event = createEvent("<a href=\"http://example.org\">BEGIN:A:ON</a>");
        assertEquals(1, event.commandTags.size());
        assertEquals(OnOffType.ON, event.commandTags.get(0).getCommand());
    }
}
