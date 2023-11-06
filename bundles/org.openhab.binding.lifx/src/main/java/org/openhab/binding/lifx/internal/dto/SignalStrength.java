/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal.dto;

/**
 * The signal strength of a light.
 *
 * @author Wouter Born - Initial contribution
 */
public class SignalStrength {

    private double milliWatts;

    public SignalStrength(double milliWatts) {
        this.milliWatts = milliWatts;
    }

    /**
     * Returns the signal strength.
     *
     * @return the signal strength in milliwatts (mW).
     */
    public double getMilliWatts() {
        return milliWatts;
    }

    /**
     * Returns the signal strength as a quality percentage:
     * <ul>
     * <li>{@code RSSI <= -100}: returns 0
     * <li>{@code -100 < RSSI < -50}: returns a value between 0 and 1 (linearly distributed)
     * <li>{@code RSSI >= -50}: returns 1
     * </ul>
     *
     * @return a value between 0 and 1. 0 being worst strength and 1
     *         being best strength.
     */
    public double toQualityPercentage() {
        return Math.min(100, Math.max(0, 2 * (toRSSI() + 100))) / 100;
    }

    /**
     * Returns the signal strength as a quality rating.
     *
     * @return one of the values: 0, 1, 2, 3 or 4. 0 being worst strength and 4
     *         being best strength.
     */
    public byte toQualityRating() {
        return (byte) Math.round(toQualityPercentage() * 4);
    }

    /**
     * Returns the received signal strength indicator (RSSI).
     *
     * @return a value {@code <= 0. 0} being best strength and more negative values indicate worser strength.
     */
    public double toRSSI() {
        return 10 * Math.log10(milliWatts);
    }

    @Override
    public String toString() {
        return "SignalStrength [milliWatts=" + milliWatts + ", rssi=" + Math.round(toRSSI()) + "]";
    }
}
