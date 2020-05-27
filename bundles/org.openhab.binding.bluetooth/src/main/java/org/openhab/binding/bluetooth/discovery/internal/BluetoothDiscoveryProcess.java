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
package org.openhab.binding.bluetooth.discovery.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCharacteristic.GattCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompanyIdentifiers;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothDiscoveryProcess} does the work of creating a DiscoveryResult from a set of
 * {@link BluetoothDisocveryParticipant}s
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public class BluetoothDiscoveryProcess implements Supplier<DiscoveryResult>, BluetoothDeviceListener {

    private static final int DISCOVERY_TTL = 300;

    private final Logger logger = LoggerFactory.getLogger(BluetoothDiscoveryProcess.class);

    private final Lock serviceDiscoveryLock = new ReentrantLock();
    private final Condition connectionCondition = serviceDiscoveryLock.newCondition();
    private final Condition serviceDiscoveryCondition = serviceDiscoveryLock.newCondition();
    private final Condition infoDiscoveryCondition = serviceDiscoveryLock.newCondition();

    private final BluetoothDeviceSnapshot device;
    private final Collection<BluetoothDiscoveryParticipant> participants;
    private final Set<BluetoothAdapter> adapters;

    private volatile boolean servicesDiscovered = false;

    /**
     * Contains characteristic which reading is ongoing or null if no ongoing readings.
     */
    private volatile @Nullable GattCharacteristic ongoingGattCharacteristic;

    public BluetoothDiscoveryProcess(BluetoothDeviceSnapshot device,
            Collection<BluetoothDiscoveryParticipant> participants, Set<BluetoothAdapter> adapters) {
        this.participants = participants;
        this.device = device;
        this.adapters = adapters;
    }

    @Override
    public DiscoveryResult get() {
        // first see if any of the participants that don't require a connection recognize this device
        List<BluetoothDiscoveryParticipant> connectionParticipants = new ArrayList<>();
        for (BluetoothDiscoveryParticipant participant : participants) {
            if (participant.requiresConnection(device)) {
                connectionParticipants.add(participant);
                continue;
            }
            try {
                DiscoveryResult result = participant.createResult(device);
                if (result != null) {
                    return result;
                }
            } catch (RuntimeException e) {
                logger.warn("Participant '{}' threw an exception", participant.getClass().getName(), e);
            }
        }

        // Since we couldn't find a result, lets try the connection based participants
        DiscoveryResult result = null;
        if (!connectionParticipants.isEmpty()) {
            BluetoothAddress address = device.getAddress();
            if (isAddressAvailable(address)) {
                result = findConnectionResult(connectionParticipants);
                // make sure to disconnect before letting go of the device
                if (device.getConnectionState() == ConnectionState.CONNECTED) {
                    try {
                        if (!device.disconnect()) {
                            logger.debug("Failed to disconnect from device {}", address);
                        }
                    } catch (RuntimeException ex) {
                        logger.warn("Error occurred during bluetooth discovery for device {} on adapter {}", address,
                                device.getAdapter().getAddress(), ex);
                    }
                }
            }
        }
        if (result == null) {
            result = createDefaultResult(device);
        }
        return result;
    }

    private boolean isAddressAvailable(BluetoothAddress address) {
        // if a device with this address has a handler on any of the adapters, we abandon discovery
        return adapters.stream().noneMatch(adapter -> adapter.hasHandlerForDevice(address));
    }

    private DiscoveryResult createDefaultResult(BluetoothDevice device) {
        // We did not find a thing type for this device, so let's treat it as a generic one
        String label = device.getName();
        if (label == null || label.length() == 0 || label.equals(device.getAddress().toString().replace(':', '-'))) {
            label = "Bluetooth Device";
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
        Integer txPower = device.getTxPower();
        if (txPower != null && txPower > 0) {
            properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(txPower));
        }
        String manufacturer = BluetoothCompanyIdentifiers.get(device.getManufacturerId());
        if (manufacturer == null) {
            logger.debug("Unknown manufacturer Id ({}) found on bluetooth device.", device.getManufacturerId());
        } else {
            properties.put(Thing.PROPERTY_VENDOR, manufacturer);
            label += " (" + manufacturer + ")";
        }

        ThingUID thingUID = new ThingUID(BluetoothBindingConstants.THING_TYPE_BEACON, device.getAdapter().getUID(),
                device.getAddress().toString().toLowerCase().replace(":", ""));

        // Create the discovery result and add to the inbox
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS).withTTL(DISCOVERY_TTL)
                .withBridge(device.getAdapter().getUID()).withLabel(label).build();
    }

    private @Nullable DiscoveryResult findConnectionResult(List<BluetoothDiscoveryParticipant> connectionParticipants) {
        try {
            device.addListener(this);
            for (BluetoothDiscoveryParticipant participant : connectionParticipants) {
                // we call this every time just in case a participant somehow closes the connection
                if (device.getConnectionState() != ConnectionState.CONNECTED) {
                    if (device.getConnectionState() != ConnectionState.CONNECTING && !device.connect()) {
                        logger.debug("Connection attempt failed to start for device {}", device.getAddress());
                        // something failed, so we abandon connection discovery
                        return null;
                    }
                    if (!awaitConnection(1, TimeUnit.SECONDS)) {
                        logger.debug("Connection to device {} timed out", device.getAddress());
                        return null;
                    }
                    if (!servicesDiscovered) {
                        device.discoverServices();
                        if (!awaitServiceDiscovery(10, TimeUnit.SECONDS)) {
                            logger.debug("Service discovery for device {} timed out", device.getAddress());
                            // something failed, so we abandon connection discovery
                            return null;
                        }
                    }
                    readDeviceInformationIfMissing();
                    logger.debug("Device information fetched from the device: {}", device);
                }

                try {
                    DiscoveryResult result = participant.createResult(device);
                    if (result != null) {
                        return result;
                    }
                } catch (RuntimeException e) {
                    logger.warn("Participant '{}' threw an exception", participant.getClass().getName(), e);
                }
            }
        } catch (InterruptedException e) {
            // do nothing
        } finally {
            device.removeListener(this);
        }
        return null;
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        if (connectionNotification.getConnectionState() == ConnectionState.CONNECTED) {
            serviceDiscoveryLock.lock();
            try {
                connectionCondition.signal();
            } finally {
                serviceDiscoveryLock.unlock();
            }
        }
    }

    private void readDeviceInformationIfMissing() throws InterruptedException {
        if (device.getName() == null) {
            fecthGattCharacteristic(GattCharacteristic.DEVICE_NAME);
        }
        if (device.getModel() == null) {
            fecthGattCharacteristic(GattCharacteristic.MODEL_NUMBER_STRING);
        }
        if (device.getSerialNumber() == null) {
            fecthGattCharacteristic(GattCharacteristic.SERIAL_NUMBER_STRING);
        }
        if (device.getHardwareRevision() == null) {
            fecthGattCharacteristic(GattCharacteristic.HARDWARE_REVISION_STRING);
        }
        if (device.getFirmwareRevision() == null) {
            fecthGattCharacteristic(GattCharacteristic.FIRMWARE_REVISION_STRING);
        }
        if (device.getSoftwareRevision() == null) {
            fecthGattCharacteristic(GattCharacteristic.SOFTWARE_REVISION_STRING);
        }
    }

    private void fecthGattCharacteristic(GattCharacteristic gattCharacteristic) throws InterruptedException {
        UUID uuid = gattCharacteristic.getUUID();
        BluetoothCharacteristic characteristic = device.getCharacteristic(uuid);
        if (characteristic == null) {
            logger.debug("Device '{}' doesn't support uuid '{}'", device.getAddress(), uuid);
            return;
        }
        if (!device.readCharacteristic(characteristic)) {
            logger.debug("Failed to aquire uuid {} from device {}", uuid, device.getAddress());
            return;
        }
        ongoingGattCharacteristic = gattCharacteristic;
        if (!awaitInfoResponse(1, TimeUnit.SECONDS)) {
            logger.debug("Device info (uuid {}) for device {} timed out", uuid, device.getAddress());
            ongoingGattCharacteristic = null;
        }
    }

    private boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
        serviceDiscoveryLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (device.getConnectionState() != ConnectionState.CONNECTED) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = connectionCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            serviceDiscoveryLock.unlock();
        }
        return true;
    }

    private boolean awaitInfoResponse(long timeout, TimeUnit unit) throws InterruptedException {
        serviceDiscoveryLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (ongoingGattCharacteristic != null) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = infoDiscoveryCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            serviceDiscoveryLock.unlock();
        }
        return true;
    }

    private boolean awaitServiceDiscovery(long timeout, TimeUnit unit) throws InterruptedException {
        serviceDiscoveryLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (!servicesDiscovered) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = serviceDiscoveryCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            serviceDiscoveryLock.unlock();
        }
        return true;
    }

    @Override
    public void onServicesDiscovered() {
        serviceDiscoveryLock.lock();
        try {
            servicesDiscovered = true;
            serviceDiscoveryCondition.signal();
        } finally {
            serviceDiscoveryLock.unlock();
        }
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        serviceDiscoveryLock.lock();
        try {
            if (status == BluetoothCompletionStatus.SUCCESS) {
                switch (characteristic.getGattCharacteristic()) {
                    case DEVICE_NAME:
                        device.setName(characteristic.getStringValue(0));
                        break;
                    case MODEL_NUMBER_STRING:
                        device.setModel(characteristic.getStringValue(0));
                        break;
                    case SERIAL_NUMBER_STRING:
                        device.setSerialNumberl(characteristic.getStringValue(0));
                        break;
                    case HARDWARE_REVISION_STRING:
                        device.setHardwareRevision(characteristic.getStringValue(0));
                        break;
                    case FIRMWARE_REVISION_STRING:
                        device.setFirmwareRevision(characteristic.getStringValue(0));
                        break;
                    case SOFTWARE_REVISION_STRING:
                        device.setSoftwareRevision(characteristic.getStringValue(0));
                        break;
                    default:
                        break;
                }
            }

            if (ongoingGattCharacteristic == characteristic.getGattCharacteristic()) {
                ongoingGattCharacteristic = null;
                infoDiscoveryCondition.signal();
            }
        } finally {
            serviceDiscoveryLock.unlock();
        }
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
    }

    @Override
    public void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor) {
    }
}
