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
package org.openhab.binding.bluetooth.radoneye.internal;

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
 * This discovery participant is able to recognize RadonEye devices and create discovery results for them.
 *
 * @author Peter Obel - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class RadoneyeDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    private static final String RADONEYE_BLUETOOTH_COMPANY_ID = "f24be3";

    private static final String RD200 = "R20"; // RadonEye First Generation BLE

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return RadoneyeBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        if (isRadoneyeDevice(device)) {
            if (RD200.equals(getModel(device))) {
                return new ThingUID(RadoneyeBindingConstants.THING_TYPE_RADONEYE, device.getAdapter().getUID(),
                        device.getAddress().toString().toLowerCase().replace(":", ""));
            }
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        if (!isRadoneyeDevice(device)) {
            return null;
        }
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }
        if (RD200.equals(getModel(device))) {
            return createResult(device, thingUID, "RadonEye (BLE)");
        }
        return null;
    }

    @Override
    public boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return isRadoneyeDevice(device);
    }

    private boolean isRadoneyeDevice(BluetoothDiscoveryDevice device) {
        String manufacturerMacId = device.getAddress().toString().toLowerCase().replace(":", "").substring(0, 6);
        if (manufacturerMacId.equals(RADONEYE_BLUETOOTH_COMPANY_ID.toLowerCase())) {
            return true;
        }
        return false;
    }

    private String getSerial(BluetoothDiscoveryDevice device) {
        String name = device.getName();
        String[] parts = name.split(":");
        if (parts.length == 3) {
            return parts[2];
        } else {
            return "";
        }
    }

    private String getManufacturer(BluetoothDiscoveryDevice device) {
        String name = device.getName();
        String[] parts = name.split(":");
        if (parts.length == 3) {
            return parts[0];
        } else {
            return "";
        }
    }

    private String getModel(BluetoothDiscoveryDevice device) {
        String name = device.getName();
        String[] parts = name.split(":");
        if (parts.length == 3) {
            return parts[1];
        } else {
            return "";
        }
    }

    private DiscoveryResult createResult(BluetoothDiscoveryDevice device, ThingUID thingUID, String label) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
        properties.put(Thing.PROPERTY_VENDOR, "RadonEye");
        String name = device.getName();
        String serialNumber = device.getSerialNumber();
        String firmwareRevision = device.getFirmwareRevision();
        String model = device.getModel();
        String hardwareRevision = device.getHardwareRevision();
        Integer txPower = device.getTxPower();
        if (serialNumber != null) {
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        } else {
            properties.put(Thing.PROPERTY_MODEL_ID, getSerial(device));
        }
        if (firmwareRevision != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareRevision);
        }
        if (model != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, model);
        } else {
            properties.put(Thing.PROPERTY_MODEL_ID, getModel(device));
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
