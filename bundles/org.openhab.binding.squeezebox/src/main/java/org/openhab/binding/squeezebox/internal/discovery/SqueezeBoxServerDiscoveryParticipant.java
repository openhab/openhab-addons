/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.squeezebox.internal.discovery;

import static org.openhab.binding.squeezebox.internal.SqueezeBoxBindingConstants.SQUEEZEBOXSERVER_THING_TYPE;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.squeezebox.internal.utils.HttpUtils;
import org.openhab.binding.squeezebox.internal.utils.SqueezeBoxCommunicationException;
import org.openhab.binding.squeezebox.internal.utils.SqueezeBoxNotAuthorizedException;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers a SqueezeServer on the network using UPNP
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mark Hilbush - Add support for LMS authentication
 *
 */
@Component
public class SqueezeBoxServerDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxServerDiscoveryParticipant.class);

    /**
     * Name of a Squeeze Server
     */
    private static final String MODEL_NAME = "Logitech Media Server";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(SQUEEZEBOXSERVER_THING_TYPE);
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
            int defaultCliPort = 9090;

            try {
                cliPort = HttpUtils.getCliPort(host, webPort);
            } catch (SqueezeBoxNotAuthorizedException e) {
                logger.debug("Not authorized to query CLI port. Using default of {}", defaultCliPort);
                cliPort = defaultCliPort;
            } catch (NumberFormatException e) {
                logger.debug("Badly formed CLI port. Using default of {}", defaultCliPort);
                cliPort = defaultCliPort;
            } catch (SqueezeBoxCommunicationException e) {
                logger.debug("Could not get cli port: {}", e.getMessage(), e);
                return null;
            }

            String label = device.getDetails().getFriendlyName();

            String representationPropertyName = "ipAddress";
            properties.put(representationPropertyName, host);
            properties.put("webport", Integer.valueOf(webPort));
            properties.put("cliPort", Integer.valueOf(cliPort));

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(representationPropertyName).withLabel(label).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                    device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device.getDetails().getFriendlyName() != null) {
            if (device.getDetails().getModelDetails().getModelName().contains(MODEL_NAME)) {
                logger.debug("Discovered a {} thing with UDN '{}'", device.getDetails().getFriendlyName(),
                        device.getIdentity().getUdn().getIdentifierString());
                return new ThingUID(SQUEEZEBOXSERVER_THING_TYPE,
                        device.getIdentity().getUdn().getIdentifierString().toUpperCase());
            }
        }
        return null;
    }
}
