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
package org.openhab.binding.bluetooth.bluez;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.bluez.handler.BlueZBridgeHandler;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothException;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattDescriptor;
import tinyb.BluetoothGattService;

/**
 * Implementation of BluetoothDevice for BlueZ via TinyB
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class BlueZBluetoothDevice extends BluetoothDevice {

    private tinyb.BluetoothDevice device;

    private final Logger logger = LoggerFactory.getLogger(BlueZBluetoothDevice.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("bluetooth");

    /**
     * Constructor
     *
     * @param adapter the bridge handler through which this device is connected
     * @param address the Bluetooth address of the device
     * @param name the name of the device
     */
    public BlueZBluetoothDevice(BlueZBridgeHandler adapter, BluetoothAddress address, String name) {
        super(adapter, address);
        this.name = name;
        logger.debug("Creating BlueZ device with address '{}'", address);
    }

    /**
     * Constructor
     *
     * @param adapter the bridge handler through which this device is connected
     * @param tinybDevice the tinyB device to use internally (which already contains address and name information)
     */
    public BlueZBluetoothDevice(BlueZBridgeHandler adapter, tinyb.BluetoothDevice tinybDevice) {
        super(adapter, new BluetoothAddress(tinybDevice.getAddress()));
        this.name = tinybDevice.getName();
        this.device = tinybDevice;
    }

    /**
     * Initializes a newly created instance of this class.
     * It tries to set the internal tinyB device, if it isn't yet available, which is done asynchronously as
     * BlueZ can take a while (it seems to do an active scan for the physical device).
     *
     * This method should always be called directly after creating a new object instance.
     */
    public synchronized void initialize() {
        scheduler.submit(() -> {
            if (this.device == null) {
                tinyb.BluetoothDevice tinybDevice = findTinybDevice(address.toString());
                if (tinybDevice != null) {
                    device = tinybDevice;
                    enableNotifications();
                }
            } else {
                enableNotifications();
            }
        });
    }

    /**
     * Updates the internally used tinyB device instance. It replaces any previous instance, disables notifications on
     * it and enables notifications on the new instance.
     *
     * @param tinybDevice the new device instance to use for communication
     */
    public synchronized void updateTinybDevice(tinyb.BluetoothDevice tinybDevice) {
        if (device != null && !tinybDevice.equals(device)) {
            // we need to replace the instance - let's deactivate notifications on the old one
            disableNotifications();
        }
        if (this.device == null || !tinybDevice.equals(device)) {
            this.device = tinybDevice;
            enableNotifications();
        }
        tinyb.BluetoothDevice dev = this.device;
        if (dev != null) {
            this.rssi = (int) dev.getRSSI();
            this.txPower = (int) dev.getTxPower();
            if (dev.getConnected()) {
                this.connectionState = ConnectionState.CONNECTED;
            }
        }
        refreshServices();
    }

    private tinyb.@Nullable BluetoothDevice findTinybDevice(String address) {
        Collection<tinyb.BluetoothDevice> deviceList = ((BlueZBridgeHandler) getAdapter()).getTinyBDevices();
        logger.trace("Searching for '{}' in {} devices.", address, deviceList.size());
        return deviceList.stream().filter(d -> d.getAddress().equals(address)).findFirst().orElse(null);
    }

    private void enableNotifications() {
        logger.debug("Enabling notifications for device '{}'", device.getAddress());
        device.enableRSSINotifications(n -> {
            rssi = (int) n;
            BluetoothScanNotification notification = new BluetoothScanNotification();
            notification.setRssi(n);
            notifyListeners(BluetoothEventType.SCAN_RECORD, notification);
        });
        device.enableManufacturerDataNotifications(n -> {
            for (Map.Entry<Short, byte[]> entry : n.entrySet()) {
                BluetoothScanNotification notification = new BluetoothScanNotification();
                byte[] data = new byte[entry.getValue().length + 2];
                data[0] = (byte) (entry.getKey() & 0xFF);
                data[1] = (byte) (entry.getKey() >>> 8);
                System.arraycopy(entry.getValue(), 0, data, 2, entry.getValue().length);
                if (logger.isDebugEnabled()) {
                    logger.debug("Received manufacturer data for '{}': {}", address, HexUtils.bytesToHex(data, " "));
                }
                notification.setManufacturerData(data);
                notifyListeners(BluetoothEventType.SCAN_RECORD, notification);
            }
        });
        device.enableConnectedNotifications(connected -> {
            connectionState = connected ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED;
            logger.debug("Connection state of '{}' changed to {}", address, connectionState);
            notifyListeners(BluetoothEventType.CONNECTION_STATE,
                    new BluetoothConnectionStatusNotification(connectionState));
        });
        device.enableServicesResolvedNotifications(resolved -> {
            logger.debug("Received services resolved event for '{}': {}", address, resolved);
            if (resolved) {
                refreshServices();
                notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
            }
        });
        device.enableServiceDataNotifications(data -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Received service data for '{}':", address);
                for (Map.Entry<String, byte[]> entry : data.entrySet()) {
                    logger.debug("{} : {}", entry.getKey(), HexUtils.bytesToHex(entry.getValue(), " "));
                }
            }
        });
    }

    private void disableNotifications() {
        logger.debug("Disabling notifications for device '{}'", device.getAddress());
        device.disableBlockedNotifications();
        device.disableManufacturerDataNotifications();
        device.disablePairedNotifications();
        device.disableRSSINotifications();
        device.disableServiceDataNotifications();
        device.disableTrustedNotifications();
    }

    protected void refreshServices() {
        if (device.getServices().size() > getServices().size()) {
            for (BluetoothGattService tinybService : device.getServices()) {
                BluetoothService service = new BluetoothService(UUID.fromString(tinybService.getUUID()),
                        tinybService.getPrimary());
                for (BluetoothGattCharacteristic tinybCharacteristic : tinybService.getCharacteristics()) {
                    BluetoothCharacteristic characteristic = new BluetoothCharacteristic(
                            UUID.fromString(tinybCharacteristic.getUUID()), 0);
                    for (BluetoothGattDescriptor tinybDescriptor : tinybCharacteristic.getDescriptors()) {
                        BluetoothDescriptor descriptor = new BluetoothDescriptor(characteristic,
                                UUID.fromString(tinybDescriptor.getUUID()));
                        characteristic.addDescriptor(descriptor);
                    }
                    service.addCharacteristic(characteristic);
                }
                addService(service);
            }
            notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
        }
    }

    @Override
    public boolean connect() {
        if (device != null && !device.getConnected()) {
            try {
                return device.connect();
            } catch (BluetoothException e) {
                if ("Timeout was reached".equals(e.getMessage())) {
                    notifyListeners(BluetoothEventType.CONNECTION_STATE,
                            new BluetoothConnectionStatusNotification(ConnectionState.DISCONNECTED));
                } else if (e.getMessage() != null && e.getMessage().contains("Protocol not available")) {
                    // this device does not seem to be connectable at all - let's log a warning and ignore it.
                    logger.warn("Bluetooth device '{}' does not allow a connection.", device.getAddress());
                } else {
                    logger.debug("Exception occurred when trying to connect device '{}': {}", device.getAddress(),
                            e.getMessage());
                }
            }
        }
        return false;
    }

    @Override
    public boolean disconnect() {
        if (device != null && device.getConnected()) {
            logger.debug("Disconnecting '{}'", address);
            try {
                return device.disconnect();
            } catch (BluetoothException e) {
                logger.debug("Exception occurred when trying to disconnect device '{}': {}", device.getAddress(),
                        e.getMessage());
            }
        }
        return false;
    }

    @Override
    public boolean readCharacteristic(BluetoothCharacteristic characteristic) {
        if (device == null) {
            throw new IllegalStateException("TinyB device is not yet set");
        }
        BluetoothGattCharacteristic c = getTinybCharacteristicByUUID(characteristic.getUuid().toString());
        scheduler.submit(() -> {
            try {
                byte[] value = c.readValue();
                characteristic.setValue(value);
                notifyListeners(BluetoothEventType.CHARACTERISTIC_READ_COMPLETE, characteristic,
                        BluetoothCompletionStatus.SUCCESS);
            } catch (BluetoothException e) {
                logger.debug("Exception occurred when trying to read characteristic '{}': {}", characteristic.getUuid(),
                        e.getMessage());
                notifyListeners(BluetoothEventType.CHARACTERISTIC_READ_COMPLETE, characteristic,
                        BluetoothCompletionStatus.ERROR);
            }
        });
        return true;
    }

    @Override
    public boolean writeCharacteristic(BluetoothCharacteristic characteristic) {
        if (device == null) {
            throw new IllegalStateException("TinyB device is not yet set");
        }
        BluetoothGattCharacteristic c = getTinybCharacteristicByUUID(characteristic.getUuid().toString());
        scheduler.submit(() -> {
            try {
                BluetoothCompletionStatus successStatus = c.writeValue(characteristic.getByteValue())
                        ? BluetoothCompletionStatus.SUCCESS
                        : BluetoothCompletionStatus.ERROR;
                notifyListeners(BluetoothEventType.CHARACTERISTIC_WRITE_COMPLETE, characteristic, successStatus);
            } catch (BluetoothException e) {
                logger.debug("Exception occurred when trying to read characteristic '{}': {}", characteristic.getUuid(),
                        e.getMessage());
                notifyListeners(BluetoothEventType.CHARACTERISTIC_WRITE_COMPLETE, characteristic,
                        BluetoothCompletionStatus.ERROR);
            }
        });
        return true;
    }

    @Override
    public boolean enableNotifications(BluetoothCharacteristic characteristic) {
        if (device == null || !device.getConnected()) {
            throw new IllegalStateException("TinyB device is not set or not connected");
        }
        BluetoothGattCharacteristic c = getTinybCharacteristicByUUID(characteristic.getUuid().toString());
        if (c != null) {
            try {
                c.enableValueNotifications(value -> {
                    logger.debug("Received new value '{}' for characteristic '{}' of device '{}'", value,
                            characteristic.getUuid(), address);
                    characteristic.setValue(value);
                    notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, characteristic);
                });
            } catch (BluetoothException e) {
                if (e.getMessage().contains("Already notifying")) {
                    return false;
                } else if (e.getMessage().contains("In Progress")) {
                    // let's retry in 10 seconds
                    scheduler.schedule(() -> enableNotifications(characteristic), 10, TimeUnit.SECONDS);
                } else {
                    logger.warn("Exception occurred while activating notifications on '{}'", address, e);
                }
            }
            return true;
        } else {
            logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
            return false;
        }
    }

    @Override
    public boolean disableNotifications(BluetoothCharacteristic characteristic) {
        if (device == null || !device.getConnected()) {
            throw new IllegalStateException("TinyB device is not set or not connected");
        }
        BluetoothGattCharacteristic c = getTinybCharacteristicByUUID(characteristic.getUuid().toString());
        if (c != null) {
            c.disableValueNotifications();
            return true;
        } else {
            logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
            return false;
        }
    }

    @Override
    public boolean enableNotifications(BluetoothDescriptor descriptor) {
        if (device == null || !device.getConnected()) {
            throw new IllegalStateException("TinyB device is not set or not connected");
        }
        BluetoothGattDescriptor d = getTinybDescriptorByUUID(descriptor.getUuid().toString());
        if (d != null) {
            d.enableValueNotifications(value -> {
                logger.debug("Received new value '{}' for descriptor '{}' of device '{}'", value, descriptor.getUuid(),
                        address);
                descriptor.setValue(value);
                notifyListeners(BluetoothEventType.DESCRIPTOR_UPDATED, descriptor);
            });
            return true;
        } else {
            logger.warn("Descriptor '{}' is missing on device '{}'.", descriptor.getUuid(), address);
            return false;
        }
    }

    @Override
    public boolean disableNotifications(BluetoothDescriptor descriptor) {
        if (device == null) {
            throw new IllegalStateException("TinyB device is not yet set");
        }
        BluetoothGattDescriptor d = getTinybDescriptorByUUID(descriptor.getUuid().toString());
        if (d != null) {
            d.disableValueNotifications();
            return true;
        } else {
            logger.warn("Descriptor '{}' is missing on device '{}'.", descriptor.getUuid(), address);
            return false;
        }
    }

    private BluetoothGattCharacteristic getTinybCharacteristicByUUID(String uuid) {
        for (BluetoothGattService service : device.getServices()) {
            for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                if (c.getUUID().equals(uuid)) {
                    return c;
                }
            }
        }
        return null;
    }

    private BluetoothGattDescriptor getTinybDescriptorByUUID(String uuid) {
        for (BluetoothGattService service : device.getServices()) {
            for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                for (BluetoothGattDescriptor d : c.getDescriptors()) {
                    if (d.getUUID().equals(uuid)) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Clean up and release memory.
     */
    public void dispose() {
        disableNotifications();
    }
}
