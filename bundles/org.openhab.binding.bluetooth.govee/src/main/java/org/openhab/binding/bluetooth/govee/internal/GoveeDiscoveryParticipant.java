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
package org.openhab.binding.bluetooth.govee.internal;

import static org.openhab.binding.bluetooth.govee.internal.GoveeBindingConstants.SUPPORTED_THING_TYPES_UIDS;

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
 * The {@link GoveeDiscoveryParticipant} handles discovery of Govee bluetooth devices
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
@Component(service = BluetoothDiscoveryParticipant.class)
public class GoveeDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    private ThingUID getThingUID(BluetoothDiscoveryDevice device, ThingTypeUID thingTypeUID) {
        return new ThingUID(thingTypeUID, device.getAdapter().getUID(),
                device.getAddress().toString().toLowerCase().replace(":", ""));
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        GoveeModel model = GoveeModel.getGoveeModel(device);
        if (model != null) {
            return getThingUID(device, model.getThingTypeUID());
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        GoveeModel model = GoveeModel.getGoveeModel(device);
        if (model != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
            properties.put(Thing.PROPERTY_VENDOR, "Govee");
            properties.put(Thing.PROPERTY_MODEL_ID, model.name());
            Integer txPower = device.getTxPower();
            if (txPower != null) {
                properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(txPower));
            }

            // Create the discovery result and add to the inbox
            return DiscoveryResultBuilder.create(getThingUID(device, model.getThingTypeUID()))
                    .withProperties(properties)
                    .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS)
                    .withBridge(device.getAdapter().getUID()).withLabel(model.getLabel()).build();
        }
        return null;
    }
}
