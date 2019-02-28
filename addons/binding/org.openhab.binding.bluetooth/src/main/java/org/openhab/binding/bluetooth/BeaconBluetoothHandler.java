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
package org.openhab.binding.bluetooth;

import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

/**
 * This is a handler for generic Bluetooth devices in beacon-mode (i.e. not connected), which at the same time can be
 * used as a base implementation for more specific thing handlers.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@NonNullByDefault
public class BeaconBluetoothHandler extends BaseThingHandler implements BluetoothDeviceListener {

    @NonNullByDefault({} /* non-null if initialized */)
    protected BluetoothAdapter adapter;

    @NonNullByDefault({} /* non-null if initialized */)
    protected BluetoothAddress address;

    @NonNullByDefault({} /* non-null if initialized */)
    protected BluetoothDevice device;

    protected final ReentrantLock deviceLock;

    public BeaconBluetoothHandler(Thing thing) {
        super(thing);
        deviceLock = new ReentrantLock();
    }

    @Override
    public void initialize() {
        try {
            address = new BluetoothAddress(getConfig().get(BluetoothBindingConstants.CONFIGURATION_ADDRESS).toString());
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Not associated with any bridge");
            return;
        }

        BridgeHandler bridgeHandler = bridge.getHandler();
        if (!(bridgeHandler instanceof BluetoothAdapter)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Associated with an unsupported bridge");
            return;
        }

        adapter = (BluetoothAdapter) bridgeHandler;

        try {
            deviceLock.lock();
            device = adapter.getDevice(address);
            device.addListener(this);
        } finally {
            deviceLock.unlock();
        }

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        try {
            deviceLock.lock();
            if (device != null) {
                device.removeListener(this);
                device.disconnect();
                device = null;
            }
        } finally {
            deviceLock.unlock();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH && channelUID.getId().equals(BluetoothBindingConstants.CHANNEL_TYPE_RSSI)) {
            updateRSSI();
        }
    }

    /**
     * Updates the RSSI channel and the Thing status according to the new received rssi value
     */
    protected void updateRSSI() {
        if (device != null) {
            Integer rssi = device.getRssi();
            if (rssi != null && rssi != 0) {
                updateState(BluetoothBindingConstants.CHANNEL_TYPE_RSSI, new DecimalType(rssi));
                updateStatusBasedOnRssi(true);
            } else {
                updateState(BluetoothBindingConstants.CHANNEL_TYPE_RSSI, UnDefType.NULL);
                updateStatusBasedOnRssi(false);
            }
        }
    }

    /**
     * This method sets the Thing status based on whether or not we can receive a signal from it.
     * This is the best logic for beacons, but connected devices might want to deactivate this by overriding the method.
     *
     * @param receivedSignal true, if the device is in reach
     */
    protected void updateStatusBasedOnRssi(boolean receivedSignal) {
        if (receivedSignal) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        int rssi = scanNotification.getRssi();
        if (rssi != Integer.MIN_VALUE) {
            device.setRssi(rssi);
            updateRSSI();
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
    }

    @Override
    public void onServicesDiscovered() {
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
    }

    @Override
    public void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor) {
    }

}
