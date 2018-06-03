/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol.field;

/**
 * The power calibration data of a relay device (Circle, Circle+, Stealth). It is used in {@link Energy} to calculate
 * energy (kWh) and power (W) from pulses.
 *
 * @author Wouter Born - Initial contribution
 */
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
