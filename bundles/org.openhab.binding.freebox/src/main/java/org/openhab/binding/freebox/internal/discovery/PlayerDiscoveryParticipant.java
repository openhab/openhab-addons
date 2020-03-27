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
package org.openhab.binding.freebox.internal.discovery;

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.FREEBOX_THING_TYPE_PLAYER;
import static org.openhab.binding.freebox.internal.config.PlayerConfiguration.*;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlayerDiscoveryParticipant} is responsible for discovering
 * the Freebox Player thing using mDNS discovery service
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
@NonNullByDefault
public class PlayerDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(PlayerDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(FREEBOX_THING_TYPE_PLAYER);
    }

    @Override
    public String getServiceType() {
        return "_hid._udp.local.";
    }

    /**
     * Gets the ip address found in the {@link ServiceInfo}
     *
     * @param service a non-null service
     * @return the ip address of the service or null if none found.
     */
    private @Nullable InetAddress getIpAddress(ServiceInfo service) {
        InetAddress address = null;
        for (InetAddress addr : service.getInet4Addresses()) {
            return addr;
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr;
        }
        return address;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service.hasData() && service.getServer() != null) {
            String application = service.getApplication();
            return new ThingUID(FREEBOX_THING_TYPE_PLAYER, application);
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            InetAddress ip = getIpAddress(service);
            if (ip != null) {
                String id = ip.toString().substring(1);
                logger.info("Created a DiscoveryResult for Freebox Player {} on address {}", thingUID, id);
                return DiscoveryResultBuilder.create(thingUID).withLabel(service.getName())
                        .withProperty(HOST_ADDRESS, id).withProperty(PORT, service.getPort())
                        .withRepresentationProperty(HOST_ADDRESS).build();
            }
        }
        return null;
    }
}
