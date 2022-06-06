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
import java.util.List;

import org.openhab.binding.publicholiday.internal.PublicHolidayHandler;
import org.openhab.core.scheduler.SchedulerRunnable;

/**
 * Holiday calculation handling
 * 
 * @author Martin Güthle - Initial contribution
 */
public class HolidayJob implements SchedulerRunnable {

    private final PublicHolidayHandler handler;
    private final List<Holiday> holidays;

    public HolidayJob(PublicHolidayHandler handler, List<Holiday> holidays) {
        this.handler = handler;
        this.holidays = holidays;
    }

    @Override
    public void run() throws Exception {
        runImpl(LocalDate.now(), true, true);
    }

    private void runImpl(LocalDate today, boolean updCurDay, boolean updNextDay) {
        boolean isHoliday = false;
        boolean isNextDayHoliday = false;
        String holidayName = "none";
        for (var curHoliday : holidays) {
            if (updCurDay) {
                if (curHoliday.isHoliday(today)) {
                    isHoliday = true;
                    holidayName = curHoliday.getName();
                }
            }
            if (updNextDay) {
                isNextDayHoliday |= curHoliday.isDayBeforeHoliday(today);
            }
        }
        handler.updateValue(isHoliday, isNextDayHoliday, holidayName);
    }

    /**
     * Trigger the value update
     *
     * Meant to be called during the system startup and refresh command call
     * 
     * @param currentDay If set, the isPublicHoliday channel will be updated
     * @param nextDay If set, the istDayBeforePublicHoliday channel will be updated
     */
    public void refreshValues(boolean currentDay, boolean nextDay) {
        runImpl(LocalDate.now(), currentDay, nextDay);
    }
}
