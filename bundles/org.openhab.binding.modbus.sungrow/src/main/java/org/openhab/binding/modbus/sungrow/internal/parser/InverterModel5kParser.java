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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sungrow.internal.dto.InverterModelBlock5k;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into an InverterModelBlock5k
 *
 * @author Nagy Attila Gabor - Initial contribution
 * @author Ferdinand Schwenk - reused for sungrow bundle
 *
 */
@NonNullByDefault
public class InverterModel5kParser extends AbstractBaseParser implements SungrowParser<InverterModelBlock5k> {

    @Override
    public InverterModelBlock5k parse(ModbusRegisterArray raw) {
        InverterModelBlock5k block = new InverterModelBlock5k();

        block.deviceType = extractUInt16(raw, 0, 0);
        block.nominalOutputPower = extractUInt16(raw, 1, 0);
        block.outputType = extractUInt16(raw, 2, 0);

        block.dailyOutputEnergy = extractUInt16(raw, 3, 0);
        block.totalOutputEnergy = extractUInt32Swap(raw, 4, 0L);

        block.insideTemperature = extractSInt16(raw, 8, (short) 0);

        block.mppt1Voltage = extractUInt16(raw, 11, 0);
        block.mppt1Current = extractUInt16(raw, 12, 0);
        block.mppt1Power = Optional.of(block.mppt1Voltage * block.mppt1Current);
        block.mppt2Voltage = extractUInt16(raw, 13, 0);
        block.mppt2Current = extractUInt16(raw, 14, 0);
        block.mppt2Power = Optional.of(block.mppt2Voltage * block.mppt2Current);

        block.totalDCPower = extractUInt32Swap(raw, 17, 0L);

        if ((block.outputType == 1) || (block.outputType == 2)) {
            block.acVoltageAB = Optional.empty();
            block.acVoltageBC = Optional.empty();
            block.acVoltageCA = Optional.empty();
            block.acVoltageAtoN = Optional.of(extractUInt16(raw, 19, 0));
            block.acVoltageBtoN = Optional.of(extractUInt16(raw, 21, 0));
            block.acVoltageCtoN = Optional.of(extractUInt16(raw, 21, 0));
        } else if (block.outputType == 3) {
            block.acVoltageAB = Optional.of(extractUInt16(raw, 19, 0));
            block.acVoltageBC = Optional.of(extractUInt16(raw, 20, 0));
            block.acVoltageCA = Optional.of(extractUInt16(raw, 21, 0));
            block.acVoltageAtoN = Optional.empty();
            block.acVoltageBtoN = Optional.empty();
            block.acVoltageCtoN = Optional.empty();
        } else {
            block.acVoltageAB = Optional.empty();
            block.acVoltageBC = Optional.empty();
            block.acVoltageCA = Optional.empty();
            block.acVoltageAtoN = Optional.empty();
            block.acVoltageBtoN = Optional.empty();
            block.acVoltageCtoN = Optional.empty();
        }

        block.acReactivePower = extractSInt32Swap(raw, 33, 0L);
        block.acPowerFactor = extractSInt16(raw, 35, (short) 0);
        block.acFrequency = extractUInt16(raw, 36, 0);

        return block;
    }
}
