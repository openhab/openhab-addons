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
package org.openhab.binding.bluetooth.generic.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCompanyIdentifiers;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the BluetoothDiscoveryParticipant for generic bluetooth devices.
 *
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = BluetoothDiscoveryParticipant.class)
public class GenericDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(GenericDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(GenericBindingConstants.THING_TYPE_GENERIC);
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            // the thingUID will never be null in practice but this makes the null checker happy
            return null;
        }
        String label = "Generic Connectable Bluetooth Device";
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

        addPropertyIfPresent(properties, Thing.PROPERTY_MODEL_ID, device.getModel());
        addPropertyIfPresent(properties, Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber());
        addPropertyIfPresent(properties, Thing.PROPERTY_HARDWARE_VERSION, device.getHardwareRevision());
        addPropertyIfPresent(properties, Thing.PROPERTY_FIRMWARE_VERSION, device.getFirmwareRevision());
        addPropertyIfPresent(properties, BluetoothBindingConstants.PROPERTY_SOFTWARE_VERSION,
                device.getSoftwareRevision());

        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS)
                .withBridge(device.getAdapter().getUID()).withLabel(label).build();
    }

    private static void addPropertyIfPresent(Map<String, Object> properties, String key, @Nullable Object value) {
        if (value != null) {
            properties.put(key, value);
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        return new ThingUID(GenericBindingConstants.THING_TYPE_GENERIC, device.getAdapter().getUID(),
                device.getAddress().toString().toLowerCase().replace(":", ""));
    }

    @Override
    public boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return true;
    }

    @Override
    public int order() {
        // we want to go last
        return Integer.MAX_VALUE;
    }
}
