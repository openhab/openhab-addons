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
package org.openhab.binding.viessmann.internal.dto.schedule;

import java.util.List;

/**
 * The {@link ScheduleDTO} provides the schedule of a scheduled feature
 *
 * @author Ronny Grun - Initial contribution
 */
public class ScheduleDTO {

    private List<DaySchedule> mon = null;
    private List<DaySchedule> tue = null;
    private List<DaySchedule> wed = null;
    private List<DaySchedule> thu = null;
    private List<DaySchedule> fri = null;
    private List<DaySchedule> sat = null;
    private List<DaySchedule> sun = null;

    public List<DaySchedule> getMon() {
        return mon;
    }

    public void setMon(List<DaySchedule> mon) {
        this.mon = mon;
    }

    public List<DaySchedule> getTue() {
        return tue;
    }

    public void setTue(List<DaySchedule> tue) {
        this.tue = tue;
    }

    public List<DaySchedule> getWed() {
        return wed;
    }

    public void setWed(List<DaySchedule> wed) {
        this.wed = wed;
    }

    public List<DaySchedule> getThu() {
        return thu;
    }

    public void setThu(List<DaySchedule> thu) {
        this.thu = thu;
    }

    public List<DaySchedule> getFri() {
        return fri;
    }

    public void setFri(List<DaySchedule> fri) {
        this.fri = fri;
    }

    public List<DaySchedule> getSat() {
        return sat;
    }

    public void setSat(List<DaySchedule> sat) {
        this.sat = sat;
    }

    public List<DaySchedule> getSun() {
        return sun;
    }

    public void setSun(List<DaySchedule> sun) {
        this.sun = sun;
    }
}
