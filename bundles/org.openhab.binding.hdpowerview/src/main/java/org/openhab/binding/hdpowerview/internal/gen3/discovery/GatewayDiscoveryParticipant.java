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
package org.openhab.binding.hdpowerview.internal.gen3.discovery;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers HD PowerView Generation 3 Gateways by means of mDNS.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
@Component
public class GatewayDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(GatewayDiscoveryParticipant.class);

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        for (String host : service.getHostAddresses()) {
            if (VALID_IP_V4_ADDRESS.matcher(host).matches()) {
                ThingUID thingUID = new ThingUID(THING_TYPE_GATEWAY, host.replace('.', '_'));
                DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewHubConfiguration.HOST, host)
                        .withRepresentationProperty(HDPowerViewHubConfiguration.HOST)
                        .withLabel("PowerView Gateway (" + host + ")").build();
                logger.debug("mDNS discovered Generation 3 Gateway on host '{}'", host);
                return hub;
            }
        }
        return null;
    }

    @Override
    public String getServiceType() {
        return "_powerview-g3._tcp.local.";
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_GATEWAY);
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        for (String host : service.getHostAddresses()) {
            if (VALID_IP_V4_ADDRESS.matcher(host).matches()) {
                return new ThingUID(THING_TYPE_GATEWAY, host.replace('.', '_'));
            }
        }
        return null;
    }
}
