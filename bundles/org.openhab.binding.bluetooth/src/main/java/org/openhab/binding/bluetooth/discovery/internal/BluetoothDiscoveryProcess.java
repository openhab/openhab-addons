/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCharacteristic.GattCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompanyIdentifiers;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothUtils;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothDiscoveryProcess} does the work of creating a DiscoveryResult from a set of
 * {@link BluetoothDisocveryParticipant}s
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public class BluetoothDiscoveryProcess implements Supplier<DiscoveryResult> {

    private static final int DISCOVERY_TTL = 300;

    private final Logger logger = LoggerFactory.getLogger(BluetoothDiscoveryProcess.class);

    private final BluetoothDeviceSnapshot device;
    private final Collection<BluetoothDiscoveryParticipant> participants;
    private final Set<BluetoothAdapter> adapters;

    public BluetoothDiscoveryProcess(BluetoothDeviceSnapshot device,
            Collection<BluetoothDiscoveryParticipant> participants, Set<BluetoothAdapter> adapters) {
        this.participants = participants;
        this.device = device;
        this.adapters = adapters;
    }

    @Override
    public DiscoveryResult get() {
        List<BluetoothDiscoveryParticipant> sortedParticipants = new ArrayList<>(participants);
        sortedParticipants.sort(Comparator.comparing(BluetoothDiscoveryParticipant::order));

        // first see if any of the participants that don't require a connection recognize this device
        List<BluetoothDiscoveryParticipant> connectionParticipants = new ArrayList<>();
        for (BluetoothDiscoveryParticipant participant : sortedParticipants) {
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
                            device.getAdapter().getUID(), ex);
                }
            }
        }
        if (result == null) {
            result = createDefaultResult();
        }
        return result;
    }

    private boolean isAddressAvailable(BluetoothAddress address) {
        // if a device with this address has a handler on any of the adapters, we abandon discovery
        return adapters.stream().noneMatch(adapter -> adapter.hasHandlerForDevice(address));
    }

    private DiscoveryResult createDefaultResult() {
        // We did not find a thing type for this device, so let's treat it as a generic beacon
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

        ThingTypeUID thingTypeUID = BluetoothBindingConstants.THING_TYPE_BEACON;

        ThingUID thingUID = new ThingUID(thingTypeUID, device.getAdapter().getUID(),
                device.getAddress().toString().toLowerCase().replace(":", ""));
        // Create the discovery result and add to the inbox
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS).withTTL(DISCOVERY_TTL)
                .withBridge(device.getAdapter().getUID()).withLabel(label).build();
    }

    private @Nullable DiscoveryResult findConnectionResult(List<BluetoothDiscoveryParticipant> connectionParticipants) {
        try {
            for (BluetoothDiscoveryParticipant participant : connectionParticipants) {
                if (device.getConnectionState() != ConnectionState.CONNECTED) {
                    if (device.getConnectionState() != ConnectionState.CONNECTING && !device.connect()) {
                        logger.debug("Connection attempt failed to start for device {}", device.getAddress());
                        // something failed, so we abandon connection discovery
                        return null;
                    }
                    if (!device.awaitConnection(1, TimeUnit.SECONDS)) {
                        logger.debug("Connection to device {} timed out", device.getAddress());
                        return null;
                    }
                    if (!device.isServicesDiscovered()) {
                        device.discoverServices();
                        if (!device.awaitServiceDiscovery(10, TimeUnit.SECONDS)) {
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
        }
        return null;
    }

    private void readDeviceInformationIfMissing() throws InterruptedException {
        if (device.getName() == null) {
            fecthGattCharacteristic(GattCharacteristic.DEVICE_NAME, device::setName);
        }
        if (device.getModel() == null) {
            fecthGattCharacteristic(GattCharacteristic.MODEL_NUMBER_STRING, device::setModel);
        }
        if (device.getSerialNumber() == null) {
            fecthGattCharacteristic(GattCharacteristic.SERIAL_NUMBER_STRING, device::setSerialNumberl);
        }
        if (device.getHardwareRevision() == null) {
            fecthGattCharacteristic(GattCharacteristic.HARDWARE_REVISION_STRING, device::setHardwareRevision);
        }
        if (device.getFirmwareRevision() == null) {
            fecthGattCharacteristic(GattCharacteristic.FIRMWARE_REVISION_STRING, device::setFirmwareRevision);
        }
        if (device.getSoftwareRevision() == null) {
            fecthGattCharacteristic(GattCharacteristic.SOFTWARE_REVISION_STRING, device::setSoftwareRevision);
        }
    }

    private void fecthGattCharacteristic(GattCharacteristic gattCharacteristic, Consumer<String> consumer)
            throws InterruptedException {
        UUID uuid = gattCharacteristic.getUUID();
        BluetoothCharacteristic characteristic = device.getCharacteristic(uuid);
        if (characteristic == null) {
            logger.debug("Device '{}' doesn't support uuid '{}'", device.getAddress(), uuid);
            return;
        }
        try {
            byte[] value = device.readCharacteristic(characteristic).get(1, TimeUnit.SECONDS);
            consumer.accept(BluetoothUtils.getStringValue(value, 0));
        } catch (ExecutionException e) {
            logger.debug("Failed to aquire uuid {} from device {}: {}", uuid, device.getAddress(), e.getMessage());
        } catch (TimeoutException e) {
            logger.debug("Device info (uuid {}) for device {} timed out: {}", uuid, device.getAddress(),
                    e.getMessage());
        }
    }
}
