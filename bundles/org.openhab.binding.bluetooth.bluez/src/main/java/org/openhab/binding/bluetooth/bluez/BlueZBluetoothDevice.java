/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothException;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.bluez.handler.BlueZBridgeHandler;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final BluezServiceContext serviceContext = new BluezServiceContext();

    private long lastSeenTime;

    /**
     * Constructor
     *
     * @param adapter the bridge handler through which this device is connected
     * @param address the Bluetooth address of the device
     * @param name the name of the device
     */
    public BlueZBluetoothDevice(BlueZBridgeHandler adapter, BluetoothAddress address) {
        super(adapter, address);
        logger.debug("Creating BlueZ device with address '{}'", address);
    }

    /**
     * Initializes a newly created instance of this class.
     * This method should always be called directly after creating a new object instance.
     */
    public void initialize() {
        updateLastSeenTime();
    }

    /**
     * Updates the internally used tinyB device instance. It replaces any previous instance, disables notifications on
     * it and enables notifications on the new instance.
     *
     * @param tinybDevice the new device instance to use for communication
     */
    public synchronized void updateTinybDevice(tinyb.BluetoothDevice tinybDevice) {
        if (Objects.equals(device, tinybDevice)) {
            return;
        }

        if (device != null) {
            // we need to replace the instance - let's deactivate notifications on the old one
            disableNotifications();
        }
        this.device = tinybDevice;

        if (this.device == null) {
            return;
        }
        updateLastSeenTime();

        this.name = device.getName();
        this.rssi = (int) device.getRSSI();
        this.txPower = (int) device.getTxPower();

        device.getManufacturerData().entrySet().stream().map(Map.Entry::getKey).filter(Objects::nonNull).findFirst()
                .ifPresent(manufacturerId ->
                // Convert to unsigned int to match the convention in BluetoothCompanyIdentifiers
                this.manufacturer = manufacturerId & 0xFFFF);

        if (device.getConnected()) {
            setConnectionState(ConnectionState.CONNECTED);
        }

        enableNotifications();
        serviceContext.refreshServices();
    }

    private void enableNotifications() {
        logger.debug("Enabling notifications for device '{}'", device.getAddress());
        device.enableRSSINotifications(n -> {
            rssi = (int) n;
            BluetoothScanNotification notification = new BluetoothScanNotification();
            notification.setRssi(n);
            notifyScanRecordReceived(notification);
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
                notifyScanRecordReceived(notification);
            }
        });
        device.enableConnectedNotifications(connected -> {
            ConnectionState connectionState = connected ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED;
            logger.debug("Connection state of '{}' changed to {}", address, connectionState);
            setConnectionState(connectionState);
        });
        device.enableServicesResolvedNotifications(resolved -> {
            logger.debug("Received services resolved event for '{}': {}", address, resolved);
            if (resolved) {
                serviceContext.refreshServices();
                notifyServicesDiscovered(serviceContext);
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

    @Override
    protected void doServiceDiscovery() throws BluetoothException {
        ensureConnected();
        serviceContext.refreshServices();
    }

    @Override
    public void doConnect() throws BluezException {
        if (device == null) {
            throw new BluezException(String.format("Cannot connect, tinyb device '%s' is missing", address));
        }
        if (device.getConnected()) {
            // mission accomplished!
            setConnectionState(ConnectionState.CONNECTED);
            return;
        }
        try {
            if (device.connect()) {
                return;
            }
        } catch (tinyb.BluetoothException e) {
            if ("Timeout was reached".equals(e.getMessage())) {
                setConnectionState(ConnectionState.DISCONNECTED);
            } else if (e.getMessage() != null && e.getMessage().contains("Protocol not available")) {
                // this device does not seem to be connectable at all - let's log a warning and ignore it.
                logger.warn("Bluetooth device '{}' does not allow a connection.", address);
            } else {
                logger.debug("Exception occurred when trying to connect device '{}': {}", address, e.getMessage());
            }
            throw new BluezException(e);
        }
        throw new BluezException(String.format("Failed to connect to device '%s'", address));
    }

    @Override
    public void doDisconnect() throws BluezException {
        if (device == null) {
            throw new BluezException(String.format("Cannot connect, tinyb device '%s' is missing", address));
        }
        if (!device.getConnected()) {
            // mission accomplished!
            setConnectionState(ConnectionState.DISCONNECTED);
            return;
        }
        logger.debug("Disconnecting '{}'", address);
        try {
            if (device.disconnect()) {
                return;
            }
        } catch (tinyb.BluetoothException e) {
            logger.debug("Exception occurred when trying to disconnect device '{}': {}", address, e.getMessage());
            throw new BluezException(e);
        }
        throw new BluezException(String.format("Failed to disconnect from device '%s'", address));
    }

    private void ensureConnected() throws BluetoothException {
        if (device == null || !device.getConnected()) {
            throw new BluezException("TinyB device '" + address + "'is not set");
        }
        if (!device.getConnected()) {
            throw new BluetoothException("Device '" + address + "' is not connected");
        }
    }

    @Override
    public CompletableFuture<byte[]> readCharacteristic(BluetoothCharacteristic characteristic) {
        try {
            ensureConnected();

            BluetoothGattCharacteristic c = getTinybCharacteristicByUUID(characteristic.getUuid().toString());
            if (c == null) {
                logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
                throw new BluetoothException(String.format("Characteristic '%s' is missing on device '%s'.",
                        characteristic.getUuid(), address));
            }
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            scheduler.submit(() -> {
                try {
                    byte[] value = c.readValue();
                    future.complete(value);
                } catch (tinyb.BluetoothException e) {
                    logger.debug("Exception occurred when trying to read characteristic '{}': {}",
                            characteristic.getUuid(), e.getMessage());

                    future.completeExceptionally(e);
                }
            });
            return future;
        } catch (BluetoothException ex) {
            return completedExceptionaly(ex);
        }
    }

    @Override
    public CompletableFuture<@Nullable Void> writeCharacteristic(BluetoothCharacteristic characteristic, byte[] value) {
        try {
            ensureConnected();

            BluetoothGattCharacteristic c = getTinybCharacteristicByUUID(characteristic.getUuid().toString());
            if (c == null) {
                logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
                throw new BluezException(String.format("Characteristic '%s' is missing on device '%s'.",
                        characteristic.getUuid(), address));
            }
            CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
            scheduler.submit(() -> {
                try {
                    if (c.writeValue(value)) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(
                                new BluezException(String.format("Failed to write characteristic %s on device '$s'",
                                        characteristic.getUuid(), address)));
                    }
                } catch (tinyb.BluetoothException e) {
                    logger.debug("Exception occurred when trying to write characteristic '{}': {}",
                            characteristic.getUuid(), e.getMessage());
                    future.completeExceptionally(e);
                }
            });
            return future;
        } catch (BluetoothException ex) {
            return completedExceptionaly(ex);
        }
    }

    @Override
    public CompletableFuture<@Nullable Void> enableNotifications(BluetoothCharacteristic characteristic,
            Consumer<byte[]> handler) {
        try {
            ensureConnected();

            BluetoothGattCharacteristic c = getTinybCharacteristicByUUID(characteristic.getUuid().toString());
            if (c == null) {
                logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
                throw new BluezException(String.format("Characteristic '%s' is missing on device '%s'.",
                        characteristic.getUuid(), address));
            }
            try {
                c.enableValueNotifications(value -> scheduler.submit(() -> handler.accept(value)));
            } catch (tinyb.BluetoothException e) {
                if (e.getMessage().contains("Already notifying")) {
                    return CompletableFuture.completedFuture(null);
                } else if (e.getMessage().contains("In Progress")) {
                    // let's retry in 10 seconds
                    // scheduler.schedule(() -> enableNotifications(characteristic, handler), 10, TimeUnit.SECONDS);
                    throw new BluezException(e);
                } else {
                    logger.warn("Exception occurred while activating notifications on '{}'", address, e);
                    throw new BluezException(e);
                }
            }
        } catch (BluetoothException ex) {
            return completedExceptionaly(ex);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<@Nullable Void> disableNotifications(BluetoothCharacteristic characteristic) {
        try {
            ensureConnected();

            BluetoothGattCharacteristic c = getTinybCharacteristicByUUID(characteristic.getUuid().toString());
            if (c == null) {
                logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
                throw new BluezException(String.format("Characteristic '%s' is missing on device '%s'.",
                        characteristic.getUuid(), address));
            }
            c.disableValueNotifications();
        } catch (BluetoothException ex) {
            return completedExceptionaly(ex);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<@Nullable Void> enableNotifications(BluetoothDescriptor descriptor,
            Consumer<byte[]> handler) {
        try {
            ensureConnected();

            BluetoothGattDescriptor d = getTinybDescriptorByUUID(descriptor.getUuid().toString());
            if (d == null) {
                logger.warn("Descriptor '{}' is missing on device '{}'.", descriptor.getUuid(), address);
                throw new BluezException(
                        String.format("Descriptor '%s' is missing on device '%s'.", descriptor.getUuid(), address));
            }
            d.enableValueNotifications(value -> scheduler.submit(() -> handler.accept(value)));
        } catch (BluetoothException ex) {
            return completedExceptionaly(ex);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<@Nullable Void> disableNotifications(BluetoothDescriptor descriptor) {
        try {
            ensureConnected();

            BluetoothGattDescriptor d = getTinybDescriptorByUUID(descriptor.getUuid().toString());
            if (d == null) {
                logger.warn("Descriptor '{}' is missing on device '{}'.", descriptor.getUuid(), address);
                throw new BluezException(
                        String.format("Descriptor '%s' is missing on device '%s'.", descriptor.getUuid(), address));
            }
            d.disableValueNotifications();
        } catch (BluetoothException ex) {
            return completedExceptionaly(ex);
        }
        return CompletableFuture.completedFuture(null);
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

    private void updateLastSeenTime() {
        this.lastSeenTime = System.currentTimeMillis();
    }

    public long getTimeSinceSeen(TimeUnit unit) {
        return unit.convert(System.currentTimeMillis() - this.lastSeenTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Clean up and release memory.
     */
    public void dispose() {
        if (device == null) {
            return;
        }
        disableNotifications();
        try {
            device.remove();
        } catch (tinyb.BluetoothException ex) {
            if (ex.getMessage().contains("Does Not Exist")) {
                // this happens when the underlying device has already been removed
                // but we don't have a way to check if that is the case beforehand so
                // we will just eat the error here.
            } else {
                logger.debug("Exception occurred when trying to remove inactive device '{}': {}", address,
                        ex.getMessage());
            }
        }
    }

    private class BluezServiceContext extends ServiceContext {

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
                notifyServicesDiscovered(this);
            }
        }
    }

}
