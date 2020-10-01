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
import static org.openhab.binding.freebox.internal.config.PlayerConfiguration.PORT;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.LanHost;
import org.openhab.binding.freebox.internal.handler.ServerHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlayerDiscoveryParticipant} is responsible for discovering
 * the Freebox Player thing using mDNS discovery service
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerDiscoveryParticipant implements MDNSDiscoveryParticipant, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(PlayerDiscoveryParticipant.class);
    private @Nullable ServerHandler bridgeHandler;

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
    private @Nullable String getIpAddress(ServiceInfo service) {
        for (InetAddress addr : service.getInet4Addresses()) {
            return addr.toString().substring(1);
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr.toString().substring(1);
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service.hasData() && service.getServer() != null) {
            String application = service.getApplication();
            return new ThingUID(FREEBOX_THING_TYPE_PLAYER, application);
        }
        return null;
    }

    // Y'a un truc qui ne me plait pas ici, la dépendance potentiellement non
    // satisfaite au bridgehandler qui peut être offline au moment de l'invocation
    // de cette méthode.
    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        ThingUID thingUID = getThingUID(service);
        String ip = getIpAddress(service);
        ServerHandler handler = bridgeHandler;
        if (thingUID != null && ip != null && handler != null && handler.getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                List<LanHost> hosts = handler.getLanHosts();
                Optional<LanHost> host = hosts.stream().filter(h -> ip.equals(h.getIpv4())).findFirst();
                if (host.isPresent()) {
                    String mac = host.get().getMAC();
                    if (mac != null) {
                        logger.info("Created a DiscoveryResult for Freebox Player {} on address {} with MAC : {}",
                                thingUID, ip, mac);
                        return DiscoveryResultBuilder.create(thingUID).withLabel(service.getName())
                                .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac).withProperty(PORT, service.getPort())
                                .withBridge(handler.getThing().getUID())
                                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
                    }
                }
            } catch (FreeboxException e) {
                logger.warn("Error searching MAC address of the Freebox Player");
            }
        }
        return null;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ServerHandler) {
            bridgeHandler = (ServerHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }
}
