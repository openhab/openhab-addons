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
 * Class for WSFloatingPointValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSFloatingPointValue extends WSResourceValue {

    protected double maximumValue;
    protected double minimumValue;
    protected double floatingPointValue;

    public WSFloatingPointValue() {
    }

    public WSFloatingPointValue(int resourceID) {
        super(resourceID);
    }

    public WSFloatingPointValue(int resourceID, double value, double minimumValue, double maximumValue) {
        super(resourceID);
        this.floatingPointValue = value;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    /**
     * Gets the value of the maximumValue property.
     *
     */
    public double getMaximumValue() {
        return maximumValue;
    }

    /**
     * Sets the value of the maximumValue property.
     *
     */
    public void setMaximumValue(double value) {
        this.maximumValue = value;
    }

    /**
     * Gets the value of the minimumValue property.
     *
     */
    public double getMinimumValue() {
        return minimumValue;
    }

    /**
     * Sets the value of the minimumValue property.
     *
     */
    public void setMinimumValue(double value) {
        this.minimumValue = value;
    }

    /**
     * Gets the value of the floatingPointValue property.
     *
     */
    public double getFloatingPointValue() {
        return floatingPointValue;
    }

    /**
     * Sets the value of the floatingPointValue property.
     *
     */
    public void setFloatingPointValue(double value) {
        this.floatingPointValue = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, value=%.2f, min=%.2f, max=%.2f]", super.resourceID, floatingPointValue,
                minimumValue, maximumValue);
    }
}
