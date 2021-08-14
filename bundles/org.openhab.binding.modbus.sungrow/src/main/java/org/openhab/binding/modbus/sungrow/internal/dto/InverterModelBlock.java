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

/**
 * Model for Sungrow compatible inverter data
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
public class InverterModelBlock {

    /**
     * Type of inverter (single phase, split phase, three phase)
     */
    public Integer phaseConfiguration;

    /**
     * Length of the block in 16bit words
     */
    public Integer length;

    /**
     * AC Total Current value
     */
    public Integer acCurrentTotal;

    /**
     * AC Phase A Current value
     */
    public Integer acCurrentPhaseA;

    /**
     * AC Phase B Current value
     */
    public Optional<Integer> acCurrentPhaseB;

    /**
     * AC Phase C Current value
     */
    public Optional<Integer> acCurrentPhaseC;

    /**
     * AC Current scale factor
     */
    public Short acCurrentSF;

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
    public Integer acVoltageAtoN;

    /**
     * AC Voltage Phase B to N value
     */
    public Optional<Integer> acVoltageBtoN;

    /**
     * AC Voltage Phase C to N value
     */
    public Optional<Integer> acVoltageCtoN;

    /**
     * AC Voltage scale factor
     */
    public Short acVoltageSF;

    /**
     * AC Power value
     */
    public Short acPower;

    /**
     * AC Power scale factor
     */
    public Short acPowerSF;

    /**
     * AC Frequency value
     */
    public Integer acFrequency;

    /**
     * AC Frequency scale factor
     */
    public Short acFrequencySF;

    /**
     * Apparent power
     */
    public Optional<Short> acApparentPower;

    /**
     * Apparent power scale factor
     */
    public Optional<Short> acApparentPowerSF;

    /**
     * Reactive power
     */
    public Optional<Short> acReactivePower;

    /**
     * Reactive power scale factor
     */
    public Optional<Short> acReactivePowerSF;

    /**
     * Power factor
     */
    public Optional<Short> acPowerFactor;

    /**
     * Power factor scale factor
     */
    public Optional<Short> acPowerFactorSF;

    /**
     * AC Lifetime Energy production
     */
    public Long acEnergyLifetime;

    /**
     * AC Lifetime Energy scale factor
     */
    public Short acEnergyLifetimeSF;

    /**
     * MPPT1 Current value
     */
    public Integer mppt1Current;

    /**
     * MPPT2 Current value
     */
    public Integer mppt2Current;

    /**
     * MPPT Current scale factor
     */
    public Short mpptCurrentSF;

    /**
     * MPPT1 Voltage value
     */
    public Integer mppt1Voltage;

    /**
     * MPPT2 Voltage value
     */
    public Integer mppt2Voltage;

    /**
     * MPPT Voltage scale factor
     */
    public Short mpptVoltageSF;

    /**
     * MPPT1 Power value
     */
    public Integer mppt1Power;

    /**
     * MPPT2 Power value
     */
    public Integer mppt2Power;

    /**
     * MPPT Power scale factor
     */
    public Short mpptPowerSF;

    /**
     * Cabinet temperature
     */
    public Short temperatureCabinet;

    /**
     * Temperature scale factor
     */
    public Short temperatureSF;

    /**
     * Current operating state
     */
    public Integer status;

    /**
     * Vendor defined operating state or error code
     */
    public Optional<Integer> statusVendor;
}
