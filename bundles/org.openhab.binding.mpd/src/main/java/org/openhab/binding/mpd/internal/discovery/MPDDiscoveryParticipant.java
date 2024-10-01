/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mpd.internal.discovery;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mpd.internal.MPDBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MDNSDiscoveryParticipant} that will discover Music Player Daemons.
 *
 * @author Stefan Röllin - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class MPDDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(MPDDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(MPDBindingConstants.THING_TYPE_MPD);
    }

    @Override
    public String getServiceType() {
        return "_mpd._tcp.local.";
    }

    @Override
    @Nullable
    public DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        String host = getHostAddress(service);
        int port = service.getPort();

        logger.debug("Music Player Daemon found on host {} port {}", host, port);

        if (uid == null || host == null || host.isEmpty()) {
            return null;
        }

        String uniquePropVal = String.format("%s-%d", host, port);

        final Map<String, Object> properties = new HashMap<>(3);
        properties.put(MPDBindingConstants.PARAMETER_IPADDRESS, host);
        properties.put(MPDBindingConstants.PARAMETER_PORT, port);
        properties.put(MPDBindingConstants.UNIQUE_ID, uniquePropVal);

        String name = service.getName();

        return DiscoveryResultBuilder.create(uid).withLabel(name).withProperties(properties)
                .withRepresentationProperty(MPDBindingConstants.UNIQUE_ID).build();
    }

    @Nullable
    private String getHostAddress(ServiceInfo service) {
        if (service.getInet4Addresses() != null) {
            for (Inet4Address addr : service.getInet4Addresses()) {
                if (addr != null) {
                    return addr.getHostAddress();
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public ThingUID getThingUID(ServiceInfo service) {
        if (getServiceType().equals(service.getType())) {
            String name = getUIDName(service.getName());
            if (!name.isEmpty()) {
                return new ThingUID(MPDBindingConstants.THING_TYPE_MPD, name);
            }
        }
        return null;
    }

    private String getUIDName(@Nullable String serviceName) {
        if (serviceName == null) {
            return "";
        }
        return serviceName.replaceAll("[^A-Za-z0-9_]", "_").replaceAll("_+", "_");
    }
}
