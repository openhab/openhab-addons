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
package org.openhab.binding.modbus.sunspec.internal.dto;

import java.util.Optional;

/**
 * Model for SunSpec compatible inverter data
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
     * DC Current value
     */
    public Optional<Integer> dcCurrent;

    /**
     * DC Current scale factor
     */
    public Optional<Short> dcCurrentSF;

    /**
     * DC Voltage value
     */
    public Optional<Integer> dcVoltage;

    /**
     * DC Voltage scale factor
     */
    public Optional<Short> dcVoltageSF;

    /**
     * DC Power value
     */
    public Optional<Short> dcPower;

    /**
     * DC Power scale factor
     */
    public Optional<Short> dcPowerSF;

    /**
     * Cabinet temperature
     */
    public Short temperatureCabinet;

    /**
     * Heat sink temperature
     */
    public Optional<Short> temperatureHeatsink;

    /**
     * Transformer temperature
     */
    public Optional<Short> temperatureTransformer;

    /**
     * Other temperature
     */
    public Optional<Short> temperatureOther;

    /**
     * Heat sink temperature scale factor
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
