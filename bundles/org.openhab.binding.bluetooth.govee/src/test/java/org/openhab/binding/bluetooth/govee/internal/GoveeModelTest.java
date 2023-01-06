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
package org.openhab.binding.bluetooth.govee.internal;

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
 *
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
}
