/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSTimeValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSTimeValue extends WSResourceValue {

    protected int hours;
    protected int minutes;
    protected int seconds;

    public WSTimeValue() {
    }

    public WSTimeValue(int resourceID) {
        super(resourceID);
    }

    public WSTimeValue(int resourceID, int hours, int minutes, int seconds) {
        super(resourceID);
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    /**
     * Gets the value of the hours property.
     *
     */
    public int getHours() {
        return hours;
    }

    /**
     * Sets the value of the hours property.
     *
     */
    public void setHours(int value) {
        this.hours = value;
    }

    /**
     * Gets the value of the minutes property.
     *
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Sets the value of the minutes property.
     *
     */
    public void setMinutes(int value) {
        this.minutes = value;
    }

    /**
     * Gets the value of the seconds property.
     *
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Sets the value of the seconds property.
     *
     */
    public void setSeconds(int value) {
        this.seconds = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, hours=%d, minutes=%d, seconds=%d]", super.resourceID, hours, minutes,
                seconds);
    }
}
