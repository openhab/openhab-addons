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
 * Java class for WSSceneDimmerValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSceneDimmerValue extends WSResourceValue {

    protected int delayTime;
    protected int dimmerPercentage;
    protected int rampTime;

    public WSSceneDimmerValue() {
    }

    public WSSceneDimmerValue(int resourceID) {
        super(resourceID);
    }

    public WSSceneDimmerValue(int resourceID, int delayTime, int dimmerPercentage, int rampTime) {
        super(resourceID);
        this.delayTime = delayTime;
        this.dimmerPercentage = dimmerPercentage;
        this.rampTime = rampTime;
    }

    /**
     * Gets the value of the delayTime property.
     *
     */
    public int getDelayTime() {
        return delayTime;
    }

    /**
     * Sets the value of the delayTime property.
     *
     */
    public void setDelayTime(int value) {
        this.delayTime = value;
    }

    /**
     * Gets the value of the dimmerPercentage property.
     *
     */
    public int getDimmerPercentage() {
        return dimmerPercentage;
    }

    /**
     * Sets the value of the dimmerPercentage property.
     *
     */
    public void setDimmerPercentage(int value) {
        this.dimmerPercentage = value;
    }

    /**
     * Gets the value of the rampTime property.
     *
     */
    public int getRampTime() {
        return rampTime;
    }

    /**
     * Sets the value of the rampTime property.
     *
     */
    public void setRampTime(int value) {
        this.rampTime = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, delayTime=%d, dimmerPercentage=%d, rampTime=%d]", super.resourceID,
                delayTime, dimmerPercentage, rampTime);
    }
}
