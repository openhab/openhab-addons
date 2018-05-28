/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.resourcevalues;

/**
 * Java class for WSDateValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSDateValue extends WSResourceValue {

    protected byte month;
    protected short year;
    protected byte day;

    /**
     * Gets the value of the month property.
     *
     */
    public byte getMonth() {
        return month;
    }

    /**
     * Sets the value of the month property.
     *
     */
    public void setMonth(byte value) {
        this.month = value;
    }

    /**
     * Gets the value of the year property.
     *
     */
    public short getYear() {
        return year;
    }

    /**
     * Sets the value of the year property.
     *
     */
    public void setYear(short value) {
        this.year = value;
    }

    /**
     * Gets the value of the day property.
     *
     */
    public byte getDay() {
        return day;
    }

    /**
     * Sets the value of the day property.
     *
     */
    public void setDay(byte value) {
        this.day = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, year=%d, month=%d, day=%d]", super.resourceID, year, month, day);
    }
}
