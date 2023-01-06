/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.discovery;

import static org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collections;
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
 * The {@link InnogyBridgeDiscoveryParticipant} is responsible for discovering
 * the innogy SmartHome bridge.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "mdnsdiscovery.innogysmarthome")
@NonNullByDefault
public class InnogyBridgeDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(InnogyBridgeDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                    .withLabel("innogy SmartHome Controller (" + service.getName() + ")").build();
            return result;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(@Nullable ServiceInfo service) {
        if (service != null) {
            String serviceName = service.getName();
            if (serviceName.startsWith("SMARTHOME")) {
                logger.debug("Found innogy bridge via mDNS:{} v4:{} v6:{}", service.getName(),
                        service.getInet4Addresses(), service.getInet6Addresses());
                return new ThingUID(THING_TYPE_BRIDGE, serviceName);
            }
        }
        return null;
    }
}
