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
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers HD PowerView hubs by means of mDNS
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component
public class HDPowerViewHubMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubMDNSDiscoveryParticipant.class);

    private final HDPowerviewPropertyGetter propertyGetter;

    @Activate
    public HDPowerViewHubMDNSDiscoveryParticipant(@Reference HDPowerviewPropertyGetter propertyGetter) {
        this.propertyGetter = propertyGetter;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_HUB);
    }

    @Override
    public String getServiceType() {
        return "_powerview._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            String serial = thingUID.getId();
            String host = getIpV4Address(service);
            if (host != null) {
                String generation;
                try {
                    generation = propertyGetter.getGenerationApiV1(host);
                } catch (HubException e) {
                    generation = "1/2";
                }
                String label = String.format("@text/%s [\"%s\", \"%s\"]", LABEL_KEY_HUB, generation, host);
                DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewHubConfiguration.HOST, host)
                        .withProperty(Thing.PROPERTY_SERIAL_NUMBER, serial)
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).withLabel(label).build();
                logger.debug("mDNS discovered Gen {} hub '{}' on host '{}'", generation, thingUID, host);
                return hub;
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String host = getIpV4Address(service);
        if (host != null) {
            try {
                String serial = propertyGetter.getSerialNumberApiV1(host);
                return new ThingUID(THING_TYPE_HUB, serial);
            } catch (HubException e) {
                logger.debug("Error discovering hub", e);
            }
        }
        return null;
    }

    private static @Nullable String getIpV4Address(ServiceInfo service) {
        return Arrays.stream(service.getHostAddresses()).filter(host -> VALID_IP_V4_ADDRESS.matcher(host).matches())
                .findFirst().orElse(null);
    }
}
