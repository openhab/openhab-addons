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
package org.openhab.binding.panasonicbdp.internal.discovery;

import static org.openhab.binding.panasonicbdp.internal.PanaBlurayBindingConstants.*;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
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
 *
 * Discovery Service for Panasonic Blu-ray Players.
 *
 * @author Michael Lobstein - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true)
public class PanaBlurayDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(PanaBlurayDiscoveryParticipant.class);

    private static final String MANUFACTURER = "Panasonic";
    private static final String UB_PREFIX = "UB";
    private static final String UPNP_RESULT_MEDIA_RENDERER = "MediaRenderer";

    private static final List<String> MODELS = List.of("BDT110", "BDT210", "BDT310", "BDT120", "BDT220", "BDT320",
            "BBT01", "BDT500", "UB420", "UB424", "UB820", "UB824", "UB9000", "UB9004");

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final Map<String, Object> properties = new HashMap<>(2);

            final URL url = device.getIdentity().getDescriptorURL();
            final String label = device.getDetails().getFriendlyName();

            properties.put(PROPERTY_UUID, uid.getId());
            properties.put(PROPERTY_HOST_NAME, url.getHost());

            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_UUID).withLabel(label).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UID '{}'", label, uid.getId());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (device.getDetails().getManufacturerDetails().getManufacturer() != null
                && device.getDetails().getModelDetails().getModelNumber() != null) {
            if (device.getDetails().getManufacturerDetails().getManufacturer().startsWith(MANUFACTURER)) {
                logger.debug("Panasonic UPNP device found at {}", device.getIdentity().getDescriptorURL().getHost());
                String id = device.getIdentity().getUdn().getIdentifierString().replaceAll(":", EMPTY);

                // Shorten to just the mac address, ie: 4D454930-0600-1000-8000-80C755A1D630 -> 80C755A1D630
                if (id.length() > 12) {
                    id = id.substring(id.length() - 12);
                }

                final String modelNumber = device.getDetails().getModelDetails().getModelNumber();

                if (MODELS.stream().anyMatch(supportedModel -> (modelNumber.contains(supportedModel)))) {
                    if (modelNumber.contains(UB_PREFIX)) {
                        // UHD (UB-nnnn) players return multiple UPNP results, ignore all but the 'MediaRenderer' result
                        if (UPNP_RESULT_MEDIA_RENDERER.equals(device.getType().getType())) {
                            return new ThingUID(THING_TYPE_UHD_PLAYER, id);
                        }
                    } else {
                        return new ThingUID(THING_TYPE_BD_PLAYER, id);
                    }
                }
            }
        }
        return null;
    }
}
