/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.draytonwiser.internal.discovery;

import static org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.*;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DraytonWiserMDNSDiscoveryParticipant} is responsible for discovering Drayton Wiser Heat Hubs. It uses the
 * central MDNS Discovery Service.
 *
 * @author Andrew Schofield - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "mdnsdiscovery.draytonwiser")
public class DraytonWiserMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(DraytonWiserMDNSDiscoveryParticipant.class);
    private final Pattern findIllegalChars = Pattern.compile("[^A-Za-z0-9_-]");

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(final ServiceInfo service) {
        if (service.getApplication().contains("http")) {
            final ThingUID uid = getThingUID(service);

            if (uid != null) {
                logger.debug("Discovered Heat Hub '{}' with uid: {}", service.getName(), uid);
                final Map<String, Object> properties = new HashMap<>(2);
                final InetAddress[] addresses = service.getInetAddresses();

                if (addresses.length > 0 && addresses[0] != null) {
                    properties.put(PROP_ADDRESS, addresses[0].getHostAddress());
                    properties.put(REFRESH_INTERVAL, DEFAULT_REFRESH_SECONDS);

                    return DiscoveryResultBuilder.create(uid).withProperties(properties)
                            .withRepresentationProperty(PROP_ADDRESS).withLabel("Heat Hub - " + service.getName())
                            .build();
                }

            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(final ServiceInfo service) {
        if (service.getType() != null && service.getType().equals(getServiceType())
                && service.getName().contains("WiserHeat")) {
            logger.trace("Discovered a Drayton Wiser Heat Hub thing with name '{}'", service.getName());
            return new ThingUID(THING_TYPE_BRIDGE, findIllegalChars.matcher(service.getName()).replaceAll(""));
        }
        return null;
    }
}
