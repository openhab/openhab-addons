/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonicbr.internal.discovery;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.panasonicbr.internal.PanasonicBindingConstants;
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
public class PanasonicDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(PanasonicDiscoveryParticipant.class);

    private static final String MANUFACTURER = "panasonic";
    private static final List<String> MODELS = Arrays.asList("BDT110", "BDT210", "BDT310", "BDT-120", "BDT220",
            "BDT320", "BBT01", "BDT500", "UB420", "UB820", "UB9000");

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return PanasonicBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final Map<String, Object> properties = new HashMap<>(3);

            final URL url = device.getIdentity().getDescriptorURL();
            final String label = device.getDetails().getFriendlyName();

            properties.put("hostName", url.getHost());

            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                    device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (device.getDetails().getManufacturerDetails().getManufacturer() != null
                && device.getDetails().getModelDetails().getModelNumber() != null) {
            logger.trace("UPNP {} : {}", device.getDetails().getManufacturerDetails().getManufacturer(),
                    device.getDetails().getModelDetails().getModelNumber());
            if (device.getDetails().getManufacturerDetails().getManufacturer().toLowerCase().startsWith(MANUFACTURER)) {
                logger.debug("Panasonic Blu-ray Player Found at {}", device.getIdentity().getDescriptorURL().getHost());
                String id = device.getIdentity().getUdn().getIdentifierString().replaceAll(":", "").toUpperCase();

                boolean foundMatch = MODELS.stream().anyMatch(model -> (device.getDetails().getModelDetails()
                        .getModelNumber().toUpperCase().contains(model)));

                if (foundMatch)
                    return new ThingUID(PanasonicBindingConstants.THING_TYPE_PLAYER, id);
            }
        }
        return null;
    }
}
