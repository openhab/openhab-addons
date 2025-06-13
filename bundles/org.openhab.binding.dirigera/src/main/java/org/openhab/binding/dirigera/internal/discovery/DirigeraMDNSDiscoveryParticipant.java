/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.discovery;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dirigera.internal.Constants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DirigeraMDNSDiscoveryParticipant} for mDNS discovery of DIRIGERA gateway
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "dirigera.mdns.discovery")
public class DirigeraMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_ihsp._tcp.local.";
    private final Logger logger = LoggerFactory.getLogger(DirigeraMDNSDiscoveryParticipant.class);

    protected final ThingRegistry thingRegistry;

    @Activate
    public DirigeraMDNSDiscoveryParticipant(final @Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(Constants.THING_TYPE_GATEWAY);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo si) {
        logger.trace("DIRIGERA mDNS createResult for {} with IPs {}", si.getQualifiedNameMap(), si.getURLs());
        Inet4Address[] ipAddresses = si.getInet4Addresses();
        String gatewayName = si.getQualifiedNameMap().get(ServiceInfo.Fields.Instance);
        if (gatewayName != null) {
            String ipAddress = null;
            if (ipAddresses.length == 0) {
                // case of mDNS isn't delivering IP address try to resolve it
                String domain = si.getQualifiedNameMap().get(ServiceInfo.Fields.Domain);
                String gatewayHostName = gatewayName + "." + domain;
                try {
                    InetAddress address = InetAddress.getByName(gatewayHostName);
                    ipAddress = address.getHostAddress();
                } catch (Exception e) {
                    logger.warn("DIRIGERA mDNS failed to resolve IP for {} reason {}", gatewayHostName, e.getMessage());
                }
            } else if (ipAddresses.length > 0) {
                ipAddress = ipAddresses[0].getHostAddress();
            }
            if (ipAddress != null) {
                Map<String, Object> properties = new HashMap<>();
                properties.put(PROPERTY_IP_ADDRESS, ipAddress);
                return DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_GATEWAY, gatewayName))
                        .withLabel("DIRIGERA Hub").withRepresentationProperty(PROPERTY_IP_ADDRESS)
                        .withProperties(properties).build();
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo si) {
        String gatewayName = si.getQualifiedNameMap().get(ServiceInfo.Fields.Instance);
        if (gatewayName != null) {
            return new ThingUID(Constants.THING_TYPE_GATEWAY, gatewayName);
        }
        return null;
    }
}
