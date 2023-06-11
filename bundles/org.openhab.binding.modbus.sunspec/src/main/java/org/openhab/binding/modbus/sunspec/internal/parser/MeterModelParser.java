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
package org.openhab.binding.modbus.sunspec.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sunspec.internal.SunSpecConstants;
import org.openhab.binding.modbus.sunspec.internal.dto.MeterModelBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

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
        block.phaseA.acPhaseCurrent = extractOptionalInt16(raw, 3);
        block.phaseB.acPhaseCurrent = extractOptionalInt16(raw, 4);
        block.phaseC.acPhaseCurrent = extractOptionalInt16(raw, 5);
        block.acCurrentSF = extractSunSSF(raw, 6);

        block.acVoltageLineToNAverage = extractOptionalInt16(raw, 7);
        block.phaseA.acVoltageToN = extractOptionalInt16(raw, 8);
        block.phaseB.acVoltageToN = extractOptionalInt16(raw, 9);
        block.phaseC.acVoltageToN = extractOptionalInt16(raw, 10);
        block.acVoltageLineToLineAverage = extractOptionalInt16(raw, 11);
        block.phaseA.acVoltageToNext = extractOptionalInt16(raw, 12);
        block.phaseB.acVoltageToNext = extractOptionalInt16(raw, 13);
        block.phaseC.acVoltageToNext = extractOptionalInt16(raw, 14);
        block.acVoltageSF = extractSunSSF(raw, 15);

        block.acFrequency = extractInt16(raw, 16, (short) 0);
        block.acFrequencySF = extractOptionalSunSSF(raw, 17);

        block.acRealPowerTotal = extractInt16(raw, 18, (short) 0);
        block.phaseA.acRealPower = extractOptionalInt16(raw, 19);
        block.phaseB.acRealPower = extractOptionalInt16(raw, 20);
        block.phaseC.acRealPower = extractOptionalInt16(raw, 21);
        block.acRealPowerSF = extractSunSSF(raw, 22);

        block.acApparentPowerTotal = extractOptionalInt16(raw, 23);
        block.phaseA.acApparentPower = extractOptionalInt16(raw, 24);
        block.phaseB.acApparentPower = extractOptionalInt16(raw, 25);
        block.phaseC.acApparentPower = extractOptionalInt16(raw, 26);
        block.acApparentPowerSF = extractOptionalSunSSF(raw, 27);

        block.acReactivePowerTotal = extractOptionalInt16(raw, 28);
        block.phaseA.acReactivePower = extractOptionalInt16(raw, 29);
        block.phaseB.acReactivePower = extractOptionalInt16(raw, 30);
        block.phaseC.acReactivePower = extractOptionalInt16(raw, 31);
        block.acReactivePowerSF = extractOptionalSunSSF(raw, 32);

        block.acPowerFactor = extractOptionalInt16(raw, 33);
        block.phaseA.acPowerFactor = extractOptionalInt16(raw, 34);
        block.phaseB.acPowerFactor = extractOptionalInt16(raw, 35);
        block.phaseC.acPowerFactor = extractOptionalInt16(raw, 36);
        block.acPowerFactorSF = extractOptionalSunSSF(raw, 37);

        block.acExportedRealEnergyTotal = extractOptionalAcc32(raw, 38);
        block.phaseA.acExportedRealEnergy = extractOptionalAcc32(raw, 40);
        block.phaseB.acExportedRealEnergy = extractOptionalAcc32(raw, 42);
        block.phaseC.acExportedRealEnergy = extractOptionalAcc32(raw, 44);
        block.acImportedRealEnergyTotal = extractAcc32(raw, 46, 0);
        block.phaseA.acImportedRealEnergy = extractOptionalAcc32(raw, 48);
        block.phaseB.acImportedRealEnergy = extractOptionalAcc32(raw, 50);
        block.phaseC.acImportedRealEnergy = extractOptionalAcc32(raw, 52);
        block.acRealEnergySF = extractSunSSF(raw, 54);

        block.acExportedApparentEnergyTotal = extractOptionalAcc32(raw, 55);
        block.phaseA.acExportedApparentEnergy = extractOptionalAcc32(raw, 57);
        block.phaseB.acExportedApparentEnergy = extractOptionalAcc32(raw, 59);
        block.phaseC.acExportedApparentEnergy = extractOptionalAcc32(raw, 61);
        block.acImportedApparentEnergyTotal = extractOptionalAcc32(raw, 63);
        block.phaseA.acImportedApparentEnergy = extractOptionalAcc32(raw, 65);
        block.phaseB.acImportedApparentEnergy = extractOptionalAcc32(raw, 67);
        block.phaseC.acImportedApparentEnergy = extractOptionalAcc32(raw, 69);
        block.acApparentEnergySF = extractOptionalSunSSF(raw, 71);

        block.acImportedReactiveEnergyQ1Total = extractOptionalAcc32(raw, 72);
        block.phaseA.acImportedReactiveEnergyQ1 = extractOptionalAcc32(raw, 74);
        block.phaseB.acImportedReactiveEnergyQ1 = extractOptionalAcc32(raw, 76);
        block.phaseC.acImportedReactiveEnergyQ1 = extractOptionalAcc32(raw, 78);
        block.acImportedReactiveEnergyQ2Total = extractOptionalAcc32(raw, 80);
        block.phaseA.acImportedReactiveEnergyQ2 = extractOptionalAcc32(raw, 82);
        block.phaseB.acImportedReactiveEnergyQ2 = extractOptionalAcc32(raw, 84);
        block.phaseC.acImportedReactiveEnergyQ2 = extractOptionalAcc32(raw, 86);
        block.acExportedReactiveEnergyQ3Total = extractOptionalAcc32(raw, 88);
        block.phaseA.acExportedReactiveEnergyQ3 = extractOptionalAcc32(raw, 90);
        block.phaseB.acExportedReactiveEnergyQ3 = extractOptionalAcc32(raw, 92);
        block.phaseC.acExportedReactiveEnergyQ3 = extractOptionalAcc32(raw, 94);
        block.acExportedReactiveEnergyQ4Total = extractOptionalAcc32(raw, 96);
        block.phaseA.acExportedReactiveEnergyQ4 = extractOptionalAcc32(raw, 98);
        block.phaseB.acExportedReactiveEnergyQ4 = extractOptionalAcc32(raw, 100);
        block.phaseC.acExportedReactiveEnergyQ4 = extractOptionalAcc32(raw, 102);
        block.acReactiveEnergySF = extractOptionalSunSSF(raw, 104);

        return block;
    }
}
