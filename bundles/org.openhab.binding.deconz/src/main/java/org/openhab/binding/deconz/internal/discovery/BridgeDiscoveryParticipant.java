/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.discovery;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
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
@Component(service = UpnpDiscoveryParticipant.class)
public class BridgeDiscoveryParticipant implements UpnpDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(BRIDGE_TYPE);
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
        String host = descriptorURL.getHost();
        int port = descriptorURL.getPort();
        name = name + " (" + host + ":" + port + ")";

        Map<String, Object> properties = new TreeMap<>();

        properties.put(CONFIG_HOST, host);
        properties.put(CONFIG_HTTP_PORT, port);
        properties.put(PROPERTY_UDN, UDN);

        return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(name)
                .withRepresentationProperty(PROPERTY_UDN).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ManufacturerDetails manufacturerDetails = details.getManufacturerDetails();
            if (manufacturerDetails != null) {
                URI manufacturerUri = manufacturerDetails.getManufacturerURI();
                if ((manufacturerUri != null && manufacturerUri.toString().contains("dresden"))
                        || "dresden elektronik".equals(manufacturerDetails.getManufacturer())) {
                    return new ThingUID(BRIDGE_TYPE, details.getSerialNumber());
                }
            }
        }
        return null;
    }
}
