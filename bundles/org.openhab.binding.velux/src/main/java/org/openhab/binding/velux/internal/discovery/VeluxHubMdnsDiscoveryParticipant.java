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
package org.openhab.binding.velux.internal.discovery;

import static org.openhab.binding.velux.internal.VeluxBindingConstants.THING_TYPE_BRIDGE;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers Velux KLF200 hubs by means of mDNS-SD lookup services
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
public class VeluxHubMdnsDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(VeluxHubMdnsDiscoveryParticipant.class);

    private static final String VELUX_KLF_LAN = "VELUX_KLF_LAN";
    private static final String HTTP_TCP_LOCAL = "_http._tcp.local.";

    /**
     * Checks if the passed {@link ServiceInfo} refers to a Velux hub, and if so tries to resolve and return its ipv4
     * address
     *
     * @param serviceInfo contains an mDNS discovered service info
     * @return returns the ipv4 address if the passed serviceInfo relates to a Velux hub whose host name can be resolved
     *         to an ipv4 address (i.e. if it is online); otherwise returns null
     */
    private @Nullable String getIpAddressIfOnlineVeluxHub(ServiceInfo serviceInfo) {
        String name = serviceInfo.getName();
        logger.trace("getIpAddressIfOnlineVeluxHub(): valid hub check for '{}'", name);
        if (name.startsWith(VELUX_KLF_LAN)) {
            try {
                String addr = InetAddress.getByName(name + "." + serviceInfo.getDomain()).getHostAddress();
                logger.trace("getIpAddressIfOnlineVeluxHub(): valid hub '{}' on ip '{}'", name, addr);
                return addr;
            } catch (UnknownHostException e) {
                // fall through
            }
        }
        logger.trace("getIpAddressIfOnlineVeluxHub(): device '{}' is not a hub", name);
        return null;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return HTTP_TCP_LOCAL;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        logger.trace("createResult(): called..");
        String ipAddr = getIpAddressIfOnlineVeluxHub(serviceInfo);
        if (ipAddr != null) {
            ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, ipAddr.replace(".", "_"));
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_BRIDGE)
                    .withProperty(VeluxBridgeConfiguration.BRIDGE_IPADDRESS, ipAddr)
                    .withRepresentationProperty(VeluxBridgeConfiguration.BRIDGE_IPADDRESS)
                    .withLabel(String.format("Velux Hub (%s)", ipAddr)).build();
            logger.trace("createResult(): return discovered hub uid '{}' on ip '{}'", thingUID, ipAddr);
            return result;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        logger.trace("getThingUID(): called..");
        String ipAddr = getIpAddressIfOnlineVeluxHub(serviceInfo);
        if (ipAddr != null) {
            ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, ipAddr.replace(".", "_"));
            logger.trace("getThingUID(): return uid '{}' for hub on ip '{}'", thingUID, ipAddr);
            return thingUID;
        }
        return null;
    }
}
