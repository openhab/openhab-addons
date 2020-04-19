/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
     * AC Phase A Current value
     */
    public Short acCurrentPhaseA;

    /**
     * AC Phase B Current value
     */
    public Optional<Short> acCurrentPhaseB;

    /**
     * AC Phase C Current value
     */
    public Optional<Short> acCurrentPhaseC;

    /**
     * AC Current scale factor
     */
    public Short acCurrentSF;

    /**
     * AC Voltage Line to line value
     */
    public Optional<Short> acVoltageLineToNAverage;

    /**
     * AC Voltage Phase A to N value
     */
    public Optional<Short> acVoltageAtoN;

    /**
     * AC Voltage Phase B to N value
     */
    public Optional<Short> acVoltageBtoN;

    /**
     * AC Voltage Phase C to N value
     */
    public Optional<Short> acVoltageCtoN;

    /**
     * AC Voltage Line to N value
     */
    public Optional<Short> acVoltageLineToLineAverage;

    /**
     * AC Voltage Phase AB value
     */
    public Optional<Short> acVoltageAB;

    /**
     * AC Voltage Phase BC value
     */
    public Optional<Short> acVoltageBC;

    /**
     * AC Voltage Phase CA value
     */
    public Optional<Short> acVoltageCA;

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
     * Phase A AC real power
     */
    public Optional<Short> acRealPowerPhaseA;

    /**
     * Phase B AC real power
     */
    public Optional<Short> acRealPowerPhaseB;

    /**
     * Phase C AC real power
     */
    public Optional<Short> acRealPowerPhaseC;

    /**
     * AC Real Power Scale Factor
     */
    public Short acRealPowerSF;

    /**
     * Total apparent power
     */
    public Optional<Short> acApparentPowerTotal;

    /**
     * Phase A AC apparent power
     */
    public Optional<Short> acApparentPowerPhaseA;

    /**
     * Phase B AC apparent power
     */
    public Optional<Short> acApparentPowerPhaseB;

    /**
     * Phase C AC apparent power
     */
    public Optional<Short> acApparentPowerPhaseC;

    /**
     * AC Apparent Power Scale Factor
     */
    public Optional<Short> acApparentPowerSF;

    /**
     * Total reactive power
     */
    public Optional<Short> acReactivePowerTotal;

    /**
     * Phase A AC reactive power
     */
    public Optional<Short> acReactivePowerPhaseA;

    /**
     * Phase B AC reactive power
     */
    public Optional<Short> acReactivePowerPhaseB;

    /**
     * Phase C AC reactive power
     */
    public Optional<Short> acReactivePowerPhaseC;

    /**
     * AC Reactive Power Scale Factor
     */
    public Optional<Short> acReactivePowerSF;

    /**
     * Power factor
     */
    public Optional<Short> acPowerFactor;

    /**
     * Phase A Power factor
     */
    public Optional<Short> acPowerFactorPhaseA;

    /**
     * Phase B Power factor
     */
    public Optional<Short> acPowerFactorPhaseB;

    /**
     * Phase C Power factor
     */
    public Optional<Short> acPowerFactorPhaseC;

    /**
     * Power factor scale factor
     */
    public Optional<Short> acPowerFactorSF;

    /**
     * Total exported real energy
     */
    public Optional<Long> acExportedRealEnergyTotal;

    /**
     * Phase A exported real energy
     */
    public Optional<Long> acExportedRealEnergyPhaseA;

    /**
     * Phase B exported real energy
     */
    public Optional<Long> acExportedRealEnergyPhaseB;

    /**
     * Phase C exported real energy
     */
    public Optional<Long> acExportedRealEnergyPhaseC;

    /**
     * Total imported real energy
     */
    public Long acImportedRealEnergyTotal;

    /**
     * Phase A imported real energy
     */
    public Optional<Long> acImportedRealEnergyPhaseA;

    /**
     * Phase B imported real energy
     */
    public Optional<Long> acImportedRealEnergyPhaseB;

    /**
     * Phase C imported real energy
     */
    public Optional<Long> acImportedRealEnergyPhaseC;

    /**
     * Real Energy Scale Factor
     */
    public Short acRealEnergySF;

    /**
     * Total exported apparent energy
     */
    public Optional<Long> acExportedApparentEnergyTotal;

    /**
     * Phase A exported apparent energy
     */
    public Optional<Long> acExportedApparentEnergyPhaseA;

    /**
     * Phase B exported apparent energy
     */
    public Optional<Long> acExportedApparentEnergyPhaseB;

    /**
     * Phase C exported apparent energy
     */
    public Optional<Long> acExportedApparentEnergyPhaseC;

    /**
     * Total imported apparent energy
     */
    public Optional<Long> acImportedApparentEnergyTotal;

    /**
     * Phase A imported apparent energy
     */
    public Optional<Long> acImportedApparentEnergyPhaseA;

    /**
     * Phase B imported apparent energy
     */
    public Optional<Long> acImportedApparentEnergyPhaseB;

    /**
     * Phase C imported apparent energy
     */
    public Optional<Long> acImportedApparentEnergyPhaseC;

    /**
     * Apparent Energy Scale Factor
     */
    public Optional<Short> acApparentEnergySF;

    /**
     * Quadrant 1: Total imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ1Total;

    /**
     * Quadrant 1: Phase A imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ1PhaseA;

    /**
     * Quadrant 1: Phase B imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ1PhaseB;

    /**
     * Quadrant 1: Phase C imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ1PhaseC;

    /**
     * Quadrant 2: Total imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ2Total;

    /**
     * Quadrant 2: Phase A imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ2PhaseA;

    /**
     * Quadrant 2: Phase B imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ2PhaseB;

    /**
     * Quadrant 2: Phase C imported reactive energy
     */
    public Optional<Long> acImportedReactiveEnergyQ2PhaseC;

    /**
     * Quadrant 3: Total exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ3Total;

    /**
     * Quadrant 3: Phase A exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ3PhaseA;

    /**
     * Quadrant 3: Phase B exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ3PhaseB;

    /**
     * Quadrant 3: Phase C exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ3PhaseC;

    /**
     * Quadrant 4: Total exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ4Total;

    /**
     * Quadrant 4: Phase A exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ4PhaseA;

    /**
     * Quadrant 4: Phase B exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ4PhaseB;

    /**
     * Quadrant 4: Phase C exported reactive energy
     */
    public Optional<Long> acExportedReactiveEnergyQ4PhaseC;

    /**
     * Reactive Energy Scale Factor
     */
    public Optional<Short> acReactiveEnergySF;

}
