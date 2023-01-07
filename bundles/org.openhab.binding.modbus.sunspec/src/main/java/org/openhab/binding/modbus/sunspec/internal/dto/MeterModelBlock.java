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
package org.openhab.binding.modbus.sunspec.internal.dto;

import java.util.Optional;

/**
 *
 * Data object for the parsed information from a sunspec meter
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
public class MeterModelBlock {

    /**
     * Sunspec device type id
     */
    public Integer sunspecDID;

    /**
     * Block length
     */
    public Integer length;

    /**
     * AC Total Current value
     */
    public Short acCurrentTotal;

    /**
     * Descriptors for phase A
     */
    public PhaseBlock phaseA = new PhaseBlock();

    /**
     * Descriptors for phase B
     */
    public PhaseBlock phaseB = new PhaseBlock();

    /**
     * Descriptors for phase C
     */
    public PhaseBlock phaseC = new PhaseBlock();

    /**
     * AC Current scale factor
     */
    public Short acCurrentSF;

    /**
     * AC Voltage Line to line value
     */
    public Optional<Short> acVoltageLineToNAverage;

    /**
     * AC Voltage Line to N value
     */
    public Optional<Short> acVoltageLineToLineAverage;

    /**
     * AC Voltage scale factor
     */
    public Short acVoltageSF;

    /**
     * AC Frequency value
     */
    public Short acFrequency;

    /**
     * AC Frequency scale factor
     */
    public Optional<Short> acFrequencySF;

    /**
     * Total real power
     */
    public Short acRealPowerTotal;

    /**
     * AC Real Power Scale Factor
     */
    public Short acRealPowerSF;

    /**
     * Total apparent power
     */
    public Optional<Short> acApparentPowerTotal;

    /**
     * AC Apparent Power Scale Factor
     */
    public Optional<Short> acApparentPowerSF;

    /**
     * Total reactive power
     */
    public Optional<Short> acReactivePowerTotal;

    /**
     * AC Reactive Power Scale Factor
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
     * Total exported real energy
     */
    public Optional<Long> acExportedRealEnergyTotal;

    /**
     * Total imported real energy
     */
    public Long acImportedRealEnergyTotal;

    /**
     * Real Energy Scale Factor
     */
    public Short acRealEnergySF;

    /**
     * Total exported apparent energy
     */
    public Optional<Long> acExportedApparentEnergyTotal;

    /**
     * Total imported apparent energy
     */
    public Optional<Long> acImportedApparentEnergyTotal;

    /**
     * Apparent Energy Scale Factor
     */
    public Optional<Short> acApparentEnergySF;

    /**
     * Quadrant 1: Total imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ1Total;

    /**
     * Quadrant 2: Total imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ2Total;

    /**
     * Quadrant 3: Total exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ3Total;

    /**
     * Quadrant 4: Total exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ4Total;

    /**
     * Reactive Energy Scale Factor
     */
    public Optional<Short> acReactiveEnergySF;

    /**
     * This subclass is used to store raw data for a single phase in
     * multi phase meters.
     */
    public static class PhaseBlock {
        /**
         * AC Phase A Current value
         */
        public Optional<Short> acPhaseCurrent;

        /**
         * AC Voltage Phase Phase to N value
         */
        public Optional<Short> acVoltageToN;

        /**
         * AC Voltage Phase Line to next Line value
         */
        public Optional<Short> acVoltageToNext;

        /**
         * Phase A AC real power
         */
        public Optional<Short> acRealPower;

        /**
         * Phase A AC apparent power
         */
        public Optional<Short> acApparentPower;

        /**
         * Phase A AC reactive power
         */
        public Optional<Short> acReactivePower;

        /**
         * Phase A Power factor
         */
        public Optional<Short> acPowerFactor;

        /**
         * Phase A exported real energy
         */
        public Optional<Long> acExportedRealEnergy;

        /**
         * Phase A imported real energy
         */
        public Optional<Long> acImportedRealEnergy;

        /**
         * Phase A exported apparent energy
         */
        public Optional<Long> acExportedApparentEnergy;

        /**
         * Phase A imported apparent energy
         */
        public Optional<Long> acImportedApparentEnergy;

        /**
         * Quadrant 1: Phase A imported reactive energy
         */
        public Optional<Long> acImportedReactiveEnergyQ1;

        /**
         * Quadrant 2: Phase A imported reactive energy
         */
        public Optional<Long> acImportedReactiveEnergyQ2;

        /**
         * Quadrant 3: Phase A exported reactive energy
         */
        public Optional<Long> acExportedReactiveEnergyQ3;

        /**
         * Quadrant 4: Phase A exported reactive energy
         */
        public Optional<Long> acExportedReactiveEnergyQ4;
    }
}
