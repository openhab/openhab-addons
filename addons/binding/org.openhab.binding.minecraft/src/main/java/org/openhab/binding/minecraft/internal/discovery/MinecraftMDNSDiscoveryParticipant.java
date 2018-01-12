/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.discovery;

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
import org.openhab.binding.minecraft.MinecraftBindingConstants;

/**
 * The {@link MinecraftMDNSDiscoveryParticipant} is responsible for discovering Minecraft servers
 * {@link MDNSDiscoveryService}.
 *
 * @author Mattias Markehed
 *
 */
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
