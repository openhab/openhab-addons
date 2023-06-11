/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.plugwise.internal.protocol.field;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The power calibration data of a relay device (Circle, Circle+, Stealth). It is used in {@link Energy} to calculate
 * energy (kWh) and power (W) from pulses.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PowerCalibration {

    private final double gainA;
    private final double gainB;
    private final double offsetTotal;
    private final double offsetNoise;

    public PowerCalibration(double gainA, double gainB, double offsetNoise, double offsetTotal) {
        this.gainA = gainA;
        this.gainB = gainB;
        this.offsetNoise = offsetNoise;
        this.offsetTotal = offsetTotal;
    }

    public double getGainA() {
        return gainA;
    }

    public double getGainB() {
        return gainB;
    }

    public double getOffsetTotal() {
        return offsetTotal;
    }

    public double getOffsetNoise() {
        return offsetNoise;
    }

    @Override
    public String toString() {
        return "PowerCalibration [gainA=" + gainA + ", gainB=" + gainB + ", offsetTotal=" + offsetTotal
                + ", offsetNoise=" + offsetNoise + "]";
    }
}
