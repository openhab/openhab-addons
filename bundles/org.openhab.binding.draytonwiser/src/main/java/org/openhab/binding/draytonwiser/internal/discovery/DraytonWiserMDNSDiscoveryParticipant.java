/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.internal.discovery;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DraytonWiserMDNSDiscoveryParticipant} is responsible for discovering Drayton Wiser Heat Hubs. It uses the
 * central
 * {@link MDNSDiscoveryService}.
 *
 * @author Andrew Schofield - Initial contribution
 *
 */
public class DraytonWiserMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(DraytonWiserMDNSDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(DraytonWiserBindingConstants.THING_TYPE_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {

        if (service.getApplication().contains("http")) {
            ThingUID uid = getThingUID(service);

            if (uid != null) {
                Map<String, Object> properties = new HashMap<>(2);

                InetAddress[] addresses = service.getInetAddresses();
                if (addresses.length > 0 && addresses[0] != null) {
                    properties.put(DraytonWiserBindingConstants.ADDRESS, addresses[0].getHostAddress());
                }

                return DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withRepresentationProperty(uid.getId()).withLabel("Heat Hub - " + service.getName()).build();
            }
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {

        if (service != null) {
            if (service.getType() != null) {
                if (service.getType().equals(getServiceType())) {
                    if (service.getName().contains("WiserHeat")) {
                        logger.trace("Discovered a Drayton Wiser Heat Hub thing with name '{}'", service.getName());
                        return new ThingUID(DraytonWiserBindingConstants.THING_TYPE_BRIDGE, service.getName());
                    }
                }
            }
        }

        return null;
    }

}
