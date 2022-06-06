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
package org.openhab.binding.publicholiday.internal.publicholiday;

import java.time.LocalDate;

/**
 * Basic holiday related handling
 * 
 * @author Martin Güthle - Initial contribution
 */
public abstract class Holiday {

    private final String name;

    public Holiday(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Determine if the given day is a holiday
     *
     * @param date The current day
     * @return True if the given day is a holiday
     */
    public abstract boolean isHoliday(LocalDate date);

    /**
     * Determine if the day after the given day is a holiday
     *
     * @param date The current day
     * @return True if the day after the given day is a holiday
     */
    public final boolean isDayBeforeHoliday(LocalDate date) {
        return isHoliday(date.plusDays(1));
    }
}
