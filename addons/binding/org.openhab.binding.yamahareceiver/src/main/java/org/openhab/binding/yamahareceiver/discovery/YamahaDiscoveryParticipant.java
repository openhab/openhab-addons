/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.discovery;

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
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.protocol.YamahaReceiverCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YamahaDiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author David Gräff - Initial contribution
 */
public class YamahaDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(YamahaDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(YamahaReceiverBindingConstants.THING_TYPE_YAMAHAAV);
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
        properties.put((String) YamahaReceiverBindingConstants.CONFIG_HOST_NAME,
                device.getIdentity().getDescriptorURL().getHost());
        properties.put((String) YamahaReceiverBindingConstants.CONFIG_ZONE,
                YamahaReceiverCommunication.Zone.Main_Zone.name());

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();

        logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                device.getDetails().getModelDetails().getModelName(),
                device.getIdentity().getUdn().getIdentifierString());
        return result;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device == null) {
            return null;
        }

        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        String modelName = device.getDetails().getModelDetails().getModelName();
        String friedlyName = device.getDetails().getFriendlyName();

        if (manufacturer == null || modelName == null) {
            return null;
        }

        // UDN shouldn't contain '-' characters.
        String udn = device.getIdentity().getUdn().getIdentifierString().replace("-", "_");

        if (manufacturer.toUpperCase().contains(YamahaReceiverBindingConstants.UPNP_MANUFACTURER)
                && device.getType().getType().equals(YamahaReceiverBindingConstants.UPNP_TYPE)) {

            logger.debug("Discovered a Yamaha Receiver '{}' model '{}' thing with UDN '{}'", friedlyName, modelName,
                    udn);

            return new ThingUID(YamahaReceiverBindingConstants.THING_TYPE_YAMAHAAV, udn);
        } else {
            return null;
        }
    }
}
