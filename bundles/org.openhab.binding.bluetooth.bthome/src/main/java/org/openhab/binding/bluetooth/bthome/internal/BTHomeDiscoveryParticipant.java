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
package org.openhab.binding.bluetooth.bthome.internal;

import static org.openhab.binding.bluetooth.bthome.internal.BTHomeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * This discovery participant is able to recognize Airthings devices and create discovery results for them.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component
public class BTHomeDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        if (isBTHomeDevice(device)) {
            if (THING_TYPE_SAMPLE.getId().equals(device.getModel())) {
                return new ThingUID(THING_TYPE_SAMPLE, device.getAdapter().getUID(),
                        device.getAddress().toString().toLowerCase().replace(":", ""));
            }
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        if (!isBTHomeDevice(device)) {
            return null;
        }
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }
        if (THING_TYPE_SAMPLE.getId().equals(device.getModel())) {
            return createResult(device, thingUID, "BTHome");
        }
        return null;
    }

    @Override
    public boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return isBTHomeDevice(device);
    }

    private boolean isBTHomeDevice(BluetoothDiscoveryDevice device) {
        Integer manufacturerId = device.getManufacturerId();
        return manufacturerId != null && manufacturerId == ALLTERCO_MFD_ID_STR;
    }

    private DiscoveryResult createResult(BluetoothDiscoveryDevice device, ThingUID thingUID, String label) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
        properties.put(Thing.PROPERTY_VENDOR, "BTHome");
        String serialNumber = device.getSerialNumber();
        String firmwareRevision = device.getFirmwareRevision();
        String model = device.getModel();
        String hardwareRevision = device.getHardwareRevision();
        Integer txPower = device.getTxPower();
        if (serialNumber != null) {
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        }
        if (firmwareRevision != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareRevision);
        }
        if (model != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, model);
        }
        if (hardwareRevision != null) {
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwareRevision);
        }
        if (txPower != null) {
            properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(txPower));
        }
        properties.put(Thing.PROPERTY_MAC_ADDRESS, device.getAddress().toString());

        // Create the discovery result and add to the inbox
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS)
                .withBridge(device.getAdapter().getUID()).withLabel(label).build();
    }
}
