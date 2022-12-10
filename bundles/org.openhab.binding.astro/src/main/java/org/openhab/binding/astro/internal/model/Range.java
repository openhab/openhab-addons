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
package org.openhab.binding.astro.internal.model;

import static org.openhab.core.library.unit.MetricPrefix.MILLI;

import java.util.Calendar;

import javax.measure.quantity.Time;

import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Range class which holds a start and an end calendar object.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
public class Range {

    private Calendar start;
    private Calendar end;

    public Range() {
    }

    public Range(Calendar start, Calendar end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the start of the range.
     */
    public Calendar getStart() {
        return start;
    }

    /**
     * Returns the end of the range.
     */
    public Calendar getEnd() {
        return end;
    }

    /**
     * Returns the duration in minutes.
     */
    public QuantityType<Time> getDuration() {
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
        if (start == null && end == null) {
            return false;
        }
        long matchStart = start != null ? start.getTimeInMillis()
                : DateTimeUtils.truncateToMidnight(cal).getTimeInMillis();
        long matchEnd = end != null ? end.getTimeInMillis() : DateTimeUtils.endOfDayDate(cal).getTimeInMillis();
        return cal.getTimeInMillis() >= matchStart && cal.getTimeInMillis() < matchEnd;
    }
}
