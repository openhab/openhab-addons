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
package org.openhab.binding.bluetooth;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothDevice} class provides a base implementation of a Bluetooth Low Energy device
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - Refactored class to use Integer instead of int, fixed bugs, diverse improvements
 * @author Connor Petty - Made most of the methods abstract
 * @author Peter Rosenberg - Improve notifications
 */
@NonNullByDefault
public abstract class BluetoothDevice {

    private final Logger logger = LoggerFactory.getLogger(BluetoothDevice.class);

    /**
     * Enumeration of Bluetooth connection states
     *
     */
    public enum ConnectionState {
        /**
         * Device is still being discovered and is not available for use.
         */
        DISCOVERING,
        /**
         * Device has been discovered. This is used for the initial notification that the device is available.
         */
        DISCOVERED,
        /**
         * Device is disconnected.
         */
        DISCONNECTED,
        /**
         * A connection is in progress.
         */
        CONNECTING,
        /**
         * The device is connected.
         */
        CONNECTED,
        /**
         * A disconnection is in progress.
         */
        DISCONNECTING
    }

    protected enum BluetoothEventType {
        CONNECTION_STATE,
        SCAN_RECORD,
        CHARACTERISTIC_UPDATED,
        DESCRIPTOR_UPDATED,
        SERVICES_DISCOVERED,
        ADAPTER_CHANGED
    }

    /**
     * The adapter the device is accessed through
     */
    protected final BluetoothAdapter adapter;

    /**
     * Devices Bluetooth address
     */
    protected final BluetoothAddress address;

    /**
     * Construct a Bluetooth device taking the Bluetooth address
     *
     * @param adapter
     * @param address
     */
    public BluetoothDevice(BluetoothAdapter adapter, BluetoothAddress address) {
        this.address = address;
        this.adapter = adapter;
    }

    /**
     * Returns the name of the Bluetooth device.
     *
     * @return The devices name
     */
    public abstract @Nullable String getName();

    /**
     * Returns the manufacturer ID of the device
     *
     * @return an integer with manufacturer ID of the device, or null if not known
     */
    public abstract @Nullable Integer getManufacturerId();

    /**
     * Returns the last Transmit Power value or null if no transmit power has been received
     *
     * @return the last reported transmitter power value in dBm
     */
    public abstract @Nullable Integer getTxPower();

    /**
     * Returns the last Receive Signal Strength Indicator (RSSI) value or null if no RSSI has been received
     *
     * @return the last RSSI value in dBm
     */
    public abstract @Nullable Integer getRssi();

    /**
     * Returns the physical address of the device.
     *
     * @return The physical address of the device
     */
    public BluetoothAddress getAddress() {
        return address;
    }

    /**
     * Returns the adapter through which the device is accessed
     *
     * @return The adapter through which the device is accessed
     */
    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    /**
     * Returns a {@link BluetoothService} if the requested service is supported
     *
     * @return the {@link BluetoothService} or null if the service is not supported.
     */
    public abstract @Nullable BluetoothService getServices(UUID uuid);

    /**
     * Returns a list of supported service UUIDs
     *
     * @return list of supported {@link BluetoothService}s.
     */
    public abstract Collection<BluetoothService> getServices();

    /**
     * Check if the device supports the specified service
     *
     * @param uuid the service {@link UUID}
     * @return true if the service is supported
     */
    public abstract boolean supportsService(UUID uuid);

    /**
     * Get the current connection state for this device
     *
     * @return the current {@link ConnectionState}
     */
    public abstract ConnectionState getConnectionState();

    /**
     * Connects to a device. This is an asynchronous method. Once the connection state is updated, the
     * {@link BluetoothDeviceListener#onConnectionStateChange} method will be called with the connection state.
     * <p>
     * If the device is already connected, this will return false.
     *
     * @return true if the connection process is started successfully
     */
    public abstract boolean connect();

    /**
     * Disconnects from a device. Once the connection state is updated, the
     * {@link BluetoothDeviceListener#onConnectionStateChange}
     * method will be called with the connection state.
     * <p>
     * If the device is not currently connected, this will return false.
     *
     * @return true if the disconnection process is started successfully
     */
    public abstract boolean disconnect();

    /**
     * Starts a discovery on a device. This will iterate through all services and characteristics to build up a view of
     * the device.
     * <p>
     * This method should be called before attempting to read or write characteristics.
     *
     * @return true if the discovery process is started successfully
     */
    public abstract boolean discoverServices();

    /**
     * Gets a Bluetooth characteristic if it is known.
     * <p>
     * Note that this method will not search for a characteristic in the remote device if it is not known.
     * You must have previously connected to the device so that the device services and characteristics can
     * be retrieved.
     *
     * @param uuid the {@link UUID} of the characteristic to return
     * @return the {@link BluetoothCharacteristic} or null if the characteristic is not found in the device
     */
    public @Nullable BluetoothCharacteristic getCharacteristic(UUID uuid) {
        for (BluetoothService service : getServices()) {
            if (service.providesCharacteristic(uuid)) {
                return service.getCharacteristic(uuid);
            }
        }
        return null;
    }

    /**
     * Reads a characteristic. Only a single read or write operation can be requested at once. Attempting to perform an
     * operation when one is already in progress will result in subsequent calls returning false.
     * <p>
     * This is an asynchronous method. Once the read is complete the returned future will be updated with the result.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to read.
     * @return a future that returns the read data is successful, otherwise throws an exception
     */
    public abstract CompletableFuture<byte[]> readCharacteristic(BluetoothCharacteristic characteristic);

    /**
     * Writes a characteristic. Only a single read or write operation can be requested at once. Attempting to perform an
     * operation when one is already in progress will result in subsequent calls returning false.
     * <p>
     * This is an asynchronous method. Once the write is complete the returned future will be updated with the result.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to write.
     * @param value the data to write
     * @return a future that returns null upon a successful write, otherwise throws an exception
     */
    public abstract CompletableFuture<@Nullable Void> writeCharacteristic(BluetoothCharacteristic characteristic,
            byte[] value);

    /**
     * Returns if notification is enabled for the given characteristic.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to check if notifications are enabled.
     * @return true if notification is enabled, false if notification is disabled, characteristic is missing on device
     *         or notifications are not supported.
     */
    public abstract boolean isNotifying(BluetoothCharacteristic characteristic);

    /**
     * Enables notifications for a characteristic. Only a single read or write operation can be requested at once.
     * Attempting to perform an operation when one is already in progress will result in subsequent calls returning
     * false.
     * <p>
     * Notifications result in CHARACTERISTIC_UPDATED events to the listeners.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to receive notifications for.
     * @return true if the characteristic notification is started successfully
     */
    public abstract CompletableFuture<@Nullable Void> enableNotifications(BluetoothCharacteristic characteristic);

    /**
     * Disables notifications for a characteristic. Only a single read or write operation can be requested at once.
     * Attempting to perform an operation when one is already in progress will result in subsequent calls returning
     * false.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to disable notifications for.
     * @return true if the characteristic notification is stopped successfully
     */
    public abstract CompletableFuture<@Nullable Void> disableNotifications(BluetoothCharacteristic characteristic);

    /**
     * Enables notifications for a descriptor. Only a single read or write operation can be requested at once.
     * Attempting to perform an operation when one is already in progress will result in subsequent calls returning
     * false.
     * <p>
     * Notifications result in DESCRIPTOR_UPDATED events to the listeners.
     *
     * @param descriptor the {@link BluetoothDescriptor} to receive notifications for.
     * @return true if the descriptor notification is started successfully
     */
    public abstract boolean enableNotifications(BluetoothDescriptor descriptor);

    /**
     * Disables notifications for a descriptor. Only a single read or write operation can be requested at once.
     * Attempting to perform an operation when one is already in progress will result in subsequent calls returning
     * false.
     *
     * @param descriptor the {@link BluetoothDescriptor} to disable notifications for.
     * @return true if the descriptor notification is stopped successfully
     */
    public abstract boolean disableNotifications(BluetoothDescriptor descriptor);

    /**
     * Adds a service to the device.
     *
     * @param service the new {@link BluetoothService} to add
     * @return true if the service was added or false if the service was already supported
     */
    protected abstract boolean addService(BluetoothService service);

    /**
     * Gets a service based on the handle.
     * This will return a service if the handle falls within the start and end handles for the service.
     *
     * @param handle the handle for the service
     * @return the {@link BluetoothService} or null if the service was not found
     */
    protected @Nullable BluetoothService getServiceByHandle(int handle) {
        for (BluetoothService service : getServices()) {
            if (service.getHandleStart() <= handle && service.getHandleEnd() >= handle) {
                return service;
            }
        }
        return null;
    }

    /**
     * Gets a characteristic based on the handle.
     *
     * @param handle the handle for the characteristic
     * @return the {@link BluetoothCharacteristic} or null if the characteristic was not found
     */
    protected @Nullable BluetoothCharacteristic getCharacteristicByHandle(int handle) {
        BluetoothService service = getServiceByHandle(handle);
        if (service != null) {
            return service.getCharacteristicByHandle(handle);
        }
        return null;
    }

    /**
     * Adds a device listener
     *
     * @param listener the {@link BluetoothDeviceListener} to add
     */
    public final void addListener(BluetoothDeviceListener listener) {
        getListeners().add(listener);
    }

    /**
     * Removes a device listener
     *
     * @param listener the {@link BluetoothDeviceListener} to remove
     */
    public final void removeListener(BluetoothDeviceListener listener) {
        getListeners().remove(listener);
    }

    /**
     * Checks if this device has any listeners
     *
     * @return true if this device has listeners
     */
    public final boolean hasListeners() {
        return !getListeners().isEmpty();
    }

    /**
     * Releases resources that this device is using.
     *
     */
    protected abstract void dispose();

    protected abstract Collection<BluetoothDeviceListener> getListeners();

    public abstract boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException;

    public abstract boolean awaitServiceDiscovery(long timeout, TimeUnit unit) throws InterruptedException;

    public abstract boolean isServicesDiscovered();

    /**
     * Notify the listeners of an event
     *
     * @param event the {@link BluetoothEventType} of this event
     * @param args an array of arguments to pass to the callback
     */
    protected void notifyListeners(BluetoothEventType event, Object... args) {
        for (BluetoothDeviceListener listener : getListeners()) {
            try {
                switch (event) {
                    case SCAN_RECORD:
                        listener.onScanRecordReceived((BluetoothScanNotification) args[0]);
                        break;
                    case CONNECTION_STATE:
                        listener.onConnectionStateChange((BluetoothConnectionStatusNotification) args[0]);
                        break;
                    case SERVICES_DISCOVERED:
                        listener.onServicesDiscovered();
                        break;
                    case CHARACTERISTIC_UPDATED:
                        listener.onCharacteristicUpdate((BluetoothCharacteristic) args[0], (byte[]) args[1]);
                        break;
                    case DESCRIPTOR_UPDATED:
                        listener.onDescriptorUpdate((BluetoothDescriptor) args[0], (byte[]) args[1]);
                        break;
                    case ADAPTER_CHANGED:
                        listener.onAdapterChanged((BluetoothAdapter) args[0]);
                        break;
                }
            } catch (Exception e) {
                logger.error("Failed to inform listener '{}': {}", listener, e.getMessage(), e);
            }
        }
    }
}
