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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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
        CHARACTERISTIC_READ_COMPLETE,
        CHARACTERISTIC_WRITE_COMPLETE,
        CHARACTERISTIC_UPDATED,
        DESCRIPTOR_UPDATED,
        SERVICES_DISCOVERED
    }

    /**
     * Current connection state
     */
    protected ConnectionState connectionState = ConnectionState.DISCOVERING;

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
    protected Integer manufacturer = null;

    /**
     * Device name.
     * <p>
     * Uses the devices long name if known, otherwise the short name if known
     */
    protected String name;

    /**
     * List of supported services
     */
    protected final Map<UUID, BluetoothService> supportedServices = new HashMap<UUID, BluetoothService>();

    /**
     * Last known RSSI
     */
    protected Integer rssi = null;

    /**
     * Last reported transmitter power
     */
    protected Integer txPower = null;

    /**
     * The event listeners will be notified of device updates
     */
    private final List<BluetoothDeviceListener> eventListeners = new CopyOnWriteArrayList<BluetoothDeviceListener>();

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
    public String getName() {
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
    public Integer getManufacturerId() {
        return manufacturer;
    }

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
    public Integer getTxPower() {
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
    public Integer getRssi() {
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
     * Get the current connection state for this device
     *
     * @return the current {@link ConnectionState}
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Connects to a device. This is an asynchronous method. Once the connection state is updated, the
     * {@link BluetoothDeviceListener.onConnectionState} method will be called with the connection state.
     * <p>
     * If the device is already connected, this will return false.
     *
     * @return true if the connection process is started successfully
     */
    public boolean connect() {
        return false;
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
    public boolean disconnect() {
        return false;
    }

    /**
     * Starts a discovery on a device. This will iterate through all services and characteristics to build up a view of
     * the device.
     * <p>
     * This method should be called before attempting to read or write characteristics.
     *
     * @return true if the discovery process is started successfully
     */
    public boolean discoverServices() {
        return false;
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
    public BluetoothCharacteristic getCharacteristic(UUID uuid) {
        for (BluetoothService service : supportedServices.values()) {
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
     * This is an asynchronous method. Once the read is complete
     * {@link BluetoothDeviceListener.onCharacteristicReadComplete}
     * method will be called with the completion state.
     * <p>
     * Note that {@link BluetoothDeviceListener.onCharacteristicUpdate} will be called when the read value is received.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to read.
     * @return true if the characteristic read is started successfully
     */
    public boolean readCharacteristic(BluetoothCharacteristic characteristic) {
        return false;
    }

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
    public boolean writeCharacteristic(BluetoothCharacteristic characteristic) {
        return false;
    }

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
    public boolean enableNotifications(BluetoothCharacteristic characteristic) {
        return false;
    }

    /**
     * Disables notifications for a characteristic. Only a single read or write operation can be requested at once.
     * Attempting to perform an operation when one is already in progress will result in subsequent calls returning
     * false.
     *
     * @param characteristic the {@link BluetoothCharacteristic} to disable notifications for.
     * @return true if the characteristic notification is stopped successfully
     */
    public boolean disableNotifications(BluetoothCharacteristic characteristic) {
        return false;
    }

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
    public boolean enableNotifications(BluetoothDescriptor descriptor) {
        return false;
    }

    /**
     * Disables notifications for a descriptor. Only a single read or write operation can be requested at once.
     * Attempting to perform an operation when one is already in progress will result in subsequent calls returning
     * false.
     *
     * @param descriptor the {@link BluetoothDescriptor} to disable notifications for.
     * @return true if the descriptor notification is stopped successfully
     */
    public boolean disableNotifications(BluetoothDescriptor descriptor) {
        return false;
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
    protected BluetoothService getServiceByHandle(int handle) {
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
     * Gets a characteristic based on the handle.
     *
     * @param handle the handle for the characteristic
     * @return the {@link BluetoothCharacteristic} or null if the characteristic was not found
     */
    protected BluetoothCharacteristic getCharacteristicByHandle(int handle) {
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
    public void addListener(BluetoothDeviceListener listener) {
        if (listener == null) {
            return;
        }
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
     * Notify the listeners of an event
     *
     * @param event the {@link BluetoothEventType} of this event
     * @param args an array of arguments to pass to the callback
     */
    protected void notifyListeners(BluetoothEventType event, Object... args) {
        for (BluetoothDeviceListener listener : eventListeners) {
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
                    case CHARACTERISTIC_READ_COMPLETE:
                        listener.onCharacteristicReadComplete((BluetoothCharacteristic) args[0],
                                (BluetoothCompletionStatus) args[1]);
                        break;
                    case CHARACTERISTIC_WRITE_COMPLETE:
                        listener.onCharacteristicWriteComplete((BluetoothCharacteristic) args[0],
                                (BluetoothCompletionStatus) args[1]);
                        break;
                    case CHARACTERISTIC_UPDATED:
                        listener.onCharacteristicUpdate((BluetoothCharacteristic) args[0]);
                        break;
                    case DESCRIPTOR_UPDATED:
                        listener.onDescriptorUpdate((BluetoothDescriptor) args[0]);
                        break;
                }
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
}
