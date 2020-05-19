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
package org.openhab.binding.bluetooth.bluegiga;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.bluetooth.BaseBluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.bluegiga.handler.BlueGigaBridgeHandler;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaEventListener;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaAttributeValueEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindInformationFoundEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaGroupFoundEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaProcedureCompletedEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaConnectionStatusEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectedEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaScanResponseEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.eir.EirDataType;
import org.openhab.binding.bluetooth.bluegiga.internal.eir.EirPacket;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BgApiResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.ConnectionStatusFlag;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification.BluetoothBeaconType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extended {@link BluetoothDevice} class to handle BlueGiga specific information
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public class BlueGigaBluetoothDevice extends BaseBluetoothDevice implements BlueGigaEventListener {
    private final long TIMEOUT_SEC = 60;

    private final Logger logger = LoggerFactory.getLogger(BlueGigaBluetoothDevice.class);

    // BlueGiga needs to know the address type when connecting
    private BluetoothAddressType addressType = BluetoothAddressType.UNKNOWN;

    // The dongle handler
    private final BlueGigaBridgeHandler bgHandler;

    // An enum to use in the state machine for interacting with the device
    private enum BlueGigaProcedure {
        NONE,
        GET_SERVICES,
        GET_CHARACTERISTICS,
        CHARACTERISTIC_READ,
        CHARACTERISTIC_WRITE
    }

    private BlueGigaProcedure procedureProgress = BlueGigaProcedure.NONE;

    // Somewhere to remember what characteristic we're working on
    private @Nullable BluetoothCharacteristic procedureCharacteristic;

    // The connection handle if the device is connected
    private int connection = -1;

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("bluetooth");

    private @Nullable ScheduledFuture<?> connectTimer;
    private @Nullable ScheduledFuture<?> procedureTimer;

    private Runnable connectTimeoutTask = new Runnable() {
        @Override
        public void run() {
            if (connectionState == ConnectionState.CONNECTING) {
                logger.debug("Connection timeout for device {}", address);
                connectionState = ConnectionState.DISCONNECTED;
            }
        }
    };

    private Runnable procedureTimeoutTask = new Runnable() {
        @Override
        public void run() {
            logger.debug("Procedure {} timeout for device {}", procedureProgress, address);
            procedureProgress = BlueGigaProcedure.NONE;
            procedureCharacteristic = null;
        }
    };

    /**
     * Creates a new {@link BlueGigaBluetoothDevice} which extends {@link BluetoothDevice} for the BlueGiga
     * implementation
     *
     * @param bgHandler the {@link BlueGigaBridgeHandler} that provides the link to the dongle
     * @param address the {@link BluetoothAddress} for this device
     * @param addressType the {@link BluetoothAddressType} of this device
     */
    public BlueGigaBluetoothDevice(BlueGigaBridgeHandler bgHandler, BluetoothAddress address,
            BluetoothAddressType addressType) {
        super(bgHandler, address);

        logger.debug("Creating new BlueGiga device {}", address);

        this.bgHandler = bgHandler;
        this.addressType = addressType;

        bgHandler.addEventListener(this);
        updateLastSeenTime();
    }

    @Override
    public boolean connect() {
        if (connection != -1) {
            // We're already connected
            return false;
        }

        cancelTimer(connectTimer);
        if (bgHandler.bgConnect(address, addressType)) {
            connectionState = ConnectionState.CONNECTING;
            connectTimer = startTimer(connectTimeoutTask, TIMEOUT_SEC);
            return true;
        } else {
            connectionState = ConnectionState.DISCONNECTED;
            return false;
        }
    }

    @Override
    public boolean disconnect() {
        if (connection == -1) {
            // We're already disconnected
            return false;
        }

        return bgHandler.bgDisconnect(connection);
    }

    @Override
    public boolean discoverServices() {
        if (procedureProgress != BlueGigaProcedure.NONE) {
            return false;
        }

        cancelTimer(procedureTimer);
        if (!bgHandler.bgFindPrimaryServices(connection)) {
            return false;
        }

        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        procedureProgress = BlueGigaProcedure.GET_SERVICES;
        return true;
    }

    @Override
    public boolean enableNotifications(BluetoothCharacteristic characteristic) {
        // TODO will be implemented in a followup PR
        return false;
    }

    @Override
    public boolean disableNotifications(BluetoothCharacteristic characteristic) {
        // TODO will be implemented in a followup PR
        return false;
    }

    @Override
    public boolean enableNotifications(BluetoothDescriptor descriptor) {
        // TODO will be implemented in a followup PR
        return false;
    }

    @Override
    public boolean disableNotifications(BluetoothDescriptor descriptor) {
        // TODO will be implemented in a followup PR
        return false;
    }

    @Override
    public boolean readCharacteristic(@Nullable BluetoothCharacteristic characteristic) {
        if (characteristic == null || characteristic.getHandle() == 0) {
            return false;
        }

        if (procedureProgress != BlueGigaProcedure.NONE) {
            return false;
        }

        cancelTimer(procedureTimer);
        if (!bgHandler.bgReadCharacteristic(connection, characteristic.getHandle())) {
            return false;
        }
        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        procedureProgress = BlueGigaProcedure.CHARACTERISTIC_READ;
        procedureCharacteristic = characteristic;

        return true;
    }

    @Override
    public boolean writeCharacteristic(@Nullable BluetoothCharacteristic characteristic) {
        if (characteristic == null || characteristic.getHandle() == 0) {
            return false;
        }

        if (procedureProgress != BlueGigaProcedure.NONE) {
            return false;
        }

        cancelTimer(procedureTimer);
        if (!bgHandler.bgWriteCharacteristic(connection, characteristic.getHandle(), characteristic.getValue())) {
            return false;
        }

        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        procedureProgress = BlueGigaProcedure.CHARACTERISTIC_WRITE;
        procedureCharacteristic = characteristic;

        return true;
    }

    @Override
    public void bluegigaEventReceived(BlueGigaResponse event) {
        if (event instanceof BlueGigaScanResponseEvent) {
            handleScanEvent((BlueGigaScanResponseEvent) event);
        }

        else if (event instanceof BlueGigaGroupFoundEvent) {
            handleGroupFoundEvent((BlueGigaGroupFoundEvent) event);
        }

        else if (event instanceof BlueGigaFindInformationFoundEvent) {
            // A Characteristic has been discovered
            handleFindInformationFoundEvent((BlueGigaFindInformationFoundEvent) event);
        }

        else if (event instanceof BlueGigaProcedureCompletedEvent) {
            handleProcedureCompletedEvent((BlueGigaProcedureCompletedEvent) event);
        }

        else if (event instanceof BlueGigaConnectionStatusEvent) {
            handleConnectionStatusEvent((BlueGigaConnectionStatusEvent) event);
        }

        else if (event instanceof BlueGigaDisconnectedEvent) {
            handleDisconnectedEvent((BlueGigaDisconnectedEvent) event);
        }

        else if (event instanceof BlueGigaAttributeValueEvent) {
            handleAttributeValueEvent((BlueGigaAttributeValueEvent) event);
        }
    }

    private void handleScanEvent(BlueGigaScanResponseEvent event) {
        // Check if this is addressed to this device
        if (!address.equals(new BluetoothAddress(event.getSender()))) {
            return;
        }

        logger.trace("scanEvent: {}", event);
        updateLastSeenTime();

        // Set device properties
        rssi = event.getRssi();
        addressType = event.getAddressType();

        byte[] manufacturerData = null;

        // If the packet contains data, then process it and add anything relevant to the device...
        if (event.getData().length > 0) {
            EirPacket eir = new EirPacket(event.getData());
            for (EirDataType record : eir.getRecords().keySet()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("  EirDataType: {}={}", record, eir.getRecord(record));
                }
                Object obj;
                switch (record) {
                    case EIR_FLAGS:
                        break;
                    case EIR_MANUFACTURER_SPECIFIC:
                        obj = eir.getRecord(EirDataType.EIR_MANUFACTURER_SPECIFIC);
                        if (obj != null) {
                            try {
                                @SuppressWarnings("unchecked")
                                Map<Short, int[]> eirRecord = (Map<Short, int[]>) obj;
                                Map.Entry<Short, int[]> eirEntry = eirRecord.entrySet().iterator().next();

                                manufacturer = eirEntry.getKey().intValue();

                                int[] manufacturerInt = eirEntry.getValue();
                                manufacturerData = new byte[manufacturerInt.length + 2];
                                // Convert short Company ID to bytes and add it to manufacturerData
                                manufacturerData[0] = (byte) (manufacturer & 0xff);
                                manufacturerData[1] = (byte) ((manufacturer >> 8) & 0xff);
                                // Add Convert int custom data nd add it to manufacturerData
                                for (int i = 0; i < manufacturerInt.length; i++) {
                                    manufacturerData[i + 2] = (byte) manufacturerInt[i];
                                }
                            } catch (ClassCastException e) {
                                logger.debug("Unsupported manufacturer specific record received from device {}",
                                        address);
                            }
                        }
                        break;
                    case EIR_NAME_LONG:
                    case EIR_NAME_SHORT:
                        name = (String) eir.getRecord(record);
                        break;
                    case EIR_SLAVEINTERVALRANGE:
                        break;
                    case EIR_SVC_DATA_UUID128:
                        break;
                    case EIR_SVC_DATA_UUID16:
                        break;
                    case EIR_SVC_DATA_UUID32:
                        break;
                    case EIR_SVC_UUID128_INCOMPLETE:
                    case EIR_SVC_UUID16_COMPLETE:
                    case EIR_SVC_UUID16_INCOMPLETE:
                    case EIR_SVC_UUID32_COMPLETE:
                    case EIR_SVC_UUID32_INCOMPLETE:
                    case EIR_SVC_UUID128_COMPLETE:
                        // addServices((List<UUID>) eir.getRecord(record));
                        break;
                    case EIR_TXPOWER:
                        obj = eir.getRecord(EirDataType.EIR_TXPOWER);
                        if (obj != null) {
                            txPower = (int) obj;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (connectionState == ConnectionState.DISCOVERING) {
            // TODO: It could make sense to wait with discovery for non-connectable devices until scan response is
            // received to eventually retrieve more about the device before it gets discovered. Anyhow, devices
            // that don't send a scan response at all also have to be supported. See also PR #6995.

            // Set our state to disconnected
            connectionState = ConnectionState.DISCONNECTED;
            connection = -1;

            // But notify listeners that the state is now DISCOVERED
            notifyListeners(BluetoothEventType.CONNECTION_STATE,
                    new BluetoothConnectionStatusNotification(ConnectionState.DISCOVERED));

            // Notify the bridge - for inbox notifications
            bgHandler.deviceDiscovered(this);
        }

        // Notify listeners of all scan records - for RSSI, beacon processing (etc)
        BluetoothScanNotification scanNotification = new BluetoothScanNotification();
        scanNotification.setRssi(event.getRssi());

        switch (event.getPacketType()) {
            case CONNECTABLE_ADVERTISEMENT:
            case DISCOVERABLE_ADVERTISEMENT:
            case NON_CONNECTABLE_ADVERTISEMENT:
                scanNotification.setBeaconType(BluetoothBeaconType.BEACON_ADVERTISEMENT);
                break;
            case SCAN_RESPONSE:
                scanNotification.setBeaconType(BluetoothBeaconType.BEACON_SCANRESPONSE);
                break;
            default:
                break;
        }

        if (manufacturerData != null) {
            scanNotification.setManufacturerData(manufacturerData);
        }

        notifyListeners(BluetoothEventType.SCAN_RECORD, scanNotification);
    }

    private void handleGroupFoundEvent(BlueGigaGroupFoundEvent event) {
        // If this is not our connection handle then ignore.
        if (connection != event.getConnection()) {
            return;
        }

        logger.trace("BlueGiga Group: {} svcs={}", this, supportedServices);
        updateLastSeenTime();

        BluetoothService service = new BluetoothService(event.getUuid(), true, event.getStart(), event.getEnd());
        addService(service);
    }

    private void handleFindInformationFoundEvent(BlueGigaFindInformationFoundEvent event) {
        // If this is not our connection handle then ignore.
        if (connection != event.getConnection()) {
            return;
        }

        logger.trace("BlueGiga FindInfo: {} svcs={}", this, supportedServices);
        updateLastSeenTime();

        BluetoothCharacteristic characteristic = new BluetoothCharacteristic(event.getUuid(), event.getChrHandle());

        BluetoothService service = getServiceByHandle(characteristic.getHandle());
        if (service == null) {
            logger.debug("BlueGiga: Unable to find service for handle {}", characteristic.getHandle());
            return;
        }
        characteristic.setService(service);
        service.addCharacteristic(characteristic);
    }

    private void handleProcedureCompletedEvent(BlueGigaProcedureCompletedEvent event) {
        // If this is not our connection handle then ignore.
        if (connection != event.getConnection()) {
            return;
        }

        if (procedureProgress == BlueGigaProcedure.NONE) {
            logger.debug("BlueGiga procedure completed but procedure is null with connection {}, address {}",
                    connection, address);
            return;
        }

        cancelTimer(procedureTimer);
        updateLastSeenTime();

        // The current procedure is now complete - move on...
        switch (procedureProgress) {
            case GET_SERVICES:
                // We've downloaded all services, now get the characteristics
                if (bgHandler.bgFindCharacteristics(connection)) {
                    procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
                    procedureProgress = BlueGigaProcedure.GET_CHARACTERISTICS;
                } else {
                    procedureProgress = BlueGigaProcedure.NONE;
                }
                break;
            case GET_CHARACTERISTICS:
                // We've downloaded all characteristics
                procedureProgress = BlueGigaProcedure.NONE;
                notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
                break;
            case CHARACTERISTIC_READ:
                // The read failed
                notifyListeners(BluetoothEventType.CHARACTERISTIC_READ_COMPLETE, procedureCharacteristic,
                        BluetoothCompletionStatus.ERROR);
                procedureProgress = BlueGigaProcedure.NONE;
                procedureCharacteristic = null;
                break;
            case CHARACTERISTIC_WRITE:
                // The write completed - failure or success
                BluetoothCompletionStatus result = event.getResult() == BgApiResponse.SUCCESS
                        ? BluetoothCompletionStatus.SUCCESS
                        : BluetoothCompletionStatus.ERROR;
                notifyListeners(BluetoothEventType.CHARACTERISTIC_WRITE_COMPLETE, procedureCharacteristic, result);
                procedureProgress = BlueGigaProcedure.NONE;
                procedureCharacteristic = null;
                break;
            default:
                break;
        }
    }

    private void handleConnectionStatusEvent(BlueGigaConnectionStatusEvent event) {
        // Check if this is addressed to this device
        if (!address.equals(new BluetoothAddress(event.getAddress()))) {
            return;
        }

        cancelTimer(connectTimer);
        updateLastSeenTime();

        // If we're connected, then remember the connection handle
        if (event.getFlags().contains(ConnectionStatusFlag.CONNECTION_CONNECTED)) {
            connectionState = ConnectionState.CONNECTED;
            connection = event.getConnection();
            notifyListeners(BluetoothEventType.CONNECTION_STATE,
                    new BluetoothConnectionStatusNotification(connectionState));
        }
    }

    private void handleDisconnectedEvent(BlueGigaDisconnectedEvent event) {
        // If this is not our connection handle then ignore.
        if (connection != event.getConnection()) {
            return;
        }

        cancelTimer(procedureTimer);
        connectionState = ConnectionState.DISCONNECTED;
        connection = -1;
        procedureProgress = BlueGigaProcedure.NONE;

        notifyListeners(BluetoothEventType.CONNECTION_STATE,
                new BluetoothConnectionStatusNotification(connectionState));
    }

    private void handleAttributeValueEvent(BlueGigaAttributeValueEvent event) {
        // If this is not our connection handle then ignore.
        if (connection != event.getConnection()) {
            return;
        }

        updateLastSeenTime();

        BluetoothCharacteristic characteristic = getCharacteristicByHandle(event.getAttHandle());
        if (characteristic == null) {
            logger.debug("BlueGiga didn't find characteristic for event {}", event);
        } else {
            characteristic.setValue(event.getValue().clone());

            // If this is the characteristic we were reading, then send a read completion
            if (procedureProgress == BlueGigaProcedure.CHARACTERISTIC_READ && procedureCharacteristic != null
                    && procedureCharacteristic.getHandle() == event.getAttHandle()) {
                procedureProgress = BlueGigaProcedure.NONE;
                procedureCharacteristic = null;
                notifyListeners(BluetoothEventType.CHARACTERISTIC_READ_COMPLETE, characteristic,
                        BluetoothCompletionStatus.SUCCESS);
            }

            // Notify the user of the updated value
            notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, characteristic);
        }
    }

    /**
     * Clean up and release memory.
     */
    @Override
    public void dispose() {
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
        cancelTimer(connectTimer);
        cancelTimer(procedureTimer);
        bgHandler.removeEventListener(this);
        procedureProgress = BlueGigaProcedure.NONE;
        connectionState = ConnectionState.DISCOVERING;
        connection = -1;
    }

    private void cancelTimer(@Nullable ScheduledFuture<?> task) {
        if (task != null) {
            task.cancel(true);
        }
    }

    private ScheduledFuture<?> startTimer(Runnable command, long timeout) {
        return scheduler.schedule(command, timeout, TimeUnit.SECONDS);
    }

}
