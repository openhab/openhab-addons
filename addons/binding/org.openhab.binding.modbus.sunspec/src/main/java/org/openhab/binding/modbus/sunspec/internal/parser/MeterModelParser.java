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
package org.openhab.binding.modbus.sunspec.internal.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.modbus.sunspec.internal.block.MeterModelBlock;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * Parser for sunspec compatible meters
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
public class MeterModelParser extends AbstractBaseParser implements SunspecParser<MeterModelBlock> {

    @Override
    public MeterModelBlock parse(@NonNull ModbusRegisterArray raw) {
        MeterModelBlock block = new MeterModelBlock();

        block.setSunspecDID(extractUInt16(raw, 0));

        block.setLength(extractUInt16(raw, 1));

        block.setAcCurrentTotal(extractInt16(raw, 2));

        block.setAcCurrentPhaseA(extractInt16(raw, 3));

        block.setAcCurrentPhaseB(extractInt16(raw, 4));

        block.setAcCurrentPhaseC(extractInt16(raw, 5));

        block.setAcCurrentSF(extractSunSSF(raw, 6));

        block.setAcVoltageLinetoNAverage(extractInt16(raw, 7));

        block.setAcVoltageAtoN(extractInt16(raw, 8));

        block.setAcVoltageBtoN(extractInt16(raw, 9));

        block.setAcVoltageCtoN(extractInt16(raw, 10));

        block.setAcVoltageLineToLineAverage(extractInt16(raw, 11));

        block.setAcVoltageAB(extractInt16(raw, 12));

        block.setAcVoltageBC(extractInt16(raw, 13));

        block.setAcVoltageCA(extractInt16(raw, 14));

        block.setAcVoltageSF(extractSunSSF(raw, 15));

        block.setAcFrequency(extractInt16(raw, 16));

        block.setAcFrequencySF(extractSunSSF(raw, 17));

        block.setAcRealPowerTotal(extractInt16(raw, 18));

        block.setAcRealPowerPhaseA(extractInt16(raw, 19));

        block.setAcRealPowerPhaseB(extractInt16(raw, 20));

        block.setAcRealPowerPhaseC(extractInt16(raw, 21));

        block.setAcRealPowerSF(extractSunSSF(raw, 22));

        block.setAcApparentPowerTotal(extractInt16(raw, 23));

        block.setAcApparentPowerPhaseA(extractInt16(raw, 24));

        block.setAcApparentPowerPhaseB(extractInt16(raw, 25));

        block.setAcApparentPowerPhaseC(extractInt16(raw, 26));

        block.setAcApparentPowerSF(extractSunSSF(raw, 27));

        block.setAcReactivePowerTotal(extractInt16(raw, 28));

        block.setAcReactivePowerPhaseA(extractInt16(raw, 29));

        block.setAcReactivePowerPhaseB(extractInt16(raw, 30));

        block.setAcReactivePowerPhaseC(extractInt16(raw, 31));

        block.setAcReactivePowerSF(extractSunSSF(raw, 32));

        block.setAcPowerFactor(extractInt16(raw, 33));

        block.setAcPowerFactorPhaseA(extractInt16(raw, 34));

        block.setAcPowerFactorPhaseB(extractInt16(raw, 35));

        block.setAcPowerFactorPhaseC(extractInt16(raw, 36));

        block.setAcPowerFactorSF(extractSunSSF(raw, 37));

        block.setAcExportedRealEnergyTotal(extractAcc32(raw, 38));

        block.setAcExportedRealEnergyPhaseA(extractAcc32(raw, 40));

        block.setAcExportedRealEnergyPhaseB(extractAcc32(raw, 42));

        block.setAcExportedRealEnergyPhaseC(extractAcc32(raw, 44));

        block.setAcImportedRealEnergyTotal(extractAcc32(raw, 46));

        block.setAcImportedRealEnergyPhaseA(extractAcc32(raw, 48));

        block.setAcImportedRealEnergyPhaseB(extractAcc32(raw, 50));

        block.setAcImportedRealEnergyPhaseC(extractAcc32(raw, 52));

        block.setAcRealEnergySF(extractSunSSF(raw, 54));

        block.setAcExportedApparentEnergyTotal(extractAcc32(raw, 55));

        block.setAcExportedApparentEnergyPhaseA(extractAcc32(raw, 57));

        block.setAcExportedApparentEnergyPhaseB(extractAcc32(raw, 59));

        block.setAcExportedApparentEnergyPhaseC(extractAcc32(raw, 61));

        block.setAcImportedApparentEnergyTotal(extractAcc32(raw, 63));

        block.setAcImportedApparentEnergyPhaseA(extractAcc32(raw, 65));

        block.setAcImportedApparentEnergyPhaseB(extractAcc32(raw, 67));

        block.setAcImportedApparentEnergyPhaseC(extractAcc32(raw, 69));

        block.setAcApparentEnergySF(extractSunSSF(raw, 71));

        block.setAcImportedReactiveEnergyQ1Total(extractAcc32(raw, 72));

        block.setAcImportedReactiveEnergyQ1PhaseA(extractAcc32(raw, 74));

        block.setAcImportedReactiveEnergyQ1PhaseB(extractAcc32(raw, 76));

        block.setAcImportedReactiveEnergyQ1PhaseC(extractAcc32(raw, 78));

        block.setAcImportedReactiveEnergyQ2Total(extractAcc32(raw, 80));

        block.setAcImportedReactiveEnergyQ2PhaseA(extractAcc32(raw, 82));

        block.setAcImportedReactiveEnergyQ2PhaseB(extractAcc32(raw, 84));

        block.setAcImportedReactiveEnergyQ2PhaseC(extractAcc32(raw, 86));

        block.setAcExportedReactiveEnergyQ3Total(extractAcc32(raw, 88));

        block.setAcExportedReactiveEnergyQ3PhaseA(extractAcc32(raw, 90));

        block.setAcExportedReactiveEnergyQ3PhaseB(extractAcc32(raw, 92));

        block.setAcExportedReactiveEnergyQ3PhaseC(extractAcc32(raw, 94));

        block.setAcExportedReactiveEnergyQ4Total(extractAcc32(raw, 96));

        block.setAcExportedReactiveEnergyQ4PhaseA(extractAcc32(raw, 98));

        block.setAcExportedReactiveEnergyQ4PhaseB(extractAcc32(raw, 100));

        block.setAcExportedReactiveEnergyQ4PhaseC(extractAcc32(raw, 102));

        block.setAcReactiveEnergySF(extractSunSSF(raw, 104));

        return block;
    }
}
