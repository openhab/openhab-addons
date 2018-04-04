/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.autelis.internal.discovery;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.autelis.AutelisBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Discovery Service for Autelis Pool Controllers.
 *
 * @author Dan Cunningham
 *
 */
public class AutelisDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(AutelisDiscoveryParticipant.class);

    private static String MANUFACTURER = "autelis";
    private static String MODEL = "pc100pi";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return AutelisBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(3);

            URL url = device.getDetails().getBaseURL();
            String label = device.getDetails().getFriendlyName();
            int port = url.getPort() > 0 ? url.getPort() : 80;

            properties.put("host", url.getHost());
            properties.put("user", "admin");
            properties.put("password", "admin");
            properties.put("port", new Integer(port));

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                    device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device != null) {
            if (device.getDetails().getManufacturerDetails().getManufacturer() != null
                    && device.getDetails().getModelDetails().getModelNumber() != null) {
                if (device.getDetails().getManufacturerDetails().getManufacturer().toLowerCase()
                        .startsWith(MANUFACTURER)
                        && device.getDetails().getModelDetails().getModelNumber().toLowerCase().equals(MODEL)) {
                    logger.debug("Autelis Pool Control Found at {}", device.getDetails().getBaseURL());
                    return new ThingUID(AutelisBindingConstants.POOLCONTROL_THING_TYPE_UID,
                            device.getIdentity().getUdn().getIdentifierString().replaceAll(":", "").toUpperCase());
                }
            }
        }
        return null;
    }
}
