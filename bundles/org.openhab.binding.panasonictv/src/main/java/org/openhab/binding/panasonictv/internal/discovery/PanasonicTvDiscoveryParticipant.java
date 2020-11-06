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
package org.openhab.binding.panasonictv.internal.discovery;

import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.*;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicTvDiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class)
public class PanasonicTvDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(PanasonicTvDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_PANASONICTV);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        return DeviceInformation.fromDevice(device).map(deviceInformation -> {
            ThingUID thingUID = deviceInformation.thingUID;
            if (thingUID == null) {
                logger.debug("Ignoring {}: No thing UID created, probably not a Panasonic TV", deviceInformation);
                return null;
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_HOST, deviceInformation.host);
            properties.put(CONFIG_UDN, deviceInformation.udn);
            String serialNumber = deviceInformation.serialNumber;
            if (serialNumber != null) {
                properties.put(PROPERTY_SERIAL, serialNumber);
            }

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'", deviceInformation.modelName,
                    deviceInformation.udn);

            String label = Objects.requireNonNullElse(deviceInformation.friendlyName, deviceInformation.udn);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CONFIG_UDN).withLabel(label).build();
        }).orElse(null);
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        return DeviceInformation.fromDevice(device).map(deviceInformation1 -> deviceInformation1.thingUID).orElse(null);
    }
}
