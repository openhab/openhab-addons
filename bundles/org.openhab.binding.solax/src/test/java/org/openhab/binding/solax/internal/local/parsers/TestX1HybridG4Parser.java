/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solax.internal.local.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.local.AbstractParserTest;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.local.LocalData;

/**
 * The {@link TestX1HybridG4Parser} Simple test that tests for proper parsing against a real data from the inverter
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX1HybridG4Parser extends AbstractParserTest {

    private static final String RAW_DATA = """
            {
                sn:SOME_SERIAL_NUMBER,
                ver:3.008.10,
                type:15,
                Data:[
                    2388,21,460,4998,4483,4483,10,1,487,65,
                    2,59781,0,70,12180,500,605,33,99,12000,
                    0,23159,0,57,100,0,39,4501,0,0,
                    0,0,12,0,13240,0,63348,2,448,43,
                    256,1314,900,0,350,311,279,33,33,279,1,1,652,0,708,1,65077,65535,65386,65535,0,0,0,0,0,0,0,0,0,0,0,0,65068,65535,4500,0,61036,65535,10,0,90,0,0,12,0,116,7,57,0,0,2320,0,110,0,0,0,0,0,0,12544,7440,5896,594,521,9252,0,0,0,0,0,1,1201,0,0,3342,3336,7296,54,21302,14389,18753,12852,16692,12355,13618,21302,14389,18753,12852,16692,12355,13618,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1025,4609,1026,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
                Information:[7.500,15,H4752TI1063020,8,1.24,0.00,1.21,1.03,0.00,1]}
            """;

    @Override
    protected InverterType getInverterType() {
        return InverterType.X1_HYBRID_G4;
    }

    @Override
    protected void assertParserSpecific(LocalData data) {
        assertEquals("SOME_SERIAL_NUMBER", data.getWifiSerial());
        assertEquals("3.008.10", data.getWifiVersion());

        assertEquals(238.8, data.getInverterVoltage()); // [0]
        assertEquals(2.1, data.getInverterCurrent()); // [1]
        assertEquals(460, data.getInverterOutputPower()); // [2]
        assertEquals(49.98, data.getInverterFrequency()); // [3]

        assertEquals(448.3, data.getPV1Voltage()); // [4]
        assertEquals(448.3, data.getPV2Voltage()); // [5]
        assertEquals(1, data.getPV1Current()); // [6]
        assertEquals(0.1, data.getPV2Current()); // [7]
        assertEquals(487, data.getPV1Power()); // [8]
        assertEquals(65, data.getPV2Power()); // [9]

        assertEquals(2, data.getInverterWorkModeCode()); // [10]
        assertEquals("2", data.getInverterWorkMode()); // [10]

        assertEquals(121.8, data.getBatteryVoltage()); // [14]
        assertEquals(5, data.getBatteryCurrent()); // [15]
        assertEquals(605, data.getBatteryPower()); // [16]
        assertEquals(33, data.getBatteryTemperature()); // [17]
        assertEquals(99, data.getBatteryLevel()); // [18]

        assertEquals(12, data.getFeedInPower()); // [32]
    }

    @Override
    protected String getRawData() {
        return RAW_DATA;
    }

    // Yield_Today: Data[13] / 10,
    // Yield_Total: read32BitUnsigned(Data[11], Data[12]) / 10,
    // PowerDc1: Data[8],
    // PowerDc2: Data[9],
    // BAT_Power: read16BitSigned(Data[16]),
    // feedInPower: read32BitSigned(Data[32], Data[33]),
    // GridAPower: read16BitSigned(Data[2]),
    // FeedInEnergy: read32BitUnsigned(Data[34], Data[35]) / 100,
    // ConsumeEnergy: read32BitUnsigned(Data[36], Data[37]) / 100,
    // RunMode: Data[10],
    // EPSAPower: read16BitSigned(Data[28]),
    // Vdc1: Data[4] / 10,
    // Vdc2: Data[5] / 10,
    // Idc1: Data[6] / 10,
    // Idc2: Data[7] / 10,
    // EPSAVoltage: Data[29] / 10,
    // EPSACurrent: read16BitSigned(Data[30]) / 10,
    // BatteryCapacity: Data[18],
    // BatteryVoltage: Data[14] / 100,
    // BatteryTemperature: read16BitSigned(Data[17]),
    // GridAVoltage: Data[0] / 10,
    // GridACurrent: read16BitSigned(Data[1]) / 10,
    // FreqacA: Data[3] / 100,
}
