/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.discovery;

import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.avmfritz.BindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AVMFritzUpnpDiscoveryParticipant} is responsible for discovering
 * new and removed FRITZ!Box devices. It uses the central
 * {@link UpnpDiscoveryService}.
 *
 * @author Robert Bausdorf - Initial contribution
 * 
 */
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class AVMFritzUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(AVMFritzUpnpDiscoveryParticipant.class);

    /**
     * Provide supported thing type uid's
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return BindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
    }

    /**
     * Create a discovery result from UPNP discovery
     */
    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            logger.debug("discovered: {} at {}", device.getDisplayString(),
                    device.getIdentity().getDescriptorURL().getHost());
            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_IP_ADDRESS, device.getIdentity().getDescriptorURL().getHost());
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getDetails().getFriendlyName()).withRepresentationProperty(CONFIG_IP_ADDRESS)
                    .withTTL(Math.max(MIN_MAX_AGE_SECS, device.getIdentity().getMaxAgeSeconds())).build();
            return result;
        }
        return null;
    }

    /**
     * Compute a FRITZ!Box thind UID.
     */
    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ModelDetails modelDetails = details.getModelDetails();
            if (modelDetails != null) {
                String modelName = modelDetails.getModelName();
                if (modelName != null) {
                    if (modelName.startsWith(BRIDGE_MODEL_NAME)) {
                        logger.debug("discovered on {}", device.getIdentity().getDiscoveredOnLocalAddress());
                        return new ThingUID(BRIDGE_THING_TYPE,
                                device.getIdentity().getDescriptorURL().getHost()
                                        // It world be better to use udn but in my case FB is discovered twice
                                        // .getIdentity().getUdn().getIdentifierString()
                                        .replaceAll("[^a-zA-Z0-9_]", "_"));
                    } else if (modelName.startsWith(PL546E_MODEL_NAME)) {
                        logger.debug("discovered on {}", device.getIdentity().getDiscoveredOnLocalAddress());
                        return new ThingUID(PL546E_STANDALONE_THING_TYPE,
                                device.getIdentity().getDescriptorURL().getHost()
                                        // It world be better to use udn but in my case PL546E is discovered twice
                                        // .getIdentity().getUdn().getIdentifierString()
                                        .replaceAll("[^a-zA-Z0-9_]", "_"));
                    }
                }
            } else {
                logger.debug("no model details");
            }
        }
        return null;
    }
}
