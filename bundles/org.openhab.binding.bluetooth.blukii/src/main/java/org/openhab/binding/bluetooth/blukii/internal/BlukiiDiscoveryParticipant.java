/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.blukii.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.blukii.BlukiiBindingConstants;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.osgi.service.component.annotations.Component;

/**
 * This discovery participant is able to recognize blukii devices and create discovery results for them.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(immediate = true)
public class BlukiiDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    @Override
    public @NonNull Set<@NonNull ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(BlukiiBindingConstants.THING_TYPE_BEACON);
    }

    @Override
    public @Nullable ThingUID getThingUID(@NonNull BluetoothDevice device) {
        String name = device.getName();
        if (name != null && name.startsWith(BlukiiBindingConstants.BLUKII_PREFIX)) {
            if (name.charAt(BlukiiBindingConstants.BLUKII_PREFIX.length()) == 'B') {
                return new ThingUID(BlukiiBindingConstants.THING_TYPE_BEACON, device.getAdapter().getUID(),
                        device.getAddress().toString().toLowerCase().replace(":", ""));
            }
        }
        return null;
    }

    @Override
    public DiscoveryResult createResult(@NonNull BluetoothDevice device) {
        ThingUID thingUID = getThingUID(device);

        if (thingUID != null) {
            String label = "Blukii SmartBeacon";

            Map<String, Object> properties = new HashMap<>();
            properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
            properties.put(Thing.PROPERTY_VENDOR, "Schneider Schreibgeräte GmbH");
            Integer txPower = device.getTxPower();
            if (txPower != null) {
                properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(txPower));
            }

            // Create the discovery result and add to the inbox
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS)
                    .withBridge(device.getAdapter().getUID()).withLabel(label).build();
        } else {
            return null;
        }
    }

}
