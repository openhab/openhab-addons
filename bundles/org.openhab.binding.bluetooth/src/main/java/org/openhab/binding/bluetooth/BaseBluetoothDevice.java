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

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseBluetoothDevice} implements parts of the BluetoothDevice functionality that is
 * shared to all concrete BluetoothDevice implementations.
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public abstract class BaseBluetoothDevice extends BluetoothDevice {

    private final Logger logger = LoggerFactory.getLogger(BaseBluetoothDevice.class);

    /**
     * Current connection state
     */
    protected ConnectionState connectionState = ConnectionState.DISCOVERING;

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
     * List of supported services
     */
    protected final Map<UUID, BluetoothService> supportedServices = new ConcurrentHashMap<>();

    /**
     * Last known RSSI
     */
    protected @Nullable Integer rssi = null;

    /**
     * Last reported transmitter power
     */
    protected @Nullable Integer txPower = null;

    protected final transient ZonedDateTime createTime = ZonedDateTime.now();

    /**
     * Last time when activity occurred on this device.
     */
    protected @Nullable ZonedDateTime lastSeenTime = null;

    /**
     * The event listeners will be notified of device updates
     */
    private final Set<BluetoothDeviceListener> eventListeners = new CopyOnWriteArraySet<>();

    private final Lock deviceLock = new ReentrantLock();
    private final Condition connectionCondition = deviceLock.newCondition();
    private final Condition serviceDiscoveryCondition = deviceLock.newCondition();

    private volatile boolean servicesDiscovered = false;

    /**
     * Construct a Bluetooth device taking the Bluetooth address
     *
     * @param adapter
     * @param address
     */
    public BaseBluetoothDevice(BluetoothAdapter adapter, BluetoothAddress address) {
        super(adapter, address);
    }

    /**
     * Returns the last time this device was active
     *
     * @return The last time this device was active
     */
    public @Nullable ZonedDateTime getLastSeenTime() {
        return lastSeenTime;
    }

    /**
     * Updates the last activity timestamp for this device.
     * Should be called whenever activity occurs on this device.
     *
     */
    public void updateLastSeenTime() {
        lastSeenTime = ZonedDateTime.now();
    }

    /**
     * Returns the name of the Bluetooth device.
     *
     * @return The devices name
     */
    @Override
    public @Nullable String getName() {
        return name;
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
    @Override
    public @Nullable Integer getManufacturerId() {
        return manufacturer;
    }

    /**
     * Returns a {@link BluetoothService} if the requested service is supported
     *
     * @return the {@link BluetoothService} or null if the service is not supported.
     */
    @Override
    public @Nullable BluetoothService getServices(UUID uuid) {
        return supportedServices.get(uuid);
    }

    /**
     * Returns a list of supported service UUIDs
     *
     * @return list of supported {@link BluetoothService}s.
     */
    @Override
    public Collection<BluetoothService> getServices() {
        return supportedServices.values();
    }

    /**
     * Sets the device transmit power
     *
     * @param txPower the current transmitter power in dBm
     */
    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    /**
     * Returns the last Transmit Power value or null if no transmit power has been received
     *
     * @return the last reported transmitter power value in dBm
     */
    @Override
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
    @Override
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

    /**
     * Check if the device supports the specified service
     *
     * @param uuid the service {@link UUID}
     * @return true if the service is supported
     */
    @Override
    public boolean supportsService(UUID uuid) {
        return supportedServices.containsKey(uuid);
    }

    /**
     * Get the current connection state for this device
     *
     * @return the current {@link ConnectionState}
     */
    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Adds a service to the device.
     *
     * @param service the new {@link BluetoothService} to add
     * @return true if the service was added or false if the service was already supported
     */
    @Override
    protected boolean addService(BluetoothService service) {
        BluetoothService oldValue = supportedServices.putIfAbsent(service.getUuid(), service);
        if (oldValue == null) {
            logger.trace("Adding new service to device {}: {}", address, service);
            return true;
        }
        return false;
    }

    @Override
    protected Collection<BluetoothDeviceListener> getListeners() {
        return eventListeners;
    }

    /**
     * Releases resources that this device is using.
     *
     */
    @Override
    protected void dispose() {
    }

    @Override
    public boolean isServicesDiscovered() {
        return servicesDiscovered;
    }

    @Override
    public boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
        deviceLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (getConnectionState() != ConnectionState.CONNECTED) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = connectionCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            deviceLock.unlock();
        }
        return true;
    }

    @Override
    public boolean awaitServiceDiscovery(long timeout, TimeUnit unit) throws InterruptedException {
        deviceLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (!servicesDiscovered) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = serviceDiscoveryCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            deviceLock.unlock();
        }
        return true;
    }

    @Override
    protected void notifyListeners(BluetoothEventType event, Object... args) {
        switch (event) {
            case SCAN_RECORD:
            case CHARACTERISTIC_UPDATED:
            case DESCRIPTOR_UPDATED:
            case SERVICES_DISCOVERED:
                updateLastSeenTime();
                break;
            default:
                break;
        }
        switch (event) {
            case SERVICES_DISCOVERED:
                deviceLock.lock();
                try {
                    servicesDiscovered = true;
                    serviceDiscoveryCondition.signal();
                } finally {
                    deviceLock.unlock();
                }
                break;
            case CONNECTION_STATE:
                deviceLock.lock();
                try {
                    connectionCondition.signal();
                } finally {
                    deviceLock.unlock();
                }
                break;
            default:
                break;
        }
        super.notifyListeners(event, args);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BluetoothDevice [address=");
        builder.append(address);
        builder.append(", manufacturer=");
        builder.append(manufacturer);
        if (BluetoothCompanyIdentifiers.get(manufacturer) != null) {
            builder.append(" (");
            builder.append(BluetoothCompanyIdentifiers.get(manufacturer));
            builder.append(')');
        }
        builder.append(", name=");
        builder.append(name);
        builder.append(", rssi=");
        builder.append(rssi);
        builder.append(']');
        return builder.toString();
    }
}
