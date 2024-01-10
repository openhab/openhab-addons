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
package org.openhab.binding.bluetooth;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

/**
 * The {@link BluetoothDeviceListener} class defines the a callback interface where devices are notified of updates to a
 * BLE device
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - Added descriptor updates
 */
@NonNullByDefault
public interface BluetoothDeviceListener {

    /**
     * Called when a scan record is received for the device
     *
     * @param scanNotification the {@link BluetoothScanNotification} providing the scan packet information
     */
    void onScanRecordReceived(BluetoothScanNotification scanNotification);

    /**
     * Called when the connection status changes
     *
     * @param connectionNotification the {@link BluetoothConnectionStatusNotification} providing the updated connection
     *            information
     */
    void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification);

    /**
     * Called when a devices services and characteristics have been completely read
     */
    void onServicesDiscovered();

    /**
     * Called when a characteristic value is received. Implementations should call this whenever a value
     * is received from the BLE device even if there is no change to the value.
     *
     * @param characteristic the updated {@link BluetoothCharacteristic}
     * @param value the update value
     */
    void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value);

    /**
     * Called when a descriptor value is received. Implementations should call this whenever a value
     * is received from the BLE device even if there is no change to the value.
     *
     * @param bluetoothDescriptor the updated {@link BluetoothDescriptor}
     * @param value the update value
     */
    void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor, byte[] value);

    /**
     * Called when the BluetoothAdapter for this BluetoothDevice changes.
     * Implementations should call this whenever they change the adapter used by this device.
     * Note: In general this is only called by a RoamingBluetoothDevice
     *
     * @param adapter the new {@link BluetoothAdapter} used by this device
     */
    void onAdapterChanged(BluetoothAdapter adapter);
}
