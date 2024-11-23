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
package org.openhab.binding.wemo.internal.discovery;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoDiscoveryParticipant} is responsible for discovering new and
 * removed Wemo devices.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Kai Kreuzer - some refactoring for performance and simplification
 *
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class)
public class WemoDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(WemoDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return WemoBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);
            String label = "WeMo Device";
            try {
                label = device.getDetails().getFriendlyName();
            } catch (Exception e) {
                // ignore and use default label
            }
            properties.put(UDN, device.getIdentity().getUdn().getIdentifierString());

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(UDN).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                    device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString());

            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(@Nullable RemoteDevice device) {
        if (device != null) {
            if (device.getDetails().getManufacturerDetails().getManufacturer() != null) {
                if (device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase().contains("BELKIN")) {
                    if (device.getDetails().getModelDetails().getModelName() != null) {
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("socket")) {
                            logger.debug("Discovered a WeMo Socket thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_SOCKET, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase()
                                .startsWith("outdoorplug")) {
                            logger.debug("Discovered a WeMo Outdoor Plug thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_SOCKET, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("insight")) {
                            logger.debug("Discovered a WeMo Insight thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_INSIGHT,
                                    device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase()
                                .startsWith("lightswitch")) {
                            logger.debug("Discovered a WeMo Lightswitch thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_LIGHTSWITCH,
                                    device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("motion")) {
                            logger.debug("Discovered a WeMo Motion thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_MOTION, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("sensor")) {
                            logger.debug("Discovered a WeMo Motion thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_MOTION, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("bridge")) {
                            logger.debug("Discovered a WeMo Bridge thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_BRIDGE, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("maker")) {
                            logger.debug("Discovered a WeMo Maker thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_MAKER, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("coffee")) {
                            logger.debug("Discovered a WeMo Coffe Maker thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_COFFEE, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("dimmer")) {
                            logger.debug("Discovered a WeMo Dimmer Switch thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_DIMMER, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("crockpot")) {
                            logger.debug("Discovered a WeMo enabled Crockpot thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_CROCKPOT,
                                    device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase()
                                .startsWith("airpurifier")) {
                            logger.debug("Discovered a WeMo enabled Holmes Air Purifier thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_PURIFIER,
                                    device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase()
                                .startsWith("humidifier")) {
                            logger.debug("Discovered a WeMo enabled Holmes Humidifier thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_HUMIDIFIER,
                                    device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("heater")) {
                            logger.debug("Discovered a WeMo enabled Heater thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_HEATER, device.getIdentity().getUdn().getIdentifierString());
                        }
                    }
                }
            }
        }
        return null;
    }
}
