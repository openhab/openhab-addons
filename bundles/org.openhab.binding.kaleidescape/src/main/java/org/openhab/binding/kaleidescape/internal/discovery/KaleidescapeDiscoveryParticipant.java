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
package org.openhab.binding.kaleidescape.internal.discovery;

import static org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link KaleidescapeDiscoveryParticipant} class discovers Strato/Encore line components automatically via SDDP.
 *
 * @author Michael Lobstein - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true)
public class KaleidescapeDiscoveryParticipant implements SddpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(KaleidescapeDiscoveryParticipant.class);

    private static final String MANUFACTURER = "Kaleidescape";

    // Component Types
    private static final String ALTO = "Alto";
    private static final String STRATO = "Strato";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_ALTO, THING_TYPE_STRATO);
    }

    @Override
    public @Nullable DiscoveryResult createResult(SddpDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final Map<String, Object> properties = new HashMap<>(3);

            properties.put(PROPERTY_UUID, uid.getId());
            properties.put(PROPERTY_HOST_NAME, device.ipAddress);
            properties.put(PROPERTY_PORT_NUM, DEFAULT_API_PORT);

            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_UUID).withLabel(device.model).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UID '{}'", device.model, uid.getId());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(SddpDevice device) {
        if (device.manufacturer.startsWith(MANUFACTURER)) {
            final String id = device.macAddress;

            logger.debug("Kaleidescape {} with id {} found at {}", device.model, id, device.host);

            if (id.isBlank()) {
                logger.debug("Invalid UDN for Kaleidescape device: {}", device.toString());
                return null;
            }

            if (device.model.contains(ALTO)) {
                return new ThingUID(THING_TYPE_ALTO, id);
            } else if (device.model.contains(STRATO)) {
                return new ThingUID(THING_TYPE_STRATO, id);
            }
        }
        return null;
    }
}
