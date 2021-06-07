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

    public static long getSetpointEndTimeFromNow(int duration_min) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, duration_min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    private static long getProgramBaseTime() {
        Calendar mondayZero = Calendar.getInstance();
        mondayZero.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        mondayZero.set(Calendar.HOUR_OF_DAY, 0);
        mondayZero.set(Calendar.MINUTE, 0);
        mondayZero.set(Calendar.SECOND, 0);
        return mondayZero.getTimeInMillis() / 1000;
    }

    private static long getTimeDiff() {
        Calendar now = Calendar.getInstance();
        long diff = (now.getTimeInMillis() / 1000 - getProgramBaseTime()) / 60;
        return diff;
    }

    public static long getNextProgramTime(@Nullable NAThermProgram activeProgram) {
        long diff = getTimeDiff();
        if (activeProgram != null) {
            // By default we'll use the first slot of next week - this case will be true if
            // we are in the last schedule of the week so below loop will not exit by break
            List<NATimeTableItem> timetable = activeProgram.getTimetable();
            int next = timetable.get(0).getMOffset() + (7 * 24 * 60);
            for (NATimeTableItem timeTable : timetable) {
                if (timeTable.getMOffset() > diff) {
                    next = timeTable.getMOffset();
                    break;
                }
            }
            return next * 60 + getProgramBaseTime();
        }
        return -1;
    }

    public static @Nullable NATimeTableItem getCurrentProgramMode(@Nullable NAThermProgram activeProgram) {
        if (activeProgram != null) {
            long diff = getTimeDiff();
            return activeProgram.getTimetable().stream().filter(t -> t.getMOffset() < diff)
                    .reduce((first, second) -> second).orElse(null);
        }
        return null;
    }
}
