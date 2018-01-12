/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal.discovery;

import static org.openhab.binding.squeezebox.SqueezeBoxBindingConstants.SQUEEZEBOXSERVER_THING_TYPE;

import java.net.URI;
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
import org.openhab.binding.squeezebox.internal.utils.HttpUtils;
import org.openhab.binding.squeezebox.internal.utils.SqueezeBoxNotAuthorizedException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers a SqueezeServer on the network using UPNP
 *
 * @author Dan Cunningham
 * @author Mark Hilbush - Add support for LMS authentication
 *
 */
@Component(immediate = true)
public class SqueezeBoxServerDiscoveryParticipant implements UpnpDiscoveryParticipant {

    /**
     * Name of a Squeeze Server
     */
    private static final String MODEL_NAME = "Logitech Media Server";

    private Logger logger = LoggerFactory.getLogger(SqueezeBoxServerDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(SQUEEZEBOXSERVER_THING_TYPE);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(3);

            URI uri = device.getDetails().getPresentationURI();

            String host = uri.getHost();
            int webPort = uri.getPort();
            int cliPort = 0;

            try {
                cliPort = HttpUtils.getCliPort(host, webPort);
            } catch (SqueezeBoxNotAuthorizedException e) {
                logger.debug("Not authorized to query CLI port from Squeeze Server. Setting CLI to default of 9090");
                cliPort = 9090;
            } catch (Exception e) {
                logger.debug("Could not get cli port: {}", e.getMessage(), e);
                return null;
            }

            String label = device.getDetails().getFriendlyName();

            properties.put("ipAddress", host);
            properties.put("webport", new Integer(webPort));
            properties.put("cliPort", new Integer(cliPort));

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
            if (device.getDetails().getFriendlyName() != null) {
                if (device.getDetails().getModelDetails().getModelName().contains(MODEL_NAME)) {
                    logger.debug("Discovered a {} thing with UDN '{}'", device.getDetails().getFriendlyName(),
                            device.getIdentity().getUdn().getIdentifierString());
                    return new ThingUID(SQUEEZEBOXSERVER_THING_TYPE,
                            device.getIdentity().getUdn().getIdentifierString().toUpperCase());
                }
            }
        }
        return null;
    }

}
