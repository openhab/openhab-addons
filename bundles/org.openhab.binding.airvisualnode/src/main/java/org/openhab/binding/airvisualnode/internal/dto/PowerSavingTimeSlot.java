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
package org.openhab.binding.airvisualnode.internal.dto;

/**
 * Power saving time slot data.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class PowerSavingTimeSlot {

    private int hourOff;
    private int hourOn;

    public PowerSavingTimeSlot(int hourOff, int hourOn) {
        this.hourOff = hourOff;
        this.hourOn = hourOn;
    }

    public int getHourOff() {
        return hourOff;
    }

    public void setHourOff(int hourOff) {
        this.hourOff = hourOff;
    }

    public int getHourOn() {
        return hourOn;
    }

    public void setHourOn(int hourOn) {
        this.hourOn = hourOn;
    }
}
