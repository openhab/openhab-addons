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
package org.openhab.binding.freeboxos.internal.discovery;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;
import static org.openhab.binding.freeboxos.internal.config.FreeboxOsConfiguration.*;

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiDiscoveryParticipant} is responsible for discovering the various servers flavors of bridges thing using
 * mDNS discovery service
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component
@NonNullByDefault
public class ApiDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final String DOMAIN_PROPERTY = "api_domain";
    private static final String PORT_PROPERTY = "https_port";
    private static final String HTTPS_PROPERTY = "https_available";

    private final Logger logger = LoggerFactory.getLogger(ApiDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return BRIDGE_TYPE_UIDS;
    }

    @Override
    public String getServiceType() {
        return "_fbx-api._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        ThingUID thingUID = getThingUID(service);
        return thingUID != null
                ? DiscoveryResultBuilder.create(thingUID).withLabel("Bridge Freebox OS")
                        .withRepresentationProperty(API_DOMAIN)
                        .withProperty(HTTPS_AVAILABLE, "1".equals(service.getPropertyString(HTTPS_PROPERTY)))
                        .withProperty(HTTPS_PORT, service.getPropertyString(PORT_PROPERTY))
                        .withProperty(API_DOMAIN, service.getPropertyString(DOMAIN_PROPERTY)).build()
                : null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String domain = service.getPropertyString(DOMAIN_PROPERTY);
        if (domain != null) {
            String[] elements = domain.split("\\.");
            if (elements.length > 0) {
                return new ThingUID(BRIDGE_TYPE_API, elements[0]);
            }
        }
        return null;
    }
}
