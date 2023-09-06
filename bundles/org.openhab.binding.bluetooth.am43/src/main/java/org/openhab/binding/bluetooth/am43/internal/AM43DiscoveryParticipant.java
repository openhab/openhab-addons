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
package org.openhab.binding.bluetooth.am43.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * This discovery participant is able to recognize AM43 devices and create discovery results for them.
 *
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class AM43DiscoveryParticipant implements BluetoothDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(AM43BindingConstants.THING_TYPE_AM43);
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }
        String label = "AM43 Blind Drive Motor";
        Map<String, Object> properties = new HashMap<>();
        properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
        properties.put(Thing.PROPERTY_MODEL_ID, "AM43-0.45/40-ES-EB");
        properties.put(Thing.PROPERTY_VENDOR, "A-OK Precision motor Ltd.");
        Integer txPower = device.getTxPower();
        if (txPower != null) {
            properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(txPower));
        }

        // Create the discovery result and add to the inbox
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS)
                .withBridge(device.getAdapter().getUID()).withLabel(label).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        if (device.getConnectionState() == ConnectionState.CONNECTED
                && device.supportsService(AM43BindingConstants.SERVICE_UUID)) {
            return new ThingUID(AM43BindingConstants.THING_TYPE_AM43, device.getAdapter().getUID(),
                    device.getAddress().toString().toLowerCase().replace(":", ""));
        }
        return null;
    }

    @Override
    public boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return device.getManufacturerId() == null && device.getName() != null;
    }
}
