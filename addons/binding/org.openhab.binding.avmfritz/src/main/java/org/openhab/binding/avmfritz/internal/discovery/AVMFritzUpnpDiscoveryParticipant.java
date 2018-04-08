/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_VENDOR;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AVMFritzUpnpDiscoveryParticipant} is responsible for discovering new and removed FRITZ!Box devices. It
 * uses the central {@link UpnpDiscoveryService}.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class AVMFritzUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(AVMFritzUpnpDiscoveryParticipant.class);

    /**
     * Provide supported ThingTypeUIDs
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_BRIDGE_THING_TYPES_UIDS;
    }

    /**
     * Create a discovery result from UPNP discovery
     */
    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            logger.debug("discovered: {} ({}) at {}", device.getDisplayString(), device.getDetails().getFriendlyName(),
                    device.getIdentity().getDescriptorURL().getHost());

            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_IP_ADDRESS, device.getIdentity().getDescriptorURL().getHost());
            properties.put(PROPERTY_VENDOR, device.getDetails().getManufacturerDetails().getManufacturer());

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getDetails().getFriendlyName()).withRepresentationProperty(CONFIG_IP_ADDRESS)
                    .build();

            return result;
        }
        return null;
    }

    /**
     * Compute a FRITZ!Box / FRITZ!Powerline ThingUID.
     */
    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        // newer FRITZ!OS versions return several upnp services (e.g. Mediaserver)
        if (device.getType().getType().equals(BRIDGE_FRITZBOX)) {
            DeviceDetails details = device.getDetails();
            if (details != null) {
                ModelDetails modelDetails = details.getModelDetails();
                if (modelDetails != null) {
                    String modelName = modelDetails.getModelName();
                    if (modelName != null) {
                        // It would be better to use udn but in my case FB is discovered twice
                        // .getIdentity().getUdn().getIdentifierString()
                        String id = device.getIdentity().getDescriptorURL().getHost().replaceAll(INVALID_PATTERN, "_");
                        if (modelName.startsWith(BOX_MODEL_NAME)) {
                            logger.debug("discovered on {}", device.getIdentity().getDiscoveredOnLocalAddress());
                            return new ThingUID(BRIDGE_THING_TYPE, id);
                        } else if (modelName.startsWith(POWERLINE_MODEL_NAME)) {
                            logger.debug("discovered on {}", device.getIdentity().getDiscoveredOnLocalAddress());
                            return new ThingUID(PL546E_STANDALONE_THING_TYPE, id);
                        }
                    }
                }
            }
        }
        return null;
    }
}
