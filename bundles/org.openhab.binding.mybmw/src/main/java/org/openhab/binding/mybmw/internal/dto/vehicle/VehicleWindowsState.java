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
package org.openhab.binding.mybmw.internal.dto.vehicle;

/**
 * 
 * derived from the API responses
 * 
 * @author Martin Grassl - initial contribution
 */
public class VehicleWindowsState {
    private String leftFront = ""; // CLOSED,
    private String leftRear = ""; // CLOSED,
    private String rightFront = ""; // CLOSED,
    private String rightRear = ""; // CLOSED,
    private String rear = ""; // CLOSED,
    private String combinedState = ""; // CLOSED

    public String getLeftFront() {
        return leftFront;
    }

    public void setLeftFront(String leftFront) {
        this.leftFront = leftFront;
    }

    public String getLeftRear() {
        return leftRear;
    }

    public void setLeftRear(String leftRear) {
        this.leftRear = leftRear;
    }

    public String getRightFront() {
        return rightFront;
    }

    public void setRightFront(String rightFront) {
        this.rightFront = rightFront;
    }

    public String getRightRear() {
        return rightRear;
    }

    public void setRightRear(String rightRear) {
        this.rightRear = rightRear;
    }

    public String getRear() {
        return rear;
    }

    public void setRear(String rear) {
        this.rear = rear;
    }

    public String getCombinedState() {
        return combinedState;
    }

    public void setCombinedState(String combinedState) {
        this.combinedState = combinedState;
    }

    @Override
    public String toString() {
        return "VehicleWindowsState [leftFront=" + leftFront + ", leftRear=" + leftRear + ", rightFront=" + rightFront
                + ", rightRear=" + rightRear + ", rear=" + rear + ", combinedState=" + combinedState + "]";
    }
}
