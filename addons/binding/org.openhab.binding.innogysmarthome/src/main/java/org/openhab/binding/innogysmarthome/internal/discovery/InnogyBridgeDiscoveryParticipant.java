/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.discovery;

import static org.openhab.binding.innogysmarthome.InnogyBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collections;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InnogyBridgeDiscoveryParticipant} is responsible for discovering
 * the innogy SmartHome bridge.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@Component(immediate = true)
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
    public DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                    .withLabel("innogy SmartHome Controller (" + service.getName() + ")").build();
            return result;
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
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
