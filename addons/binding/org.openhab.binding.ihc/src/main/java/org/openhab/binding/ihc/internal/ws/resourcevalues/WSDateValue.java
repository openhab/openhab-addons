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
 * Class for WSDateValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSDateValue extends WSResourceValue {

    protected short year;
    protected byte month;
    protected byte day;

    public WSDateValue() {
    }

    public WSDateValue(int resourceID) {
        super(resourceID);
    }

    public WSDateValue(int resourceID, short year, byte month, byte day) {
        super(resourceID);
        this.year = year;
        this.month = month;
        this.day = day;
    }

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
