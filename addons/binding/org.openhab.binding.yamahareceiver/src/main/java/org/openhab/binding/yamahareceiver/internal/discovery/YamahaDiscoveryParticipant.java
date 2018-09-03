/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.discovery;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.*;
import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Configs.CONFIG_HOST_NAME;

/**
 * The {@link YamahaDiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Introduced config object, migrated to newer UPnP api
 */
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class YamahaDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(YamahaDiscoveryParticipant.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(BRIDGE_THING_TYPE);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        Map<String, Object> properties = new HashMap<>(3);

        String label = "Yamaha Receiver";
        try {
            label += " " + device.getDetails().getModelDetails().getModelName();
        } catch (Exception e) {
            // ignore and use the default label
        }

        URL url = device.getIdentity().getDescriptorURL();
        properties.put(CONFIG_HOST_NAME, url.getHost());

        // The port via UPNP is unreliable, sometimes it is 8080, on some models 49154.
        // But so far the API was always reachable via port 80.
        // We provide the port config therefore, if the user ever needs to adjust the port.
        // Note the port is set in the thing-types.xml to 80 by default.

        DiscoveryResult result = DiscoveryResultBuilder
                .create(uid)
                .withTTL(MIN_MAX_AGE_SECS)
                .withProperties(properties)
                .withLabel(label)
                .withRepresentationProperty(CONFIG_HOST_NAME)
                .build();

        logger.debug("Discovered a Yamaha Receiver '{}' model '{}' thing with UDN '{}'",
                device.getDetails().getFriendlyName(), device.getDetails().getModelDetails().getModelName(),
                device.getIdentity().getUdn().getIdentifierString());

        return result;
    }

    public static ThingUID getThingUID(String manufacturer, String deviceType, String udn) {
        if (manufacturer == null || deviceType == null) {
            return null;
        }

        if (manufacturer.toUpperCase().contains(UPNP_MANUFACTURER) && deviceType.equals(UPNP_TYPE)) {
            return new ThingUID(BRIDGE_THING_TYPE, udn);
        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        String deviceType = device.getType().getType();
        
        // UDN shouldn't contain '-' characters.
        return getThingUID(manufacturer, deviceType, device.getIdentity().getUdn().getIdentifierString().replace("-", "_"));
    }
}
