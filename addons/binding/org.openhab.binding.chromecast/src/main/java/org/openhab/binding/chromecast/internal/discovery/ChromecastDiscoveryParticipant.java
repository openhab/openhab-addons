/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.chromecast.internal.discovery;

import static org.openhab.binding.chromecast.internal.ChromecastBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChromecastDiscoveryParticipant} is responsible for discovering Chromecast devices through UPnP.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Daniel Walters - Change discovery protocol to mDNS
 */
@Component(immediate = true)
public class ChromecastDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final String PROPERTY_MODEL = "md";
    private static final String PROPERTY_FRIENDLY_NAME = "fn";
    private static final String PROPERTY_DEVICE_ID = "id";
    private static final String SERVICE_TYPE = "_googlecast._tcp.local.";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
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
        properties.put(HOST, host);
        int port = service.getPort();
        properties.put(PORT, port);
        logger.debug("Chromecast Found: {} {}", host, port);
        String id = service.getPropertyString(PROPERTY_DEVICE_ID);
        properties.put(DEVICE_ID, id);
        String friendlyName = service.getPropertyString(PROPERTY_FRIENDLY_NAME); // friendly name;

        final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withThingType(getThingType(service))
                .withProperties(properties).withRepresentationProperty(DEVICE_ID).withLabel(friendlyName).build();

        return result;
    }

    private ThingTypeUID getThingType(final ServiceInfo service) {
        String model = service.getPropertyString(PROPERTY_MODEL); // model
        logger.debug("Chromecast Type: {}", model);
        if (model == null) {
            return null;
        }
        if (model.equals("Chromecast Audio")) {
            return THING_TYPE_AUDIO;
        } else if (model.equals("Google Cast Group")) {
            return THING_TYPE_AUDIOGROUP;
        } else {
            return THING_TYPE_CHROMECAST;
        }
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        ThingTypeUID thingTypeUID = getThingType(service);
        if (thingTypeUID != null) {
            String id = service.getPropertyString(PROPERTY_DEVICE_ID); // device id
            return new ThingUID(thingTypeUID, id);
        } else {
            return null;
        }
    }
}
