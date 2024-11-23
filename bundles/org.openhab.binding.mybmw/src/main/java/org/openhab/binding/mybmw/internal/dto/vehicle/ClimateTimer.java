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
package org.openhab.binding.mybmw.internal.dto.vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * derived from the API responses
 * 
 * @author Martin Grassl - initial contribution
 */
public class ClimateTimer {
    private boolean isWeeklyTimer = false; // true,
    private String timerAction = ""; // DEACTIVATE,
    private List<String> timerWeekDays = new ArrayList<>(); // [ MONDAY ]
    private DepartureTime departureTime = new DepartureTime();

    public boolean isWeeklyTimer() {
        return isWeeklyTimer;
    }

    public void setWeeklyTimer(boolean isWeeklyTimer) {
        this.isWeeklyTimer = isWeeklyTimer;
    }

    public String getTimerAction() {
        return timerAction;
    }

    public void setTimerAction(String timerAction) {
        this.timerAction = timerAction;
    }

    public List<String> getTimerWeekDays() {
        return timerWeekDays;
    }

    public void setTimerWeekDays(List<String> timerWeekDays) {
        this.timerWeekDays = timerWeekDays;
    }

    public DepartureTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(DepartureTime departureTime) {
        this.departureTime = departureTime;
    }

    @Override
    public String toString() {
        return "ClimateTimer [isWeeklyTimer=" + isWeeklyTimer + ", timerAction=" + timerAction + ", timerWeekDays="
                + timerWeekDays + ", departureTime=" + departureTime + "]";
    }
}
