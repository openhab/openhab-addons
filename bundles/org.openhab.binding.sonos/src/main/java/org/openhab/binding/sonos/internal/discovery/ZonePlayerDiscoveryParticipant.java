/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sonos.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.sonos.internal.SonosBindingConstants;
import org.openhab.binding.sonos.internal.SonosXMLParser;
import org.openhab.binding.sonos.internal.config.ZonePlayerConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZonePlayerDiscoveryParticipant} is responsible processing the
 * results of searches for UPNP devices
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
@Component
public class ZonePlayerDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ZonePlayerDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            String roomName = getSonosRoomName(device);
            if (roomName != null) {
                Map<String, Object> properties = new HashMap<>(3);
                String label = "Sonos device";
                try {
                    label = device.getDetails().getModelDetails().getModelName();
                } catch (Exception e) {
                    // ignore and use default label
                }
                label += " (" + roomName + ")";
                properties.put(ZonePlayerConfiguration.UDN, device.getIdentity().getUdn().getIdentifierString());
                properties.put(SonosBindingConstants.IDENTIFICATION, roomName);

                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                        .withRepresentationProperty(ZonePlayerConfiguration.UDN).build();

                logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                        device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString());
                return result;
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (device.getDetails().getManufacturerDetails().getManufacturer() != null) {
            if (device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase().contains("SONOS")) {
                boolean ignored = false;
                String modelName = getModelName(device);
                switch (modelName) {
                    case "ZP80":
                        modelName = "CONNECT";
                        break;
                    case "ZP100":
                        modelName = "CONNECTAMP";
                        break;
                    case "One SL":
                        modelName = "OneSL";
                        break;
                    case "Arc SL":
                        modelName = "ArcSL";
                        break;
                    case "Sub":
                        // The Sonos Sub is ignored
                        ignored = true;
                        break;
                    default:
                        break;
                }
                if (!ignored) {
                    ThingTypeUID thingUID = new ThingTypeUID(SonosBindingConstants.BINDING_ID, modelName);
                    if (!SonosBindingConstants.SUPPORTED_KNOWN_THING_TYPES_UIDS.contains(thingUID)) {
                        // Try with the model name all in uppercase
                        thingUID = new ThingTypeUID(SonosBindingConstants.BINDING_ID, modelName.toUpperCase());
                        // In case a new "unknown" Sonos player is discovered a generic ThingTypeUID will be used
                        if (!SonosBindingConstants.SUPPORTED_KNOWN_THING_TYPES_UIDS.contains(thingUID)) {
                            thingUID = SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;
                        }
                    }

                    logger.debug("Discovered a Sonos '{}' thing with UDN '{}'", thingUID,
                            device.getIdentity().getUdn().getIdentifierString());
                    return new ThingUID(thingUID, device.getIdentity().getUdn().getIdentifierString());
                }
            }
        }

        return null;
    }

    private String getModelName(RemoteDevice device) {
        // For Ikea SYMFONISK models, the model name now starts with "SYMFONISK" with recent firmwares
        // We can no more use extractModelName as it deletes the first word ("Sonos" for all other devices)
        return device.getDetails().getModelDetails().getModelName().toUpperCase().contains("SYMFONISK") ? "SYMFONISK"
                : SonosXMLParser.extractModelName(device.getDetails().getModelDetails().getModelName());
    }

    private @Nullable String getSonosRoomName(RemoteDevice device) {
        return SonosXMLParser.getRoomName(device.getIdentity().getDescriptorURL().toString());
    }
}
