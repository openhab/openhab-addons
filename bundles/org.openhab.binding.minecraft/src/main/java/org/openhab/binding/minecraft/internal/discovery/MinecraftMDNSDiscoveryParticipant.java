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
package org.openhab.binding.minecraft.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.openhab.binding.minecraft.internal.MinecraftBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MinecraftMDNSDiscoveryParticipant} is responsible for discovering Minecraft servers
 * {@link MDNSDiscoveryService}.
 *
 * @author Mattias Markehed - Initial contribution
 */
@Component
public class MinecraftMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(MinecraftBindingConstants.THING_TYPE_SERVER);
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        if (service.getName().equals("wc-minecraft")) {
            ThingUID uid = getThingUID(service);

            if (uid != null) {
                Map<String, Object> properties = new HashMap<>();
                int port = service.getPort();
                String host = service.getInetAddresses()[0].getHostAddress();

                properties.put(MinecraftBindingConstants.PARAMETER_HOSTNAME, host);
                properties.put(MinecraftBindingConstants.PARAMETER_PORT, port);

                return DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withRepresentationProperty(uid.getId()).withLabel("Minecraft Server (" + host + ")").build();
            }
        }
        return null;
    }

    /**
     * Check if service is a minecraft server.
     *
     * @param service the service to check.
     * @return true if minecraft server, else false.
     */
    private boolean isMinecraftServer(ServiceInfo service) {
        return (service != null && service.getType() != null && service.getType().equals(getServiceType()));
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if (isMinecraftServer(service) && service.getInetAddresses().length > 0) {
            String host = service.getInetAddresses()[0].getHostAddress();
            host = host.replace('.', '_');

            return new ThingUID(MinecraftBindingConstants.THING_TYPE_SERVER, host);
        }

        return null;
    }
}
