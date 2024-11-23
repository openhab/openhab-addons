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
public class VehicleTireStateDetailsClassification {
    private int speedRating = -1; // 240,
    private boolean atLeast = false; // false

    public int getSpeedRating() {
        return speedRating;
    }

    public void setSpeedRating(int speedRating) {
        this.speedRating = speedRating;
    }

    public boolean isAtLeast() {
        return atLeast;
    }

    public void setAtLeast(boolean atLeast) {
        this.atLeast = atLeast;
    }

    @Override
    public String toString() {
        return "VehicleTireStateDetailsClassification [speedRating=" + speedRating + ", atLeast=" + atLeast + "]";
    }
}
