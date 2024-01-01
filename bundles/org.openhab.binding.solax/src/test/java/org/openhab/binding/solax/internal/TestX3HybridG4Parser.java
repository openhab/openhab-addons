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
package org.openhab.binding.solax.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solax.internal.connectivity.rawdata.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterData;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.parsers.RawDataParser;

/**
 * The {@link TestX3HybridG4Parser} simple test that tests for proper parsing against a real data from the inverter
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX3HybridG4Parser {

    String rawData = """
            {
                sn:XYZ,
                ver:3.005.01,
                type:14,Data:[
                    2316,2329,2315,18,18,18,372,363,365,1100,
                    12,23,34,45,56,67,4996,4996,4996,2,
                    0,0,0,0,0,0,0,0,0,0,
                    0,0,0,1,65494,65535,0,0,0,31330,
                    320,1034,3078,1,44,1100,256,1294,0,0,
                    7445,5895,100,0,38,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,505,0,
                    396,0,0,0,102,0,142,0,62,110,
                    570,0,463,0,0,0,1925,0,369,0,
                    506,1925,304,309,0,0,0,0,0,0,
                    0,0,0,45,1,59,1,34,54,256,
                    3504,2400,300,300,295,276,33,33,2,1620,779,15163,15163,14906,0,0,0,3270,3264,45581,0,20564,12339,18753,12353,18742,12356,13625,20564,12339,18754,12866,18743,14151,13104,20564,12339,18754,12866,18743,14151,12592,20564,12339,18754,12865,18738,12871,13620,0,0,0,0,0,0,0,1025,8195,769,259,0,31460,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
                Information:[12.000,14,XY,8,1.23,0.00,1.24,1.09,0.00,1]
             }
            """;

    @Test
    public void testParser() {
        LocalConnectRawDataBean bean = LocalConnectRawDataBean.fromJson(rawData);
        int type = bean.getType();
        InverterType inverterType = InverterType.fromIndex(type);
        assertEquals(InverterType.X3_HYBRID_G4, inverterType, "Inverter type not recognized properly");

        RawDataParser parser = inverterType.getParser();
        assertNotNull(parser);

        InverterData data = parser.getData(bean);
        assertEquals("XYZ", data.getWifiSerial());
        assertEquals("3.005.01", data.getWifiVersion());

        assertEquals(231.6, data.getVoltagePhase1()); // [0]
        assertEquals(232.9, data.getVoltagePhase2()); // [1]
        assertEquals(231.5, data.getVoltagePhase3()); // [2]

        assertEquals(1.8, data.getCurrentPhase1()); // [3]
        assertEquals(1.8, data.getCurrentPhase2()); // [4]
        assertEquals(1.8, data.getCurrentPhase3()); // [5]

        assertEquals(372, data.getOutputPowerPhase1()); // [6]
        assertEquals(363, data.getOutputPowerPhase2()); // [7]
        assertEquals(365, data.getOutputPowerPhase3()); // [8]

        assertEquals(1100, data.getTotalOutputPower()); // [9]

        assertEquals(1.2, data.getPV1Voltage()); // [10]
        assertEquals(2.3, data.getPV2Voltage()); // [11]
        assertEquals(3.4, data.getPV1Current()); // [12]
        assertEquals(4.5, data.getPV2Current()); // [13]
        assertEquals(56, data.getPV1Power()); // [14]
        assertEquals(67, data.getPV2Power()); // [15]

        assertEquals(49.96, data.getFrequencyPhase1()); // [16]
        assertEquals(49.96, data.getFrequencyPhase2()); // [17]
        assertEquals(49.96, data.getFrequencyPhase3()); // [18]

        assertEquals(-41, data.getFeedInPower()); // [34] - [35]

        assertEquals(313.3, data.getBatteryVoltage()); // [39]
        assertEquals(3.2, data.getBatteryCurrent()); // [40]
        assertEquals(1034, data.getBatteryPower()); // [41]
        assertEquals(45, data.getBatteryLevel()); // [103]
        assertEquals(59, data.getBatteryTemperature()); // [105]

        // Totals
        assertEquals(1294, data.getPowerUsage()); // [47]
        assertEquals(50.5, data.getTotalEnergy()); // [68]
        assertEquals(102, data.getTotalBatteryDischargeEnergy()); // [74]
        assertEquals(142, data.getTotalBatteryChargeEnergy()); // [76]
        assertEquals(57, data.getTotalPVEnergy()); // [80]
        assertEquals(1925, data.getTotalFeedInEnergy()); // [86]
        assertEquals(36.9, data.getTotalConsumption()); // [88]
        assertEquals(46.3, data.getTodayEnergy()); // [82] / 10
        assertEquals(5.06, data.getTodayFeedInEnergy()); // [90] / 100
        assertEquals(3.04, data.getTodayConsumption()); // [92] / 100
        assertEquals(6.2, data.getTodayBatteryDischargeEnergy()); // [78] / 100
        assertEquals(11, data.getTodayBatteryChargeEnergy()); // [79] / 100
    }
}
