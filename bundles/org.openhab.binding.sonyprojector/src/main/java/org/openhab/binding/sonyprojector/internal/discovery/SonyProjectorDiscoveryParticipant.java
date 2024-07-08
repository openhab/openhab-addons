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
package org.openhab.binding.sonyprojector.internal.discovery;

import static org.openhab.binding.sonyprojector.internal.SonyProjectorBindingConstants.THING_TYPE_ETHERNET;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonyprojector.internal.configuration.SonyProjectorEthernetConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.sddp.SddpDevice;
import org.openhab.core.config.discovery.sddp.SddpDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery Service for Sony Projectors that support SDDP.
 *
 * @author Laurent Garnier - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true)
public class SonyProjectorDiscoveryParticipant implements SddpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(SonyProjectorDiscoveryParticipant.class);

    private static final String SONY = "SONY";
    private static final String TYPE_PROJECTOR = "PROJECTOR";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_ETHERNET);
    }

    @Override
    public @Nullable DiscoveryResult createResult(SddpDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final String label = device.manufacturer + " " + device.model;
            final Map<String, Object> properties = Map.of("host", device.ipAddress, //
                    "port", SonyProjectorEthernetConfiguration.DEFAULT_PORT, //
                    "model", SonyProjectorEthernetConfiguration.MODEL_AUTO, //
                    Thing.PROPERTY_MAC_ADDRESS, device.macAddress);
            logger.debug("Created a DiscoveryResult for device '{}' with UID '{}'", label, uid.getId());
            return DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withLabel(label).build();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(SddpDevice device) {
        if (device.manufacturer.toUpperCase(Locale.ENGLISH).contains(SONY)
                && device.type.toUpperCase(Locale.ENGLISH).contains(TYPE_PROJECTOR) && !device.macAddress.isBlank()
                && !device.ipAddress.isBlank()) {

            logger.debug("Sony projector with mac {} found at {}", device.macAddress, device.ipAddress);
            return new ThingUID(THING_TYPE_ETHERNET, device.macAddress);
        }
        return null;
    }
}
