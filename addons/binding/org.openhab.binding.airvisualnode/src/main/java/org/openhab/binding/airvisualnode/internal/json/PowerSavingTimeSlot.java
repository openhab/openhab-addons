/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airvisualnode.internal.json;

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
