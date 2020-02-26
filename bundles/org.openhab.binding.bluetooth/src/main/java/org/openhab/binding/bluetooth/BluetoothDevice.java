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
package org.openhab.binding.bluetooth;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
         * Device is either still being discovered or an error occurred during connection
         */
        UNKNOWN,
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

    private final Object connectionStateLock = new Object();
    /**
     * Current connection state
     */
    private ConnectionState connectionState = ConnectionState.UNKNOWN;

    /**
     * The adapter the device is accessed through
     */
    protected final BluetoothAdapter adapter;

    /**
     * Devices Bluetooth address
     */
    protected final BluetoothAddress address;

    /**
     * Manufacturer id
     */
    protected @Nullable Integer manufacturer = null;

    /**
     * Device name.
     * <p>
     * Uses the devices long name if known, otherwise the short name if known
     */
    protected @Nullable String name;

    /**
     * Last known RSSI
     */
    protected @Nullable Integer rssi = null;

    /**
     * Last reported transmitter power
     */
    protected @Nullable Integer txPower = null;

    /**
     * The event listeners will be notified of device updates
     */
    private final List<BluetoothDeviceListener> eventListeners = new CopyOnWriteArrayList<BluetoothDeviceListener>();

    private @Nullable CompletableFuture<@Nullable Void> pendingConnectFuture = null;
    private @Nullable CompletableFuture<@Nullable Void> pendingDisconnectFuture = null;

    private AtomicReference<@Nullable CompletableFuture<ServiceContext>> discoveredServicesFuture = new AtomicReference<>();

    /**
     * Construct a Bluetooth device taking the Bluetooth address
     *
     * @param adapter
     * @param sender
     */
    public BluetoothDevice(BluetoothAdapter adapter, BluetoothAddress address) {
        this.address = address;
        this.adapter = adapter;
    }

    /**
     * Returns the the name of the Bluetooth device.
     *
     * @return The devices name
     */
    public @Nullable String getName() {
        return name;
    }

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
     * Sets the manufacturer id for the device
     *
     * @param manufacturer the manufacturer id
     */
    public void setManufacturerId(int manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Returns the manufacturer ID of the device
     *
     * @return an integer with manufacturer ID of the device, or null if not known
     */
    public @Nullable Integer getManufacturerId() {
        return manufacturer;
    }

    /**
     * Sets the device transmit power
     *
     * @param power the current transmitter power in dBm
     */
    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    /**
     * Returns the last Transmit Power value or null if no transmit power has been received
     *
     * @return the last reported transmitter power value in dBm
     */
    public @Nullable Integer getTxPower() {
        return txPower;
    }

    /**
     * Sets the current Receive Signal Strength Indicator (RSSI) value
     *
     * @param rssi the current RSSI value in dBm
     * @return true if the RSSI has changed, false if it was the same as previous
     */
    public boolean setRssi(int rssi) {
        boolean changed = (this.rssi == null || this.rssi != rssi);
        this.rssi = rssi;

        return changed;
    }

    /**
     * Returns the last Receive Signal Strength Indicator (RSSI) value or null if no RSSI has been received
     *
     * @return the last RSSI value in dBm
     */
    public @Nullable Integer getRssi() {
        return rssi;
    }

    /**
     * Set the name of the device
     *
     * @param name a {@link String} defining the device name
     */
    public void setName(String name) {
        this.name = name;
    }

    public CompletableFuture<@Nullable Void> connect() {
        synchronized (connectionStateLock) {
            switch (connectionState) {
                case CONNECTING:
                    Objects.requireNonNull(pendingConnectFuture);// make null checker happy
                    return pendingConnectFuture;
                case CONNECTED:
                    return CompletableFuture.completedFuture(null);
                case DISCONNECTING:
                    Objects.requireNonNull(pendingDisconnectFuture);// make null checker happy
                    // we will run a connect after the disconnect finishes
                    return pendingDisconnectFuture.thenCompose(v -> connect());
                default:
                    // init connection
                    connectionState = ConnectionState.CONNECTING;
                    CompletableFuture<@Nullable Void> future = pendingConnectFuture = new CompletableFuture<@Nullable Void>();
                    try {
                        doConnect();
                    } catch (BluetoothException e) {
                        if (connectionState == ConnectionState.CONNECTING) {
                            connectionState = ConnectionState.UNKNOWN;
                            pendingConnectFuture = null;
                        }
                        return completedExceptionaly(e);
                    }
                    return future;
            }
        }
    }

    /**
     * Connects to a device. This is an asynchronous method. Once the connection state is updated, the
     * {@link BluetoothDeviceListener.onConnectionState} method will be called with the connection state.
     * <p>
     * If the device is already connected, this will return false.
     *
     * @return true if the connection process is started successfully
     */
    protected abstract void doConnect() throws BluetoothException;

    public CompletableFuture<@Nullable Void> disconnect() {
        synchronized (connectionStateLock) {
            switch (connectionState) {
                case DISCONNECTING:
                    Objects.requireNonNull(pendingDisconnectFuture);// make null checker happy
                    return pendingDisconnectFuture;
                case DISCONNECTED:
                    return CompletableFuture.completedFuture(null);
                case CONNECTING:
                    Objects.requireNonNull(pendingConnectFuture);// make null checker happy
                    // we will run a disconnect after the connection finishes
                    return pendingConnectFuture.thenCompose(v -> disconnect());
                default:
                    // try to disconnect
                    connectionState = ConnectionState.DISCONNECTING;
                    CompletableFuture<@Nullable Void> future = pendingDisconnectFuture = new CompletableFuture<@Nullable Void>();
                    try {
                        doDisconnect();
                    } catch (BluetoothException e) {
                        if (connectionState == ConnectionState.DISCONNECTING) {
                            connectionState = ConnectionState.UNKNOWN;
                            pendingDisconnectFuture = null;
                        }
                        return completedExceptionaly(e);
                    }
                    return future;
            }
        }
    }

    /**
     * Disconnects from a device. Once the connection state is updated, the
     * {@link BluetoothDeviceListener.onConnectionState}
     * method will be called with the connection state.
     * <p>
     * If the device is not currently connected, this will return false.
     *
     * @return true if the disconnection process is started successfully
     */
    protected abstract void doDisconnect() throws BluetoothException;

    public CompletableFuture<ServiceContext> discoverServices() {
        return connect().thenCompose(v -> discoveredServicesFuture.updateAndGet(future -> {
            if (future == null || future.isCompletedExceptionally()) {
                future = new CompletableFuture<>();
                try {
                    doServiceDiscovery();
                } catch (BluetoothException e) {
                    future.completeExceptionally(e);
                }
            }
            return future;
        }));
    }

    /**
     * Starts a discovery on a device. This will iterate through all services and characteristics to build up a view of
     * the device.
     * <p>
     * This method should be called before attempting to read or write characteristics.
     *
     * @return true if the discovery process is started successfully
     */
    protected abstract void doServiceDiscovery() throws BluetoothException;

    /**
     * Reads a characteristic. Only a single read or write operation can be requested at once. Attempting to perform an
     * operation when one is already in progress will result in subsequent calls returning false.
     * <p>
     * This is an asynchronous method. Once the read is complete
     * {@link BluetoothDeviceListener.onCharacteristicReadComplete}
     * method will be called with the completion state.
     * <p>
     * Note that {@link BluetoothDeviceListener.onCharacteristicUpdate} will be called when the read value is received.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to read.
     * @return true if the characteristic read is started successfully
     */
    public abstract CompletableFuture<byte[]> readCharacteristic(BluetoothCharacteristic characteristic);

    /**
     * Writes a characteristic. Only a single read or write operation can be requested at once. Attempting to perform an
     * operation when one is already in progress will result in subsequent calls returning false.
     * <p>
     * This is an asynchronous method. Once the write is complete
     * {@link BluetoothDeviceListener.onCharacteristicWriteComplete} method will be called with the completion state.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to read.
     * @return true if the characteristic write is started successfully
     */
    public abstract CompletableFuture<@Nullable Void> writeCharacteristic(BluetoothCharacteristic characteristic,
            byte[] value);

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
    public abstract CompletableFuture<@Nullable Void> enableNotifications(BluetoothCharacteristic characteristic,
            Consumer<byte[]> handler);

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
    public abstract CompletableFuture<@Nullable Void> enableNotifications(BluetoothDescriptor descriptor,
            Consumer<byte[]> handler);

    /**
     * Disables notifications for a descriptor. Only a single read or write operation can be requested at once.
     * Attempting to perform an operation when one is already in progress will result in subsequent calls returning
     * false.
     *
     * @param descriptor the {@link BluetoothDescriptor} to disable notifications for.
     * @return true if the descriptor notification is stopped successfully
     */
    public abstract CompletableFuture<@Nullable Void> disableNotifications(BluetoothDescriptor descriptor);

    /**
     * Adds a device listener
     *
     * @param listener the {@link BluetoothDeviceListener} to add
     */
    public void addListener(BluetoothDeviceListener listener) {
        eventListeners.add(listener);
    }

    /**
     * Removes a device listener
     *
     * @param listener the {@link BluetoothDeviceListener} to remove
     */
    public void removeListener(BluetoothDeviceListener listener) {
        eventListeners.remove(listener);
    }

    /**
     * Checks if this device has any listeners
     *
     * @return true if this device has listeners
     */
    public boolean hasListeners() {
        return !eventListeners.isEmpty();
    }

    /**
     * Get the current connection state for this device
     *
     * @return the current {@link ConnectionState}
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    protected void setConnectionState(ConnectionState newState) {
        synchronized (connectionStateLock) {
            this.connectionState = newState;
            switch (newState) {
                case CONNECTED: {
                    if (pendingDisconnectFuture != null) {
                        pendingDisconnectFuture.completeExceptionally(new BluetoothException("Failed to disconnect"));
                    }
                    pendingDisconnectFuture = null;

                    if (pendingConnectFuture != null) {
                        pendingConnectFuture.complete(null);
                    }
                    pendingConnectFuture = null;
                    break;
                }
                case DISCONNECTED: {
                    if (pendingConnectFuture != null) {
                        pendingConnectFuture.completeExceptionally(new BluetoothException("Failed to connect"));
                    }
                    pendingConnectFuture = null;

                    if (pendingDisconnectFuture != null) {
                        pendingDisconnectFuture.complete(null);
                    }
                    pendingDisconnectFuture = null;
                    break;
                }
                default:
                    break;
            }
        }
        BluetoothConnectionStatusNotification notification = new BluetoothConnectionStatusNotification(newState);
        for (BluetoothDeviceListener listener : eventListeners) {
            try {
                listener.onConnectionStateChange(notification);
            } catch (Exception e) {
                logger.error("Failed to inform listener '{}': {}", listener, e.getMessage(), e);
            }
        }
    }

    protected void notifyServicesDiscovered(ServiceContext context) {
        discoveredServicesFuture.updateAndGet(future -> {
            if (future == null || future.isDone()) {
                return CompletableFuture.completedFuture(context);
            }
            future.complete(context);
            return future;
        });
        for (BluetoothDeviceListener listener : eventListeners) {
            try {
                listener.onServicesDiscovered(context);
            } catch (Exception e) {
                logger.error("Failed to inform listener '{}': {}", listener, e.getMessage(), e);
            }
        }
    }

    protected void notifyScanRecordReceived(BluetoothScanNotification scanNotification) {
        for (BluetoothDeviceListener listener : eventListeners) {
            try {
                listener.onScanRecordReceived(scanNotification);
            } catch (Exception e) {
                logger.error("Failed to inform listener '{}': {}", listener, e.getMessage(), e);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BluetoothDevice [address=");
        builder.append(address);
        builder.append(", name=");
        builder.append(name);
        builder.append(", rssi=");
        builder.append(rssi);
        builder.append(", manufacturer=");
        builder.append(manufacturer);
        if (BluetoothCompanyIdentifiers.get(manufacturer) != null) {
            builder.append(" (");
            builder.append(BluetoothCompanyIdentifiers.get(manufacturer));
            builder.append(')');
        }
        builder.append(']');
        return builder.toString();
    }

    public class ServiceContext {

        /**
         * List of supported services
         */
        protected final Map<UUID, BluetoothService> supportedServices = new HashMap<UUID, BluetoothService>();

        /**
         * Returns a {@link BluetoothService} if the requested service is supported
         *
         * @return the {@link BluetoothService} or null if the service is not supported.
         */
        public BluetoothService getServices(UUID uuid) {
            return supportedServices.get(uuid);
        }

        /**
         * Returns a list of supported service UUIDs
         *
         * @return list of supported {@link BluetoothService}s.
         */
        public Collection<BluetoothService> getServices() {
            return supportedServices.values();
        }

        /**
         * Check if the device supports the specified service
         *
         * @param uuid the service {@link UUID}
         * @return true if the service is supported
         */
        public boolean supportsService(UUID uuid) {
            return supportedServices.containsKey(uuid);
        }

        /**
         * Adds a service to the device.
         *
         * @param service the new {@link BluetoothService} to add
         * @return true if the service was added or false if the service was already supported
         */
        protected boolean addService(BluetoothService service) {
            if (supportedServices.containsKey(service.getUuid())) {
                return false;
            }
            logger.trace("Adding new service to device {}: {}", address, service);
            supportedServices.put(service.getUuid(), service);
            return true;
        }

        /**
         * Adds a list of services to the device
         *
         * @param uuids
         */
        protected void addServices(List<UUID> uuids) {
            for (UUID uuid : uuids) {
                // Check if we already know about this service
                if (supportsService(uuid)) {
                    continue;
                }

                // Create a new service and add it to the device
                addService(new BluetoothService(uuid));
            }
        }

        /**
         * Gets a service based on the handle.
         * This will return a service if the handle falls within the start and end handles for the service.
         *
         * @param handle the handle for the service
         * @return the {@link BluetoothService} or null if the service was not found
         */
        protected @Nullable BluetoothService getServiceByHandle(int handle) {
            synchronized (supportedServices) {
                for (BluetoothService service : supportedServices.values()) {
                    if (service.getHandleStart() <= handle && service.getHandleEnd() >= handle) {
                        return service;
                    }
                }
            }
            return null;
        }

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
            for (BluetoothService service : supportedServices.values()) {
                if (service.providesCharacteristic(uuid)) {
                    return service.getCharacteristic(uuid);
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

    }

    protected static <T> CompletableFuture<T> completedExceptionaly(Throwable th) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(th);
        return future;
    }

}
