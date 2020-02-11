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
import java.time.Instant;
import java.util.List;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
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
        calendar = new BiweeklyPresentableCalendar(new FileInputStream("src/test/resources/test.ics"));
        calendar2 = new BiweeklyPresentableCalendar(new FileInputStream("src/test/resources/test2.ics"));
        calendar3 = new BiweeklyPresentableCalendar(new FileInputStream("src/test/resources/test3.ics"));
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
     * This test checks for Events that have just begun or ended, and if so it checks for Command Tags
     * and checks if these tags are valid
     */
    @Test
    public void testCommandTagCode() {
        List<Event> events = null;
        int eventCount = 2;
        int tagsPerEvent = 8;

        // test just begun events: first in the series
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T15:55:00Z"),
                Instant.parse("2020-01-28T16:05:00Z"));
        assertNotNull(events);
        assertEquals(eventCount, events.size());
        for (Event event : events) {
            List<CommandTag> cmdTags = event.commandTags;
            assertEquals(tagsPerEvent, cmdTags.size());
            int beginTags = 0;
            for (CommandTag cmdTag : cmdTags) {
                if (cmdTag.getTagType() == CommandTagType.BEGIN) {
                    assertTrue(cmdTag.isAuthorized("abc"));
                    assertTrue(cmdTag.getItemName().matches("^\\w+$"));
                    assertTrue(cmdTag.getCommand() != null);
                    beginTags++;
                }
            }
            assertEquals(tagsPerEvent / 2, beginTags);
        }

        // test just begun events: third in the series
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-30T15:55:00Z"),
                Instant.parse("2020-01-30T16:05:00Z"));
        assertNotNull(events);
        assertEquals(eventCount, events.size());
        for (Event event : events) {
            List<CommandTag> cmdTags = event.commandTags;
            assertEquals(tagsPerEvent, cmdTags.size());
            int beginTags = 0;
            for (CommandTag cmdTag : cmdTags) {
                if (cmdTag.getTagType() == CommandTagType.BEGIN) {
                    assertTrue(cmdTag.isAuthorized("abc"));
                    assertTrue(cmdTag.getItemName().matches("^\\w+$"));
                    assertTrue(cmdTag.getCommand() != null);
                    beginTags++;
                }
            }
            assertEquals(tagsPerEvent / 2, beginTags);
        }

        // test outside of window: begun events, too early
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T15:50:00Z"),
                Instant.parse("2020-01-28T15:55:00Z"));
        assertNull(events);

        // test outside of window: begun events, too late
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T16:05:00Z"),
                Instant.parse("2020-01-28T16:10:00Z"));
        assertNull(events);

        // test just ended events: first in the series
        events = calendar3.getJustEndedEvents(Instant.parse("2020-01-28T16:25:00Z"),
                Instant.parse("2020-01-28T16:35:00Z"));
        assertNotNull(events);
        assertEquals(eventCount, events.size());
        for (Event event : events) {
            List<CommandTag> cmdTags = event.commandTags;
            assertEquals(tagsPerEvent, cmdTags.size());
            int endTags = 0;
            for (CommandTag cmdTag : cmdTags) {
                if (cmdTag.getTagType() == CommandTagType.END) {
                    assertTrue(cmdTag.isAuthorized("abc"));
                    assertTrue(cmdTag.getItemName().matches("^\\w+$"));
                    assertTrue(cmdTag.getCommand() != null);
                    endTags++;
                }
            }
            assertEquals(tagsPerEvent / 2, endTags);
        }

        // test outside of window: ended events, too early
        events = calendar3.getJustEndedEvents(Instant.parse("2020-01-28T16:20:00Z"),
                Instant.parse("2020-01-28T16:25:00Z"));
        assertNull(events);

        // test outside of window: ended events, too late
        events = calendar3.getJustEndedEvents(Instant.parse("2020-01-28T16:35:00Z"),
                Instant.parse("2020-01-28T16:40:00Z"));
        assertNull(events);

        // test a valid just begun event with both good and bad authorization codes
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T15:55:00Z"),
                Instant.parse("2020-01-28T16:05:00Z"));
        assertNotNull(events);
        assertTrue(events.size() > 0);
        assertTrue(events.get(0).commandTags.size() > 0);
        CommandTag cmd = events.get(0).commandTags.get(0);
        // accept correct, empty or null configuration codes
        assertTrue(cmd.isAuthorized("abc"));
        assertTrue(cmd.isAuthorized(""));
        assertTrue(cmd.isAuthorized(null));
        // reject incorrect configuration code
        assertFalse(cmd.isAuthorized("123"));

        // test tag syntax: Test Series #1
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T19:25:00Z"),
                Instant.parse("2020-01-28T19:35:00Z"));
        assertNotNull(events);
        assertEquals(1, events.size());
        List<CommandTag> cmdTags = events.get(0).commandTags;
        assertEquals(11, cmdTags.size());

        // BEGIN:Calendar_Test_Color:ON:abc
        assertEquals("Calendar_Test_Color", cmdTags.get(0).getItemName());
        assertTrue(cmdTags.get(0).isAuthorized("abc"));
        assertEquals(OnOffType.class, cmdTags.get(0).getCommand().getClass());

        // BEGIN:Calendar_Test_Contact:OPEN:abc
        assertEquals(OpenClosedType.class, cmdTags.get(1).getCommand().getClass());

        // BEGIN:Calendar_Test_Dimmer:ON:abc
        assertEquals(OnOffType.class, cmdTags.get(2).getCommand().getClass());

        // BEGIN:Calendar_Test_Number:12.3:abc
        assertEquals(QuantityType.class, cmdTags.get(3).getCommand().getClass());

        // BEGIN:Calendar_Test_Temperature:12.3°C:abc
        assertEquals(QuantityType.class, cmdTags.get(4).getCommand().getClass());

        // BEGIN:Calendar_Test_Pressure:12.3hPa:abc
        assertEquals(QuantityType.class, cmdTags.get(5).getCommand().getClass());

        // BEGIN:Calendar_Test_Speed:12.3m/s:abc
        assertEquals(QuantityType.class, cmdTags.get(6).getCommand().getClass());

        // BEGIN:Calendar_Test_Player:PLAY:abc
        assertEquals(PlayPauseType.class, cmdTags.get(7).getCommand().getClass());

        // BEGIN:Calendar_Test_RollerShutter:UP:abc
        assertEquals(UpDownType.class, cmdTags.get(8).getCommand().getClass());

        // BEGIN:Calendar_Test_String:Test Series #1:abc
        assertEquals(StringType.class, cmdTags.get(9).getCommand().getClass());

        // BEGIN:Calendar_Test_Switch:ON:abc
        assertEquals(OnOffType.class, cmdTags.get(10).getCommand().getClass());

        // test tag syntax: Test Series #4
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T20:10:00Z"),
                Instant.parse("2020-01-28T20:20:00Z"));
        assertNotNull(events);
        assertEquals(1, events.size());
        cmdTags = events.get(0).commandTags;
        assertEquals(11, cmdTags.size());

        // BEGIN:Calendar_Test_Color:0%:abc
        assertEquals(PercentType.class, cmdTags.get(0).getCommand().getClass());

        // BEGIN:Calendar_Test_Contact:CLOSED:abc
        assertEquals(OpenClosedType.class, cmdTags.get(1).getCommand().getClass());

        // BEGIN:Calendar_Test_Dimmer:0%:abc
        assertEquals(PercentType.class, cmdTags.get(2).getCommand().getClass());

        // BEGIN:Calendar_Test_Number:-12.3:abc
        assertEquals(QuantityType.class, cmdTags.get(3).getCommand().getClass());

        // BEGIN:Calendar_Test_Temperature:-12.3°C:abc
        assertEquals(QuantityType.class, cmdTags.get(4).getCommand().getClass());

        // BEGIN:Calendar_Test_Pressure:500mmHg:abc
        assertEquals(QuantityType.class, cmdTags.get(5).getCommand().getClass());

        // BEGIN:Calendar_Test_Speed:12300000mm/h:abc
        assertEquals(QuantityType.class, cmdTags.get(6).getCommand().getClass());

        // BEGIN:Calendar_Test_Player:REWIND:abc
        assertEquals(RewindFastforwardType.class, cmdTags.get(7).getCommand().getClass());

        // BEGIN:Calendar_Test_RollerShutter:100%:abc
        assertEquals(PercentType.class, cmdTags.get(8).getCommand().getClass());

        // BEGIN:Calendar_Test_String:Test Series #4:abc
        assertEquals(StringType.class, cmdTags.get(9).getCommand().getClass());

        // BEGIN:Calendar_Test_Switch:OFF:abc
        assertEquals(OnOffType.class, cmdTags.get(10).getCommand().getClass());

        // test tag syntax: Test Series #5
        events = calendar3.getJustBegunEvents(Instant.parse("2020-01-28T20:25:00Z"),
                Instant.parse("2020-01-28T20:35:00Z"));
        assertNotNull(events);
        assertEquals(1, events.size());
        cmdTags = events.get(0).commandTags;
        assertEquals(11, cmdTags.size());

        // BEGIN:Calendar_Test_Color:240,100,100:abc
        assertEquals(HSBType.class, cmdTags.get(0).getCommand().getClass());

        // BEGIN:Calendar_Test_Contact:OPEN:abc
        assertEquals(OpenClosedType.class, cmdTags.get(1).getCommand().getClass());

        // BEGIN:Calendar_Test_Dimmer:50%:abc
        assertEquals(PercentType.class, cmdTags.get(2).getCommand().getClass());

        // BEGIN:Calendar_Test_Number:-0:abc
        assertEquals(QuantityType.class, cmdTags.get(3).getCommand().getClass());

        // BEGIN:Calendar_Test_Temperature:0K:abc
        assertEquals(QuantityType.class, cmdTags.get(4).getCommand().getClass());

        // BEGIN:Calendar_Test_Pressure:12.3hPa:abc
        assertEquals(QuantityType.class, cmdTags.get(5).getCommand().getClass());

        // BEGIN:Calendar_Test_Speed:12.3km/h:abc
        assertEquals(QuantityType.class, cmdTags.get(6).getCommand().getClass());

        // BEGIN:Calendar_Test_Player:PLAY:abc
        assertEquals(PlayPauseType.class, cmdTags.get(7).getCommand().getClass());

        // BEGIN:Calendar_Test_RollerShutter:50%:abc
        assertEquals(PercentType.class, cmdTags.get(8).getCommand().getClass());

        // BEGIN:Calendar_Test_String:Test Series #5:abc
        assertEquals(StringType.class, cmdTags.get(9).getCommand().getClass());

        // BEGIN:Calendar_Test_Switch:ON:abc
        assertEquals(OnOffType.class, cmdTags.get(10).getCommand().getClass());

    }

}
