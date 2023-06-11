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
package org.openhab.binding.omnikinverter.internal;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Hans van den Bogert - Initial contribution
 *
 */
@NonNullByDefault
public class OmnikInverterMessage {

    private final byte[] bytes;

    public OmnikInverterMessage(byte[] b) {
        this.bytes = b;
    }

    private double getShort(int offset, int compensationFactor) {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.put(bytes, offset, 2);
        buf.rewind();
        return (double) buf.getShort() / compensationFactor;
    }

    private double getInt(int offset, int compensationFactor) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.put(bytes, offset, 4);
        buf.rewind();
        return (double) buf.getInt() / compensationFactor;
    }

    /**
     * @return the voltage for PV1
     */
    public double getVoltagePV1() {
        return getShort(33, 10);
    }

    /**
     * @return the voltage for PV2
     */
    public double getVoltagePV2() {
        return getShort(35, 10);
    }

    /**
     * @return the voltage for PV3
     */
    public double getVoltagePV3() {
        return getShort(37, 10);
    }

    /**
     * @return the amperage for PV1
     */
    public double getCurrentPV1() {
        return getShort(39, 10);
    }

    /**
     * @return the amperage for PV2
     */
    public double getCurrentPV2() {
        return getShort(41, 10);
    }

    /**
     * @return the amperage for PV3
     */
    public double getCurrentPV3() {
        return getShort(43, 10);
    }

    /**
     * @return the amperage for AC1
     */
    public double getAmperageAC1() {
        return getShort(45, 10);
    }

    /**
     * @return the amperage for AC2
     */
    public double getAmperageAC2() {
        return getShort(47, 10);
    }

    /**
     * @return the amperage for AC3
     */
    public double getAmperageAC3() {
        return getShort(49, 10);
    }

    /**
     * @return the voltage for AC1
     */
    public double getVoltageAC1() {
        return getShort(51, 10);
    }

    /**
     * @return the voltage for AC2
     */
    public double getVoltageAC2() {
        return getShort(53, 10);
    }

    /**
     * @return the voltage for AC3
     */
    public double getVoltageAC3() {
        return getShort(55, 10);
    }

    /**
     * @return the Frequency for AC1
     */
    public double getFrequencyAC1() {
        return getShort(57, 100);
    }

    /**
     * @return the power for AC1
     *
     * @deprecated
     */
    public double getPower() {
        return getShort(59, 1);
    }

    /**
     * @return the power for AC1
     */
    public double getPowerAC1() {
        return getShort(59, 1);
    }

    /**
     * @return the Frequency for AC2
     */
    public double getFrequencyAC2() {
        return getShort(61, 100);
    }

    /**
     * @return the power for AC2
     */
    public double getPowerAC2() {
        return getShort(63, 1);
    }

    /**
     * @return the Frequency for AC3
     */
    public double getFrequencyAC3() {
        return getShort(65, 100);
    }

    /**
     * @return the power for AC3
     */
    public double getPowerAC3() {
        return getShort(67, 1);
    }

    /**
     *
     * @return the total energy outputted this day in kWh
     */
    public double getEnergyToday() {
        return getShort(69, 100);
    }

    /**
     *
     * @return the total energy outputted in kWh
     */
    public double getTotalEnergy() {
        return getInt(71, 10);
    }
}
