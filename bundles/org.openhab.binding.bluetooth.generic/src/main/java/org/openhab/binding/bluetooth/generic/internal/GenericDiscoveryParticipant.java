/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
        // If the transport reports the advertising connectability and the device is a non-connectable beacon
        // (ADV_NONCONN_IND / ADV_SCAN_IND), do not claim it as a generic device: returning null lets discovery
        // fall through to the default beacon result. When connectability is unknown (null) we keep the previous
        // behavior and claim it (the process still connect-probes it via requiresConnection).
        if (Boolean.FALSE.equals(device.getConnectable())) {
            return null;
        }
        // Prefer the advertised device name (Complete/Shortened Local Name AD field, exposed as getName()) so
        // the inbox shows something recognizable. Many devices don't advertise a name (or only expose it via
        // GATT after connecting), and some report their address as the name; in those cases fall back to a
        // generic label. The manufacturer (from the company id) is appended when known.
        String name = device.getName();
        boolean hasName = name != null && !name.isEmpty()
                && !name.equals(device.getAddress().toString().replace(':', '-'));
        String label = hasName ? name : "Generic Connectable Bluetooth Device";
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
        // When the transport reports advertising connectability we don't need a connection to decide: a
        // connectable device is surfaced as generic directly from advertisement data (no connect probe), and a
        // non-connectable beacon is declined in createResult(). Only when connectability is unknown (null) do we
        // fall back to the legacy behavior of connecting to fingerprint/enrich the device.
        return device.getConnectable() == null;
    }

    @Override
    public int order() {
        // we want to go last
        return Integer.MAX_VALUE;
    }
}
