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
 * Class for WSWeekdayValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSWeekdayValue extends WSResourceValue {

    protected int weekdayNumber;

    public WSWeekdayValue() {
    }

    public WSWeekdayValue(int resourceID) {
        super(resourceID);
    }

    public WSWeekdayValue(int resourceID, int weekdayNumber) {
        super(resourceID);
        this.weekdayNumber = weekdayNumber;
    }

    /**
     * Gets the value of the weekdayNumber property.
     *
     */
    public int getWeekdayNumber() {
        return weekdayNumber;
    }

    /**
     * Sets the value of the weekdayNumber property.
     *
     */
    public void setWeekdayNumber(int value) {
        this.weekdayNumber = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, weekdayNumber=%d]", super.resourceID, weekdayNumber);
    }
}
