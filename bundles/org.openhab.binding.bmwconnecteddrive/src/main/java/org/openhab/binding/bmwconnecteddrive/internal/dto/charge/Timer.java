/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.charge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.Day;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileUtils;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link Timer} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - contributor
 */
public class Timer {
    public String departureTime;// ": "05:00",
    public boolean timerEnabled;// ": false,
    public List<String> weekdays;

    /**
     * "MONDAY",
     * "TUESDAY",
     * "WEDNESDAY",
     * "THURSDAY",
     * "FRIDAY"
     * ] '
     */
    public String getDays() {
        if (weekdays == null) {
            return Constants.UNDEF;
        }
        StringBuilder days = new StringBuilder();
        weekdays.forEach(entry -> {
            if (days.length() == 0) {
                days.append(Constants.DAYS.get(entry));
            } else {
                days.append(Constants.COMMA).append(Constants.DAYS.get(entry));
            }
        });
        return days.toString();
    }

    public void completeTimer() {
        if (weekdays == null) {
            weekdays = new ArrayList<>();
        }
        if (departureTime == null || departureTime.isEmpty()) {
            departureTime = Constants.NULL_TIME;
        }
    }

    public void dayOn(final Day day) {
        if (!weekdays.contains(day.name())) {
            weekdays.add(day.toString());
            Collections.sort(weekdays, new Comparator<>() {

                @Override
                public int compare(String day0, String day1) {
                    return Day.valueOf(day0).ordinal() - Day.valueOf(day1).ordinal();
                }
            });
        }
    }

    public void dayOff(final Day day) {
        weekdays.remove(day.name());
    }

    public boolean isDayOn(final Day day) {
        return weekdays.contains(day.name());
    }

    public void setDepartureMinute(int minute) {
        departureTime = ChargeProfileUtils.withMinute(departureTime, minute);
    }

    public void setDepartureHour(int hour) {
        departureTime = ChargeProfileUtils.withHour(departureTime, hour);
    }
}
