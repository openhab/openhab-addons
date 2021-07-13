/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.utils;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAThermProgram;
import org.openhab.binding.netatmo.internal.api.dto.NATimeTableItem;

/**
 * This class holds various Netatmo planning related functions
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoCalendarUtils {

    public static long setpointEndTimeFromNow(int duration_min) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, duration_min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    private static ZonedDateTime programBaseTimeZdt() {
        return ZonedDateTime.now().with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
    }

    private static long minutesSinceProgramBaseTime() {
        return ChronoUnit.MINUTES.between(programBaseTimeZdt(), ZonedDateTime.now());
    }

    public static @Nullable ZonedDateTime nextProgramTime(@Nullable NAThermProgram activeProgram) {
        ZonedDateTime result = null;
        // long diff = getTimeDiff();
        if (activeProgram != null) {
            long diff = minutesSinceProgramBaseTime();
            // By default we'll use the first slot of next week - this case will be true if
            // we are in the last schedule of the week so below loop will not exit by break
            List<NATimeTableItem> timetable = activeProgram.getTimetable();
            int next = timetable.get(0).getMinuteOffset() + (7 * 24 * 60);
            for (NATimeTableItem timeTable : timetable) {
                if (timeTable.getMinuteOffset() > diff) {
                    next = timeTable.getMinuteOffset();
                    break;
                }
            }
            // return next * 60 + getProgramBaseTime();
            result = programBaseTimeZdt().plusMinutes(next);
        }
        return result;
    }

    public static @Nullable NATimeTableItem currentProgramMode(@Nullable NAThermProgram activeProgram) {
        if (activeProgram != null) {
            long diff = minutesSinceProgramBaseTime();
            return activeProgram.getTimetable().stream().filter(t -> t.getMinuteOffset() < diff)
                    .reduce((first, second) -> second).orElse(null);
        }
        return null;
    }
}
