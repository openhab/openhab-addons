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
 * derived from API response
 * 
 * @author Martin Grassl - initial contribution
 */
public class VehicleTireStateStatus {
    private int currentPressure = -1; // 280,
    private int targetPressure = -1; // 290

    public int getCurrentPressure() {
        return currentPressure;
    }

    public void setCurrentPressure(int currentPressure) {
        this.currentPressure = currentPressure;
    }

    public int getTargetPressure() {
        return targetPressure;
    }

    public void setTargetPressure(int targetPressure) {
        this.targetPressure = targetPressure;
    }

    @Override
    public String toString() {
        return "VehicleTireStateStatus [currentPressure=" + currentPressure + ", targetPressure=" + targetPressure
                + "]";
    }
}
