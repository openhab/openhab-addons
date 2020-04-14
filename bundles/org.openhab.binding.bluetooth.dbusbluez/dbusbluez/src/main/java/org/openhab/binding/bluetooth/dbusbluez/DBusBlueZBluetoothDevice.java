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
package org.openhab.binding.bluetooth.dbusbluez;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.util.HexUtils;
import org.freedesktop.dbus.errors.NoReply;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.dbusbluez.handler.DBusBlueZBridgeHandler;
import org.openhab.binding.bluetooth.dbusbluez.handler.DBusBlueZEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.DBusBlueZEventListener;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.CharacteristicUpdateEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.ConnectedEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.ManufacturerDataEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.NameEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.RssiEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.ServicesResolvedEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.TXPowerEvent;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattDescriptor;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService;

/**
 * Implementation of BluetoothDevice for BlueZ via DBus-BlueZ API
 *
 *
 * @author Benjamin Lafois - Initial contribution and API
 *
 */
public class DBusBlueZBluetoothDevice extends BluetoothDevice implements DBusBlueZEventListener {

    // TODO implement characteristic read notification
    // TODO implement characteristic write notification
    // TODO implement descriptor notifications ?

    private final Logger logger = LoggerFactory.getLogger(DBusBlueZBluetoothDevice.class);

    // Device from native lib
    private com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice device;

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("bluetooth");

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor
     *
     * @param adapter the bridge handler through which this device is connected
     * @param address the Bluetooth address of the device
     * @param name the name of the device
     */
    public DBusBlueZBluetoothDevice(DBusBlueZBridgeHandler adapter, BluetoothAddress address) {
        super(adapter, address);
        logger.debug("Creating DBusBlueZ device with address '{}'", address);

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean connect() {
        logger.debug("Connect({})", device);

        if (device != null) {
            if (Boolean.FALSE.equals(device.isConnected())) {

                try {
                    boolean ret = device.connect();
                    logger.debug("Connect result: {}", ret);
                    return ret;
                } catch (NoReply e) {
                    // Have to double check because sometimes, exception but still worked
                    logger.debug("Got a timeout - but sometimes happen. Is Connected ? {}", device.isConnected());
                    if (Boolean.FALSE.equals(device.isConnected())) {

                        notifyListeners(BluetoothEventType.CONNECTION_STATE,
                                new BluetoothConnectionStatusNotification(ConnectionState.DISCONNECTED));
                        return false;
                    } else {
                        return true;
                    }

                } catch (Exception e) {
                    logger.error("error occured while trying to connect", e);
                }

            } else {
                logger.debug("Device was already connected");
            }
        }
        return false;

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean disconnect() {

        if (device != null && device.isConnected()) {
            logger.debug("Disconnecting '{}'", address);
            try {
                return device.disconnect();
            } catch (Exception e) {
                logger.debug("Exception occurred when trying to disconnect device '{}': {}", device.getAddress(),
                        e.getMessage());
            }
        }
        return false;

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void ensureConnected() {
        if (device == null || !device.isConnected()) {
            throw new IllegalStateException("DBusBlueZ device is not set or not connected");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private BluetoothGattCharacteristic getDBusBlueZCharacteristicByUUID(String uuid) {
        for (BluetoothGattService service : device.getGattServices()) {
            for (BluetoothGattCharacteristic c : service.getGattCharacteristics()) {
                if (c.getUuid().equalsIgnoreCase(uuid)) {
                    return c;
                }
            }
        }
        return null;
    }

    private BluetoothGattCharacteristic getDBusBlueZCharacteristicByDBusPath(String dBusPath) {
        for (BluetoothGattService service : this.device.getGattServices()) {
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

    @SuppressWarnings("unused")
    private BluetoothGattDescriptor getDBusBlueZDescriptorByUUID(String uuid) {
        for (BluetoothGattService service : device.getGattServices()) {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean enableNotifications(BluetoothCharacteristic characteristic) {
        ensureConnected();

        BluetoothGattCharacteristic c = getDBusBlueZCharacteristicByUUID(characteristic.getUuid().toString());
        if (c != null) {

            try {
                c.startNotify();
            } catch (Exception e) {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean writeCharacteristic(BluetoothCharacteristic characteristic) {
        logger.debug("writeCharacteristic()");

        ensureConnected();

        BluetoothGattCharacteristic c = getDBusBlueZCharacteristicByUUID(characteristic.getUuid().toString());
        if (c == null) {
            logger.warn("Characteristic '{}' is missing on device '{}'.", characteristic.getUuid(), address);
            return false;
        }

        scheduler.submit(() -> {
            try {
                c.writeValue(characteristic.getByteValue(), null);
                notifyListeners(BluetoothEventType.CHARACTERISTIC_WRITE_COMPLETE, characteristic,
                        BluetoothCompletionStatus.SUCCESS);

            } catch (Exception e) {
                logger.debug("Exception occurred when trying to write characteristic '{}': {}",
                        characteristic.getUuid(), e.getMessage());
                notifyListeners(BluetoothEventType.CHARACTERISTIC_WRITE_COMPLETE, characteristic,
                        BluetoothCompletionStatus.ERROR);
            }
        });
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDBusBlueZEvent(DBusBlueZEvent event) {

        logger.debug("onDBusBlueZEvent(): {}", event);

        if (event == null || event.getDevice() == null) {
            return;
        }

        switch (event.getEventType()) {
            case RSSI_UPDATE:
                onRssiUpdate((RssiEvent) event);
                break;
            case TXPOWER:
                onTxPowerUpdate((TXPowerEvent) event);
                break;
            case CHARACTERISTIC_NOTIFY:
                onCharacteristicNotify((CharacteristicUpdateEvent) event);
                break;
            case MANUFACTURER_DATA:
                onManufacturerDataUpdate((ManufacturerDataEvent) event);
                break;
            case CONNECTED:
                onConnectedStatusUpdate((ConnectedEvent) event);
                break;
            case NAME:
                onNameUpdate((NameEvent) event);
                break;
            case SERVICES_RESOLVED:
                onServicesResolved((ServicesResolvedEvent) event);
                break;
            default:
                logger.debug("Unsupported event: {}", event.getEventType());
                break;
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onServicesResolved(ServicesResolvedEvent event) {
        if (event.isResolved()) {
            notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onNameUpdate(NameEvent event) {
        BluetoothScanNotification notification = new BluetoothScanNotification();
        notification.setDeviceName(event.getName());
        notifyListeners(BluetoothEventType.SCAN_RECORD, notification);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onManufacturerDataUpdate(ManufacturerDataEvent event) {

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

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onTxPowerUpdate(TXPowerEvent event) {
        this.txPower = (int) event.getTxPower();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onCharacteristicNotify(CharacteristicUpdateEvent event) {
        // Here it is a bit special - as the event is linked to the DBUS path, not characteristic UUID.
        // So we need to find the characteristic by its DBUS path.
        BluetoothGattCharacteristic characteristic = getDBusBlueZCharacteristicByDBusPath(event.getDbusPath());
        if (characteristic == null) {
            logger.info("Received a notification for a characteristic not found on device.");
            return;
        }
        BluetoothCharacteristic c = getCharacteristic(UUID.fromString(characteristic.getUuid()));
        if (c != null) {
            c.setValue(event.getData());
            notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, c, BluetoothCompletionStatus.SUCCESS);
            logger.debug("Has notified that a characteristic has been updated");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("null")
    private void onRssiUpdate(RssiEvent event) {
        this.rssi = (int) event.getRssi();
        BluetoothScanNotification notification = new BluetoothScanNotification();
        notification.setRssi(this.rssi);
        notifyListeners(BluetoothEventType.SCAN_RECORD, notification);
    }

    private void onConnectedStatusUpdate(ConnectedEvent event) {
        connectionState = event.isConnected() ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED;
        notifyListeners(BluetoothEventType.CONNECTION_STATE,
                new BluetoothConnectionStatusNotification(connectionState));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void refreshServices() {
        if (device.getGattServices().size() > getServices().size()) {

            for (BluetoothGattService dBusBlueZService : device.getGattServices()) {
                BluetoothService service = new BluetoothService(UUID.fromString(dBusBlueZService.getUuid()),
                        dBusBlueZService.isPrimary());

                for (BluetoothGattCharacteristic dBusBlueZCharacteristic : dBusBlueZService.getGattCharacteristics()) {
                    BluetoothCharacteristic characteristic = new BluetoothCharacteristic(
                            UUID.fromString(dBusBlueZCharacteristic.getUuid()), 0);

                    for (BluetoothGattDescriptor dBusBlueZDescriptor : dBusBlueZCharacteristic.getGattDescriptors()) {
                        BluetoothDescriptor descriptor = new BluetoothDescriptor(characteristic,
                                UUID.fromString(dBusBlueZDescriptor.getUuid()));
                        characteristic.addDescriptor(descriptor);
                    }

                    service.addCharacteristic(characteristic);
                }

                addService(service);
            }

            notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void updateDBusBlueZDevice(
            com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice dBusBlueZDevice) {

        if (Objects.equals(device, dBusBlueZDevice)) {
            return;
        }

        this.device = dBusBlueZDevice;

        if (this.device == null) {
            return;
        }
        updateLastSeenTime();

        this.name = device.getName();

        if (Boolean.TRUE.equals(device.isConnected())) {
            this.connectionState = ConnectionState.CONNECTED;
        }

        refreshServices();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Clean up and release memory.
     */
    @Override
    public void dispose() {
        this.device = null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

}
