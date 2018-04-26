/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm.internal.config;

/**
 * The alarm controller configuration.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class AlarmControllerConfig {
    private int entryTime;
    private int exitTime;
    private int passthroughTime;
    private int alarmDelay;
    private int alarmZones;

    /**
     * Returns the entry time.
     */
    public int getEntryTime() {
        return entryTime;
    }

    /**
     * Sets the entry time.
     */
    public void setEntryTime(int entryTime) {
        this.entryTime = entryTime;
    }

    /**
     * Returns the exit time.
     */
    public int getExitTime() {
        return exitTime;
    }

    /**
     * Sets the exit time.
     */
    public void setExitTime(int exitTime) {
        this.exitTime = exitTime;
    }

    /**
     * Get passthrough time.
     */
    public int getPassthroughTime() {
        return passthroughTime;
    }

    /**
     * Set passthrough time.
     */
    public void setPassthroughTime(int passthroughTime) {
        this.passthroughTime = passthroughTime;
    }

    /**
     * Returns the alarm delay.
     */
    public int getAlarmDelay() {
        return alarmDelay;
    }

    /**
     * Sets the alarm delay.
     */
    public void setAlarmDelay(int alarmDelay) {
        this.alarmDelay = alarmDelay;
    }

    /**
     * Returns the number of alarm zones.
     */
    public int getAlarmZones() {
        return alarmZones;
    }

    /**
     * Sets the number of alarm zones.
     */
    public void setAlarmZones(int alarmZones) {
        this.alarmZones = alarmZones;
    }
}
