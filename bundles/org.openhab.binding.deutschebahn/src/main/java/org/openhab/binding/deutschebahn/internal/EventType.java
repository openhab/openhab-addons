/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Event;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;

/**
 * Type of an {@link Event} within a {@link TimetableStop}.
 * 
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public enum EventType {

    /**
     * Selects the Arrival-Element (i.e. ar).
     */
    ARRIVAL(TimetableStop::getAr, TimetableStop::getDp),

    /**
     * Selects the departure element (i.e. dp).
     */
    DEPARTURE(TimetableStop::getDp, TimetableStop::getAr);

    private final Function<TimetableStop, @Nullable Event> getter;
    private final Function<TimetableStop, @Nullable Event> oppositeGetter;

    private EventType(Function<TimetableStop, @Nullable Event> getter,
            Function<TimetableStop, @Nullable Event> oppositeGetter) {
        this.getter = getter;
        this.oppositeGetter = oppositeGetter;
    }

    /**
     * Returns the selected event from the given {@link TimetableStop}.
     */
    @Nullable
    public final Event getEvent(TimetableStop stop) {
        return this.getter.apply(stop);
    }

    /**
     * Returns the opposite event from the given {@link TimetableStop}.
     */
    @Nullable
    public final Event getOppositeEvent(TimetableStop stop) {
        return this.oppositeGetter.apply(stop);
    }
}
