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
package org.openhab.binding.hdpowerview.internal.discovery;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.sddp.SddpDevice;
import org.openhab.core.config.discovery.sddp.SddpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers HD PowerView hubs by means of SDDP
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component
public class HDPowerViewHubDiscoveryParticipantSddp implements SddpDiscoveryParticipant {

    private static final String LABEL_KEY = "discovery.hub.label";

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubDiscoveryParticipantSddp.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_HUB);
    }

    @Override
    public @Nullable DiscoveryResult createResult(SddpDevice device) {
        ThingUID thingUID = getThingUID(device);
        if (thingUID != null) {
            String ipAddress = device.ipAddress;
            DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                    .withProperty(HDPowerViewHubConfiguration.HOST, ipAddress)
                    .withRepresentationProperty(HDPowerViewHubConfiguration.HOST)
                    .withLabel(String.format("@text/%s [\"%s\"]", LABEL_KEY, ipAddress)).build();
            logger.debug("SDDP discovered hub on host '{}'", ipAddress);
            return hub;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(SddpDevice device) {
        String ipAddress = device.ipAddress;
        if (VALID_IP_V4_ADDRESS.matcher(ipAddress).matches()) {
            return new ThingUID(THING_TYPE_HUB, ipAddress.replace('.', '_'));
        }
        return null;
    }
}
