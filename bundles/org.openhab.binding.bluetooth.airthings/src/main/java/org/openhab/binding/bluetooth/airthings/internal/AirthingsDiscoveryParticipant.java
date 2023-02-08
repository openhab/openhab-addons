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
package org.openhab.binding.bluetooth.airthings.internal;

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
 * @author Pauli Anttila - Initial contribution
 * @author Kai Kreuzer - Added Airthings Wave Mini support
 * @author Davy Wong - Added Airthings Wave Gen 1 support
 *
 */
@NonNullByDefault
@Component
public class AirthingsDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    private static final int AIRTHINGS_COMPANY_ID = 820; // Formerly Corentium AS

    private static final String WAVE_PLUS_MODEL = "2930";
    private static final String WAVE_MINI_MODEL = "2920";
    private static final String WAVE_GEN1_MODEL = "2900"; // Wave 1st Gen SN 2900xxxxxx

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return AirthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        if (isAirthingsDevice(device)) {
            if (WAVE_PLUS_MODEL.equals(device.getModel())) {
                return new ThingUID(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_PLUS,
                        device.getAdapter().getUID(), device.getAddress().toString().toLowerCase().replace(":", ""));
            }
            if (WAVE_MINI_MODEL.equals(device.getModel())) {
                return new ThingUID(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_MINI,
                        device.getAdapter().getUID(), device.getAddress().toString().toLowerCase().replace(":", ""));
            }
            if (WAVE_GEN1_MODEL.equals(device.getModel())) {
                return new ThingUID(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_GEN1,
                        device.getAdapter().getUID(), device.getAddress().toString().toLowerCase().replace(":", ""));
            }
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        if (!isAirthingsDevice(device)) {
            return null;
        }
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }
        if (WAVE_PLUS_MODEL.equals(device.getModel())) {
            return createResult(device, thingUID, "Airthings Wave Plus");
        }
        if (WAVE_MINI_MODEL.equals(device.getModel())) {
            return createResult(device, thingUID, "Airthings Wave Mini");
        }
        if (WAVE_GEN1_MODEL.equals(device.getModel())) {
            return createResult(device, thingUID, "Airthings Wave Gen 1");
        }
        return null;
    }

    @Override
    public boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return isAirthingsDevice(device);
    }

    private boolean isAirthingsDevice(BluetoothDiscoveryDevice device) {
        Integer manufacturerId = device.getManufacturerId();
        return manufacturerId != null && manufacturerId == AIRTHINGS_COMPANY_ID;
    }

    private DiscoveryResult createResult(BluetoothDiscoveryDevice device, ThingUID thingUID, String label) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
        properties.put(Thing.PROPERTY_VENDOR, "Airthings AS");
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
