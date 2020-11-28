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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BaseBluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
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
import org.openhab.core.common.ThreadPoolManager;
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

    private Map<Integer, UUID> handleToUUID = new HashMap<>();
    private NavigableMap<Integer, BlueGigaBluetoothCharacteristic> handleToCharacteristic = new TreeMap<>();

    // BlueGiga needs to know the address type when connecting
    private BluetoothAddressType addressType = BluetoothAddressType.UNKNOWN;

    // The dongle handler
    private final BlueGigaBridgeHandler bgHandler;

    // An enum to use in the state machine for interacting with the device
    private enum BlueGigaProcedure {
        NONE,
        GET_SERVICES,
        GET_CHARACTERISTICS,
        READ_CHARACTERISTIC_DECL,
        CHARACTERISTIC_READ,
        CHARACTERISTIC_WRITE,
        NOTIFICATION_ENABLE,
        NOTIFICATION_DISABLE
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

    public void setAddressType(BluetoothAddressType addressType) {
        this.addressType = addressType;
    }

    @Override
    public boolean connect() {
        if (connection != -1) {
            // We're already connected
            return true;
        }

        cancelTimer(connectTimer);
        if (bgHandler.bgConnect(address, addressType)) {
            connectionState = ConnectionState.CONNECTING;
            connectTimer = startTimer(connectTimeoutTask, 10);
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
            return true;
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
        if (connection == -1) {
            logger.debug("Cannot enable notifications, device not connected {}", this);
            return false;
        }

        BlueGigaBluetoothCharacteristic ch = (BlueGigaBluetoothCharacteristic) characteristic;
        if (ch.isNotificationEnabled()) {
            return true;
        }

        BluetoothDescriptor descriptor = ch
                .getDescriptor(BluetoothDescriptor.GattDescriptor.CLIENT_CHARACTERISTIC_CONFIGURATION.getUUID());

        if (descriptor == null || descriptor.getHandle() == 0) {
            logger.debug("unable to find CCC for characteristic {}", characteristic.getUuid());
            return false;
        }

        if (procedureProgress != BlueGigaProcedure.NONE) {
            logger.debug("Procedure already in progress {}", procedureProgress);
            return false;
        }

        int[] value = { 1, 0 };
        byte[] bvalue = toBytes(value);
        descriptor.setValue(bvalue);

        cancelTimer(procedureTimer);
        if (!bgHandler.bgWriteCharacteristic(connection, descriptor.getHandle(), value)) {
            logger.debug("bgWriteCharacteristic returned false");
            return false;
        }

        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        procedureProgress = BlueGigaProcedure.NOTIFICATION_ENABLE;
        procedureCharacteristic = characteristic;

        try {
            // we intentionally sleep here in order to give this procedure a chance to complete.
            // ideally we would use locks/conditions to make this wait until completiong but
            // I have a better solution planned for later. - Connor Petty
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    @Override
    public boolean disableNotifications(BluetoothCharacteristic characteristic) {
        if (connection == -1) {
            logger.debug("Cannot enable notifications, device not connected {}", this);
            return false;
        }

        BlueGigaBluetoothCharacteristic ch = (BlueGigaBluetoothCharacteristic) characteristic;
        if (ch.isNotificationEnabled()) {
            return true;
        }

        BluetoothDescriptor descriptor = ch
                .getDescriptor(BluetoothDescriptor.GattDescriptor.CLIENT_CHARACTERISTIC_CONFIGURATION.getUUID());

        if (descriptor == null || descriptor.getHandle() == 0) {
            logger.debug("unable to find CCC for characteristic {}", characteristic.getUuid());
            return false;
        }

        if (procedureProgress != BlueGigaProcedure.NONE) {
            logger.debug("Procedure already in progress {}", procedureProgress);
            return false;
        }

        int[] value = { 0, 0 };
        byte[] bvalue = toBytes(value);
        descriptor.setValue(bvalue);

        cancelTimer(procedureTimer);
        if (!bgHandler.bgWriteCharacteristic(connection, descriptor.getHandle(), value)) {
            logger.debug("bgWriteCharacteristic returned false");
            return false;
        }

        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        procedureProgress = BlueGigaProcedure.NOTIFICATION_DISABLE;
        procedureCharacteristic = characteristic;

        try {
            // we intentionally sleep here in order to give this procedure a chance to complete.
            // ideally we would use locks/conditions to make this wait until completiong but
            // I have a better solution planned for later. - Connor Petty
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
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
        if (connection == -1) {
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
        if (connection == -1) {
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

        logger.trace("BlueGiga Group: {} event={}", this, event);
        updateLastSeenTime();

        BluetoothService service = new BluetoothService(event.getUuid(), true, event.getStart(), event.getEnd());
        addService(service);
    }

    private void handleFindInformationFoundEvent(BlueGigaFindInformationFoundEvent event) {
        // If this is not our connection handle then ignore.
        if (connection != event.getConnection()) {
            return;
        }

        logger.trace("BlueGiga FindInfo: {} event={}", this, event);
        updateLastSeenTime();

        int handle = event.getChrHandle();
        UUID attUUID = event.getUuid();

        BluetoothService service = getServiceByHandle(handle);
        if (service == null) {
            logger.debug("BlueGiga: Unable to find service for handle {}", handle);
            return;
        }
        handleToUUID.put(handle, attUUID);

        if (BluetoothBindingConstants.ATTR_CHARACTERISTIC_DECLARATION.equals(attUUID)) {
            BlueGigaBluetoothCharacteristic characteristic = new BlueGigaBluetoothCharacteristic(handle);
            characteristic.setService(service);
            handleToCharacteristic.put(handle, characteristic);
        } else {
            Integer chrHandle = handleToCharacteristic.floorKey(handle);
            if (chrHandle == null) {
                logger.debug("BlueGiga: Unable to find characteristic for handle {}", handle);
                return;
            }
            BlueGigaBluetoothCharacteristic characteristic = handleToCharacteristic.get(chrHandle);
            characteristic.addDescriptor(new BluetoothDescriptor(characteristic, attUUID, handle));
        }
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
                // We've downloaded all attributes, now read the characteristic declarations
                if (bgHandler.bgReadCharacteristicDeclarations(connection)) {
                    procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
                    procedureProgress = BlueGigaProcedure.READ_CHARACTERISTIC_DECL;
                } else {
                    procedureProgress = BlueGigaProcedure.NONE;
                }
                break;
            case READ_CHARACTERISTIC_DECL:
                // We've downloaded read all the declarations, we are done now
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
            case NOTIFICATION_ENABLE:
                boolean success = event.getResult() == BgApiResponse.SUCCESS;
                if (!success) {
                    logger.debug("write to descriptor failed");
                }
                ((BlueGigaBluetoothCharacteristic) procedureCharacteristic).setNotificationEnabled(success);
                procedureProgress = BlueGigaProcedure.NONE;
                procedureCharacteristic = null;
                break;
            case NOTIFICATION_DISABLE:
                success = event.getResult() == BgApiResponse.SUCCESS;
                if (!success) {
                    logger.debug("write to descriptor failed");
                }
                ((BlueGigaBluetoothCharacteristic) procedureCharacteristic).setNotificationEnabled(!success);
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

        for (BlueGigaBluetoothCharacteristic ch : handleToCharacteristic.values()) {
            ch.setNotificationEnabled(false);
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

        logger.trace("BlueGiga AttributeValue: {} event={}", this, event);

        int handle = event.getAttHandle();

        Map.Entry<Integer, BlueGigaBluetoothCharacteristic> entry = handleToCharacteristic.floorEntry(handle);
        if (entry == null) {
            logger.debug("BlueGiga didn't find characteristic for event {}", event);
            return;
        }

        BlueGigaBluetoothCharacteristic characteristic = entry.getValue();

        if (handle == entry.getKey()) {
            // this is the declaration
            if (parseDeclaration(characteristic, event.getValue())) {
                BluetoothService service = getServiceByHandle(handle);
                if (service == null) {
                    logger.debug("BlueGiga: Unable to find service for handle {}", handle);
                    return;
                }
                service.addCharacteristic(characteristic);
            }
            return;
        }
        if (handle == characteristic.getHandle()) {
            characteristic.setValue(event.getValue().clone());

            // If this is the characteristic we were reading, then send a read completion
            if (procedureProgress == BlueGigaProcedure.CHARACTERISTIC_READ && procedureCharacteristic != null
                    && procedureCharacteristic.getHandle() == event.getAttHandle()) {
                procedureProgress = BlueGigaProcedure.NONE;
                procedureCharacteristic = null;
                notifyListeners(BluetoothEventType.CHARACTERISTIC_READ_COMPLETE, characteristic,
                        BluetoothCompletionStatus.SUCCESS);
                return;
            }

            // Notify the user of the updated value
            notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, characteristic);
        } else {
            // it must be one of the descriptors we need to update
            UUID attUUID = handleToUUID.get(handle);
            BluetoothDescriptor descriptor = characteristic.getDescriptor(attUUID);
            descriptor.setValue(toBytes(event.getValue()));
            notifyListeners(BluetoothEventType.DESCRIPTOR_UPDATED, descriptor);
        }
    }

    private static byte @Nullable [] toBytes(int @Nullable [] value) {
        if (value == null) {
            return null;
        }
        byte[] ret = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            ret[i] = (byte) value[i];
        }
        return ret;
    }

    private boolean parseDeclaration(BlueGigaBluetoothCharacteristic ch, int[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(toBytes(value));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        ch.setProperties(Byte.toUnsignedInt(buffer.get()));
        ch.setHandle(Short.toUnsignedInt(buffer.getShort()));

        switch (buffer.remaining()) {
            case 2:
                long key = Short.toUnsignedLong(buffer.getShort());
                ch.setUUID(BluetoothBindingConstants.createBluetoothUUID(key));
                return true;
            case 4:
                key = Integer.toUnsignedLong(buffer.getInt());
                ch.setUUID(BluetoothBindingConstants.createBluetoothUUID(key));
                return true;
            case 16:
                long lower = buffer.getLong();
                long upper = buffer.getLong();
                ch.setUUID(new UUID(upper, lower));
                return true;
            default:
                logger.debug("Unexpected uuid length: {}", buffer.remaining());
                return false;
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
