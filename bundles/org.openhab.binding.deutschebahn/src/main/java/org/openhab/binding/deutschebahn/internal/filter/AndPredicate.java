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
package org.openhab.binding.deutschebahn.internal.filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;

/**
 * And conjunction for {@link TimetableStopPredicate}.
 * 
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public final class AndPredicate implements TimetableStopPredicate {

    private final TimetableStopPredicate first;
    private final TimetableStopPredicate second;

    /**
     * Creates a new {@link AndPredicate}.
     */
    public AndPredicate(TimetableStopPredicate first, TimetableStopPredicate second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean test(TimetableStop t) {
        return first.test(t) && second.test(t);
    }

    /**
     * Returns first argument.
     */
    TimetableStopPredicate getFirst() {
        return first;
    }

    /**
     * Returns second argument.
     */
    TimetableStopPredicate getSecond() {
        return second;
    }
}
