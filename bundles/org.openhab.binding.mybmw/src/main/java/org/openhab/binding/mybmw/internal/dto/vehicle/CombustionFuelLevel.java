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
public class CombustionFuelLevel {
    private int remainingFuelPercent = -1; // 65,
    private int remainingFuelLiters = -1; // 34,
    private int range = -1; // 435

    public int getRemainingFuelPercent() {
        return remainingFuelPercent;
    }

    public void setRemainingFuelPercent(int remainingFuelPercent) {
        this.remainingFuelPercent = remainingFuelPercent;
    }

    public int getRemainingFuelLiters() {
        return remainingFuelLiters;
    }

    public void setRemainingFuelLiters(int remainingFuelLiters) {
        this.remainingFuelLiters = remainingFuelLiters;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    @Override
    public String toString() {
        return "CombustionFuelLevel [remainingFuelPercent=" + remainingFuelPercent + ", remainingFuelLiters="
                + remainingFuelLiters + ", range=" + range + "]";
    }
}
