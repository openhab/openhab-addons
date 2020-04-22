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
package org.openhab.binding.modbus.sunspec.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sunspec.internal.SunSpecConstants;
import org.openhab.binding.modbus.sunspec.internal.dto.MeterModelBlock;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * Parser for sunspec compatible meters
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
@NonNullByDefault
public class MeterModelParser extends AbstractBaseParser implements SunspecParser<MeterModelBlock> {

    @Override
    public MeterModelBlock parse(ModbusRegisterArray raw) {
        MeterModelBlock block = new MeterModelBlock();

        block.sunspecDID = extractUInt16(raw, 0, SunSpecConstants.METER_SINGLE_PHASE);
        block.length = extractUInt16(raw, 1, raw.size());
        block.acCurrentTotal = extractInt16(raw, 2, (short) 0);
        block.acCurrentPhaseA = extractInt16(raw, 3, (short) 0);
        block.acCurrentPhaseB = extractOptionalInt16(raw, 4);
        block.acCurrentPhaseC = extractOptionalInt16(raw, 5);
        block.acCurrentSF = extractSunSSF(raw, 6);

        block.acVoltageLineToNAverage = extractOptionalInt16(raw, 7);
        block.acVoltageAtoN = extractOptionalInt16(raw, 8);
        block.acVoltageBtoN = extractOptionalInt16(raw, 9);
        block.acVoltageCtoN = extractOptionalInt16(raw, 10);
        block.acVoltageLineToLineAverage = extractOptionalInt16(raw, 11);
        block.acVoltageAB = extractOptionalInt16(raw, 12);
        block.acVoltageBC = extractOptionalInt16(raw, 13);
        block.acVoltageCA = extractOptionalInt16(raw, 14);
        block.acVoltageSF = extractSunSSF(raw, 15);

        block.acFrequency = extractInt16(raw, 16, (short) 0);
        block.acFrequencySF = extractOptionalSunSSF(raw, 17);

        block.acRealPowerTotal = extractInt16(raw, 18, (short) 0);
        block.acRealPowerPhaseA = extractOptionalInt16(raw, 19);
        block.acRealPowerPhaseB = extractOptionalInt16(raw, 20);
        block.acRealPowerPhaseC = extractOptionalInt16(raw, 21);
        block.acRealPowerSF = extractSunSSF(raw, 22);

        block.acApparentPowerTotal = extractOptionalInt16(raw, 23);
        block.acApparentPowerPhaseA = extractOptionalInt16(raw, 24);
        block.acApparentPowerPhaseB = extractOptionalInt16(raw, 25);
        block.acApparentPowerPhaseC = extractOptionalInt16(raw, 26);
        block.acApparentPowerSF = extractOptionalSunSSF(raw, 27);

        block.acReactivePowerTotal = extractOptionalInt16(raw, 28);
        block.acReactivePowerPhaseA = extractOptionalInt16(raw, 29);
        block.acReactivePowerPhaseB = extractOptionalInt16(raw, 30);
        block.acReactivePowerPhaseC = extractOptionalInt16(raw, 31);
        block.acReactivePowerSF = extractOptionalSunSSF(raw, 32);

        block.acPowerFactor = extractOptionalInt16(raw, 33);
        block.acPowerFactorPhaseA = extractOptionalInt16(raw, 34);
        block.acPowerFactorPhaseB = extractOptionalInt16(raw, 35);
        block.acPowerFactorPhaseC = extractOptionalInt16(raw, 36);
        block.acPowerFactorSF = extractOptionalSunSSF(raw, 37);

        block.acExportedRealEnergyTotal = extractOptionalAcc32(raw, 38);
        block.acExportedRealEnergyPhaseA = extractOptionalAcc32(raw, 40);
        block.acExportedRealEnergyPhaseB = extractOptionalAcc32(raw, 42);
        block.acExportedRealEnergyPhaseC = extractOptionalAcc32(raw, 44);
        block.acImportedRealEnergyTotal = extractAcc32(raw, 46, 0);
        block.acImportedRealEnergyPhaseA = extractOptionalAcc32(raw, 48);
        block.acImportedRealEnergyPhaseB = extractOptionalAcc32(raw, 50);
        block.acImportedRealEnergyPhaseC = extractOptionalAcc32(raw, 52);
        block.acRealEnergySF = extractSunSSF(raw, 54);

        block.acExportedApparentEnergyTotal = extractOptionalAcc32(raw, 55);
        block.acExportedApparentEnergyPhaseA = extractOptionalAcc32(raw, 57);
        block.acExportedApparentEnergyPhaseB = extractOptionalAcc32(raw, 59);
        block.acExportedApparentEnergyPhaseC = extractOptionalAcc32(raw, 61);
        block.acImportedApparentEnergyTotal = extractOptionalAcc32(raw, 63);
        block.acImportedApparentEnergyPhaseA = extractOptionalAcc32(raw, 65);
        block.acImportedApparentEnergyPhaseB = extractOptionalAcc32(raw, 67);
        block.acImportedApparentEnergyPhaseC = extractOptionalAcc32(raw, 69);
        block.acApparentEnergySF = extractOptionalSunSSF(raw, 71);

        block.acImportedReactiveEnergyQ1Total = extractOptionalAcc32(raw, 72);
        block.acImportedReactiveEnergyQ1PhaseA = extractOptionalAcc32(raw, 74);
        block.acImportedReactiveEnergyQ1PhaseB = extractOptionalAcc32(raw, 76);
        block.acImportedReactiveEnergyQ1PhaseC = extractOptionalAcc32(raw, 78);
        block.acImportedReactiveEnergyQ2Total = extractOptionalAcc32(raw, 80);
        block.acImportedReactiveEnergyQ2PhaseA = extractOptionalAcc32(raw, 82);
        block.acImportedReactiveEnergyQ2PhaseB = extractOptionalAcc32(raw, 84);
        block.acImportedReactiveEnergyQ2PhaseC = extractOptionalAcc32(raw, 86);
        block.acExportedReactiveEnergyQ3Total = extractOptionalAcc32(raw, 88);
        block.acExportedReactiveEnergyQ3PhaseA = extractOptionalAcc32(raw, 90);
        block.acExportedReactiveEnergyQ3PhaseB = extractOptionalAcc32(raw, 92);
        block.acExportedReactiveEnergyQ3PhaseC = extractOptionalAcc32(raw, 94);
        block.acExportedReactiveEnergyQ4Total = extractOptionalAcc32(raw, 96);
        block.acExportedReactiveEnergyQ4PhaseA = extractOptionalAcc32(raw, 98);
        block.acExportedReactiveEnergyQ4PhaseB = extractOptionalAcc32(raw, 100);
        block.acExportedReactiveEnergyQ4PhaseC = extractOptionalAcc32(raw, 102);
        block.acReactiveEnergySF = extractOptionalSunSSF(raw, 104);

        return block;
    }
}
