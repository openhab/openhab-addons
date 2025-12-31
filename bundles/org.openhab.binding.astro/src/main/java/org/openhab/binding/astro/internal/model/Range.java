/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.model;

import static org.openhab.core.library.unit.MetricPrefix.MILLI;

import java.util.Calendar;
import java.util.Comparator;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Range class which holds a start and an end calendar object.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class Range {

    private @Nullable Calendar start;
    private @Nullable Calendar end;

    public Range() {
    }

    public Range(@Nullable Calendar start, @Nullable Calendar end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the start of the range.
     */
    @Nullable
    public Calendar getStart() {
        return start;
    }

    /**
     * Returns the end of the range.
     */
    @Nullable
    public Calendar getEnd() {
        return end;
    }

    /**
     * Returns the duration in minutes.
     */
    @Nullable
    public QuantityType<Time> getDuration() {
        Calendar start = this.start;
        Calendar end = this.end;
        if (start == null || end == null) {
            return null;
        }
        if (start.after(end)) {
            return new QuantityType<>(0, Units.MINUTE);
        }
        return new QuantityType<>(end.getTimeInMillis() - start.getTimeInMillis(), MILLI(Units.SECOND))
                .toUnit(Units.MINUTE);
    }

    /**
     * Returns true, if the given calendar matches into the range.
     */
    public boolean matches(Calendar cal) {
        Calendar start = this.start;
        Calendar end = this.end;
        if (start == null && end == null) {
            return false;
        }
        long matchStart = start != null ? start.getTimeInMillis()
                : DateTimeUtils.truncateToMidnight(cal).getTimeInMillis();
        long matchEnd = end != null ? end.getTimeInMillis() : DateTimeUtils.endOfDayDate(cal).getTimeInMillis();
        return cal.getTimeInMillis() >= matchStart && cal.getTimeInMillis() < matchEnd;
    }

    private static Comparator<@Nullable Calendar> nullSafeCalendarComparator = (c1, c2) -> {
        if (c1 == null) {
            return (c2 == null) ? 0 : -1;
        }
        return c2 == null ? 1 : c1.compareTo(c2);
    };

    private static Comparator<Range> rangeComparator = Comparator.comparing(Range::getStart, nullSafeCalendarComparator)
            .thenComparing(Range::getEnd, nullSafeCalendarComparator);

    public int compareTo(Range that) {
        return rangeComparator.compare(this, that);
    }
}
