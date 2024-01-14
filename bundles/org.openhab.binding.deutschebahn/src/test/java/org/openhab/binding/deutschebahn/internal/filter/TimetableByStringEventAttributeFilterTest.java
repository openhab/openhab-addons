/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal.filter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.deutschebahn.internal.EventAttribute;
import org.openhab.binding.deutschebahn.internal.EventAttributeSelection;
import org.openhab.binding.deutschebahn.internal.EventType;
import org.openhab.binding.deutschebahn.internal.TripLabelAttribute;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Event;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TripLabel;

/**
 * Tests for {@link TimetableStopByStringEventAttributeFilter}
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public final class TimetableByStringEventAttributeFilterTest {

    @Test
    public void testFilterTripLabelAttribute() {
        final TimetableStopByStringEventAttributeFilter filter = new TimetableStopByStringEventAttributeFilter(
                TripLabelAttribute.C, Pattern.compile("IC.*"));
        final TimetableStop stop = new TimetableStop();

        // TripLabel is not set -> does not match
        assertFalse(filter.test(stop));

        final TripLabel label = new TripLabel();
        stop.setTl(label);

        // Attribute is not set -> does not match
        assertFalse(filter.test(stop));

        // Set attribute -> matches depending on value
        label.setC("RE");
        assertFalse(filter.test(stop));
        label.setC("ICE");
        assertTrue(filter.test(stop));
        label.setC("IC");
        assertTrue(filter.test(stop));
    }

    @Test
    public void testFilterEventAttribute() {
        final EventAttributeSelection eventAttribute = new EventAttributeSelection(EventType.DEPARTURE,
                EventAttribute.L);
        final TimetableStopByStringEventAttributeFilter filter = new TimetableStopByStringEventAttributeFilter(
                eventAttribute, Pattern.compile("RE.*"));
        final TimetableStop stop = new TimetableStop();

        // Event is not set -> does not match
        assertFalse(filter.test(stop));

        Event event = new Event();
        stop.setDp(event);

        // Attribute is not set -> does not match
        assertFalse(filter.test(stop));

        // Set attribute -> matches depending on value
        event.setL("S5");
        assertFalse(filter.test(stop));
        event.setL("5");
        assertFalse(filter.test(stop));
        event.setL("RE60");
        assertTrue(filter.test(stop));

        // Set wrong event
        stop.setAr(event);
        stop.setDp(null);
        assertFalse(filter.test(stop));
    }

    @Test
    public void testFilterEventAttributeList() {
        final EventAttributeSelection eventAttribute = new EventAttributeSelection(EventType.DEPARTURE,
                EventAttribute.PPTH);
        final TimetableStopByStringEventAttributeFilter filter = new TimetableStopByStringEventAttributeFilter(
                eventAttribute, Pattern.compile("Hannover.*"));
        final TimetableStop stop = new TimetableStop();
        Event event = new Event();
        stop.setDp(event);

        event.setPpth("Hannover Hbf|Hannover-Kleefeld|Hannover Karl-Wiechert-Allee|Hannover Anderten-Misburg|Ahlten");
        assertTrue(filter.test(stop));
        event.setPpth(
                "Ahlten|Hannover Hbf|Hannover-Kleefeld|Hannover Karl-Wiechert-Allee|Hannover Anderten-Misburg|Ahlten");
        assertTrue(filter.test(stop));
        event.setPpth(
                "Wolfsburg Hbf|Fallersleben|Calberlah|Gifhorn|Leiferde(b Gifhorn)|Meinersen|Dedenhausen|Dollbergen|Immensen-Arpke");
        assertFalse(filter.test(stop));
    }
}
