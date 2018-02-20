/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.chromecast.ChromecastBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChromecastDiscoveryParticipant} is responsible for discovering Chromecast devices through UPnP.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Daniel Walters - Change discovery protocol to mDNS
 *
 */
@Component(immediate = true)
public class ChromecastDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final String SERVICE_TYPE = "_googlecast._tcp.local.";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return ChromecastBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        final ThingUID uid = getThingUID(service);
        if (uid == null) {
            return null;
        }

        final Map<String, Object> properties = new HashMap<>(2);
        String host = service.getHostAddresses()[0];
        properties.put(ChromecastBindingConstants.HOST, host);
        int port = service.getPort();
        properties.put(ChromecastBindingConstants.PORT, port);
        logger.debug("Chromecast Found: {} {}", host, port);
        String friendlyName = service.getPropertyString("fn"); // friendly name;

        final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withThingType(getThingType(service))
                .withProperties(properties).withLabel(friendlyName).build();

        return result;
    }

    private ThingTypeUID getThingType(final ServiceInfo service) {
        String model = service.getPropertyString("md"); // model
        logger.debug("Chromecast Type: {}", model);
        if (model == null) {
            return null;
        }
        if (model.equals("Chromecast Audio")) {
            return ChromecastBindingConstants.THING_TYPE_AUDIO;
        } else if (model.equals("Google Cast Group")) {
            return ChromecastBindingConstants.THING_TYPE_AUDIOGROUP;
        } else {
            return ChromecastBindingConstants.THING_TYPE_CHROMECAST;
        }
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        ThingTypeUID thingTypeUID = getThingType(service);
        if (thingTypeUID != null) {
            String id = service.getPropertyString("id"); // device id
            return new ThingUID(thingTypeUID, id);
        } else {
            return null;
        }
    }

}
