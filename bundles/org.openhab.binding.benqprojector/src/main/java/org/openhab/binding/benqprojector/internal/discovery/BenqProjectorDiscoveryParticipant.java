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
package org.openhab.binding.benqprojector.internal.discovery;

import static org.openhab.binding.benqprojector.internal.BenqProjectorBindingConstants.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 *
 * Discovery Service for BenQ Projectors that support SDDP.
 *
 * @author Michael Lobstein - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true)
public class BenqProjectorDiscoveryParticipant implements SddpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(BenqProjectorDiscoveryParticipant.class);

    private static final String BENQ = "BENQ";
    private static final String TYPE_PROJECTOR = "PROJECTOR";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_PROJECTOR_TCP);
    }

    @Override
    public @Nullable DiscoveryResult createResult(SddpDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final Map<String, Object> properties = new HashMap<>(3);
            final String label = device.manufacturer + " " + device.model;

            properties.put(Thing.PROPERTY_MAC_ADDRESS, uid.getId());
            properties.put(THING_PROPERTY_HOST, device.ipAddress);
            properties.put(THING_PROPERTY_PORT, DEFAULT_PORT);

            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withLabel(label).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UID '{}'", label, uid.getId());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(SddpDevice device) {
        if (device.manufacturer.toUpperCase(Locale.ENGLISH).contains(BENQ)
                && device.type.toUpperCase(Locale.ENGLISH).contains(TYPE_PROJECTOR) && !device.macAddress.isBlank()
                && !device.ipAddress.isBlank()) {
            logger.debug("BenQ projector with mac {} found at {}", device.macAddress, device.ipAddress);

            return new ThingUID(THING_TYPE_PROJECTOR_TCP,
                    device.macAddress.replaceAll("-", "").toUpperCase(Locale.ENGLISH));
        }
        return null;
    }
}
