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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.freebox.internal.config.PlayerConfiguration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlayerDiscoveryParticipant} is responsible for discovering
 * the Freebox Player thing using mDNS discovery service
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(immediate = true)
@NonNullByDefault
public class PlayerDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(PlayerDiscoveryParticipant.class);

    private static final String SERVICE_TYPE = "_hid._udp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(FREEBOX_THING_TYPE_PLAYER);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    @Nullable
    public ThingUID getThingUID(ServiceInfo service) {
        String[] hosts = service.getHostAddresses();
        if (getServiceType().equals(service.getType()) && hosts != null && hosts.length > 0 && !hosts[0].isEmpty()) {
            return new ThingUID(FREEBOX_THING_TYPE_PLAYER, hosts[0].replaceAll("[^A-Za-z0-9_]", "_"));
        }
        return null;
    }

    @Override
    @Nullable
    public DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            logger.info("Created a DiscoveryResult for Freebox Player {} on IP {}", thingUID,
                    service.getHostAddresses()[0]);
            Map<String, Object> properties = new HashMap<>(2);
            properties.put(PlayerConfiguration.HOST_ADDRESS, service.getHostAddresses()[0]);
            properties.put(PlayerConfiguration.PORT, service.getPort());
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(service.getName())
                    .build();
        }
        return null;
    }
}
