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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.freedesktop.dbus.errors.NoReply;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.UInt16;
import org.openhab.binding.bluetooth.BaseBluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEventListener;
import org.openhab.binding.bluetooth.bluez.internal.events.CharacteristicUpdateEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ConnectedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ManufacturerDataEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.NameEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.RssiEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ServiceDataEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ServicesResolvedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.TXPowerEvent;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.binding.bluetooth.util.RetryException;
import org.openhab.binding.bluetooth.util.RetryFuture;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattDescriptor;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService;

/**
 * Implementation of BluetoothDevice for BlueZ via DBus-BlueZ API
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Benjamin Lafois - Replaced tinyB with bluezDbus
 * @author Peter Rosenberg - Improve notifications and properties support
 *
 */
@NonNullByDefault
public class BlueZBluetoothDevice extends BaseBluetoothDevice implements BlueZEventListener {

    private final Logger logger = LoggerFactory.getLogger(BlueZBluetoothDevice.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("bluetooth");

    // Device from native lib
    private @Nullable BluetoothDevice device = null;

    /**
     * Constructor
     *
     * @param adapter the bridge handler through which this device is connected
     * @param address the Bluetooth address of the device
     * @param name the name of the device
     */
    public BlueZBluetoothDevice(BlueZBridgeHandler adapter, BluetoothAddress address) {
        super(adapter, address);
        logger.debug("Creating DBusBlueZ device with address '{}'", address);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public synchronized void updateBlueZDevice(@Nullable BluetoothDevice blueZDevice) {
        if (this.device != null && this.device == blueZDevice) {
            return;
        }
        logger.debug("updateBlueZDevice({})", blueZDevice);

        this.device = blueZDevice;

        if (blueZDevice == null) {
            return;
        }

        Short rssi = blueZDevice.getRssi();
        if (rssi != null) {
            this.rssi = rssi.intValue();
        }
        this.name = blueZDevice.getName();
        Map<UInt16, byte[]> manData = blueZDevice.getManufacturerData();
        if (manData != null) {
            manData.entrySet().stream().map(Map.Entry::getKey).filter(Objects::nonNull).findFirst()
                    .ifPresent((UInt16 manufacturerId) ->
                    // Convert to unsigned int to match the convention in BluetoothCompanyIdentifiers
                    this.manufacturer = manufacturerId.intValue() & 0xFFFF);
        }

        if (Boolean.TRUE.equals(blueZDevice.isConnected())) {
            setConnectionState(ConnectionState.CONNECTED);
        }

        discoverServices();
    }

    /**
     * Clean up and release memory.
     */
    @Override
    public void dispose() {
        BluetoothDevice dev = device;
        if (dev != null) {
            if (Boolean.TRUE.equals(dev.isPaired())) {
                return;
            }

            try {
                dev.getAdapter().removeDevice(dev.getRawDevice());
            } catch (DBusException ex) {
                String exceptionMessage = ex.getMessage();
                if (exceptionMessage == null || exceptionMessage.contains("Does Not Exist")) {
                    logger.debug("Exception occurred when trying to remove inactive device '{}': {}", address,
                            ex.getMessage());
                }
                // this codeblock will only be hit when the underlying device has already
                // been removed but we don't have a way to check if that is the case beforehand
                // so we will just eat the error here.
            } catch (RuntimeException ex) {
                // try to catch any other exceptions
                logger.debug("Exception occurred when trying to remove inactive device '{}': {}", address,
                        ex.getMessage());
            }
        }
    }

    private void setConnectionState(ConnectionState state) {
        if (this.connectionState != state) {
            this.connectionState = state;
            notifyListeners(BluetoothEventType.CONNECTION_STATE, new BluetoothConnectionStatusNotification(state));
        }
    }

    @Override
    public boolean connect() {
        logger.debug("Connect({})", device);

        BluetoothDevice dev = device;
        if (dev != null) {
            if (Boolean.FALSE.equals(dev.isConnected())) {
                try {
                    boolean ret = dev.connect();
                    logger.debug("Connect result: {}", ret);
                    return ret;
                } catch (NoReply e) {
                    // Have to double check because sometimes, exception but still worked
                    logger.debug("Got a timeout - but sometimes happen. Is Connected ? {}", dev.isConnected());
                    if (Boolean.FALSE.equals(dev.isConnected())) {
                        notifyListeners(BluetoothEventType.CONNECTION_STATE,
                                new BluetoothConnectionStatusNotification(ConnectionState.DISCONNECTED));
                        return false;
                    } else {
                        return true;
                    }
                } catch (DBusExecutionException e) {
                    // Catch "software caused connection abort"
                    return false;
                } catch (Exception e) {
                    logger.warn("error occured while trying to connect", e);
                }
            } else {
                logger.debug("Device was already connected");
                // we might be stuck in another state atm so we need to trigger a connected in this case
                setConnectionState(ConnectionState.CONNECTED);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean disconnect() {
        BluetoothDevice dev = device;
        if (dev != null) {
            logger.debug("Disconnecting '{}'", address);
            return dev.disconnect();
        }
        return false;
    }

    private void ensureConnected() {
        BluetoothDevice dev = device;
        if (dev == null || !dev.isConnected()) {
            throw new IllegalStateException("DBusBlueZ device is not set or not connected");
        }
    }

    private @Nullable BluetoothGattCharacteristic getDBusBlueZCharacteristicByUUID(String uuid) {
        BluetoothDevice dev = device;
        if (dev == null) {
            return null;
        }
        for (BluetoothGattService service : dev.getGattServices()) {
            for (BluetoothGattCharacteristic c : service.getGattCharacteristics()) {
                if (c.getUuid().equalsIgnoreCase(uuid)) {
                    return c;
                }
            }
        }
        return null;
    }

    private @Nullable BluetoothGattCharacteristic getDBusBlueZCharacteristicByDBusPath(String dBusPath) {
        BluetoothDevice dev = device;
        if (dev == null) {
            return null;
        }
        for (BluetoothGattService service : dev.getGattServices()) {
            if (dBusPath.startsWith(service.getDbusPath())) {
                for (BluetoothGattCharacteristic characteristic : service.getGattCharacteristics()) {
                    if (dBusPath.startsWith(characteristic.getDbusPath())) {
                        return characteristic;
                    }
                }
            }
        }
        return null;
    }

    private @Nullable BluetoothGattDescriptor getDBusBlueZDescriptorByUUID(String uuid) {
        BluetoothDevice dev = device;
        if (dev == null) {
            return null;
        }
        for (BluetoothGattService service : dev.getGattServices()) {
            for (BluetoothGattCharacteristic c : service.getGattCharacteristics()) {
                for (BluetoothGattDescriptor d : c.getGattDescriptors()) {
                    if (d.getUuid().equalsIgnoreCase(uuid)) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<@Nullable Void> enableNotifications(BluetoothCharacteristic characteristic) {
        BluetoothDevice dev = device;
        if (dev == null || !dev.isConnected()) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("DBusBlueZ device is not set or not connected"));
        }

        BluetoothGattCharacteristic c = getDBusBlueZCharacteristicByUUID(characteristic.getUuid().toString());
        if (c == null) {
            logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Characteristic " + characteristic.getUuid() + " is missing on device"));
        }

        return RetryFuture.callWithRetry(() -> {
            try {
                c.startNotify();
            } catch (DBusException e) {
                String exceptionMessage = e.getMessage();
                if (exceptionMessage != null && exceptionMessage.contains("Already notifying")) {
                    return null;
                } else if (exceptionMessage != null && exceptionMessage.contains("In Progress")) {
                    // let's retry in half a second
                    throw new RetryException(500, TimeUnit.MILLISECONDS);
                } else {
                    logger.warn("Exception occurred while activating notifications on '{}'", address, e);
                    throw e;
                }
            }
            return null;
        }, scheduler);
    }

    @Override
    public CompletableFuture<@Nullable Void> writeCharacteristic(BluetoothCharacteristic characteristic, byte[] value) {
        logger.debug("writeCharacteristic()");

        BluetoothDevice dev = device;
        if (dev == null || !dev.isConnected()) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("DBusBlueZ device is not set or not connected"));
        }

        BluetoothGattCharacteristic c = getDBusBlueZCharacteristicByUUID(characteristic.getUuid().toString());
        if (c == null) {
            logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Characteristic " + characteristic.getUuid() + " is missing on device"));
        }

        return RetryFuture.callWithRetry(() -> {
            try {
                c.writeValue(value, null);
                return null;
            } catch (DBusException e) {
                logger.debug("Exception occurred when trying to write characteristic '{}': {}",
                        characteristic.getUuid(), e.getMessage());
                throw e;
            }
        }, scheduler);
    }

    @Override
    public void onDBusBlueZEvent(BlueZEvent event) {
        logger.debug("Unsupported event: {}", event);
    }

    @Override
    public void onServicesResolved(ServicesResolvedEvent event) {
        if (event.isResolved()) {
            notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
        }
    }

    @Override
    public void onNameUpdate(NameEvent event) {
        BluetoothScanNotification notification = new BluetoothScanNotification();
        notification.setDeviceName(event.getName());
        notifyListeners(BluetoothEventType.SCAN_RECORD, notification);
    }

    @Override
    public void onManufacturerDataUpdate(ManufacturerDataEvent event) {
        for (Map.Entry<Short, byte[]> entry : event.getData().entrySet()) {
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
    }

    @Override
    public void onServiceDataUpdate(ServiceDataEvent event) {
        BluetoothScanNotification notification = new BluetoothScanNotification();
        notification.setServiceData(event.getData());
        notifyListeners(BluetoothEventType.SCAN_RECORD, notification);
    }

    @Override
    public void onTxPowerUpdate(TXPowerEvent event) {
        this.txPower = (int) event.getTxPower();
    }

    @Override
    public void onCharacteristicNotify(CharacteristicUpdateEvent event) {
        // Here it is a bit special - as the event is linked to the DBUS path, not characteristic UUID.
        // So we need to find the characteristic by its DBUS path.
        BluetoothGattCharacteristic characteristic = getDBusBlueZCharacteristicByDBusPath(event.getDbusPath());
        if (characteristic == null) {
            logger.debug("Received a notification for a characteristic not found on device.");
            return;
        }
        BluetoothCharacteristic c = getCharacteristic(UUID.fromString(characteristic.getUuid()));
        if (c != null) {
            notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, c, event.getData());
        }
    }

    @Override
    public void onRssiUpdate(RssiEvent event) {
        int rssiTmp = event.getRssi();
        this.rssi = rssiTmp;
        BluetoothScanNotification notification = new BluetoothScanNotification();
        notification.setRssi(rssiTmp);
        notifyListeners(BluetoothEventType.SCAN_RECORD, notification);
    }

    @Override
    public void onConnectedStatusUpdate(ConnectedEvent event) {
        this.connectionState = event.isConnected() ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED;
        notifyListeners(BluetoothEventType.CONNECTION_STATE,
                new BluetoothConnectionStatusNotification(connectionState));
    }

    @Override
    public boolean discoverServices() {
        BluetoothDevice dev = device;
        if (dev == null) {
            return false;
        }
        if (dev.getGattServices().size() > getServices().size()) {
            for (BluetoothGattService dBusBlueZService : dev.getGattServices()) {
                BluetoothService service = new BluetoothService(UUID.fromString(dBusBlueZService.getUuid()),
                        dBusBlueZService.isPrimary());
                for (BluetoothGattCharacteristic dBusBlueZCharacteristic : dBusBlueZService.getGattCharacteristics()) {
                    BluetoothCharacteristic characteristic = new BluetoothCharacteristic(
                            UUID.fromString(dBusBlueZCharacteristic.getUuid()), 0);
                    convertCharacteristicProperties(dBusBlueZCharacteristic, characteristic);

                    for (BluetoothGattDescriptor dBusBlueZDescriptor : dBusBlueZCharacteristic.getGattDescriptors()) {
                        BluetoothDescriptor descriptor = new BluetoothDescriptor(characteristic,
                                UUID.fromString(dBusBlueZDescriptor.getUuid()), 0);
                        characteristic.addDescriptor(descriptor);
                    }
                    service.addCharacteristic(characteristic);
                }
                addService(service);
            }
            notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
        }
        return true;
    }

    /**
     * Convert the flags of BluetoothGattCharacteristic to the int bitset used by BluetoothCharacteristic.
     *
     * @param dBusBlueZCharacteristic source characteristic to read the flags from
     * @param characteristic destination characteristic to write to properties to
     */
    private void convertCharacteristicProperties(BluetoothGattCharacteristic dBusBlueZCharacteristic,
            BluetoothCharacteristic characteristic) {
        int properties = 0;

        for (String property : dBusBlueZCharacteristic.getFlags()) {
            switch (property) {
                case "broadcast":
                    properties |= BluetoothCharacteristic.PROPERTY_BROADCAST;
                    break;
                case "read":
                    properties |= BluetoothCharacteristic.PROPERTY_READ;
                    break;
                case "write-without-response":
                    properties |= BluetoothCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
                    break;
                case "write":
                    properties |= BluetoothCharacteristic.PROPERTY_WRITE;
                    break;
                case "notify":
                    properties |= BluetoothCharacteristic.PROPERTY_NOTIFY;
                    break;
                case "indicate":
                    properties |= BluetoothCharacteristic.PROPERTY_INDICATE;
                    break;
            }
        }

        characteristic.setProperties(properties);
    }

    @Override
    public CompletableFuture<byte[]> readCharacteristic(BluetoothCharacteristic characteristic) {
        BluetoothDevice dev = device;
        if (dev == null || !dev.isConnected()) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("DBusBlueZ device is not set or not connected"));
        }

        BluetoothGattCharacteristic c = getDBusBlueZCharacteristicByUUID(characteristic.getUuid().toString());
        if (c == null) {
            logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Characteristic " + characteristic.getUuid() + " is missing on device"));
        }

        return RetryFuture.callWithRetry(() -> {
            try {
                return c.readValue(null);
            } catch (DBusException | DBusExecutionException e) {
                // DBusExecutionException is thrown if the value cannot be read
                logger.debug("Exception occurred when trying to read characteristic '{}': {}", characteristic.getUuid(),
                        e.getMessage());
                throw e;
            }
        }, scheduler);
    }

    @Override
    public boolean isNotifying(BluetoothCharacteristic characteristic) {
        BluetoothGattCharacteristic c = getDBusBlueZCharacteristicByUUID(characteristic.getUuid().toString());
        if (c != null) {
            Boolean isNotifying = c.isNotifying();
            return Objects.requireNonNullElse(isNotifying, false);
        } else {
            logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
            return false;
        }
    }

    @Override
    public CompletableFuture<@Nullable Void> disableNotifications(BluetoothCharacteristic characteristic) {
        BluetoothDevice dev = device;
        if (dev == null || !dev.isConnected()) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("DBusBlueZ device is not set or not connected"));
        }
        BluetoothGattCharacteristic c = getDBusBlueZCharacteristicByUUID(characteristic.getUuid().toString());
        if (c == null) {
            logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Characteristic " + characteristic.getUuid() + " is missing on device"));
        }

        return RetryFuture.callWithRetry(() -> {
            try {
                c.stopNotify();
            } catch (DBusException e) {
                String exceptionMessage = e.getMessage();
                if (exceptionMessage != null && exceptionMessage.contains("Already notifying")) {
                    return null;
                } else if (exceptionMessage != null && exceptionMessage.contains("In Progress")) {
                    // let's retry in half a second
                    throw new RetryException(500, TimeUnit.MILLISECONDS);
                } else {
                    logger.warn("Exception occurred while deactivating notifications on '{}'", address, e);
                    throw e;
                }
            }
            return null;
        }, scheduler);
    }

    @Override
    public boolean enableNotifications(BluetoothDescriptor descriptor) {
        // Not sure if it is possible to implement this
        return false;
    }

    @Override
    public boolean disableNotifications(BluetoothDescriptor descriptor) {
        // Not sure if it is possible to implement this
        return false;
    }
}
