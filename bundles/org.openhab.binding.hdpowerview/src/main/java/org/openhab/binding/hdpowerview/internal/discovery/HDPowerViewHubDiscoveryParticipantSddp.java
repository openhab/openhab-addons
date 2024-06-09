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

    private static final String LABEL_KEY_HUB = "discovery.hub.label";
    private static final String LABEL_KEY_GATEWAY = "discovery.gateway.label";

    private static final String HUNTER_DOUGLAS = "hunterdouglas:";
    private static final String POWERVIEW_HUB_ID = "hub:powerview";
    private static final String POWERVIEW_GEN3_ID = "powerview:gen3:gateway";

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubDiscoveryParticipantSddp.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_HUB, THING_TYPE_GATEWAY);
    }

    @Override
    public @Nullable DiscoveryResult createResult(SddpDevice device) {
        final ThingUID thingUID = getThingUID(device);
        if (thingUID != null) {
            try {
                DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewHubConfiguration.HOST, device.ipAddress)
                        .withRepresentationProperty(HDPowerViewHubConfiguration.HOST)
                        .withLabel(String.format("@text/%s [\"%s\"]",
                                isGateway(device) ? LABEL_KEY_GATEWAY : LABEL_KEY_HUB, device.ipAddress))
                        .build();
                logger.debug("SDDP discovered hub/gateway '{}' on host '{}'", thingUID, device.ipAddress);
                return hub;
            } catch (IllegalArgumentException e) {
                // error already logged, so fall through
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(SddpDevice device) {
        if (device.type.startsWith(HUNTER_DOUGLAS)) {
            try {
                if (VALID_IP_V4_ADDRESS.matcher(device.ipAddress).matches()) {
                    return new ThingUID(isGateway(device) ? THING_TYPE_GATEWAY : THING_TYPE_HUB,
                            device.ipAddress.replace('.', '_'));
                }
            } catch (IllegalArgumentException e) {
                // error already logged, so fall through
            }
        }
        return null;
    }

    /**
     * Check if the device 'type' property represents a Gen 3 gateway or a Gen 1/2 hub.
     *
     * @return true if a Gen 3 gateway or false if a Gen 1/2 hub.
     * @throws IllegalArgumentException if neither Gen 3, 2 or 1.
     */
    private boolean isGateway(SddpDevice device) throws IllegalArgumentException {
        if (device.type.contains(POWERVIEW_GEN3_ID)) {
            return true;
        }
        if (device.type.contains(POWERVIEW_HUB_ID)) {
            return false;
        }
        final IllegalArgumentException e = new IllegalArgumentException("Device has unexpected 'type' property");
        logger.debug("{}", e.getMessage());
        throw e;
    }
}
