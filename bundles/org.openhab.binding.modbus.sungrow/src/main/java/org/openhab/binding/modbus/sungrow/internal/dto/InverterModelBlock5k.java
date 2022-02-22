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
package org.openhab.binding.modbus.sungrow.internal.dto;

import java.util.OptionalInt;

/**
 * Model for Sungrow compatible inverter data
 *
 * @author Ferdinand Schwenk - Initial contribution
 *
 */
public class InverterModelBlock5k {

    /**
     * Device Type of the inverter
     */
    public Integer deviceType;

    /**
     * Nominal output power
     */
    public Integer nominalOutputPower;

    /**
     * Output type; Type of inverter (single phase, split phase, three phase)
     */
    public Integer outputType;

    /**
     * Daily output energy
     */
    public Integer dailyOutputEnergy;

    /**
     * Total output energy
     */
    public Long totalOutputEnergy;

    /**
     * Inside temperature
     */
    public Short insideTemperature;

    /**
     * MPPT1 Voltage value
     */
    public Integer mppt1Voltage;

    /**
     * MPPT1 Current value
     */
    public Integer mppt1Current;

    /**
     * MPPT1 Power value
     */
    public OptionalInt mppt1Power;

    /**
     * MPPT2 Voltage value
     */
    public Integer mppt2Voltage;

    /**
     * MPPT2 Current value
     */
    public Integer mppt2Current;

    /**
     * MPPT2 Power value
     */
    public OptionalInt mppt2Power;

    /**
     * Total DC power
     */
    public Long totalDCPower;

    /**
     * AC Voltage Phase AB value
     */
    public OptionalInt acVoltageAB;

    /**
     * AC Voltage Phase BC value
     */
    public OptionalInt acVoltageBC;

    /**
     * AC Voltage Phase CA value
     */
    public OptionalInt acVoltageCA;

    /**
     * AC Voltage Phase A to N value
     */
    public OptionalInt acVoltageAtoN;

    /**
     * AC Voltage Phase B to N value
     */
    public OptionalInt acVoltageBtoN;

    /**
     * AC Voltage Phase C to N value
     */
    public OptionalInt acVoltageCtoN;

    /**
     * Reactive power
     */
    public Long acReactivePower;

    /**
     * Power factor
     */
    public Short acPowerFactor;

    /**
     * AC Frequency value
     */
    public Integer acFrequency;

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "[\n" + "  acPowerFactor=" + this.acPowerFactor + "\n"
                + "  acReactivePower=" + this.acReactivePower + "\n" + "  acVoltageAB=" + this.acVoltageAB + "\n"
                + "  acVoltageBC=" + this.acVoltageBC + "\n" + "  acVoltageCA=" + this.acVoltageCA + "\n"
                + "  acVoltageAtoN=" + this.acVoltageAtoN + "\n" + "  acVoltageBtoN=" + this.acVoltageBtoN + "\n"
                + "  acVoltageCtoN=" + this.acVoltageCtoN + "\n" + "  acFrequency=" + this.acFrequency + "\n"
                + "  dailyOutputEnergy=" + this.dailyOutputEnergy + "\n" + "  deviceType=" + this.deviceType + "\n"
                + "  insideTemperature=" + this.insideTemperature + "\n" + "  mppt1Current=" + this.mppt1Current + "\n"
                + "  mppt1Power=" + this.mppt1Power + "\n" + "  mppt1Voltage=" + this.mppt1Voltage + "\n"
                + "  mppt2Current=" + this.mppt2Current + "\n" + "  mppt2Power=" + this.mppt2Power + "\n"
                + "  mppt2Voltage=" + this.mppt2Voltage + "\n" + "  nominalOutputPower=" + this.nominalOutputPower
                + "\n" + "  outputType=" + this.outputType + "\n" + "  totalDCPower=" + this.totalDCPower + "\n"
                + "  totalOutputEnergy=" + this.totalOutputEnergy + "\n" + "}";
    }
}
