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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
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
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.AttributeValueType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BgApiResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.ConnectionStatusFlag;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification.BluetoothBeaconType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extended {@link BluetoothDevice} class to handle BlueGiga specific information
 *
 * @author Chris Jackson - Initial contribution
 */
public class BlueGigaBluetoothDevice extends BluetoothDevice implements BlueGigaEventListener {
    private final Logger logger = LoggerFactory.getLogger(BlueGigaBluetoothDevice.class);

    // BlueGiga needs to know the address type when connecting
    private BluetoothAddressType addressType;

    // The dongle handler
    private final BlueGigaBridgeHandler bgHandler;

    private volatile Procedure currentProcedure = null;

    private Map<UUID, Consumer<byte[]>> updateCallbacks = new ConcurrentHashMap<>();

    private BlueGigaServiceContext serviceContext = new BlueGigaServiceContext();

    // The connection handle if the device is connected
    private int connection = -1;

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
    }

    @Override
    public void doConnect() throws BluetoothException {
        if (connection != -1) {
            // We're already connected
            return;
        }

        if (!bgHandler.bgConnect(address, addressType)) {
            throw new BluetoothException("Failed to connect");
        }
    }

    @Override
    public void doDisconnect() throws BluetoothException {
        if (connection == -1) {
            // We're already disconnected
            return;
        }

        if (!bgHandler.bgDisconnect(connection)) {
            throw new BluetoothException("Failed to disconnect");
        }
    }

    @Override
    protected void doServiceDiscovery() throws BluetoothException {
        currentProcedure = new Procedure(Procedure.Type.GET_SERVICES);
        if (!bgHandler.bgFindPrimaryServices(connection)) {
            throw new BluetoothException("Failed to find primary services");
        }
    }

    @Override
    public CompletableFuture<byte[]> readCharacteristic(BluetoothCharacteristic characteristic) {
        if (characteristic == null || characteristic.getHandle() == 0) {
            return completedExceptionaly(new BluetoothException("Characteristic is either null or had no handle"));
        }

        if (currentProcedure != null) {
            return completedExceptionaly(new BluetoothException(
                    "Failed to read characteristic '" + characteristic.getUuid() + "': procedure already in progress"));
        }

        if (!bgHandler.bgReadCharacteristic(connection, characteristic.getHandle())) {
            return completedExceptionaly(
                    new BluetoothException("Failed to read characteristic '" + characteristic.getUuid() + "'"));
        }

        CompletableFuture<byte[]> future = new CompletableFuture<>();
        currentProcedure = new Procedure(Procedure.Type.CHARACTERISTIC_READ, characteristic, future);
        return future;
    }

    @Override
    public CompletableFuture<Void> writeCharacteristic(BluetoothCharacteristic characteristic, byte[] value) {
        if (characteristic == null || characteristic.getHandle() == 0) {
            return completedExceptionaly(new BluetoothException("Characteristic is either null or had no handle"));
        }

        if (currentProcedure != null) {
            return completedExceptionaly(new BluetoothException(
                    "Failed to read characteristic '" + characteristic.getUuid() + "': procedure already in progress"));
        }

        if (!bgHandler.bgWriteCharacteristic(connection, characteristic.getHandle(),
                BluetoothUtils.toIntArray(value))) {
            return completedExceptionaly(
                    new BluetoothException("Failed to write characteristic '" + characteristic.getUuid() + "'"));
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        currentProcedure = new Procedure(Procedure.Type.CHARACTERISTIC_READ, characteristic, future);
        return future;
    }

    @Override
    public @NonNull CompletableFuture<@Nullable Void> enableNotifications(
            @NonNull BluetoothCharacteristic characteristic, @NonNull Consumer<byte @NonNull []> handler) {
        if (characteristic == null || characteristic.getHandle() == 0) {
            return completedExceptionaly(new BluetoothException("Characteristic is either null or had no handle"));
        }
        updateCallbacks.put(characteristic.getUuid(), handler);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NonNull CompletableFuture<@Nullable Void> disableNotifications(
            @NonNull BluetoothCharacteristic characteristic) {
        if (characteristic == null || characteristic.getHandle() == 0) {
            return completedExceptionaly(new BluetoothException("Characteristic is either null or had no handle"));
        }
        updateCallbacks.remove(characteristic.getUuid());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NonNull CompletableFuture<@Nullable Void> enableNotifications(@NonNull BluetoothDescriptor descriptor,
            @NonNull Consumer<byte @NonNull []> handler) {
        if (descriptor == null || descriptor.getUuid() == null) {
            return completedExceptionaly(new BluetoothException("BluetoothDescriptor is null or has no uuid"));
        }
        updateCallbacks.put(descriptor.getUuid(), handler);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NonNull CompletableFuture<@Nullable Void> disableNotifications(@NonNull BluetoothDescriptor descriptor) {
        if (descriptor == null || descriptor.getUuid() == null) {
            return completedExceptionaly(new BluetoothException("BluetoothDescriptor is null or has no uuid"));
        }
        updateCallbacks.remove(descriptor.getUuid());
        return CompletableFuture.completedFuture(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void bluegigaEventReceived(BlueGigaResponse event) {
        if (event instanceof BlueGigaScanResponseEvent) {
            BlueGigaScanResponseEvent scanEvent = (BlueGigaScanResponseEvent) event;

            // Check if this is addressed to this device
            if (!address.equals(new BluetoothAddress(scanEvent.getSender()))) {
                return;
            }

            // Set device properties
            rssi = scanEvent.getRssi();
            addressType = scanEvent.getAddressType();

            byte[] manufacturerData = null;

            // If the packet contains data, then process it and add anything relevant to the device...
            if (scanEvent.getData() != null) {
                EirPacket eir = new EirPacket(scanEvent.getData());
                for (EirDataType record : eir.getRecords().keySet()) {
                    switch (record) {
                        case EIR_FLAGS:
                            break;
                        case EIR_MANUFACTURER_SPECIFIC:
                            Object obj = eir.getRecord(EirDataType.EIR_MANUFACTURER_SPECIFIC);
                            if (obj != null) {
                                try {
                                    @SuppressWarnings("unchecked")
                                    Map<Short, int[]> eirRecord = (Map<Short, int[]>) obj;
                                    Map.Entry<Short, int[]> eirEntry = eirRecord.entrySet().iterator().next();

                                    manufacturer = (int) eirEntry.getKey();

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
                            txPower = (int) eir.getRecord(EirDataType.EIR_TXPOWER);
                            break;
                        default:
                            break;
                    }
                }
            }

            if (getConnectionState() == ConnectionState.UNKNOWN) {
                // TODO: It could make sense to wait with discovery for non-connectable devices until scan response is
                // received to eventually retrieve more about the device before it gets discovered. Anyhow, devices
                // that don't send a scan response at all also have to be supported. See also PR #6995.

                // Set our state to disconnected
                connection = -1;
                setConnectionState(ConnectionState.DISCONNECTED);

                // Notify the bridge - for inbox notifications
                bgHandler.deviceDiscovered(this);
            }

            // Notify listeners of all scan records - for RSSI, beacon processing (etc)
            BluetoothScanNotification scanNotification = new BluetoothScanNotification();
            scanNotification.setRssi(scanEvent.getRssi());

            switch (scanEvent.getPacketType()) {
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
            notifyScanRecordReceived(scanNotification);
            return;
        }

        if (event instanceof BlueGigaGroupFoundEvent) {
            // A Service has been discovered
            BlueGigaGroupFoundEvent serviceEvent = (BlueGigaGroupFoundEvent) event;

            // If this is not our connection handle then ignore.
            if (connection != serviceEvent.getConnection()) {
                return;
            }

            logger.trace("BlueGiga Group: {} svcs={}", this, serviceContext.getServices());

            BluetoothService service = new BluetoothService(serviceEvent.getUuid(), true, serviceEvent.getStart(),
                    serviceEvent.getEnd());
            serviceContext.addService(service);

            return;
        }

        if (event instanceof BlueGigaFindInformationFoundEvent) {
            // A Characteristic has been discovered
            BlueGigaFindInformationFoundEvent infoEvent = (BlueGigaFindInformationFoundEvent) event;

            // If this is not our connection handle then ignore.
            if (connection != infoEvent.getConnection()) {
                return;
            }

            logger.trace("BlueGiga FindInfo: {} svcs={}", this, serviceContext.getServices());

            BluetoothCharacteristic characteristic = new BluetoothCharacteristic(infoEvent.getUuid(),
                    infoEvent.getChrHandle());

            BluetoothService service = serviceContext.getServiceByHandle(characteristic.getHandle());
            if (service == null) {
                logger.debug("BlueGiga: Unable to find service for handle {}", characteristic.getHandle());
                return;
            }
            characteristic.setService(service);
            service.addCharacteristic(characteristic);

            return;
        }

        if (event instanceof BlueGigaProcedureCompletedEvent) {
            BlueGigaProcedureCompletedEvent completedEvent = (BlueGigaProcedureCompletedEvent) event;

            // If this is not our connection handle then ignore.
            if (connection != completedEvent.getConnection()) {
                return;
            }

            if (currentProcedure == null) {
                logger.debug("BlueGiga procedure completed but procedure is null with connection {}, address {}",
                        connection, address);
                return;
            }

            // The current procedure is now complete - move on...
            switch (currentProcedure.type) {
                case GET_SERVICES:
                    // We've downloaded all services, now get the characteristics
                    currentProcedure = new Procedure(Procedure.Type.GET_CHARACTERISTICS);
                    bgHandler.bgFindCharacteristics(connection);
                    break;
                case GET_CHARACTERISTICS:
                    // We've downloaded all characteristics
                    currentProcedure = null;
                    notifyServicesDiscovered(serviceContext);
                    break;
                case CHARACTERISTIC_READ:
                    // The read failed
                    currentProcedure.resultFuture.completeExceptionally(new BluetoothException(
                            "Failed to read from characteristic '" + currentProcedure.characteristic.getUuid() + "'"));
                    currentProcedure = null;
                    break;
                case CHARACTERISTIC_WRITE:
                    // The write completed - failure or success
                    if (completedEvent.getResult() == BgApiResponse.SUCCESS) {
                        currentProcedure.resultFuture.complete(null);
                    } else {
                        currentProcedure.resultFuture
                                .completeExceptionally(new BluetoothException("Failed to write to characteristic '"
                                        + currentProcedure.characteristic.getUuid() + "'"));
                    }
                    currentProcedure = null;
                    break;
                default:
                    break;
            }

            return;
        }

        if (event instanceof BlueGigaConnectionStatusEvent) {
            BlueGigaConnectionStatusEvent connectionEvent = (BlueGigaConnectionStatusEvent) event;

            // Check if this is addressed to this device
            if (!address.equals(new BluetoothAddress(connectionEvent.getAddress()))) {
                return;
            }

            // If we're connected, then remember the connection handle
            if (connectionEvent.getFlags().contains(ConnectionStatusFlag.CONNECTION_CONNECTED)) {
                connection = connectionEvent.getConnection();
                setConnectionState(ConnectionState.CONNECTED);
            }

            return;
        }

        if (event instanceof BlueGigaDisconnectedEvent) {
            BlueGigaDisconnectedEvent disconnectedEvent = (BlueGigaDisconnectedEvent) event;

            // If this is not our connection handle then ignore.
            if (connection != disconnectedEvent.getConnection()) {
                return;
            }

            connection = -1;
            setConnectionState(ConnectionState.DISCONNECTED);

            return;
        }

        if (event instanceof BlueGigaAttributeValueEvent) {
            // A read request has completed - update the characteristic
            BlueGigaAttributeValueEvent valueEvent = (BlueGigaAttributeValueEvent) event;

            BluetoothCharacteristic characteristic = serviceContext
                    .getCharacteristicByHandle(valueEvent.getAttHandle());
            if (characteristic == null) {
                logger.debug("BlueGiga didn't find characteristic for event {}", event);
            } else {
                // characteristic.setValue(valueEvent.getValue().clone());

                // If this is the characteristic we were reading, then send a read completion
                if (valueEvent.getType() == AttributeValueType.ATTCLIENT_ATTRIBUTE_VALUE_TYPE_NOTIFY) {
                    Consumer<byte[]> callback = updateCallbacks.get(characteristic.getUuid());
                    // Notify the user of the updated value
                    if (callback != null) {
                        callback.accept(BluetoothUtils.toByteArray(valueEvent.getValue()));
                    }
                } else if (currentProcedure != null && currentProcedure.type == Procedure.Type.CHARACTERISTIC_READ
                        && currentProcedure.characteristic.getHandle() == valueEvent.getAttHandle()) {
                    currentProcedure.resultFuture.complete(BluetoothUtils.toByteArray(valueEvent.getValue()));
                    currentProcedure = null;
                }
            }
        }
    }

    private static class Procedure {
        private Type type;
        private BluetoothCharacteristic characteristic;
        @SuppressWarnings("rawtypes")
        private CompletableFuture resultFuture;

        public Procedure(Type type) {
            this.type = type;
        }

        @SuppressWarnings("unused")
        public Procedure(Type type, BluetoothCharacteristic characteristic, CompletableFuture resultFuture) {
            this.type = type;
            this.characteristic = characteristic;
            this.resultFuture = resultFuture;
        }

        // An enum to use in the state machine for interacting with the device
        private enum Type {
            GET_SERVICES,
            GET_CHARACTERISTICS,
            CHARACTERISTIC_READ,
            CHARACTERISTIC_WRITE
        }

    }

    private class BlueGigaServiceContext extends ServiceContext {

        @Override
        protected boolean addService(@NonNull BluetoothService service) {
            return super.addService(service);
        }

        @Override
        protected @Nullable BluetoothCharacteristic getCharacteristicByHandle(int handle) {
            return super.getCharacteristicByHandle(handle);
        }

        @Override
        protected @Nullable BluetoothService getServiceByHandle(int handle) {
            return super.getServiceByHandle(handle);
        }

    }

}
