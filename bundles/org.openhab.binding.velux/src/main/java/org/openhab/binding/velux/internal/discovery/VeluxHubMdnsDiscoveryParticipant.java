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

import java.net.Inet4Address;
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
 * Discovers Velux KLF200 hubs by means of mDNS-SD
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component
public class VeluxHubMdnsDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(VeluxHubMdnsDiscoveryParticipant.class);

    private static final String HTTP_TCP_LOCAL = "_http._tcp.local.";
    private static final String VELUX_KLF_LAN = "VELUX_KLF_LAN";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return HTTP_TCP_LOCAL;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo info) {
        if (logger.isTraceEnabled()) {
            logger.trace("createResult(): {}", info.toString());
        }
        if (info.getName().startsWith(VELUX_KLF_LAN)) {
            for (Inet4Address ipv4Addr : info.getInet4Addresses()) {
                String ipAddr = ipv4Addr.getHostAddress();
                ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, ipAddr.replace('.', '_'));
                DiscoveryResult klfHub = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(VeluxBridgeConfiguration.BRIDGE_IPADDRESS, ipAddr)
                        .withRepresentationProperty(VeluxBridgeConfiguration.BRIDGE_IPADDRESS)
                        .withLabel("Velux KLF200 Hub (" + ipAddr + ")").build();
                logger.debug("mDNS discovered hub on host '{}'", ipAddr);
                return klfHub;
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo info) {
        if (logger.isTraceEnabled()) {
            logger.trace("getThingUID(): {}", info.toString());
        }
        if (info.getName().startsWith(VELUX_KLF_LAN)) {
            for (Inet4Address ipv4Addr : info.getInet4Addresses()) {
                return new ThingUID(THING_TYPE_BRIDGE, ipv4Addr.getHostAddress().replace('.', '_'));
            }
        }
        return null;
    }
}
