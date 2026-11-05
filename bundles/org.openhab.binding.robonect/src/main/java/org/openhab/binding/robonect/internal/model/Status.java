/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Object holding information from the status section of the mowers status response.
 *
 * @author Marco Meyer - Initial contribution
 */
public class Status {

    private @Nullable Integer battery;
    private @Nullable Integer duration;
    private @Nullable Integer hours;
    private MowerStatus status;
    private MowerMode mode;
    private @Nullable Boolean stopped;
    private @Nullable Boolean home;
    private @Nullable Integer prevent;
    private @Nullable Integer distance;

    /**
     * Returns the battery level in percent. (0-100).
     *
     * @return - the battery level in percent. (0-100)
     */
    public @Nullable Integer getBattery() {
        return battery;
    }

    /**
     * Returns the duration in seconds the mower is already in the current {@link #status}.
     *
     * @return - The duration in seconds the mower is already in the current {@link #status}.
     */
    public @Nullable Integer getDuration() {
        return duration;
    }

    /**
     * Returns the distance from the charging station (in case it searches the remote starting point).
     *
     * @return - The distance from the charging station (in case it searches the remote starting point)
     */
    public @Nullable Integer getDistance() {
        return distance;
    }

    /**
     * Returns the hours the mower was in use so far.
     *
     * @return - The hours the mower was in use so far.
     */
    public @Nullable Integer getHours() {
        return hours;
    }

    /**
     * Returns the status the mower is currently in; see {@link MowerStatus} for details.
     *
     * @return - The status the mower is currently in; see {@link MowerStatus} for details.
     */
    public MowerStatus getStatus() {
        return status;
    }

    /**
     * Reports whether the mower is currently stopped.
     *
     * @return {@code true} if the mower is currently stopped, {@code false} otherwise
     */
    public @Nullable Boolean isStopped() {
        return stopped;
    }

    /**
     * Reports whether the mower reports being at home.
     *
     * @return whether the mower reports being at home
     */
    public @Nullable Boolean getHome() {
        return home;
    }

    /**
     * Returns the reported prevention code.
     *
     * @return the reported prevention code
     */
    public @Nullable Integer getPrevent() {
        return prevent;
    }

    /**
     * Returns the mode the mower is currently in. See {@link MowerMode} for details.
     *
     * @return - The mode the mower is currently in. See {@link MowerMode} for details.
     */
    public MowerMode getMode() {
        return mode;
    }

    /**
     * Sets the battery.
     *
     * @param battery the battery
     */
    public void setBattery(int battery) {
        this.battery = battery;
    }

    /**
     * Sets the duration.
     *
     * @param duration the duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Sets the distance.
     *
     * @param distance the distance
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * Sets the hours.
     *
     * @param hours the hours
     */
    public void setHours(int hours) {
        this.hours = hours;
    }

    /**
     * Sets the status.
     *
     * @param status the status
     */
    public void setStatus(MowerStatus status) {
        this.status = status;
    }

    /**
     * Sets the mode.
     *
     * @param mode the mode
     */
    public void setMode(MowerMode mode) {
        this.mode = mode;
    }

    /**
     * Sets the stopped.
     *
     * @param stopped the stopped
     */
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
