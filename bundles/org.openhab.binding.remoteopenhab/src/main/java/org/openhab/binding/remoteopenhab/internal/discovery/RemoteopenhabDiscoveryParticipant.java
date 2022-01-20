/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.remoteopenhab.internal.discovery;

import static org.openhab.binding.remoteopenhab.internal.RemoteopenhabBindingConstants.*;
import static org.openhab.binding.remoteopenhab.internal.config.RemoteopenhabServerConfiguration.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteopenhabDiscoveryParticipant} is responsible for discovering
 * the remote openHAB servers using mDNS discovery service.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "mdnsdiscovery.remoteopenhab")
public class RemoteopenhabDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_openhab-server._tcp.local.";

    private final Logger logger = LoggerFactory.getLogger(RemoteopenhabDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_BRIDGE_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        // We use the first host address as thing ID
        String ip = (service.getHostAddresses() != null && service.getHostAddresses().length > 0
                && !service.getHostAddresses()[0].isEmpty()) ? service.getHostAddresses()[0].replaceAll("\\[|\\]", "")
                        : null;
        // Host address matching a local IP address are ignored
        if (getServiceType().equals(service.getType()) && ip != null && !matchLocalIpAddress(ip)) {
            return new ThingUID(BRIDGE_TYPE_SERVER, ip.replaceAll("[^A-Za-z0-9_]", "_"));
        }
        return null;
    }

    private boolean matchLocalIpAddress(String ipAddress) {
        List<String> localIpAddresses = NetUtil.getAllInterfaceAddresses().stream()
                .filter(a -> !a.getAddress().isLinkLocalAddress())
                .map(a -> a.getAddress().getHostAddress().split("%")[0]).collect(Collectors.toList());
        return localIpAddresses.contains(ipAddress);
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        DiscoveryResult result = null;
        String ip = (service.getHostAddresses() != null && service.getHostAddresses().length > 0
                && !service.getHostAddresses()[0].isEmpty()) ? service.getHostAddresses()[0].replaceAll("\\[|\\]", "")
                        : null;
        String restPath = service.getPropertyString("uri");
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null && ip != null && restPath != null) {
            logger.debug("Create a DiscoveryResult for remote openHAB server {} with IP {}", thingUID, ip);
            Map<String, Object> properties = Map.of(HOST, ip, REST_PATH, restPath);
            result = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withRepresentationProperty(HOST)
                    .withLabel("@text/discovery.server.label").build();
        }
        return result;
    }
}
