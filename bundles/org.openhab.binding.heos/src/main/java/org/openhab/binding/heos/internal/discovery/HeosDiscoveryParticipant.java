/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.discovery;

import static org.openhab.binding.heos.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.NAME;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosDiscoveryParticipant} discovers the HEOS Player of the
 * network via an UPnP interface.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, immediate = true, configurationPid = "discovery.heos")
public class HeosDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(HeosDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_BRIDGE);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(3);
            properties.put(HOST, device.getIdentity().getDescriptorURL().getHost());
            properties.put(NAME, device.getDetails().getModelDetails().getModelName());
            properties.put(PROP_ROLE, PROP_BRIDGE); // Used to hide other bridges if one is already used
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(" Bridge - " + device.getDetails().getFriendlyName())
                    .withRepresentationProperty("Device").build();
            logger.debug("Found HEOS device with UID: {}", uid.getAsString());
            return result;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        Optional<RemoteDevice> optDevice = Optional.ofNullable(device);
        String modelName = optDevice.map(RemoteDevice::getDetails).map(DeviceDetails::getModelDetails)
                .map(ModelDetails::getModelName).orElse("UNKNOWN");
        String modelManufacturer = optDevice.map(RemoteDevice::getDetails).map(DeviceDetails::getManufacturerDetails)
                .map(ManufacturerDetails::getManufacturer).orElse("UNKNOWN");

        if (modelManufacturer.equals("Denon")) {
            if (modelName.startsWith("HEOS") || modelName.endsWith("H")) {
                String deviceType = device.getType().getType();
                if (deviceType.startsWith("ACT") || deviceType.startsWith("Aios")) {
                    return new ThingUID(THING_TYPE_BRIDGE,
                            optDevice.get().getIdentity().getUdn().getIdentifierString());
                }
            }
        }
        return null;
    }
}
