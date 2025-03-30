/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.discovery;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.util.Arrays;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
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
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            String serial = thingUID.getId();
            String host = getIpV4Address(service);
            if (host != null) {
                String label = String.format("@text/%s [\"%s\"]", LABEL_KEY_GATEWAY, host);
                DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewHubConfiguration.HOST, host)
                        .withProperty(Thing.PROPERTY_SERIAL_NUMBER, serial)
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).withLabel(label).build();
                logger.debug("mDNS discovered Gen 3 gateway '{}' on host '{}'", thingUID, host);
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
        return Set.of(THING_TYPE_GATEWAY);
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String host = getIpV4Address(service);
        if (host != null) {
            String serial = service.getPropertyString("sn");
            if (serial != null) {
                return new ThingUID(THING_TYPE_GATEWAY, serial);
            } else {
                logger.debug("Error discovering gateway 'missing serial number'");
            }
        }
        return null;
    }

    private static @Nullable String getIpV4Address(ServiceInfo service) {
        return Arrays.stream(service.getHostAddresses()).filter(host -> VALID_IP_V4_ADDRESS.matcher(host).matches())
                .findFirst().orElse(null);
    }
}
