/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * Object holding information from the status section of the mowers status response.
 *
 * @author Marco Meyer - Initial contribution
 */
public class Status {

    private int battery;
    private int duration;
    private int hours;
    private MowerStatus status;
    private MowerMode mode;
    private boolean stopped;
    private int distance;

    /**
     * @return - the battery level in percent. (0-100)
     */
    public int getBattery() {
        return battery;
    }

    /**
     * @return - The duration in seconds the mower is already in the current {@link #status}.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return - The distance from the charging station (in case it searches the remote starting point)
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @return - The hours the mower was in use so far.
     */
    public int getHours() {
        return hours;
    }

    /**
     * @return - The status the mower is currently in. see {@link MowerStatus} for details.
     */
    public MowerStatus getStatus() {
        return status;
    }

    /**
     * @return - true if the mower is currentyl stopped, false otherwise.
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * @return - The mode the mower is currently in. See {@link MowerMode} for details.
     */
    public MowerMode getMode() {
        return mode;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setStatus(MowerStatus status) {
        this.status = status;
    }

    public void setMode(MowerMode mode) {
        this.mode = mode;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
