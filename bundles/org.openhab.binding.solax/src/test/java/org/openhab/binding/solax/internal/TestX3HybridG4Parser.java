/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.solax.internal.model.ThreePhaseInverterData;
import org.openhab.binding.solax.internal.model.parsers.RawDataParser;

/**
 * The {@link TestX3HybridG4Parser} simple test that tests for proper parsing against a real data from the inverter
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX3HybridG4Parser {

    String rawData = "{\"sn\":\"XYZ\",\"ver\":\"3.005.01\",\"type\":14,\"Data\":["
            + "2316,2329,2315,18,18,18,372,363,365,1100," // [0-9]
            + "12,23,34,45,56,67,4996,4996,4996,2," // [10-19]
            + "0,0,0,0,0,0,0,0,0,0," // [20-29]
            + "0,0,0,1,65494,65535,0,0,0,31330," // [30-39]
            + "320,1034,3078,1,44,1100,256,1294,0,0," // [40-49]
            + "7445,5895,100,0,38,0,0,0,0,0," // [50-59]
            + "0,0,0,0,0,0,0,0,505,0," // [60-69]
            + "396,0,0,0,102,0,142,0,62,110," // [70-79]
            + "570,0,463,0,0,0,1925,0,369,0," // [80-89]
            + "506,1925,304,309,0,0,0,0,0,0," // [90-99]
            + "0,0,0,45,1,59,1,34,54,256," // [100-109]
            + "3504,2400,300,300,295,276,33,33,2,1620,779,15163,15163,14906,0,0,0,3270,3264,45581,0,20564,12339,18753,12353,18742,12356,13625,20564,12339,18754,12866,18743,14151,13104,20564,12339,18754,12866,18743,14151,12592,20564,12339,18754,12865,18738,12871,13620,0,0,0,0,0,0,0,1025,8195,769,259,0,31460,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"Information\":[12.000,14,\"XY\",8,1.23,0.00,1.24,1.09,0.00,1]}";

    @Test
    public void testParser() {
        LocalConnectRawDataBean bean = LocalConnectRawDataBean.fromJson(rawData);
        int type = bean.getType();
        InverterType inverterType = InverterType.fromIndex(type);
        assertEquals(InverterType.X3_HYBRID_G4, inverterType, "Inveter type not recognized properly");

        RawDataParser parser = inverterType.getParser();
        assertNotNull(parser);

        InverterData data = parser.getData(bean);
        assertEquals("XYZ", data.getWifiSerial());
        assertEquals("3.005.01", data.getWifiVersion());

        assertTrue(data instanceof ThreePhaseInverterData, "Data not parsed as a three-phase inverter data");
        ThreePhaseInverterData threePhaseData = (ThreePhaseInverterData) data;
        assertEquals(231.6, threePhaseData.getVoltagePhase1()); // [0]
        assertEquals(232.9, threePhaseData.getVoltagePhase2()); // [1]
        assertEquals(231.5, threePhaseData.getVoltagePhase3()); // [2]

        assertEquals(1.8, threePhaseData.getCurrentPhase1()); // [3]
        assertEquals(1.8, threePhaseData.getCurrentPhase2()); // [4]
        assertEquals(1.8, threePhaseData.getCurrentPhase3()); // [5]

        assertEquals(372, threePhaseData.getOutputPowerPhase1()); // [6]
        assertEquals(363, threePhaseData.getOutputPowerPhase2()); // [7]
        assertEquals(365, threePhaseData.getOutputPowerPhase3()); // [8]

        assertEquals(1100, threePhaseData.getTotalOutputPower()); // [9]

        assertEquals(1.2, threePhaseData.getPV1Voltage()); // [10]
        assertEquals(2.3, threePhaseData.getPV2Voltage()); // [11]
        assertEquals(3.4, threePhaseData.getPV1Current()); // [12]
        assertEquals(4.5, threePhaseData.getPV2Current()); // [13]
        assertEquals(56, threePhaseData.getPV1Power()); // [14]
        assertEquals(67, threePhaseData.getPV2Power()); // [15]

        assertEquals(49.96, threePhaseData.getFrequencyPhase1()); // [16]
        assertEquals(49.96, threePhaseData.getFrequencyPhase2()); // [17]
        assertEquals(49.96, threePhaseData.getFrequencyPhase3()); // [18]

        assertEquals(-41, threePhaseData.getFeedInPower()); // [34] - [35]

        assertEquals(313.3, threePhaseData.getBatteryVoltage()); // [39]
        assertEquals(3.2, threePhaseData.getBatteryCurrent()); // [40]
        assertEquals(1034, threePhaseData.getBatteryPower()); // [41]
        assertEquals(45, threePhaseData.getBatteryLevel()); // [103]
        assertEquals(59, threePhaseData.getBatteryTemperature()); // [105]

        // Totals
        assertEquals(1294, threePhaseData.getPowerUsage()); // [47]
        assertEquals(50.5, threePhaseData.getTotalEnergy()); // [68]
        assertEquals(102, threePhaseData.getTotalBatteryDischargeEnergy()); // [74]
        assertEquals(142, threePhaseData.getTotalBatteryChargeEnergy()); // [76]
        assertEquals(57, threePhaseData.getTotalPVEnergy()); // [80]
        assertEquals(1925, threePhaseData.getTotalFeedInEnergy()); // [86]
        assertEquals(36.9, threePhaseData.getTotalConsumption()); // [88]
        assertEquals(46.3, threePhaseData.getTodayEnergy()); // [82] / 10
        assertEquals(5.06, threePhaseData.getTodayFeedInEnergy()); // [90] / 100
        assertEquals(3.04, threePhaseData.getTodayConsumption()); // [92] / 100
        assertEquals(6.2, threePhaseData.getTodayBatteryDischargeEnergy()); // [78] / 100
        assertEquals(11, threePhaseData.getTodayBatteryChargeEnergy()); // [79] / 100
    }
}
