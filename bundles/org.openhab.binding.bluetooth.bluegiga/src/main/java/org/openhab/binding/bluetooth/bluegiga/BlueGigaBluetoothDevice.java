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
package org.openhab.binding.bluetooth.bluegiga;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BaseBluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothException;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.BluetoothUtils;
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
 * An extended {@link BaseBluetoothDevice} class to handle BlueGiga specific information
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public class BlueGigaBluetoothDevice extends BaseBluetoothDevice implements BlueGigaEventListener {
    private static final long TIMEOUT_SEC = 60;

    private final Logger logger = LoggerFactory.getLogger(BlueGigaBluetoothDevice.class);

    private static final BlueGigaProcedure PROCEDURE_NONE = new BlueGigaProcedure(BlueGigaProcedure.Type.NONE);
    private static final BlueGigaProcedure PROCEDURE_GET_SERVICES = new BlueGigaProcedure(
            BlueGigaProcedure.Type.GET_SERVICES);
    private static final BlueGigaProcedure PROCEDURE_GET_CHARACTERISTICS = new BlueGigaProcedure(
            BlueGigaProcedure.Type.GET_CHARACTERISTICS);
    private static final BlueGigaProcedure PROCEDURE_READ_CHARACTERISTIC_DECL = new BlueGigaProcedure(
            BlueGigaProcedure.Type.READ_CHARACTERISTIC_DECL);

    private Map<Integer, UUID> handleToUUID = new HashMap<>();
    private NavigableMap<Integer, BlueGigaBluetoothCharacteristic> handleToCharacteristic = new TreeMap<>();

    // BlueGiga needs to know the address type when connecting
    private BluetoothAddressType addressType = BluetoothAddressType.UNKNOWN;

    // The dongle handler
    private final BlueGigaBridgeHandler bgHandler;

    private BlueGigaProcedure currentProcedure = PROCEDURE_NONE;

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
            BlueGigaProcedure procedure = currentProcedure;
            logger.debug("Procedure {} timeout for device {}", procedure.type, address);
            switch (procedure.type) {
                case CHARACTERISTIC_READ:
                    ReadCharacteristicProcedure readProcedure = (ReadCharacteristicProcedure) procedure;
                    readProcedure.readFuture.completeExceptionally(new TimeoutException("Read characteristic "
                            + readProcedure.characteristic.getUuid() + " timeout for device " + address));
                    break;
                case CHARACTERISTIC_WRITE:
                    WriteCharacteristicProcedure writeProcedure = (WriteCharacteristicProcedure) procedure;
                    writeProcedure.writeFuture.completeExceptionally(new TimeoutException("Write characteristic "
                            + writeProcedure.characteristic.getUuid() + " timeout for device " + address));
                    break;
                default:
                    break;
            }

            currentProcedure = PROCEDURE_NONE;
        }
    };

    /**
     * Creates a new {@link BlueGigaBluetoothDevice} which extends {@link BaseBluetoothDevice} for the BlueGiga
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
        if (!PROCEDURE_NONE.equals(currentProcedure)) {
            return false;
        }

        cancelTimer(procedureTimer);
        if (!bgHandler.bgFindPrimaryServices(connection)) {
            return false;
        }

        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        currentProcedure = PROCEDURE_GET_SERVICES;
        return true;
    }

    @Override
    public CompletableFuture<@Nullable Void> enableNotifications(BluetoothCharacteristic characteristic) {
        if (connection == -1) {
            return CompletableFuture.failedFuture(new BluetoothException("Not connected"));
        }

        BlueGigaBluetoothCharacteristic ch = (BlueGigaBluetoothCharacteristic) characteristic;
        if (ch.isNotifying()) {
            return CompletableFuture.completedFuture(null);
        }

        BluetoothDescriptor descriptor = ch
                .getDescriptor(BluetoothDescriptor.GattDescriptor.CLIENT_CHARACTERISTIC_CONFIGURATION.getUUID());

        if (descriptor == null || descriptor.getHandle() == 0) {
            return CompletableFuture.failedFuture(
                    new BluetoothException("Unable to find CCC for characteristic [" + characteristic.getUuid() + "]"));
        }

        if (!PROCEDURE_NONE.equals(currentProcedure)) {
            return CompletableFuture.failedFuture(new BluetoothException("Another procedure is already in progress"));
        }

        int[] value = { 1, 0 };

        cancelTimer(procedureTimer);
        if (!bgHandler.bgWriteCharacteristic(connection, descriptor.getHandle(), value)) {
            return CompletableFuture.failedFuture(new BluetoothException(
                    "Failed to write to CCC for characteristic [" + characteristic.getUuid() + "]"));
        }

        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        WriteCharacteristicProcedure notifyProcedure = new WriteCharacteristicProcedure(ch,
                BlueGigaProcedure.Type.NOTIFICATION_ENABLE);
        currentProcedure = notifyProcedure;
        try {
            // we intentionally sleep here in order to give this procedure a chance to complete.
            // ideally we would use locks/conditions to make this wait until completiong but
            // I have a better solution planned for later. - Connor Petty
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return notifyProcedure.writeFuture;
    }

    @Override
    public CompletableFuture<@Nullable Void> disableNotifications(BluetoothCharacteristic characteristic) {
        if (connection == -1) {
            return CompletableFuture.failedFuture(new BluetoothException("Not connected"));
        }

        BlueGigaBluetoothCharacteristic ch = (BlueGigaBluetoothCharacteristic) characteristic;
        if (!ch.isNotifying()) {
            return CompletableFuture.completedFuture(null);
        }

        BluetoothDescriptor descriptor = ch
                .getDescriptor(BluetoothDescriptor.GattDescriptor.CLIENT_CHARACTERISTIC_CONFIGURATION.getUUID());

        if (descriptor == null || descriptor.getHandle() == 0) {
            return CompletableFuture.failedFuture(
                    new BluetoothException("Unable to find CCC for characteristic [" + characteristic.getUuid() + "]"));
        }

        if (!PROCEDURE_NONE.equals(currentProcedure)) {
            return CompletableFuture.failedFuture(new BluetoothException("Another procedure is already in progress"));
        }

        int[] value = { 0, 0 };

        cancelTimer(procedureTimer);
        if (!bgHandler.bgWriteCharacteristic(connection, descriptor.getHandle(), value)) {
            return CompletableFuture.failedFuture(new BluetoothException(
                    "Failed to write to CCC for characteristic [" + characteristic.getUuid() + "]"));
        }

        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        WriteCharacteristicProcedure notifyProcedure = new WriteCharacteristicProcedure(ch,
                BlueGigaProcedure.Type.NOTIFICATION_DISABLE);
        currentProcedure = notifyProcedure;

        return notifyProcedure.writeFuture;
    }

    @Override
    public boolean isNotifying(BluetoothCharacteristic characteristic) {
        BlueGigaBluetoothCharacteristic ch = (BlueGigaBluetoothCharacteristic) characteristic;
        return ch.isNotifying();
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
    public CompletableFuture<byte[]> readCharacteristic(BluetoothCharacteristic characteristic) {
        if (characteristic.getHandle() == 0) {
            return CompletableFuture.failedFuture(new BluetoothException("Cannot read characteristic with no handle"));
        }
        if (connection == -1) {
            return CompletableFuture.failedFuture(new BluetoothException("Not connected"));
        }

        if (!PROCEDURE_NONE.equals(currentProcedure)) {
            return CompletableFuture.failedFuture(new BluetoothException("Another procedure is already in progress"));
        }

        cancelTimer(procedureTimer);
        if (!bgHandler.bgReadCharacteristic(connection, characteristic.getHandle())) {
            return CompletableFuture.failedFuture(
                    new BluetoothException("Failed to read characteristic [" + characteristic.getUuid() + "]"));
        }
        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        ReadCharacteristicProcedure readProcedure = new ReadCharacteristicProcedure(characteristic);
        currentProcedure = readProcedure;

        return readProcedure.readFuture;
    }

    @Override
    public CompletableFuture<@Nullable Void> writeCharacteristic(BluetoothCharacteristic characteristic, byte[] value) {
        if (characteristic.getHandle() == 0) {
            return CompletableFuture.failedFuture(new BluetoothException("Cannot write characteristic with no handle"));
        }
        if (connection == -1) {
            return CompletableFuture.failedFuture(new BluetoothException("Not connected"));
        }

        if (!PROCEDURE_NONE.equals(currentProcedure)) {
            return CompletableFuture.failedFuture(new BluetoothException("Another procedure is already in progress"));
        }

        cancelTimer(procedureTimer);
        if (!bgHandler.bgWriteCharacteristic(connection, characteristic.getHandle(),
                BluetoothUtils.toIntArray(value))) {
            return CompletableFuture.failedFuture(
                    new BluetoothException("Failed to write characteristic [" + characteristic.getUuid() + "]"));
        }

        procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
        WriteCharacteristicProcedure writeProcedure = new WriteCharacteristicProcedure(
                (BlueGigaBluetoothCharacteristic) characteristic, BlueGigaProcedure.Type.CHARACTERISTIC_WRITE);
        currentProcedure = writeProcedure;

        return writeProcedure.writeFuture;
    }

    @Override
    public void bluegigaEventReceived(BlueGigaResponse event) {
        if (event instanceof BlueGigaScanResponseEvent responseEvent) {
            handleScanEvent(responseEvent);
        }

        else if (event instanceof BlueGigaGroupFoundEvent foundEvent) {
            handleGroupFoundEvent(foundEvent);
        }

        else if (event instanceof BlueGigaFindInformationFoundEvent foundEvent) {
            // A Characteristic has been discovered
            handleFindInformationFoundEvent(foundEvent);
        }

        else if (event instanceof BlueGigaProcedureCompletedEvent completedEvent) {
            handleProcedureCompletedEvent(completedEvent);
        }

        else if (event instanceof BlueGigaConnectionStatusEvent statusEvent) {
            handleConnectionStatusEvent(statusEvent);
        }

        else if (event instanceof BlueGigaDisconnectedEvent disconnectedEvent) {
            handleDisconnectedEvent(disconnectedEvent);
        }

        else if (event instanceof BlueGigaAttributeValueEvent valueEvent) {
            handleAttributeValueEvent(valueEvent);
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
            @Nullable
            Integer chrHandle = handleToCharacteristic.floorKey(handle);
            if (chrHandle == null) {
                logger.debug("BlueGiga: Unable to find characteristic for handle {}", handle);
                return;
            }
            @Nullable
            BlueGigaBluetoothCharacteristic characteristic = handleToCharacteristic.get(chrHandle);
            if (characteristic != null) {
                characteristic.addDescriptor(new BluetoothDescriptor(characteristic, attUUID, handle));
            }
        }
    }

    private void handleProcedureCompletedEvent(BlueGigaProcedureCompletedEvent event) {
        // If this is not our connection handle then ignore.
        if (connection != event.getConnection()) {
            return;
        }

        if (PROCEDURE_NONE.equals(currentProcedure)) {
            logger.debug("BlueGiga procedure completed but procedure is null with connection {}, address {}",
                    connection, address);
            return;
        }

        cancelTimer(procedureTimer);
        updateLastSeenTime();

        // The current procedure is now complete - move on...
        switch (currentProcedure.type) {
            case GET_SERVICES:
                // We've downloaded all services, now get the characteristics
                if (bgHandler.bgFindCharacteristics(connection)) {
                    procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
                    currentProcedure = PROCEDURE_GET_CHARACTERISTICS;
                } else {
                    currentProcedure = PROCEDURE_NONE;
                }
                break;
            case GET_CHARACTERISTICS:
                // We've downloaded all attributes, now read the characteristic declarations
                if (bgHandler.bgReadCharacteristicDeclarations(connection)) {
                    procedureTimer = startTimer(procedureTimeoutTask, TIMEOUT_SEC);
                    currentProcedure = PROCEDURE_READ_CHARACTERISTIC_DECL;
                } else {
                    currentProcedure = PROCEDURE_NONE;
                }
                break;
            case READ_CHARACTERISTIC_DECL:
                // We've downloaded read all the declarations, we are done now
                currentProcedure = PROCEDURE_NONE;
                notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
                break;
            case CHARACTERISTIC_READ:
                // The read failed
                ReadCharacteristicProcedure readProcedure = (ReadCharacteristicProcedure) currentProcedure;
                readProcedure.readFuture.completeExceptionally(new BluetoothException(
                        "Read characteristic failed: " + readProcedure.characteristic.getUuid()));
                currentProcedure = PROCEDURE_NONE;
                break;
            case CHARACTERISTIC_WRITE:
                // The write completed - failure or success
                WriteCharacteristicProcedure writeProcedure = (WriteCharacteristicProcedure) currentProcedure;
                if (event.getResult() == BgApiResponse.SUCCESS) {
                    writeProcedure.writeFuture.complete(null);
                } else {
                    writeProcedure.writeFuture.completeExceptionally(new BluetoothException(
                            "Write characteristic failed: " + writeProcedure.characteristic.getUuid()));
                }
                currentProcedure = PROCEDURE_NONE;
                break;
            case NOTIFICATION_ENABLE:
                WriteCharacteristicProcedure notifyEnableProcedure = (WriteCharacteristicProcedure) currentProcedure;
                boolean success = event.getResult() == BgApiResponse.SUCCESS;
                if (success) {
                    notifyEnableProcedure.writeFuture.complete(null);
                } else {
                    notifyEnableProcedure.writeFuture
                            .completeExceptionally(new BluetoothException("Enable characteristic notification failed: "
                                    + notifyEnableProcedure.characteristic.getUuid()));
                }
                notifyEnableProcedure.characteristic.setNotifying(success);
                currentProcedure = PROCEDURE_NONE;
                break;
            case NOTIFICATION_DISABLE:
                WriteCharacteristicProcedure notifyDisableProcedure = (WriteCharacteristicProcedure) currentProcedure;
                success = event.getResult() == BgApiResponse.SUCCESS;
                if (success) {
                    notifyDisableProcedure.writeFuture.complete(null);
                } else {
                    notifyDisableProcedure.writeFuture
                            .completeExceptionally(new BluetoothException("Disable characteristic notification failed: "
                                    + notifyDisableProcedure.characteristic.getUuid()));
                }
                notifyDisableProcedure.characteristic.setNotifying(!success);
                currentProcedure = PROCEDURE_NONE;
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
            ch.setNotifying(false);
        }

        cancelTimer(procedureTimer);
        connectionState = ConnectionState.DISCONNECTED;
        connection = -1;

        BlueGigaProcedure procedure = currentProcedure;
        switch (procedure.type) {
            case CHARACTERISTIC_READ:
                ReadCharacteristicProcedure readProcedure = (ReadCharacteristicProcedure) procedure;
                readProcedure.readFuture.completeExceptionally(new BluetoothException("Read characteristic "
                        + readProcedure.characteristic.getUuid() + " failed due to disconnect of device " + address));
                break;
            case CHARACTERISTIC_WRITE:
                WriteCharacteristicProcedure writeProcedure = (WriteCharacteristicProcedure) procedure;
                writeProcedure.writeFuture.completeExceptionally(new BluetoothException("Write characteristic "
                        + writeProcedure.characteristic.getUuid() + " failed due to disconnect of device " + address));
                break;
            default:
                break;
        }
        currentProcedure = PROCEDURE_NONE;

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
            byte[] value = BluetoothUtils.toByteArray(event.getValue());
            BlueGigaProcedure procedure = currentProcedure;
            // If this is the characteristic we were reading, then send a read completion
            if (procedure.type == BlueGigaProcedure.Type.CHARACTERISTIC_READ) {
                ReadCharacteristicProcedure readProcedure = (ReadCharacteristicProcedure) currentProcedure;
                if (readProcedure.characteristic.getHandle() == event.getAttHandle()) {
                    readProcedure.readFuture.complete(value);
                    currentProcedure = PROCEDURE_NONE;
                    return;
                }
            }
            // Notify the user of the updated value
            notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, characteristic, value);
        } else {
            // it must be one of the descriptors we need to update
            UUID attUUID = handleToUUID.get(handle);
            if (attUUID != null) {
                BluetoothDescriptor descriptor = characteristic.getDescriptor(attUUID);
                notifyListeners(BluetoothEventType.DESCRIPTOR_UPDATED, descriptor,
                        BluetoothUtils.toByteArray(event.getValue()));
            }
        }
    }

    private boolean parseDeclaration(BlueGigaBluetoothCharacteristic ch, int[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(BluetoothUtils.toByteArray(value));
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
        currentProcedure = PROCEDURE_NONE;
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

    private static class BlueGigaProcedure {
        private final Type type;

        public BlueGigaProcedure(Type type) {
            this.type = type;
        }

        // An enum to use in the state machine for interacting with the device
        enum Type {
            NONE,
            GET_SERVICES,
            GET_CHARACTERISTICS,
            READ_CHARACTERISTIC_DECL,
            CHARACTERISTIC_READ,
            CHARACTERISTIC_WRITE,
            NOTIFICATION_ENABLE,
            NOTIFICATION_DISABLE
        }
    }

    private static class ReadCharacteristicProcedure extends BlueGigaProcedure {

        private final BluetoothCharacteristic characteristic;

        private final CompletableFuture<byte[]> readFuture = new CompletableFuture<>();

        public ReadCharacteristicProcedure(BluetoothCharacteristic characteristic) {
            super(Type.CHARACTERISTIC_READ);
            this.characteristic = characteristic;
        }
    }

    private static class WriteCharacteristicProcedure extends BlueGigaProcedure {

        private final BlueGigaBluetoothCharacteristic characteristic;

        private final CompletableFuture<@Nullable Void> writeFuture = new CompletableFuture<>();

        public WriteCharacteristicProcedure(BlueGigaBluetoothCharacteristic characteristic, Type type) {
            super(type);
            this.characteristic = characteristic;
        }
    }
}
