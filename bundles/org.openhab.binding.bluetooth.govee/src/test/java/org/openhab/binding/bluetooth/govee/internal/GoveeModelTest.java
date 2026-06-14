/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.govee.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.MockBluetoothAdapter;
import org.openhab.binding.bluetooth.MockBluetoothDevice;
import org.openhab.binding.bluetooth.TestUtils;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;

/**
 * @author Connor Petty - Initial contribution
 * @author Matthias Bläsing - Fix reading advertisement data
 */
@NonNullByDefault
class GoveeModelTest {

    // the participant is stateless so this is fine.
    // private GoveeDiscoveryParticipant participant = new GoveeDiscoveryParticipant();

    @Test
    void noMatchTest() {
        MockBluetoothAdapter adapter = new MockBluetoothAdapter();
        MockBluetoothDevice mockDevice = adapter.getDevice(TestUtils.randomAddress());
        mockDevice.setName("asdfasdf");

        Assertions.assertNull(GoveeModel.getGoveeModel(new BluetoothDiscoveryDevice(mockDevice)));
    }

    @Test
    @DisplayName("testGovee_H5074_84DD")
    void testGoveeH507484DD() {
        MockBluetoothAdapter adapter = new MockBluetoothAdapter();
        MockBluetoothDevice mockDevice = adapter.getDevice(TestUtils.randomAddress());
        mockDevice.setName("Govee_H5074_84DD");

        Assertions.assertEquals(GoveeModel.H5074, GoveeModel.getGoveeModel(new BluetoothDiscoveryDevice(mockDevice)));
    }

    @Test
    @DisplayName("testGVH5102_77E9")
    void testGVH510277E9() {
        MockBluetoothAdapter adapter = new MockBluetoothAdapter();
        MockBluetoothDevice mockDevice = adapter.getDevice(TestUtils.randomAddress());
        mockDevice.setName("GVH5102_77E9");

        Assertions.assertEquals(GoveeModel.H5102, GoveeModel.getGoveeModel(new BluetoothDiscoveryDevice(mockDevice)));
    }

    @Test
    void testDecodeManufacturerDataH5075() {
        // Advertising data recorded for H5075
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, 0x03, 0x7d, 0x48, 0x60, 0x00 };
        // The scanning data is postprocessed by the handler
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5075.parseManufacturerData(testData);
        assertNotNull(manufacturerData);
        assertEquals(2280, manufacturerData.temperature()); // 22.8°C
        assertEquals(6800, manufacturerData.humidity()); // 68%
        assertEquals(96, manufacturerData.battery()); // 96%
        assertEquals(0, manufacturerData.wifiLevel()); // no wifi
    }

    @Test
    void testDecodeManufacturerDataWrongManufacturerId() {
        byte[] testData = new byte[] { 0x01, 0x02, 0x00, 0x03, 0x7d, 0x48, 0x60, 0x00 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5075.parseManufacturerData(testData);
        assertNull(manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataEmpty() {
        byte[] testData = new byte[] {};
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5075.parseManufacturerData(testData);
        assertNull(manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataTruncated() {
        // Advertising data recorded for H5075
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, 0x03, 0x7d, 0x48, 0x60, 0x00 };
        // Check that full data can be decoded
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5075.parseManufacturerData(testData);
        assertNotNull(manufacturerData);
        byte[] truncatedTestData = Arrays.copyOf(testData, testData.length - 2);
        GoveeModel.ManufacturerDataSet truncatedManufacturerData = GoveeModel.H5075
                .parseManufacturerData(truncatedTestData);
        assertNull(truncatedManufacturerData);
    }
}
