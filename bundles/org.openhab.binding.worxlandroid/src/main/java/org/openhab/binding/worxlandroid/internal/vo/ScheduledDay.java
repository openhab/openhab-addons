/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.worxlandroid.internal.vo;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ScheduledDay} holds data of the schedule details for a given day
 *
 * @author Nils - Initial contribution
 */
@NonNullByDefault
public class ScheduledDay {
    public static final ScheduledDay BLANK = new ScheduledDay("00:00", 0, false);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int DEFAULT_DURATION = 15;

    private LocalTime startTime = LocalTime.MIN;
    private boolean edgecut;
    private int durationRestore = DEFAULT_DURATION;
    private int duration;

    public ScheduledDay(String hhMm, int newDuration, boolean edgecut) {
        this.startTime = LocalTime.parse(hhMm);
        this.duration = newDuration;
        this.edgecut = edgecut;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(String hhMm) throws DateTimeParseException {
        startTime = LocalTime.parse(hhMm);
    }

    public void setStartTime(ZonedDateTime zdt) {
        startTime = zdt.toLocalTime();
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int newDuration) {
        if (newDuration == 0 && duration > 0) {
            durationRestore = duration;
        }

        duration = newDuration;
    }

    public boolean isEdgecut() {
        return edgecut;
    }

    public void setEdgecut(boolean edgecut) {
        this.edgecut = edgecut;
    }

    public boolean isEnabled() {
        return duration != 0;
    }

    public void setEnable(boolean newStatus) {
        setDuration(newStatus && duration == 0 ? durationRestore : 0);
    }

    public Object[] asArray() {
        return new Object[] { startTime.format(TIME_FORMAT), duration, edgecut ? 1 : 0 };
    }
}
