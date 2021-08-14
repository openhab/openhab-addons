/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.Optional;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
    public Optional<Integer> mppt1Power;

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
    public Optional<Integer> mppt2Power;

    /**
     * Total DC power
     */
    public Long totalDCPower;

    /**
     * AC Voltage Phase AB value
     */
    public Optional<Integer> acVoltageAB;

    /**
     * AC Voltage Phase BC value
     */
    public Optional<Integer> acVoltageBC;

    /**
     * AC Voltage Phase CA value
     */
    public Optional<Integer> acVoltageCA;

    /**
     * AC Voltage Phase A to N value
     */
    public Optional<Integer> acVoltageAtoN;

    /**
     * AC Voltage Phase B to N value
     */
    public Optional<Integer> acVoltageBtoN;

    /**
     * AC Voltage Phase C to N value
     */
    public Optional<Integer> acVoltageCtoN;

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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
