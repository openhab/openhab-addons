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
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.MockBluetoothAdapter;
import org.openhab.binding.bluetooth.MockBluetoothDevice;
import org.openhab.binding.bluetooth.TestUtils;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.govee.internal.GoveeModel.ManufacturerDataSet;

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
    void testDecodeManufacturerDataH5051() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, (byte) 0xba, 0x0a, (byte) 0xf9, 0x0f, 0x63, 0x02,
                0x01, 0x01 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5051.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 2746, 4089, 99), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5052() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, (byte) 0x1c, 0x01, (byte) 0xa7, 0x14, 0x3b, 0x00,
                0x00, 0x02 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5052.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 284, 5287, 59), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5071() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, 0x21, 0x0A, (byte) 0xb6, 0x12, 0x18, (byte) 0xc8,
                0x00, 0x01 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5071.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 2593, 4790, 24), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5072() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, 0x03, 0x4D, (byte) 0xb2, 0x64, 0x00 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5072.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 2160, 4980, 100), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5074() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, (byte) 0xe6, 0x09, (byte) 0xbc, 0x12, 0x64,
                0x02 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5074.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 2534, 4796, 100), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5177() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { 0x01, 0x00, 0x01, 0x01, 0x03, 0x36, 0x26, 0x64, 0x4C, 0x00, 0x02, 0x15, 0x49,
                0x4E, 0x54, 0x45, 0x4C, 0x4C, 0x49, 0x5F, 0x52, 0x4F, 0x43, 0x4B, 0x53, 0x5F, 0x48, 0x57, 0x51, 0x77,
                (byte) 0xf2, (byte) 0xff, (byte) 0xc2 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5177.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 2100, 4700, 100), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5179() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { 0x01, 0x00, 0x01, 0x01, 0x03, (byte) 0xb3, 0x14, 0x64 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5179.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 2420, 4520, 100), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5179_2() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { 0x01, (byte) 0x88, (byte) 0xec, 0x00, 0x01, 0x01, 0x0A, 0x0A, (byte) 0xa4, 0x06,
                0x64 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5179.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 2570, 1700, 100), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataB5178() {
        // Advertising data from
        // https://github.com/Bluetooth-Devices/govee-ble/blob/20cbc524d6addc3734e4fa9ea7ec6ed1a434d255/tests/test_parser.py
        byte[] testData = new byte[] { 0x01, 0x00, 0x01, 0x01, 0x01, 0x00, 0x2a, (byte) 0xf7, 0x64, 0x00, 0x03 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.B5178.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 100, 9990, 100), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5075() {
        // Advertising data recorded
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, 0x03, 0x7d, 0x48, 0x60, 0x00 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5075.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) 2280, 6800, 96), manufacturerData);
    }

    @Test
    void testDecodeManufacturerDataH5075NegativeTemp() {
        // Advertising data recorded
        byte[] testData = new byte[] { (byte) 0x88, (byte) 0xEC, 0x00, (byte) 0x80, (byte) 0xa1, (byte) 0x86, 0x47,
                0x00 };
        GoveeModel.ManufacturerDataSet manufacturerData = GoveeModel.H5075.parseManufacturerData(testData);
        assertMatches(new ManufacturerDataSet((short) -410, 3500, 71), manufacturerData);
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

    private void assertMatches(ManufacturerDataSet expected, @Nullable ManufacturerDataSet actual) {
        assertNotNull(actual);
        assertEquals(expected.temperature(), actual.temperature(), "Temperate did not match");
        assertEquals(expected.humidity(), actual.humidity(), "Humidity did not match");
        assertEquals(expected.battery(), actual.battery(), "Battery did not match");
    }
}
