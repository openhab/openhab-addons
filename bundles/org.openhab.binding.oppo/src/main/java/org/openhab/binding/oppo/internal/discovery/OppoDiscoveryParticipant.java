/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.oppo.internal.discovery;

import static org.openhab.binding.oppo.internal.OppoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UPnP Discovery Service for OPPO BDP-10X and UDP-20X Blu-ray players.
 *
 * @author Michael Lobstein - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true)
public class OppoDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(OppoDiscoveryParticipant.class);

    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_HOST = "host";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_BDP103, THING_TYPE_BDP105, THING_TYPE_UDP203, THING_TYPE_UDP205);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final Map<String, Object> properties = new HashMap<>(2);
            final String host = device.getIdentity().getDescriptorURL().getHost();
            final String label = device.getDetails().getModelDetails().getModelName();

            properties.put(PROPERTY_UUID, uid.getId());
            properties.put(PROPERTY_HOST, host);

            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_HOST).withLabel(label).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UID '{}'", label, uid.getId());
            return result;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if ("OPPO".equals(device.getDetails().getManufacturerDetails().getManufacturer())) {
            final String modelName = device.getDetails().getModelDetails().getModelName();
            final String id = device.getIdentity().getDescriptorURL().getHost().replace(".", "_");

            logger.debug("OPPO UPnP device model {} found at {}", modelName,
                    device.getIdentity().getDescriptorURL().getHost());

            return switch (modelName) {
                case "OPPO BDP-103", "OPPO BDP-103D" -> new ThingUID(THING_TYPE_BDP103, id);
                case "OPPO BDP-105", "OPPO BDP-105D" -> new ThingUID(THING_TYPE_BDP105, id);
                case "OPPO UDP-203" -> new ThingUID(THING_TYPE_UDP203, id);
                case "OPPO UDP-205" -> new ThingUID(THING_TYPE_UDP205, id);
                default -> null;
            };
        }
        return null;
    }
}
