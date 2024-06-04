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
package org.openhab.binding.samsungtv.internal.discovery;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.SAMSUNG_TV_THING_TYPE;
import static org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration.HOST_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.samsungtv.internal.Utils;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungTvDiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author Pauli Anttila - Initial contribution
 * @author Arjan Mels - Changed to upnp.UpnpDiscoveryParticipant
 * @author Nick Waterton - use Utils class
 */
@NonNullByDefault
@Component
public class SamsungTvDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(SamsungTvDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(SAMSUNG_TV_THING_TYPE);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(HOST_NAME, Utils.getHost(device));

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(HOST_NAME).withLabel(getLabel(device)).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}' and properties: {}",
                    Utils.getModelName(device), Utils.getUdn(device), properties);
            return result;
        }
        return null;
    }

    private String getLabel(RemoteDevice device) {
        String label = Utils.getFriendlyName(device);
        return label.isBlank() ? "Samsung TV" : label;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (Utils.getManufacturer(device).toUpperCase().contains("SAMSUNG ELECTRONICS")) {
            // One Samsung TV contains several UPnP devices.
            // Create unique Samsung TV thing for every MediaRenderer
            // device and ignore rest of the UPnP devices.
            // use MediaRenderer udn for ThingID.

            if ("MediaRenderer".equals(Utils.getType(device))) {
                String udn = Utils.getUdn(device);
                if (logger.isDebugEnabled()) {
                    logger.debug("Retrieved Thing UID for a Samsung TV '{}' model '{}' thing with UDN '{}'",
                            Utils.getFriendlyName(device), Utils.getModelName(device), udn);
                }

                return new ThingUID(SAMSUNG_TV_THING_TYPE, udn);
            }
        }
        return null;
    }
}
