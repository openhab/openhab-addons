/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Transport class for a single event.
 *
 * @author Michael Wodniok - Initial contribution
 */
@NonNullByDefault
public class Event {
    public String title;
    public Instant start;
    public Instant end;

    public Event(String title, Instant start, Instant end) {
        this.title = title;
        this.start = start;
        this.end = end;
    }

    @Override
    public int hashCode() {
        return title.hashCode() + start.hashCode() + end.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        Event otherEvent = (Event) other;
        return (this.title.equals(otherEvent.title) && this.start.equals(otherEvent.start)
                && this.end.equals(otherEvent.end));
    }
}
