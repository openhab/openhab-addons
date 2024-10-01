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
package org.openhab.binding.atlona.internal.discovery;

import static org.openhab.binding.atlona.internal.AtlonaBindingConstants.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atlona.internal.pro3.AtlonaPro3Config;
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
 * Discovery Service for Atlona HDMI matrices that support SDDP.
 *
 * @author Michael Lobstein - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true)
public class AtlonaDiscoveryParticipant implements SddpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(AtlonaDiscoveryParticipant.class);

    private static final String ATLONA = "ATLONA";
    private static final String PROXY_AVSWITCH = "avswitch";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_PRO3_44M, THING_TYPE_PRO3_66M, THING_TYPE_PRO3_88M, THING_TYPE_PRO3_1616M,
                THING_TYPE_PRO3HD_44M, THING_TYPE_PRO3HD_66M);
    }

    @Override
    public @Nullable DiscoveryResult createResult(SddpDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final Map<String, Object> properties = new HashMap<>(2);
            final String label = device.model + " (" + device.ipAddress + ")";

            properties.put(Thing.PROPERTY_MAC_ADDRESS, device.macAddress);
            properties.put(AtlonaPro3Config.IP_ADDRESS, device.ipAddress);

            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withLabel(label).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UID '{}'", label, uid.getId());
            return result;
        } else {
            return null;
        }
    }

    /*
     * The Atlona SDDP message has the following format
     *
     * <pre>
     * NOTIFY ALIVE SDDP/1.0
     * From: "192.168.1.30:1902"
     * Host: "AT-UHD-PRO3-88M_B898B0030F4D"
     * Type: "AT-UHD-PRO3-88M"
     * Max-Age: 1800
     * Primary-Proxy: "avswitch"
     * Proxies: "avswitch"
     * Manufacturer: "Atlona"
     * Model: "AT-UHD-PRO3-88M"
     * Driver: "avswitch_Atlona_AT-UHD-PRO3-88M_IP.c4i"
     * Config-URL: "http://192.168.1.30/"
     * </pre>
     */
    @Override
    public @Nullable ThingUID getThingUID(SddpDevice device) {
        if (device.manufacturer.toUpperCase(Locale.ENGLISH).contains(ATLONA)
                && PROXY_AVSWITCH.equals(device.primaryProxy) && !device.macAddress.isBlank()
                && !device.ipAddress.isBlank()) {
            final ThingTypeUID typeId;

            switch (device.model) {
                case "AT-UHD-PRO3-44M":
                    typeId = THING_TYPE_PRO3_44M;
                    break;
                case "AT-UHD-PRO3-66M":
                    typeId = THING_TYPE_PRO3_66M;
                    break;
                case "AT-UHD-PRO3-88M":
                    typeId = THING_TYPE_PRO3_88M;
                    break;
                case "AT-UHD-PRO3-1616M":
                    typeId = THING_TYPE_PRO3_1616M;
                    break;
                case "AT-PRO3HD44M":
                    typeId = THING_TYPE_PRO3HD_44M;
                    break;
                case "AT-PRO3HD66M":
                    typeId = THING_TYPE_PRO3HD_66M;
                    break;
                default:
                    logger.warn("Unknown model #: {}", device.model);
                    return null;
            }

            logger.debug("Atlona matrix with mac {} found at {}", device.macAddress, device.ipAddress);
            return new ThingUID(typeId, device.macAddress);
        }
        return null;
    }
}
