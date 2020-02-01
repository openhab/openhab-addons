/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for presentable calendar.
 *
 * @author Michael Wodniok - Initial contribution.
 * 
 * @author Andrew Fiddian-Green - Tests for Command Tag code
 *
 */
public class BiweeklyPresentableCalendarTest {
    private AbstractPresentableCalendar calendar;
    private AbstractPresentableCalendar calendar2;
    private AbstractPresentableCalendar calendar3;

    @Before
    public void setUp() throws IOException, CalendarException {
        calendar = new BiweeklyPresentableCalendar(new FileInputStream("src/test/resources/test.ics"),
                Duration.ofDays(2));
        calendar2 = new BiweeklyPresentableCalendar(new FileInputStream("src/test/resources/test2.ics"),
                Duration.ofDays(30));
        calendar3 = new BiweeklyPresentableCalendar(new FileInputStream("src/test/resources/test3.ics"),
                Duration.ofDays(30));
    }

    /**
     * Tests recurrence and whether TimeZone is interpolated in a right way.
     */
    @Test
    public void testIsEventPresent() {
        // Test series
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-08T09:04:00Z")));
        assertTrue(calendar.isEventPresent(Instant.parse("2019-09-08T09:08:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-08T09:11:00Z")));

        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-09T09:04:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-09T09:08:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-09T09:11:00Z")));

        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-10T09:04:00Z")));
        assertTrue(calendar.isEventPresent(Instant.parse("2019-09-10T09:08:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-10T09:11:00Z")));

        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-11T09:04:00Z")));
        assertTrue(calendar.isEventPresent(Instant.parse("2019-09-11T09:08:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-11T09:11:00Z")));

        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-12T09:04:00Z")));
        assertTrue(calendar.isEventPresent(Instant.parse("2019-09-12T09:08:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-12T09:11:00Z")));

        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-13T09:04:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-13T09:08:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-13T09:11:00Z")));

        // Test in CEST (UTC+2)
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-14T07:59:00Z")));
        assertTrue(calendar.isEventPresent(Instant.parse("2019-09-14T08:03:00Z")));
        assertFalse(calendar.isEventPresent(Instant.parse("2019-09-14T09:01:00Z")));

        // Test Series with cancelled event by Davdroid
        assertFalse(calendar2.isEventPresent(Instant.parse("2019-11-03T09:55:00Z")));
        assertTrue(calendar2.isEventPresent(Instant.parse("2019-11-03T10:01:00Z")));
        assertFalse(calendar2.isEventPresent(Instant.parse("2019-11-03T13:00:00Z")));

        assertFalse(calendar2.isEventPresent(Instant.parse("2019-11-24T09:55:00Z")));
        assertFalse(calendar2.isEventPresent(Instant.parse("2019-11-24T10:01:00Z")));
        assertFalse(calendar2.isEventPresent(Instant.parse("2019-11-24T13:00:00Z")));
    }

    /**
     * This test relies on a working isEventPresent and assumes the calculation
     * of recurrence is done the same way.
     */
    @Test
    public void testGetCurrentEvent() {
        Event currentEvent = calendar.getCurrentEvent(Instant.parse("2019-09-10T09:07:00Z"));
        assertNotNull(currentEvent);
        assertTrue("Test Series in UTC".contentEquals(currentEvent.title));
        assertEquals(0, Instant.parse("2019-09-10T09:05:00Z").compareTo(currentEvent.start));
        assertEquals(0, Instant.parse("2019-09-10T09:10:00Z").compareTo(currentEvent.end));

        Event nonExistingEvent = calendar.getCurrentEvent(Instant.parse("2019-09-09T09:07:00Z"));
        assertNull(nonExistingEvent);
    }

    /**
     * This test relies on a working isEventPresent and assumes the calculation
     * of recurrence is done the same way.
     */
    @Test
    public void testGetNextEvent() {
        // positive case: next event of series
        Event nextEventOfSeries = calendar.getNextEvent(Instant.parse("2019-09-10T09:07:00Z"));
        assertNotNull(nextEventOfSeries);
        assertTrue("Test Series in UTC".contentEquals(nextEventOfSeries.title));
        assertEquals(0, Instant.parse("2019-09-11T09:05:00Z").compareTo(nextEventOfSeries.start));
        assertEquals(0, Instant.parse("2019-09-11T09:10:00Z").compareTo(nextEventOfSeries.end));

        // positive case: next event after series
        Event nextEventOutsideSeries = calendar.getNextEvent(Instant.parse("2019-09-12T09:07:00Z"));
        assertNotNull(nextEventOutsideSeries);
        assertTrue("Test Event in UTC+2".contentEquals(nextEventOutsideSeries.title));
        assertEquals(0, Instant.parse("2019-09-14T08:00:00Z").compareTo(nextEventOutsideSeries.start));
        assertEquals(0, Instant.parse("2019-09-14T09:00:00Z").compareTo(nextEventOutsideSeries.end));

        // positive case: next event should be also set if currently none is present
        Event nextEventIndependent = calendar.getNextEvent(Instant.parse("2019-09-13T09:07:00Z"));
        assertTrue(nextEventOutsideSeries.equals(nextEventIndependent));

        // negative case: after last event there is no next
        Event nonExistingEvent = calendar.getNextEvent(Instant.parse("2019-09-14T12:00:00Z"));
        assertNull(nonExistingEvent);

        // mixed case: cancelled events also not show up as next
        Event nextEventAfterCancelled = calendar2.getNextEvent(Instant.parse("2019-11-24T09:55:00Z"));
        assertNotNull(nextEventAfterCancelled);
        assertEquals(0, Instant.parse("2019-12-01T10:00:00Z").compareTo(nextEventAfterCancelled.start));
    }

    /**
     * This test checks for Events that have just begun or ended, and if so if checks for Command Tags
     * and checks if these tags are valid
     */
    @Test
    public void testCommandTagCode() {
        List<Event> events = null;
        int eventCount = 2;
        int tagsPerEvent = 8;
        
        // test just begun events: first in the series
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T15:55:00Z"), Instant.parse("2020-01-28T16:05:00Z"));
        assertNotNull(events);
        assertEquals(events.size(), eventCount);
        for (Event event : events) {
            List<CommandTag> cmdTags = event.commandTags;
            assertEquals(cmdTags.size(), tagsPerEvent);
            int beginTags = 0;
            for (CommandTag cmdTag : cmdTags) { 
                if (cmdTag.tagType == CommandTagType.BEGIN) {
                    assertTrue(cmdTag.isAuthorized("abc"));
                    assertTrue(cmdTag.itemName.matches("^\\w+$"));
                    assertTrue(cmdTag.getCommand() != null);
                    beginTags++;
                }
            }
            assertEquals(beginTags, tagsPerEvent / 2);
        }

        // test just begun events: third in the series
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-30T15:55:00Z"), Instant.parse("2020-01-30T16:05:00Z"));
        assertNotNull(events);
        assertEquals(events.size(), eventCount);
        for (Event event : events) {
            List<CommandTag> cmdTags = event.commandTags;
            assertEquals(cmdTags.size(), tagsPerEvent);
            int beginTags = 0;
            for (CommandTag cmdTag : cmdTags) { 
                if (cmdTag.tagType == CommandTagType.BEGIN) {
                    assertTrue(cmdTag.isAuthorized("abc"));
                    assertTrue(cmdTag.itemName.matches("^\\w+$"));
                    assertTrue(cmdTag.getCommand() != null);
                    beginTags++;
                }
            }
            assertEquals(beginTags, tagsPerEvent / 2);
        }

        // test outside of window: begun events, too early 
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T15:50:00Z"), Instant.parse("2020-01-28T15:55:00Z"));
        assertNull(events);

        // test outside of window: begun events, too late 
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T16:05:00Z"), Instant.parse("2020-01-28T16:10:00Z"));
        assertNull(events);

        // test just ended events: first in the series
        events = calendar3.getJustEndedEvents(Instant.parse("2020-01-28T16:25:00Z"), Instant.parse("2020-01-28T16:35:00Z"));
        assertNotNull(events);
        assertEquals(events.size(), eventCount);
        for (Event event : events) {
            List<CommandTag> cmdTags = event.commandTags;
            assertEquals(cmdTags.size(), tagsPerEvent);
            int endTags = 0;
            for (CommandTag cmdTag : cmdTags) { 
                if (cmdTag.tagType == CommandTagType.END) {
                    assertTrue(cmdTag.isAuthorized("abc"));
                    assertTrue(cmdTag.itemName.matches("^\\w+$"));
                    assertTrue(cmdTag.getCommand() != null);
                    endTags++;
                }
            }
            assertEquals(endTags, tagsPerEvent / 2);
        }

        // test outside of window: ended events, too early 
        events = calendar3.getJustEndedEvents(Instant.parse("2020-01-28T16:20:00Z"), Instant.parse("2020-01-28T16:25:00Z"));
        assertNull(events);

        // test outside of window: ended events, too late 
        events = calendar3.getJustEndedEvents(Instant.parse("2020-01-28T16:35:00Z"), Instant.parse("2020-01-28T16:40:00Z"));
        assertNull(events);
    }

}
