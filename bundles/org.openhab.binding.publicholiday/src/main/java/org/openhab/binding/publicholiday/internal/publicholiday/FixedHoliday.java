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

import org.openhab.binding.publicholiday.internal.publicholiday.definitions.FixedDayDef;

/**
 * Fixed date holiday representation
 * 
 * @author Martin Güthle - Initial contribution
 */
public class FixedHoliday extends Holiday {

    private final int month;
    private final int day;

    public FixedHoliday(FixedDayDef fixedDay) {
        super(fixedDay.getName());
        this.month = fixedDay.getMonth();
        this.day = fixedDay.getDay();
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return this.month == date.getMonthValue() && this.day == date.getDayOfMonth();
    }
}
