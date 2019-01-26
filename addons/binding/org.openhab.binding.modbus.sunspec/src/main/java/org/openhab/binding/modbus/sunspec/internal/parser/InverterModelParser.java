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
import org.openhab.binding.modbus.sunspec.internal.block.InverterModelBlock;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into an InverterModelBlock
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
public class InverterModelParser extends AbstractBaseParser implements SunspecParser<InverterModelBlock> {

    @Override
    public InverterModelBlock parse(@NonNull ModbusRegisterArray raw) {
        InverterModelBlock block = new InverterModelBlock();

        block.setPhaseConfiguration(extractUInt16(raw, 0));

        block.setLength(extractUInt16(raw, 1));

        block.setAcCurrentTotal(extractUInt16(raw, 2));

        block.setAcCurrentPhaseA(extractUInt16(raw, 3));

        block.setAcCurrentPhaseB(extractUInt16(raw, 4));

        block.setAcCurrentPhaseC(extractUInt16(raw, 5));

        block.setAcCurrentSF(extractSunSSF(raw, 6));

        block.setAcVoltageAB(extractUInt16(raw, 7));

        block.setAcVoltageBC(extractUInt16(raw, 8));

        block.setAcVoltageCA(extractUInt16(raw, 9));

        block.setAcVoltageAtoN(extractUInt16(raw, 10));

        block.setAcVoltageBtoN(extractUInt16(raw, 11));

        block.setAcVoltageCtoN(extractUInt16(raw, 12));

        block.setAcVoltageSF(extractSunSSF(raw, 13));

        block.setAcPower(extractInt16(raw, 14));

        block.setAcPowerSF(extractSunSSF(raw, 15));

        block.setAcFrequency(extractUInt16(raw, 16));

        block.setAcFrequencySF(extractSunSSF(raw, 17));

        block.setAcApparentPower(extractInt16(raw, 18));

        block.setAcApparentPowerSF(extractSunSSF(raw, 19));

        block.setAcReactivePower(extractInt16(raw, 20));

        block.setAcReactivePowerSF(extractSunSSF(raw, 21));

        block.setAcPowerFactor(extractInt16(raw, 22));

        block.setAcPowerFactorSF(extractSunSSF(raw, 23));

        block.setAcEnergyLifetime(extractAcc32(raw, 24));

        block.setAcEnergyLifetimeSF(extractSunSSF(raw, 26));

        block.setDcCurrent(extractUInt16(raw, 27));

        block.setDcCurrentSF(extractSunSSF(raw, 28));

        block.setDcVoltage(extractUInt16(raw, 29));

        block.setDcVoltageSF(extractSunSSF(raw, 30));

        block.setDcPower(extractInt16(raw, 31));

        block.setDcPowerSF(extractSunSSF(raw, 32));

        block.setTemperatureCabinet(extractInt16(raw, 33));

        block.setTemperatureHeatsink(extractInt16(raw, 34));

        block.setTemperatureTransformer(extractInt16(raw, 35));

        block.setTemperatureOther(extractInt16(raw, 36));

        block.setTemperatureSF(extractSunSSF(raw, 37));

        block.setStatus(extractUInt16(raw, 38));

        block.setStatusVendor(extractUInt16(raw, 39));

        return block;
    }

}
