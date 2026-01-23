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
package org.openhab.binding.myenergi.internal.model;

import java.time.DayOfWeek;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.exception.InvalidDataException;

/**
 * The {@link DaysOfWeekMap} is a class to hold a bit map of days within a week.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class DaysOfWeekMap {

    private static final String ALL_DAYS_OFF = "00000000";
    private static final Pattern MAP_PATTERN = Pattern.compile("^[0-1]+$");

    private String map = ALL_DAYS_OFF;

    public DaysOfWeekMap() {
    }

    public DaysOfWeekMap(DayOfWeek activeDay) {
        setDay(activeDay, true);
    }

    public DaysOfWeekMap(DayOfWeek[] activeDays) {
        setDays(activeDays, true);
    }

    public String getMapAsString() {
        return map;
    }

    public void setMap(String map) throws InvalidDataException {
        Matcher m = MAP_PATTERN.matcher(map);
        if (map.length() == 8 && m.matches()) {
            this.map = map;
        } else {
            throw new InvalidDataException("Invalid DaysOfWeekMap: " + map);
        }
    }

    public void setDay(DayOfWeek activeDay, boolean active) {
        StringBuilder newMap = new StringBuilder(map);
        newMap.setCharAt(activeDay.getValue(), active ? '1' : '0');
        map = newMap.toString();
    }

    public void setDays(DayOfWeek[] activeDays, boolean active) {
        char newChar = active ? '1' : '0';
        StringBuilder newMap = new StringBuilder(map);
        for (DayOfWeek activeDay : activeDays) {
            newMap.setCharAt(activeDay.getValue(), newChar);
        }
        map = newMap.toString();
    }
}
