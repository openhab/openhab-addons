/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.neohub.internal;

import java.net.Inet4Address;
import java.util.Collections;
import java.util.Set;

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
 * Discovers NeoHubs by means of mDNS-SD
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class NeoHubDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String HEATMISER_NEO_HUB = "Heatmiser neoHub";

    private final Logger logger = LoggerFactory.getLogger(NeoHubDiscoveryParticipant.class);

    /**
     * Check if the {@link ServiceInfo} refers to a valid NeoHub, and if so return its IPv4 address
     *
     * @param serviceInfo
     * @return the ip address if it is a valid neohub, or null if not
     */
    private @Nullable String getIpAddressIfValidNeoHub(ServiceInfo serviceInfo) {
        if (HEATMISER_NEO_HUB.equals(serviceInfo.getName())) {
            for (Inet4Address ipAddr : serviceInfo.getInet4Addresses()) {
                String ipStr = ipAddr.getHostAddress();
                return ipStr;
            }
        }
        return null;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(NeoHubBindingConstants.THING_TYPE_NEOHUB);
    }

    @Override
    public String getServiceType() {
        return "_hap._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        @Nullable
        String ipStr = getIpAddressIfValidNeoHub(serviceInfo);
        if (ipStr != null) {
            ThingUID thingUID = new ThingUID(NeoHubBindingConstants.THING_TYPE_NEOHUB, ipStr.replace('.', '_'));
            DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                    .withProperty(NeoHubConfiguration.HOST_NAME, ipStr)
                    .withRepresentationProperty(NeoHubConfiguration.HOST_NAME).withLabel("NeoHub (" + ipStr + ")")
                    .build();
            logger.debug("Discovered a NeoHub on host '{}'", ipStr);
            return hub;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        @Nullable
        String ipStr = getIpAddressIfValidNeoHub(serviceInfo);
        if (ipStr != null) {
            return new ThingUID(NeoHubBindingConstants.THING_TYPE_NEOHUB, ipStr.replace('.', '_'));
        }
        return null;
    }
}
