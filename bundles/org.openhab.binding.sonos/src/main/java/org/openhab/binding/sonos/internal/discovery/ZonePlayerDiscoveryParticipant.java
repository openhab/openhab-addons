/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.sonos.internal.SonosBindingConstants;
import org.openhab.binding.sonos.internal.SonosXMLParser;
import org.openhab.binding.sonos.internal.config.ZonePlayerConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
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

    private Set<ThingTypeUID> supportedThingTypes = SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    private boolean isAutoDiscoveryEnabled = true;

    /**
     * Called at the service activation.
     *
     * @param componentContext
     */
    @Activate
    protected void activate(ComponentContext componentContext) {
        if (componentContext.getProperties() instanceof Dictionary properties
                && properties.get("enableAutoDiscovery") instanceof String autoDiscoveryValue
                && !autoDiscoveryValue.isEmpty()) {
            isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryValue);
        }
        supportedThingTypes = isAutoDiscoveryEnabled ? SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS : Set.of();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return supportedThingTypes;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        if (getThingUID(device) instanceof ThingUID uid && getSonosRoomName(device) instanceof String roomName) {
            Map<String, Object> properties = new HashMap<>(3);
            String label = "Sonos device";
            DeviceDetails deviceDetails = device.getDetails();
            try {
                label = deviceDetails.getModelDetails().getModelName();
            } catch (Exception e) {
                // ignore and use default label
            }
            label += " (" + roomName + ")";
            properties.put(ZonePlayerConfiguration.UDN, device.getIdentity().getUdn().getIdentifierString());
            properties.put(SonosBindingConstants.IDENTIFICATION, roomName);

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(ZonePlayerConfiguration.UDN).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'", deviceDetails.getFriendlyName(),
                    device.getIdentity().getUdn().getIdentifierString());
            return result;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails deviceDetails = device.getDetails();
        if (deviceDetails.getManufacturerDetails().getManufacturer() instanceof String manufacturer
                && manufacturer.toUpperCase().contains("SONOS")) {
            String id = SonosXMLParser.buildThingTypeIdFromModelName(deviceDetails.getModelDetails().getModelName());
            String udn = device.getIdentity().getUdn().getIdentifierString();
            if (!id.isEmpty() && !SonosBindingConstants.UNSUPPORTED_KNOWN_IDS.contains(id.toLowerCase())
                    && !udn.isEmpty()) {
                ThingTypeUID thingTypeUID = new ThingTypeUID(SonosBindingConstants.BINDING_ID, id);
                if (!SonosBindingConstants.SUPPORTED_KNOWN_THING_TYPES_UIDS.contains(thingTypeUID)) {
                    // Try with the model name all in uppercase
                    thingTypeUID = new ThingTypeUID(SonosBindingConstants.BINDING_ID, id.toUpperCase());
                    // In case a new "unknown" Sonos player is discovered a generic ThingTypeUID will be used
                    if (!SonosBindingConstants.SUPPORTED_KNOWN_THING_TYPES_UIDS.contains(thingTypeUID)) {
                        thingTypeUID = SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;
                        logger.warn(
                                "'{}' is not yet a supported model, thing type '{}' is considered as default; please open an issue",
                                deviceDetails.getModelDetails().getModelName(), thingTypeUID);
                    }
                }

                logger.debug("Discovered a Sonos '{}' thing with UDN '{}'", thingTypeUID, udn);
                return new ThingUID(thingTypeUID, udn);
            }
        }
        return null;
    }

    private @Nullable String getSonosRoomName(RemoteDevice device) {
        return SonosXMLParser.getRoomName(device.getIdentity().getDescriptorURL());
    }
}
