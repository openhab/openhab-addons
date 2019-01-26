/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sunspec.internal.block;

/**
 *
 * Data object for the parsed information from a sunspec meter
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
public class MeterModelBlock extends AbstractSunSpecMessageBlock {

    /**
     * Sunspec device type id
     */
    private int sunspecDID;

    /**
     * Block length
     */
    private int length;

    /**
     * AC Total Current value
     */
    private Short acCurrentTotal;

    /**
     * AC Phase A Current value
     */
    private Short acCurrentPhaseA;

    /**
     * AC Phase B Current value
     */
    private Short acCurrentPhaseB;

    /**
     * AC Phase C Current value
     */
    private Short acCurrentPhaseC;

    /**
     * AC Current scale factor
     */
    private Short acCurrentSF;

    /**
     * AC Voltage Line to line value
     */
    private Short acVoltageLineToLineAverage;

    /**
     * AC Voltage Phase AB value
     */
    private Short acVoltageAB;

    /**
     * AC Voltage Phase BC value
     */
    private Short acVoltageBC;

    /**
     * AC Voltage Phase CA value
     */
    private Short acVoltageCA;

    /**
     * AC Voltage Line to N value
     */
    private Short acVoltageLinetoNAverage;

    /**
     * AC Voltage Phase A to N value
     */
    private Short acVoltageAtoN;

    /**
     * AC Voltage Phase B to N value
     */
    private Short acVoltageBtoN;

    /**
     * AC Voltage Phase C to N value
     */
    private Short acVoltageCtoN;

    /**
     * AC Voltage scale factor
     */
    private Short acVoltageSF;

    /**
     * AC Frequency value
     */
    private Short acFrequency;

    /**
     * AC Frequency scale factor
     */
    private Short acFrequencySF;

    /**
     * Total real power
     */
    private Short acRealPowerTotal;

    /**
     * Phase A AC real power
     */
    private Short acRealPowerPhaseA;

    /**
     * Phase B AC real power
     */
    private Short acRealPowerPhaseB;

    /**
     * Phase C AC real power
     */
    private Short acRealPowerPhaseC;

    /**
     * AC Real Power Scale Factor
     */
    private Short acRealPowerSF;

    /**
     * Total apparent power
     */
    private Short acApparentPowerTotal;

    /**
     * Phase A AC apparent power
     */
    private Short acApparentPowerPhaseA;

    /**
     * Phase B AC apparent power
     */
    private Short acApparentPowerPhaseB;

    /**
     * Phase C AC apparent power
     */
    private Short acApparentPowerPhaseC;

    /**
     * AC Apparent Power Scale Factor
     */
    private Short acApparentPowerSF;

    /**
     * Total reactive power
     */
    private Short acReactivePowerTotal;

    /**
     * Phase A AC reactive power
     */
    private Short acReactivePowerPhaseA;

    /**
     * Phase B AC reactive power
     */
    private Short acReactivePowerPhaseB;

    /**
     * Phase C AC reactive power
     */
    private Short acReactivePowerPhaseC;

    /**
     * AC Reactive Power Scale Factor
     */
    private Short acReactivePowerSF;

    /**
     * Power factor
     */
    private Short acPowerFactor;

    /**
     * Phase A Power factor
     */
    private Short acPowerFactorPhaseA;

    /**
     * Phase B Power factor
     */
    private Short acPowerFactorPhaseB;

    /**
     * Phase C Power factor
     */
    private Short acPowerFactorPhaseC;

    /**
     * Power factor scale factor
     */
    private Short acPowerFactorSF;

    /**
     * Total exported real energy
     */
    private Long acExportedRealEnergyTotal;

    /**
     * Phase A exported real energy
     */
    private Long acExportedRealEnergyPhaseA;

    /**
     * Phase B exported real energy
     */
    private Long acExportedRealEnergyPhaseB;

    /**
     * Phase C exported real energy
     */
    private Long acExportedRealEnergyPhaseC;

    /**
     * Total imported real energy
     */
    private Long acImportedRealEnergyTotal;

    /**
     * Phase A imported real energy
     */
    private Long acImportedRealEnergyPhaseA;

    /**
     * Phase B imported real energy
     */
    private Long acImportedRealEnergyPhaseB;

    /**
     * Phase C imported real energy
     */
    private Long acImportedRealEnergyPhaseC;

    /**
     * Real Energy Scale Factor
     */
    private Short acRealEnergySF;

    /**
     * Total exported apparent energy
     */
    private Long acExportedApparentEnergyTotal;

    /**
     * Phase A exported apparent energy
     */
    private Long acExportedApparentEnergyPhaseA;

    /**
     * Phase B exported apparent energy
     */
    private Long acExportedApparentEnergyPhaseB;

    /**
     * Phase C exported apparent energy
     */
    private Long acExportedApparentEnergyPhaseC;

    /**
     * Total imported apparent energy
     */
    private Long acImportedApparentEnergyTotal;

    /**
     * Phase A imported apparent energy
     */
    private Long acImportedApparentEnergyPhaseA;

    /**
     * Phase B imported apparent energy
     */
    private Long acImportedApparentEnergyPhaseB;

    /**
     * Phase C imported apparent energy
     */
    private Long acImportedApparentEnergyPhaseC;

    /**
     * Apparent Energy Scale Factor
     */
    private Short acApparentEnergySF;

    /**
     * Quadrant 1: Total imported reactive energy
     */
    private Long acImportedReactiveEnergyQ1Total;

    /**
     * Quadrant 1: Phase A imported reactive energy
     */
    private Long acImportedReactiveEnergyQ1PhaseA;

    /**
     * Quadrant 1: Phase B imported reactive energy
     */
    private Long acImportedReactiveEnergyQ1PhaseB;

    /**
     * Quadrant 1: Phase C imported reactive energy
     */
    private Long acImportedReactiveEnergyQ1PhaseC;

    /**
     * Quadrant 2: Total imported reactive energy
     */
    private Long acImportedReactiveEnergyQ2Total;

    /**
     * Quadrant 2: Phase A imported reactive energy
     */
    private Long acImportedReactiveEnergyQ2PhaseA;

    /**
     * Quadrant 2: Phase B imported reactive energy
     */
    private Long acImportedReactiveEnergyQ2PhaseB;

    /**
     * Quadrant 2: Phase C imported reactive energy
     */
    private Long acImportedReactiveEnergyQ2PhaseC;

    /**
     * Quadrant 3: Total exported reactive energy
     */
    private Long acExportedReactiveEnergyQ3Total;

    /**
     * Quadrant 3: Phase A exported reactive energy
     */
    private Long acExportedReactiveEnergyQ3PhaseA;

    /**
     * Quadrant 3: Phase B exported reactive energy
     */
    private Long acExportedReactiveEnergyQ3PhaseB;

    /**
     * Quadrant 3: Phase C exported reactive energy
     */
    private Long acExportedReactiveEnergyQ3PhaseC;

    /**
     * Quadrant 4: Total exported reactive energy
     */
    private Long acExportedReactiveEnergyQ4Total;

    /**
     * Quadrant 4: Phase A exported reactive energy
     */
    private Long acExportedReactiveEnergyQ4PhaseA;

    /**
     * Quadrant 4: Phase B exported reactive energy
     */
    private Long acExportedReactiveEnergyQ4PhaseB;

    /**
     * Quadrant 4: Phase C exported reactive energy
     */
    private Long acExportedReactiveEnergyQ4PhaseC;

    /**
     * Reactive Energy Scale Factor
     */
    private Short acReactiveEnergySF;

    public int getSunspecDID() {
        return sunspecDID;
    }

    public void setSunspecDID(int sunspecDID) {
        this.sunspecDID = sunspecDID;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Short getAcCurrentTotal() {
        return acCurrentTotal;
    }

    public void setAcCurrentTotal(Short acCurrentTotal) {
        this.acCurrentTotal = acCurrentTotal;
    }

    public Short getAcCurrentPhaseA() {
        return acCurrentPhaseA;
    }

    public void setAcCurrentPhaseA(Short acCurrentPhaseA) {
        this.acCurrentPhaseA = acCurrentPhaseA;
    }

    public Short getAcCurrentPhaseB() {
        return acCurrentPhaseB;
    }

    public void setAcCurrentPhaseB(Short acCurrentPhaseB) {
        this.acCurrentPhaseB = acCurrentPhaseB;
    }

    public Short getAcCurrentPhaseC() {
        return acCurrentPhaseC;
    }

    public void setAcCurrentPhaseC(Short acCurrentPhaseC) {
        this.acCurrentPhaseC = acCurrentPhaseC;
    }

    public Short getAcCurrentSF() {
        return acCurrentSF;
    }

    public void setAcCurrentSF(Short acCurrentSF) {
        this.acCurrentSF = acCurrentSF;
    }

    public Short getAcVoltageLineToLineAverage() {
        return acVoltageLineToLineAverage;
    }

    public void setAcVoltageLineToLineAverage(Short acVoltageLineToLineAverage) {
        this.acVoltageLineToLineAverage = acVoltageLineToLineAverage;
    }

    public Short getAcVoltageAB() {
        return acVoltageAB;
    }

    public void setAcVoltageAB(Short acVoltageAB) {
        this.acVoltageAB = acVoltageAB;
    }

    public Short getAcVoltageBC() {
        return acVoltageBC;
    }

    public void setAcVoltageBC(Short acVoltageBC) {
        this.acVoltageBC = acVoltageBC;
    }

    public Short getAcVoltageCA() {
        return acVoltageCA;
    }

    public void setAcVoltageCA(Short acVoltageCA) {
        this.acVoltageCA = acVoltageCA;
    }

    public Short getAcVoltageLinetoNAverage() {
        return acVoltageLinetoNAverage;
    }

    public void setAcVoltageLinetoNAverage(Short acVoltageLinetoNAverage) {
        this.acVoltageLinetoNAverage = acVoltageLinetoNAverage;
    }

    public Short getAcVoltageAtoN() {
        return acVoltageAtoN;
    }

    public void setAcVoltageAtoN(Short acVoltageAtoN) {
        this.acVoltageAtoN = acVoltageAtoN;
    }

    public Short getAcVoltageBtoN() {
        return acVoltageBtoN;
    }

    public void setAcVoltageBtoN(Short acVoltageBtoN) {
        this.acVoltageBtoN = acVoltageBtoN;
    }

    public Short getAcVoltageCtoN() {
        return acVoltageCtoN;
    }

    public void setAcVoltageCtoN(Short acVoltageCtoN) {
        this.acVoltageCtoN = acVoltageCtoN;
    }

    public Short getAcVoltageSF() {
        return acVoltageSF;
    }

    public void setAcVoltageSF(Short acVoltageSF) {
        this.acVoltageSF = acVoltageSF;
    }

    public Short getAcFrequency() {
        return acFrequency;
    }

    public void setAcFrequency(Short acFrequency) {
        this.acFrequency = acFrequency;
    }

    public Short getAcFrequencySF() {
        return acFrequencySF;
    }

    public void setAcFrequencySF(Short acFrequencySF) {
        this.acFrequencySF = acFrequencySF;
    }

    public Short getAcRealPowerTotal() {
        return acRealPowerTotal;
    }

    public void setAcRealPowerTotal(Short acRealPowerTotal) {
        this.acRealPowerTotal = acRealPowerTotal;
    }

    public Short getAcRealPowerPhaseA() {
        return acRealPowerPhaseA;
    }

    public void setAcRealPowerPhaseA(Short acRealPowerPhaseA) {
        this.acRealPowerPhaseA = acRealPowerPhaseA;
    }

    public Short getAcRealPowerPhaseB() {
        return acRealPowerPhaseB;
    }

    public void setAcRealPowerPhaseB(Short acRealPowerPhaseB) {
        this.acRealPowerPhaseB = acRealPowerPhaseB;
    }

    public Short getAcRealPowerPhaseC() {
        return acRealPowerPhaseC;
    }

    public void setAcRealPowerPhaseC(Short acRealPowerPhaseC) {
        this.acRealPowerPhaseC = acRealPowerPhaseC;
    }

    public Short getAcRealPowerSF() {
        return acRealPowerSF;
    }

    public void setAcRealPowerSF(Short acRealPowerSF) {
        this.acRealPowerSF = acRealPowerSF;
    }

    public Short getAcApparentPowerTotal() {
        return acApparentPowerTotal;
    }

    public void setAcApparentPowerTotal(Short acApparentPowerTotal) {
        this.acApparentPowerTotal = acApparentPowerTotal;
    }

    public Short getAcApparentPowerPhaseA() {
        return acApparentPowerPhaseA;
    }

    public void setAcApparentPowerPhaseA(Short acApparentPowerPhaseA) {
        this.acApparentPowerPhaseA = acApparentPowerPhaseA;
    }

    public Short getAcApparentPowerPhaseB() {
        return acApparentPowerPhaseB;
    }

    public void setAcApparentPowerPhaseB(Short acApparentPowerPhaseB) {
        this.acApparentPowerPhaseB = acApparentPowerPhaseB;
    }

    public Short getAcApparentPowerPhaseC() {
        return acApparentPowerPhaseC;
    }

    public void setAcApparentPowerPhaseC(Short acApparentPowerPhaseC) {
        this.acApparentPowerPhaseC = acApparentPowerPhaseC;
    }

    public Short getAcApparentPowerSF() {
        return acApparentPowerSF;
    }

    public void setAcApparentPowerSF(Short acApparentPowerSF) {
        this.acApparentPowerSF = acApparentPowerSF;
    }

    public Short getAcReactivePowerTotal() {
        return acReactivePowerTotal;
    }

    public void setAcReactivePowerTotal(Short acReactivePowerTotal) {
        this.acReactivePowerTotal = acReactivePowerTotal;
    }

    public Short getAcReactivePowerPhaseA() {
        return acReactivePowerPhaseA;
    }

    public void setAcReactivePowerPhaseA(Short acReactivePowerPhaseA) {
        this.acReactivePowerPhaseA = acReactivePowerPhaseA;
    }

    public Short getAcReactivePowerPhaseB() {
        return acReactivePowerPhaseB;
    }

    public void setAcReactivePowerPhaseB(Short acReactivePowerPhaseB) {
        this.acReactivePowerPhaseB = acReactivePowerPhaseB;
    }

    public Short getAcReactivePowerPhaseC() {
        return acReactivePowerPhaseC;
    }

    public void setAcReactivePowerPhaseC(Short acReactivePowerPhaseC) {
        this.acReactivePowerPhaseC = acReactivePowerPhaseC;
    }

    public Short getAcReactivePowerSF() {
        return acReactivePowerSF;
    }

    public void setAcReactivePowerSF(Short acReactivePowerSF) {
        this.acReactivePowerSF = acReactivePowerSF;
    }

    public Short getAcPowerFactor() {
        return acPowerFactor;
    }

    public void setAcPowerFactor(Short acPowerFactor) {
        this.acPowerFactor = acPowerFactor;
    }

    public Short getAcPowerFactorPhaseA() {
        return acPowerFactorPhaseA;
    }

    public void setAcPowerFactorPhaseA(Short acPowerFactorPhaseA) {
        this.acPowerFactorPhaseA = acPowerFactorPhaseA;
    }

    public Short getAcPowerFactorPhaseB() {
        return acPowerFactorPhaseB;
    }

    public void setAcPowerFactorPhaseB(Short acPowerFactorPhaseB) {
        this.acPowerFactorPhaseB = acPowerFactorPhaseB;
    }

    public Short getAcPowerFactorPhaseC() {
        return acPowerFactorPhaseC;
    }

    public void setAcPowerFactorPhaseC(Short acPowerFactorPhaseC) {
        this.acPowerFactorPhaseC = acPowerFactorPhaseC;
    }

    public Short getAcPowerFactorSF() {
        return acPowerFactorSF;
    }

    public void setAcPowerFactorSF(Short acPowerFactorSF) {
        this.acPowerFactorSF = acPowerFactorSF;
    }

    public Long getAcExportedRealEnergyTotal() {
        return acExportedRealEnergyTotal;
    }

    public void setAcExportedRealEnergyTotal(Long acExportedRealEnergyTotal) {
        this.acExportedRealEnergyTotal = acExportedRealEnergyTotal;
    }

    public Long getAcExportedRealEnergyPhaseA() {
        return acExportedRealEnergyPhaseA;
    }

    public void setAcExportedRealEnergyPhaseA(Long acExportedRealEnergyPhaseA) {
        this.acExportedRealEnergyPhaseA = acExportedRealEnergyPhaseA;
    }

    public Long getAcExportedRealEnergyPhaseB() {
        return acExportedRealEnergyPhaseB;
    }

    public void setAcExportedRealEnergyPhaseB(Long acExportedRealEnergyPhaseB) {
        this.acExportedRealEnergyPhaseB = acExportedRealEnergyPhaseB;
    }

    public Long getAcExportedRealEnergyPhaseC() {
        return acExportedRealEnergyPhaseC;
    }

    public void setAcExportedRealEnergyPhaseC(Long acExportedRealEnergyPhaseC) {
        this.acExportedRealEnergyPhaseC = acExportedRealEnergyPhaseC;
    }

    public Long getAcImportedRealEnergyTotal() {
        return acImportedRealEnergyTotal;
    }

    public void setAcImportedRealEnergyTotal(Long acImportedRealEnergyTotal) {
        this.acImportedRealEnergyTotal = acImportedRealEnergyTotal;
    }

    public Long getAcImportedRealEnergyPhaseA() {
        return acImportedRealEnergyPhaseA;
    }

    public void setAcImportedRealEnergyPhaseA(Long acImportedRealEnergyPhaseA) {
        this.acImportedRealEnergyPhaseA = acImportedRealEnergyPhaseA;
    }

    public Long getAcImportedRealEnergyPhaseB() {
        return acImportedRealEnergyPhaseB;
    }

    public void setAcImportedRealEnergyPhaseB(Long acImportedRealEnergyPhaseB) {
        this.acImportedRealEnergyPhaseB = acImportedRealEnergyPhaseB;
    }

    public Long getAcImportedRealEnergyPhaseC() {
        return acImportedRealEnergyPhaseC;
    }

    public void setAcImportedRealEnergyPhaseC(Long acImportedRealEnergyPhaseC) {
        this.acImportedRealEnergyPhaseC = acImportedRealEnergyPhaseC;
    }

    public Short getAcRealEnergySF() {
        return acRealEnergySF;
    }

    public void setAcRealEnergySF(Short acRealEnergySF) {
        this.acRealEnergySF = acRealEnergySF;
    }

    public Long getAcExportedApparentEnergyTotal() {
        return acExportedApparentEnergyTotal;
    }

    public void setAcExportedApparentEnergyTotal(Long acExportedApparentEnergyTotal) {
        this.acExportedApparentEnergyTotal = acExportedApparentEnergyTotal;
    }

    public Long getAcExportedApparentEnergyPhaseA() {
        return acExportedApparentEnergyPhaseA;
    }

    public void setAcExportedApparentEnergyPhaseA(Long acExportedApparentEnergyPhaseA) {
        this.acExportedApparentEnergyPhaseA = acExportedApparentEnergyPhaseA;
    }

    public Long getAcExportedApparentEnergyPhaseB() {
        return acExportedApparentEnergyPhaseB;
    }

    public void setAcExportedApparentEnergyPhaseB(Long acExportedApparentEnergyPhaseB) {
        this.acExportedApparentEnergyPhaseB = acExportedApparentEnergyPhaseB;
    }

    public Long getAcExportedApparentEnergyPhaseC() {
        return acExportedApparentEnergyPhaseC;
    }

    public void setAcExportedApparentEnergyPhaseC(Long acExportedApparentEnergyPhaseC) {
        this.acExportedApparentEnergyPhaseC = acExportedApparentEnergyPhaseC;
    }

    public Long getAcImportedApparentEnergyTotal() {
        return acImportedApparentEnergyTotal;
    }

    public void setAcImportedApparentEnergyTotal(Long acImportedApparentEnergyTotal) {
        this.acImportedApparentEnergyTotal = acImportedApparentEnergyTotal;
    }

    public Long getAcImportedApparentEnergyPhaseA() {
        return acImportedApparentEnergyPhaseA;
    }

    public void setAcImportedApparentEnergyPhaseA(Long acImportedApparentEnergyPhaseA) {
        this.acImportedApparentEnergyPhaseA = acImportedApparentEnergyPhaseA;
    }

    public Long getAcImportedApparentEnergyPhaseB() {
        return acImportedApparentEnergyPhaseB;
    }

    public void setAcImportedApparentEnergyPhaseB(Long acImportedApparentEnergyPhaseB) {
        this.acImportedApparentEnergyPhaseB = acImportedApparentEnergyPhaseB;
    }

    public Long getAcImportedApparentEnergyPhaseC() {
        return acImportedApparentEnergyPhaseC;
    }

    public void setAcImportedApparentEnergyPhaseC(Long acImportedApparentEnergyPhaseC) {
        this.acImportedApparentEnergyPhaseC = acImportedApparentEnergyPhaseC;
    }

    public Short getAcApparentEnergySF() {
        return acApparentEnergySF;
    }

    public void setAcApparentEnergySF(Short acApparentEnergySF) {
        this.acApparentEnergySF = acApparentEnergySF;
    }

    public Long getAcImportedReactiveEnergyQ1Total() {
        return acImportedReactiveEnergyQ1Total;
    }

    public void setAcImportedReactiveEnergyQ1Total(Long acImportedReactiveEnergyQ1Total) {
        this.acImportedReactiveEnergyQ1Total = acImportedReactiveEnergyQ1Total;
    }

    public Long getAcImportedReactiveEnergyQ1PhaseA() {
        return acImportedReactiveEnergyQ1PhaseA;
    }

    public void setAcImportedReactiveEnergyQ1PhaseA(Long acImportedReactiveEnergyQ1PhaseA) {
        this.acImportedReactiveEnergyQ1PhaseA = acImportedReactiveEnergyQ1PhaseA;
    }

    public Long getAcImportedReactiveEnergyQ1PhaseB() {
        return acImportedReactiveEnergyQ1PhaseB;
    }

    public void setAcImportedReactiveEnergyQ1PhaseB(Long acImportedReactiveEnergyQ1PhaseB) {
        this.acImportedReactiveEnergyQ1PhaseB = acImportedReactiveEnergyQ1PhaseB;
    }

    public Long getAcImportedReactiveEnergyQ1PhaseC() {
        return acImportedReactiveEnergyQ1PhaseC;
    }

    public void setAcImportedReactiveEnergyQ1PhaseC(Long acImportedReactiveEnergyQ1PhaseC) {
        this.acImportedReactiveEnergyQ1PhaseC = acImportedReactiveEnergyQ1PhaseC;
    }

    public Long getAcImportedReactiveEnergyQ2Total() {
        return acImportedReactiveEnergyQ2Total;
    }

    public void setAcImportedReactiveEnergyQ2Total(Long acImportedReactiveEnergyQ2Total) {
        this.acImportedReactiveEnergyQ2Total = acImportedReactiveEnergyQ2Total;
    }

    public Long getAcImportedReactiveEnergyQ2PhaseA() {
        return acImportedReactiveEnergyQ2PhaseA;
    }

    public void setAcImportedReactiveEnergyQ2PhaseA(Long acImportedReactiveEnergyQ2PhaseA) {
        this.acImportedReactiveEnergyQ2PhaseA = acImportedReactiveEnergyQ2PhaseA;
    }

    public Long getAcImportedReactiveEnergyQ2PhaseB() {
        return acImportedReactiveEnergyQ2PhaseB;
    }

    public void setAcImportedReactiveEnergyQ2PhaseB(Long acImportedReactiveEnergyQ2PhaseB) {
        this.acImportedReactiveEnergyQ2PhaseB = acImportedReactiveEnergyQ2PhaseB;
    }

    public Long getAcImportedReactiveEnergyQ2PhaseC() {
        return acImportedReactiveEnergyQ2PhaseC;
    }

    public void setAcImportedReactiveEnergyQ2PhaseC(Long acImportedReactiveEnergyQ2PhaseC) {
        this.acImportedReactiveEnergyQ2PhaseC = acImportedReactiveEnergyQ2PhaseC;
    }

    public Long getAcExportedReactiveEnergyQ3Total() {
        return acExportedReactiveEnergyQ3Total;
    }

    public void setAcExportedReactiveEnergyQ3Total(Long acExportedReactiveEnergyQ3Total) {
        this.acExportedReactiveEnergyQ3Total = acExportedReactiveEnergyQ3Total;
    }

    public Long getAcExportedReactiveEnergyQ3PhaseA() {
        return acExportedReactiveEnergyQ3PhaseA;
    }

    public void setAcExportedReactiveEnergyQ3PhaseA(Long acExportedReactiveEnergyQ3PhaseA) {
        this.acExportedReactiveEnergyQ3PhaseA = acExportedReactiveEnergyQ3PhaseA;
    }

    public Long getAcExportedReactiveEnergyQ3PhaseB() {
        return acExportedReactiveEnergyQ3PhaseB;
    }

    public void setAcExportedReactiveEnergyQ3PhaseB(Long acExportedReactiveEnergyQ3PhaseB) {
        this.acExportedReactiveEnergyQ3PhaseB = acExportedReactiveEnergyQ3PhaseB;
    }

    public Long getAcExportedReactiveEnergyQ3PhaseC() {
        return acExportedReactiveEnergyQ3PhaseC;
    }

    public void setAcExportedReactiveEnergyQ3PhaseC(Long acExportedReactiveEnergyQ3PhaseC) {
        this.acExportedReactiveEnergyQ3PhaseC = acExportedReactiveEnergyQ3PhaseC;
    }

    public Long getAcExportedReactiveEnergyQ4Total() {
        return acExportedReactiveEnergyQ4Total;
    }

    public void setAcExportedReactiveEnergyQ4Total(Long acExportedReactiveEnergyQ4Total) {
        this.acExportedReactiveEnergyQ4Total = acExportedReactiveEnergyQ4Total;
    }

    public Long getAcExportedReactiveEnergyQ4PhaseA() {
        return acExportedReactiveEnergyQ4PhaseA;
    }

    public void setAcExportedReactiveEnergyQ4PhaseA(Long acExportedReactiveEnergyQ4PhaseA) {
        this.acExportedReactiveEnergyQ4PhaseA = acExportedReactiveEnergyQ4PhaseA;
    }

    public Long getAcExportedReactiveEnergyQ4PhaseB() {
        return acExportedReactiveEnergyQ4PhaseB;
    }

    public void setAcExportedReactiveEnergyQ4PhaseB(Long acExportedReactiveEnergyQ4PhaseB) {
        this.acExportedReactiveEnergyQ4PhaseB = acExportedReactiveEnergyQ4PhaseB;
    }

    public Long getAcExportedReactiveEnergyQ4PhaseC() {
        return acExportedReactiveEnergyQ4PhaseC;
    }

    public void setAcExportedReactiveEnergyQ4PhaseC(Long acExportedReactiveEnergyQ4PhaseC) {
        this.acExportedReactiveEnergyQ4PhaseC = acExportedReactiveEnergyQ4PhaseC;
    }

    public Short getAcReactiveEnergySF() {
        return acReactiveEnergySF;
    }

    public void setAcReactiveEnergySF(Short acReactiveEnergySF) {
        this.acReactiveEnergySF = acReactiveEnergySF;
    }

}
