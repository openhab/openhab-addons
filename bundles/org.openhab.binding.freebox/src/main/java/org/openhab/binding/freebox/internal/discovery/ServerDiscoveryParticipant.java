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

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * The {@link ServerDiscoveryParticipant} is responsible for discovering
 * the Freebox Server (bridge) thing using mDNS discovery service
 *
 * @author Laurent Garnier - Initial contribution
 */
@Component(immediate = true)
@NonNullByDefault
public class ServerDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ServerDiscoveryParticipant.class);

    private static final String SERVICE_TYPE = "_fbx-api._tcp.local.";
    private static final String FBX_DELTA_GW = "fbxgw7";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.unmodifiableSet(
                Stream.of(FREEBOX_THING_TYPE_SERVER, FREEBOX_THING_TYPE_DELTA).collect(Collectors.toSet()));
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (getServiceType().equals(service.getType()) && (service.getPropertyString("uid") != null)) {
            String id = service.getPropertyString("uid").replaceAll("[^A-Za-z0-9_]", "_");
            String boxModel = service.getPropertyString("box_model");
            if (boxModel.contains(FBX_DELTA_GW)) {
                return new ThingUID(FREEBOX_THING_TYPE_DELTA, id);
            } else {
                return new ThingUID(FREEBOX_THING_TYPE_SERVER, id);
            }
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            logger.info("Created a DiscoveryResult for Freebox Server {}.", thingUID);
            String id = service.getPropertyString("uid").replaceAll("[^A-Za-z0-9_]", "_");
            ThingUID bridgeUID = new ThingUID(FREEBOX_BRIDGE_TYPE_API, id);

            return DiscoveryResultBuilder.create(thingUID).withLabel(service.getName()).withBridge(bridgeUID).build();
        }
        return null;
    }

}
