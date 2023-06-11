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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.filter.TimetableStopPredicate;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;

/**
 * Filter that selects {@link TimetableStop}, if they have a departure or an arrival element (or both).
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public enum TimetableStopFilter implements TimetableStopPredicate {

    /**
     * Selects all entries.
     */
    ALL {
        @Override
        public boolean test(TimetableStop t) {
            return true;
        }
    },

    /**
     * Selects only stops with a departure.
     */
    DEPARTURES {
        @Override
        public boolean test(TimetableStop t) {
            return t.getDp() != null;
        }
    },

    /**
     * Selects only stops with an arrival.
     */
    ARRIVALS {
        @Override
        public boolean test(TimetableStop t) {
            return t.getAr() != null;
        }
    }
}
