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

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.*;
import org.openhab.binding.panasonictv.internal.service.MediaRendererService;
import org.openhab.binding.panasonictv.internal.service.RemoteControllerService;
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
    private final HashMap<String, DeviceInformation> incompleteDiscoveryResults = new HashMap<>();

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_PANASONICTV);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        DeviceInformation deviceInformation = DeviceInformation.fromDevice(device);
        if (deviceInformation == null) {
            return null;
        }

        if (!deviceInformation.manufacturer.toUpperCase().contains("PANASONIC")) {
            logger.trace("Ignoring {}: Not a Panasonic TV", deviceInformation);
            return null;
        }

        logger.debug("Processing {}", deviceInformation);
        DeviceInformation resultingDeviceInformation = incompleteDiscoveryResults.compute(deviceInformation.host,
                (k, v) -> deviceInformation.merge(v));
        if (resultingDeviceInformation == null || !resultingDeviceInformation.isComplete()) {
            logger.debug("{} still incomplete", deviceInformation.host);
            return null;
        }

        ThingUID thingUid = resultingDeviceInformation.thingUid;
        String mrUdn = resultingDeviceInformation.services.get(MediaRendererService.SERVICE_NAME);
        String rcUdn = resultingDeviceInformation.services.get(RemoteControllerService.SERVICE_NAME);

        if (thingUid == null || mrUdn == null || rcUdn == null) {
            logger.warn(
                    "Found a complete result but something is missing: thingUid={}, mrUdn={}, rcUdn={}. Please report a bug.",
                    thingUid, mrUdn, rcUdn);
            return null;
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_MEDIARENDERER_UDN, mrUdn);
        properties.put(CONFIG_REMOTECONTROLLER_UDN, rcUdn);
        String serialNumber = resultingDeviceInformation.serialNumber;
        if (serialNumber != null) {
            properties.put(PROPERTY_SERIAL, serialNumber);
        }

        logger.debug("Created a DiscoveryResult for device '{}' ({}) with UDNs '{}' and '{}'",
                resultingDeviceInformation.modelName, resultingDeviceInformation.host, mrUdn, rcUdn);

        String label = Objects.requireNonNullElse(resultingDeviceInformation.friendlyName, mrUdn);
        return DiscoveryResultBuilder.create(thingUid).withProperties(properties)
                .withRepresentationProperty(CONFIG_MEDIARENDERER_UDN).withLabel(label).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceInformation deviceInformation = DeviceInformation.fromDevice(device);
        return deviceInformation != null ? deviceInformation.thingUid : null;
    }
}
