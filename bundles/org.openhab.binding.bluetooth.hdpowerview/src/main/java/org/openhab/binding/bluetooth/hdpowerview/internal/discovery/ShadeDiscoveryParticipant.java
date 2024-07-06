/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.hdpowerview.internal.discovery;

import static org.openhab.binding.bluetooth.BluetoothBindingConstants.*;
import static org.openhab.binding.bluetooth.hdpowerview.internal.ShadeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery participant recognizes Hunter Douglas Powerview Shades and create discovery results for them.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class ShadeDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        Integer manufacturerId = device.getManufacturerId();
        if (manufacturerId != null && manufacturerId.intValue() == HUNTER_DOUGLAS_MANUFACTURER_ID) {
            return new ThingUID(THING_TYPE_SHADE, device.getAdapter().getUID(),
                    device.getAddress().toString().toLowerCase().replace(":", ""));
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        ThingUID thingUID = getThingUID(device);
        if (thingUID != null) {
            Map<String, Object> properties = new HashMap<>();

            properties.put(CONFIGURATION_ADDRESS, device.getAddress().toString());
            properties.put(Thing.PROPERTY_VENDOR, HUNTER_DOUGLAS);
            properties.put(Thing.PROPERTY_MAC_ADDRESS, device.getAddress().toString());

            String serialNumber = device.getSerialNumber();
            if (serialNumber != null) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
            }

            String firmwareRevision = device.getFirmwareRevision();
            if (firmwareRevision != null) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareRevision);
            }

            String model = device.getModel();
            if (model != null) {
                properties.put(Thing.PROPERTY_MODEL_ID, model);
            }

            String hardwareRevision = device.getHardwareRevision();
            if (hardwareRevision != null) {
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwareRevision);
            }

            Integer txPower = device.getTxPower();
            if (txPower != null) {
                properties.put(PROPERTY_TXPOWER, Integer.toString(txPower));
            }

            String label = String.format("%s (%s)", SHADE_LABEL, device.getName());

            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CONFIGURATION_ADDRESS).withBridge(device.getAdapter().getUID())
                    .withLabel(label).build();
        }
        return null;
    }

    @Override
    public boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return false;
    }

    @Override
    public int order() {
        // we want to go first
        return Integer.MIN_VALUE;
    }
}
