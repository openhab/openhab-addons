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
package org.openhab.binding.venstarthermostat.internal.dto;

/**
 * The {@link VenstarInfoData} represents a thermostat state from the REST API.
 *
 * @author William Welliver - Initial contribution
 * @author Matthew Davies - added VenstarAwayMode to include away mode in binding
 */
public class VenstarInfoData {
    private double cooltemp;
    private double heattemp;

    private VenstarSystemState state;
    private VenstarSystemMode mode;
    private VenstarAwayMode away;
    private VenstarFanMode fan;
    private VenstarFanState fanstate;
    private VenstarScheduleMode schedule;
    private VenstarSchedulePart schedulepart;
    private int tempunits;

    public VenstarInfoData() {
    }

    public VenstarInfoData(double cooltemp, double heattemp, VenstarSystemState state, VenstarSystemMode mode,
            VenstarAwayMode away, VenstarFanMode fan, VenstarFanState fanstate, VenstarScheduleMode schedule,
            VenstarSchedulePart schedulepart) {
        this.cooltemp = cooltemp;
        this.heattemp = heattemp;
        this.state = state;
        this.mode = mode;
        this.away = away;
        this.fan = fan;
        this.fanstate = fanstate;
        this.schedule = schedule;
        this.schedulepart = schedulepart;
    }

    public double getCooltemp() {
        return cooltemp;
    }

    public void setCooltemp(double cooltemp) {
        this.cooltemp = cooltemp;
    }

    public double getHeattemp() {
        return heattemp;
    }

    public void setHeattemp(double heattemp) {
        this.heattemp = heattemp;
    }

    public VenstarSystemState getSystemState() {
        return state;
    }

    public void setSystemState(VenstarSystemState state) {
        this.state = state;
    }

    public VenstarSystemMode getSystemMode() {
        return mode;
    }

    public void setSystemMode(VenstarSystemMode mode) {
        this.mode = mode;
    }

    public int getTempunits() {
        return tempunits;
    }

    public void setTempunits(int tempunits) {
        this.tempunits = tempunits;
    }

    public VenstarAwayMode getAwayMode() {
        return away;
    }

    public void setAwayMode(VenstarAwayMode away) {
        this.away = away;
    }

    public VenstarFanMode getFanMode() {
        return fan;
    }

    public void setFanMode(VenstarFanMode fan) {
        this.fan = fan;
    }

    public VenstarFanState getFanState() {
        return fanstate;
    }

    public void setFanState(VenstarFanState fanstate) {
        this.fanstate = fanstate;
    }

    public VenstarScheduleMode getScheduleMode() {
        return schedule;
    }

    public void setScheduleMode(VenstarScheduleMode schedule) {
        this.schedule = schedule;
    }

    public VenstarSchedulePart getSchedulePart() {
        return schedulepart;
    }

    public void setSchedulePart(VenstarSchedulePart schedulepart) {
        this.schedulepart = schedulepart;
    }
}
