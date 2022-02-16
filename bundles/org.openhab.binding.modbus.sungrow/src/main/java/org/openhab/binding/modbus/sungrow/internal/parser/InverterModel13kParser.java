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
package org.openhab.binding.modbus.sungrow.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sungrow.internal.dto.InverterModelBlock13k;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into an InverterModelBlock13k
 *
 * @author Nagy Attila Gabor - Initial contribution
 * @author Ferdinand Schwenk - reused for sungrow bundle
 *
 */
@NonNullByDefault
public class InverterModel13kParser extends AbstractBaseParser implements SungrowParser<InverterModelBlock13k> {

    @Override
    public InverterModelBlock13k parse(ModbusRegisterArray raw) {
        InverterModelBlock13k block = new InverterModelBlock13k();

        block.systemState = extractUInt16(raw, 0, 0);
        block.runningState = extractUInt16(raw, 1, 0);

        block.dailyPVGeneration = extractUInt16(raw, 2, 0);
        block.totalPVGeneration = extractUInt32Swap(raw, 3, 0);
        block.dailyPVExport = extractUInt16(raw, 5, 0);
        block.totalPVExport = extractUInt32Swap(raw, 6, 0);

        block.loadPower = extractSInt32Swap(raw, 8, 0);
        block.exportPower = extractSInt32Swap(raw, 10, 0);

        block.dailyBatteryCharge = extractUInt16(raw, 12, 0);
        block.totalBatteryCharge = extractUInt32Swap(raw, 13, 0);
        block.co2Reduction = extractUInt32Swap(raw, 15, 0);

        block.dailyDirectConsumption = extractUInt16(raw, 17, 0);
        block.totalDirectConsumption = extractUInt32Swap(raw, 18, 0);

        block.batteryCapacity = extractUInt16(raw, 39, 0);
        block.batteryVoltage = extractUInt16(raw, 20, 0);
        block.batteryCurrent = extractUInt16(raw, 21, 0);
        block.batteryPower = extractUInt16(raw, 22, 0);
        block.batteryLevel = extractUInt16(raw, 23, 0);
        block.batteryHealth = extractUInt16(raw, 24, 0);
        block.batteryTemperature = extractSInt16(raw, 25, (short) 0);
        block.dailyBatteryDischarge = extractUInt16(raw, 26, 0);
        block.totalBatteryDischarge = extractUInt32Swap(raw, 27, 0);
        block.dailyChargeEnergy = extractUInt16(raw, 40, 0);
        block.totalChargeEnergy = extractUInt32Swap(raw, 41, 0);
        block.todaySelfConsumption = extractUInt16(raw, 29, 0);

        block.gridState = extractUInt16(raw, 30, 0);
        block.drmState = extractUInt16(raw, 43, 0);
        block.acCurrentPhaseA = extractSInt16(raw, 31, (short) 0);
        block.acCurrentPhaseB = extractOptionalSInt16(raw, 32);
        block.acCurrentPhaseC = extractOptionalSInt16(raw, 33);
        block.totalActivePower = extractSInt32Swap(raw, 34, 0);
        block.dailyImportEnergy = extractUInt16(raw, 36, 0);
        block.totalImportEnergy = extractUInt32Swap(raw, 37, 0);
        block.dailyExportEnergy = extractUInt16(raw, 45, 0);
        block.totalExportEnergy = extractUInt32Swap(raw, 46, 0);

        return block;
    }
}
