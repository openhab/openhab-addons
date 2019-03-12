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
 * Class for WSIntegerValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSIntegerValue extends WSResourceValue {

    protected int integer;
    protected int maximumValue;
    protected int minimumValue;

    public WSIntegerValue() {
    }

    public WSIntegerValue(int resourceID) {
        super(resourceID);
    }

    public WSIntegerValue(int resourceID, int value, int minimumValue, int maximumValue) {
        super(resourceID);
        this.integer = value;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

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
