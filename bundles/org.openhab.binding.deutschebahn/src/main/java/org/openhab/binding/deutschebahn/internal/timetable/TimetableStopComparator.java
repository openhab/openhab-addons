/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal.timetable;

import java.util.Comparator;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.EventAttribute;
import org.openhab.binding.deutschebahn.internal.EventType;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Event;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;

/**
 * {@link Comparator} that sorts the {@link TimetableStop} according planned date and time.
 *
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public class TimetableStopComparator implements Comparator<TimetableStop> {

    private final EventType eventSelection;

    /**
     * Creates an new {@link TimetableStopComparator} that sorts {@link TimetableStop} according the Event selected
     * selected by the given {@link EventType}.
     */
    public TimetableStopComparator(EventType eventSelection) {
        this.eventSelection = eventSelection;
    }

    @Override
    public int compare(TimetableStop o1, TimetableStop o2) {
        return determinePlannedDate(o1, this.eventSelection).compareTo(determinePlannedDate(o2, this.eventSelection));
    }

    /**
     * Returns the planned-Time for the given {@link TimetableStop}.
     * The time will be returned from the {@link Event} selected by the given {@link EventType}.
     * If the {@link TimetableStop} has no according {@link Event} the other Event will be used.
     */
    private static Date determinePlannedDate(TimetableStop stop, EventType eventSelection) {
        Event selectedEvent = eventSelection.getEvent(stop);
        if (selectedEvent == null) {
            selectedEvent = eventSelection.getOppositeEvent(stop);
        }
        if (selectedEvent == null) {
            throw new AssertionError("one event is always present");
        }
        final Date value = EventAttribute.PT.getValue(selectedEvent);
        if (value == null) {
            throw new AssertionError("planned time cannot be null");
        }
        return value;
    }
}
