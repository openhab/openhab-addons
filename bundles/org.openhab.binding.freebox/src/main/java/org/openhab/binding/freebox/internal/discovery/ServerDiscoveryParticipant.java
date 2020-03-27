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
import static org.openhab.binding.freebox.internal.config.ServerConfiguration.*;

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
 * the various servers flavors of bridges thing using mDNS discovery service
 *
 * @author Laurent Garnier - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
@NonNullByDefault
public class ServerDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final String FBX_DELTA_GW = "fbxgw7";

    private final Logger logger = LoggerFactory.getLogger(ServerDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.unmodifiableSet(
                Stream.of(FREEBOX_BRIDGE_TYPE_REVOLUTION, FREEBOX_BRIDGE_TYPE_DELTA).collect(Collectors.toSet()));
    }

    @Override
    public String getServiceType() {
        return "_fbx-api._tcp.local.";
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service.hasData()) {
            String application = service.getApplication();
            String boxModel = service.getPropertyString("box_model");
            if (boxModel != null && application != null && !application.isEmpty()) {
                return new ThingUID(
                        boxModel.contains(FBX_DELTA_GW) ? FREEBOX_BRIDGE_TYPE_DELTA : FREEBOX_BRIDGE_TYPE_REVOLUTION,
                        application.replaceAll("[^A-Za-z0-9_]", "_"));
            }

        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            String apiDomain = service.getPropertyString("api_domain");
            Boolean httpsAvailable = "1".equals(service.getPropertyString("https_available"));
            String httpsPort = service.getPropertyString("https_port");
            logger.info("Created a DiscoveryResult for Freebox Server {}.", thingUID);
            return DiscoveryResultBuilder.create(thingUID).withLabel(service.getName())
                    .withProperty(HOST_ADDRESS, apiDomain).withProperty(HTTPS_AVAILABLE, httpsAvailable)
                    .withProperty(REMOTE_HTTPS_PORT, httpsPort).withRepresentationProperty(HOST_ADDRESS).build();
        }
        return null;
    }

}
