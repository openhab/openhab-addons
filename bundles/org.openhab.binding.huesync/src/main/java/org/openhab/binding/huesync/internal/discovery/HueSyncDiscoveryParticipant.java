package org.openhab.binding.huesync.internal.discovery;

/**
 * Copyright (c) 2024-2024 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.huesync.internal.HueSyncBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueSyncDiscoveryParticipant} is responsible for discovering
 * the remote huesync.boxes using mDNS discovery service.
 *
 * @author Marco Kawon - Initial contribution
 * @author Patrik Gfeller - Integration into official repository, update to 4.x infrastructure
 * 
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "mdnsdiscovery.huesync")
public class HueSyncDiscoveryParticipant implements MDNSDiscoveryParticipant {
    @SuppressWarnings("null")
    private Logger logger = LoggerFactory.getLogger(HueSyncDiscoveryParticipant.class);

    /**
     *
     * Match the hostname + identifier of the discovered huesync-box.
     * Input is like "HueSyncBox-C4299605AAB2._huesync._tcp.local."
     * 
     * @see·<a·href=
     * "https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml?search=huesync">
     * Service·Name·and·Transport·Protocol·Port·Number·Registry</a>
     */
    private static final String SERVICE_TYPE = "_huesync._tcp.local.";

    @SuppressWarnings("null")
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(HueSyncBindingConstants.THING_TYPE_SYNCBOX);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        String qualifiedName = serviceInfo.getQualifiedName();

        logger.debug("HueSync Device found: {}", qualifiedName);

        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        return null;
    }
}
