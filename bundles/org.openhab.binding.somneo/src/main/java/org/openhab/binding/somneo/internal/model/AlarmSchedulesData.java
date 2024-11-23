/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.somneo.internal.model;

import java.time.LocalTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the audio state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
public class AlarmSchedulesData {

    /**
     * None = 0,
     * Monday = 2,
     * Tuesday = 4,
     * Wednesday = 8,
     * Thursday = 16,
     * Friday = 32,
     * Saturday = 64,
     * Sunday = 128
     */
    @SerializedName("daynm")
    private List<Integer> repeatDays;

    @SerializedName("almhr")
    private List<Integer> hours;

    @SerializedName("almmn")
    private List<Integer> minutes;

    public @NonNull State getRepeatDayState(int position) {
        final List<Integer> repeatDays = this.repeatDays;
        if (repeatDays == null) {
            return UnDefType.NULL;
        }
        final Integer repeatDay = repeatDays.get(position - 1);
        if (repeatDay == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(repeatDay);
    }

    public LocalTime getAlarmTime(int position) {
        final List<Integer> hours = this.hours;
        if (hours == null) {
            return null;
        }
        final List<Integer> minutes = this.minutes;
        if (minutes == null) {
            return null;
        }
        final Integer hour = hours.get(position - 1);
        final Integer minute = minutes.get(position - 1);
        return LocalTime.of(hour, minute);
    }

    public @NonNull State getAlarmTimeState(int position) {
        final LocalTime time = getAlarmTime(position);
        if (time == null) {
            return UnDefType.NULL;
        }
        final String alarmTimeString = String.format("%02d:%02d:00", time.getHour(), time.getMinute());
        if (alarmTimeString == null) {
            return UnDefType.NULL;
        }
        return DateTimeType.valueOf(alarmTimeString);
    }
}
