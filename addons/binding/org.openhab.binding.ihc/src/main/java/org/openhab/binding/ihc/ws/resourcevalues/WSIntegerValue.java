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
 * Java class for WSIntegerValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSIntegerValue extends WSResourceValue {

    protected int integer;
    protected int maximumValue;
    protected int minimumValue;

    /**
     * Gets the value of the integer property.
     *
     */
    public int getInteger() {
        return integer;
    }

    /**
     * Sets the value of the integer property.
     *
     */
    public void setInteger(int value) {
        this.integer = value;
    }

    /**
     * Gets the value of the maximumValue property.
     *
     */
    public int getMaximumValue() {
        return maximumValue;
    }

    /**
     * Sets the value of the maximumValue property.
     *
     */
    public void setMaximumValue(int value) {
        this.maximumValue = value;
    }

    /**
     * Gets the value of the minimumValue property.
     *
     */
    public int getMinimumValue() {
        return minimumValue;
    }

    /**
     * Sets the value of the minimumValue property.
     *
     */
    public void setMinimumValue(int value) {
        this.minimumValue = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, value=%d, min=%d, max=%d]", super.resourceID, integer, minimumValue,
                maximumValue);
    }
}
