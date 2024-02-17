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
public class VehicleTireStates {
    private VehicleTireState frontLeft = new VehicleTireState();
    private VehicleTireState frontRight = new VehicleTireState();
    private VehicleTireState rearLeft = new VehicleTireState();
    private VehicleTireState rearRight = new VehicleTireState();

    public VehicleTireState getFrontLeft() {
        return frontLeft;
    }

    public void setFrontLeft(VehicleTireState frontLeft) {
        this.frontLeft = frontLeft;
    }

    public VehicleTireState getFrontRight() {
        return frontRight;
    }

    public void setFrontRight(VehicleTireState frontRight) {
        this.frontRight = frontRight;
    }

    public VehicleTireState getRearLeft() {
        return rearLeft;
    }

    public void setRearLeft(VehicleTireState rearLeft) {
        this.rearLeft = rearLeft;
    }

    public VehicleTireState getRearRight() {
        return rearRight;
    }

    public void setRearRight(VehicleTireState rearRight) {
        this.rearRight = rearRight;
    }

    @Override
    public String toString() {
        return "VehicleTireStates [frontLeft=" + frontLeft + ", frontRight=" + frontRight + ", rearLeft=" + rearLeft
                + ", rearRight=" + rearRight + "]";
    }
}
