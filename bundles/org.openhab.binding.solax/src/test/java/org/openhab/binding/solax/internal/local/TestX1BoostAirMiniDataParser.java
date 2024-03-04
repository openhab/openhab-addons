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
package org.openhab.binding.solax.internal.local;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.local.LocalInverterData;

/**
 * The {@link TestX1BoostAirMiniDataParser} Simple test that tests for proper parsing against a real data from the
 * inverter
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX1BoostAirMiniDataParser extends AbstractParserTest {

    private static final String RAW_DATA = """
            {
                sn:SR***,
                ver:3.006.04,
                type:4,
                Data:[
                    2263,7,128,1519,0,9,0,138,0,5000,
                    2,15569,0,7,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,13,
                    0,4071,0,3456,0,0,0,0,0,0,
                    0,0,0,0,0,23,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
                ],
                Information:[1.500,4,XM3A15IA669518,8,2.27,0.00,1.43,0.00,0.00,1]}
            """;

    @Override
    protected String getRawData() {
        return RAW_DATA;
    }

    @Override
    protected void assertParserSpecific(LocalInverterData data) {
        assertEquals("SR***", data.getWifiSerial());
        assertEquals("3.006.04", data.getWifiVersion());

        assertEquals(226.3, data.getInverterVoltage()); // [0]
        assertEquals(0.7, data.getInverterCurrent()); // [1]
        assertEquals(128, data.getInverterOutputPower()); // [2]

        assertEquals(151.9, data.getPV1Voltage()); // [3]
        assertEquals(0, data.getPV2Voltage()); // [4]
        assertEquals(0.9, data.getPV1Current()); // [5]
        assertEquals(0, data.getPV2Current()); // [6]
        assertEquals(138, data.getPV1Power()); // [7]
        assertEquals(0, data.getPV2Power()); // [8]

        assertEquals(50, data.getInverterFrequency()); // [9]

        assertEquals(1556.9, data.getTotalEnergy()); // [11]
        assertEquals(0.7, data.getTodayEnergy()); // [13]

        assertEquals(346, data.getPowerUsage()); // [43]
    }

    @Override
    protected InverterType getInverterType() {
        return InverterType.X1_BOOST_AIR_MINI;
    }
}
