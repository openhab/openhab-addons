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

import org.openhab.binding.publicholiday.internal.publicholiday.definitions.EasternRelatedHolidayDef;

/**
 * Eastern related holiday representation
 * 
 * @author Martin Güthle - Initial contribution
 */
public class EasternRelatedHoliday extends Holiday {

    private final int offset;

    public EasternRelatedHoliday(EasternRelatedHolidayDef day) {
        super(day.name);
        this.offset = day.offset;
    }

    private LocalDate easterDate() {
        int year = getCurrentYear();
        int i = year % 19;
        int j = year / 100;
        int k = year % 100;

        int l = (19 * i + j - (j / 4) - ((j - ((j + 8) / 25) + 1) / 3) + 15) % 30;
        int m = (32 + 2 * (j % 4) + 2 * (k / 4) - l - (k % 4)) % 7;
        int n = l + m - 7 * ((i + 11 * l + 22 * m) / 451) + 114;

        int month = n / 31;
        int dayOfMonth = (n % 31) + 1;
        return LocalDate.of(year, month, dayOfMonth);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return easterDate().plusDays(offset).equals(date);
    }

    /**
     * Needed for testing purpose
     *
     * @return The current year value
     */
    protected int getCurrentYear() {
        return LocalDate.now().getYear();
    }
}
