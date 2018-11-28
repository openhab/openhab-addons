/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.discovery;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.deconz.internal.BindingConstants;
import org.osgi.service.component.annotations.Component;

/**
 * Discover deCONZ software instances. They announce themselves as HUE bridges,
 * and their REST API is compatible to HUE bridges. But they also provide a websocket
 * real-time channel for sensors.
 *
 * We check for the manufacturer string of "dresden elektronik".
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class BridgeDiscoveryParticipant implements UpnpDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(BindingConstants.BRIDGE_TYPE);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }
        URL descriptorURL = device.getIdentity().getDescriptorURL();
        String UDN = device.getIdentity().getUdn().getIdentifierString();

        // Friendly name is like "name (host)"
        String name = device.getDetails().getFriendlyName();
        // Cut out the pure name
        if (name.indexOf('(') - 1 > 0) {
            name = name.substring(0, name.indexOf('(') - 1);
        }
        // Add host+port
        String host = descriptorURL.getHost() + ":" + String.valueOf(descriptorURL.getPort());
        name = name + " (" + host + ")";

        Map<String, Object> properties = new TreeMap<>();

        properties.put(BindingConstants.CONFIG_HOST, host);

        return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(name)
                .withRepresentationProperty(UDN).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null && details.getManufacturerDetails() != null
                && "dresden elektronik".equals(details.getManufacturerDetails().getManufacturer())) {
            return new ThingUID(BindingConstants.BRIDGE_TYPE, details.getSerialNumber());
        }
        return null;
    }
}
