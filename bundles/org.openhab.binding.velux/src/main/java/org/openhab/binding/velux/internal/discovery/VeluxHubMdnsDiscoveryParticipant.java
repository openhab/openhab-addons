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
 * Discovers Velux KLF200 gateways by means of mDNS-SD lookup services
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component
public class VeluxHubMdnsDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(VeluxHubMdnsDiscoveryParticipant.class);

    private static final String VELUX_KLF = "VELUX_KLF_";
    private static final String VELUX_KLF_LAN = VELUX_KLF + "LAN";
    private static final String HTTP_TCP_LOCAL = "_http._tcp.local.";

    /**
     * Checks if the passed {@link ServiceInfo} refers to a Velux gateway, and if so returns its ipv4 address.
     *
     * Note: the KLF200 gateway has a sub-standard mDNS implementation, so we try two different ways to resolve its ipv4
     * addresses.
     *
     * @param serviceInfo contains an mDNS discovered service info
     * @return returns the ipv4 address if the passed serviceInfo relates to a Velux hub, and its host name can be
     *         resolved to an ipv4 address on a LAN connection (i.e. if it is online); otherwise returns null
     */
    private String getIpAddress(ServiceInfo serviceInfo) {
        String svcName = serviceInfo.getName();
        logger.trace("getIpAddress(): called for device '{}'", svcName);
        if (svcName.startsWith(VELUX_KLF_LAN)) {
            String ipv4;

            /*
             * if the serviceInfo has provided ipv4 addresses, return the first one
             */
            for (Inet4Address ipAddr : serviceInfo.getInet4Addresses()) {
                ipv4 = ipAddr.getHostAddress();
                logger.trace("getIpAddress(): gateway '{}' found on '{}' (resolved by mDNS)", svcName, ipv4);
                return ipv4;
            }

            /*
             * but if there was no ipv4 in serviceInfo, try to resolve the host using plain DNS
             */
            try {
                for (InetAddress ipAddr : InetAddress.getAllByName(svcName + "." + serviceInfo.getDomain())) {
                    if (ipAddr instanceof Inet4Address) {
                        ipv4 = ipAddr.getHostAddress();
                        logger.trace("getIpAddress(): gateway '{}' found on '{}' (resolved by DNS)", svcName, ipv4);
                        return ipv4;
                    }
                }
            } catch (UnknownHostException e1) {
                // fall through
            }
            logger.trace("getIpAddress(): gateway '{}' ignored (no ip address)", svcName);
        } else if (svcName.startsWith(VELUX_KLF)) {
            logger.trace("getIpAddress(): gateway '{}' ignored (not ethernet)", svcName);
        }
        return "";
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
        String ipAddr = getIpAddress(serviceInfo);
        if (!ipAddr.isEmpty()) {
            ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, ipAddr.replace(".", "_"));
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_BRIDGE)
                    .withProperty(VeluxBridgeConfiguration.BRIDGE_IPADDRESS, ipAddr)
                    .withRepresentationProperty(VeluxBridgeConfiguration.BRIDGE_IPADDRESS)
                    .withLabel(String.format("Velux Bridge (%s)", ipAddr)).build();
            return result;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        logger.trace("getThingUID(): called..");
        String ipAddr = getIpAddress(serviceInfo);
        if (!ipAddr.isEmpty()) {
            ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE, ipAddr.replace(".", "_"));
            logger.debug("getThingUID(): returning uid '{}' for gateway on ip '{}'", thingUID, ipAddr);
            return thingUID;
        }
        return null;
    }
}
