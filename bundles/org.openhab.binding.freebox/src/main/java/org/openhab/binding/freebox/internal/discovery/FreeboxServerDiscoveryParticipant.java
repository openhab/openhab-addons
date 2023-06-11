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
package org.openhab.binding.freebox.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.openhab.binding.freebox.internal.FreeboxBindingConstants;
import org.openhab.binding.freebox.internal.config.FreeboxServerConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxServerDiscoveryParticipant} is responsible for discovering
 * the Freebox Server (bridge) thing using mDNS discovery service
 *
 * @author Laurent Garnier - Initial contribution
 */
@Component
public class FreeboxServerDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(FreeboxServerDiscoveryParticipant.class);

    private static final String SERVICE_TYPE = "_fbx-api._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return FreeboxBindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if ((service.getType() != null) && service.getType().equals(getServiceType())
                && (service.getPropertyString("uid") != null)) {
            return new ThingUID(FreeboxBindingConstants.FREEBOX_BRIDGE_TYPE_SERVER,
                    service.getPropertyString("uid").replaceAll("[^A-Za-z0-9_]", "_"));
        }
        return null;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        DiscoveryResult result = null;
        String ip = null;
        if (service.getHostAddresses() != null && service.getHostAddresses().length > 0
                && !service.getHostAddresses()[0].isEmpty()) {
            ip = service.getHostAddresses()[0];
        }
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null && ip != null) {
            logger.info("Created a DiscoveryResult for Freebox Server {} on IP {}", thingUID, ip);
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(FreeboxServerConfiguration.FQDN, ip + ":" + service.getPort());
            properties.put(FreeboxServerConfiguration.USE_ONLY_HTTP, "true");
            if (service.getPropertyString("device_type") != null) {
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, service.getPropertyString("device_type"));
            }
            if (service.getPropertyString("api_base_url") != null) {
                properties.put(FreeboxBindingConstants.API_BASE_URL, service.getPropertyString("api_base_url"));
            }
            if (service.getPropertyString("api_version") != null) {
                properties.put(FreeboxBindingConstants.API_VERSION, service.getPropertyString("api_version"));
            }
            result = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(service.getName())
                    .build();
        }
        return result;
    }
}
