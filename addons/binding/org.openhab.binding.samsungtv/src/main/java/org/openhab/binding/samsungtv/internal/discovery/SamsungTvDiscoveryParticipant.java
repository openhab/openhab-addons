/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.discovery;

import static org.openhab.binding.samsungtv.SamsungTvBindingConstants.SAMSUNG_TV_THING_TYPE;
import static org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration.HOST_NAME;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungTvDiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SamsungTvDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(SamsungTvDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(SAMSUNG_TV_THING_TYPE);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(HOST_NAME, device.getIdentity().getDescriptorURL().getHost());

            DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                    .withProperties(properties)
                    .withLabel(getLabel(device))
                    .build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                    device.getDetails().getModelDetails().getModelName(),
                    device.getIdentity().getUdn().getIdentifierString());
            return result;
        } else {
            return null;
        }
    }

    private String getLabel(RemoteDevice device) {
        String label = "Samsung TV";
        try {
            label = device.getDetails().getFriendlyName();
        } catch (Exception e) {
            // ignore and use the default label
        }
        return label;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device != null) {

            String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
            String modelName = device.getDetails().getModelDetails().getModelName();
            String friendlyName = device.getDetails().getFriendlyName();

            if (manufacturer != null && modelName != null) {

                if (manufacturer.toUpperCase().contains("SAMSUNG ELECTRONICS")) {

                    // UDN shouldn't contain '-' characters.
                    String udn = device.getIdentity().getUdn().getIdentifierString().replace("-", "_");

                    // One Samsung TV contains several UPnP devices.
                    // Create unique Samsung TV thing for every MediaRenderer
                    // device and ignore rest of the UPnP devices.

                    if (device.getType().getType().equals("MediaRenderer")) {
                        logger.debug("Discovered a Samsung TV '{}' model '{}' thing with UDN '{}'", friendlyName,
                                modelName, udn);

                        return new ThingUID(SAMSUNG_TV_THING_TYPE, udn);
                    }
                }
            }
        }
        return null;
    }
}
