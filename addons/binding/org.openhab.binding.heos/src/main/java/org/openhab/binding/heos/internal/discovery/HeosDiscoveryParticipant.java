/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.discovery;

import static org.openhab.binding.heos.HeosBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosDiscoveryParticipant} discovers the HEOS Player of the
 * network via an UPnP interface.
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(HeosDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_BRIDGE);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);
            properties.put(HOST, device.getIdentity().getDescriptorURL().getHost());
            properties.put(NAME, device.getDetails().getModelDetails().getModelName());
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getDetails().getFriendlyName()).withRepresentationProperty(PLAYER_TYPE).build();
            logger.info("Found HEOS device with UID: {}", uid.getAsString());
            return result;
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ModelDetails modelDetails = details.getModelDetails();
            ManufacturerDetails modelManufacturerDetails = details.getManufacturerDetails();
            if (modelDetails != null && modelManufacturerDetails != null) {
                String modelName = modelDetails.getModelName();
                String modelManufacturer = modelManufacturerDetails.getManufacturer();
                if (modelName != null && modelManufacturer != null) {
                    if (modelManufacturer.equals("Denon")) {
                        if (modelName.startsWith("HEOS") || modelName.endsWith("H")) {
                            if (device.getType().getType().startsWith("ACT")) {
                                return new ThingUID(THING_TYPE_BRIDGE,
                                        device.getIdentity().getUdn().getIdentifierString());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
