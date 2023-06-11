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
package org.openhab.binding.heos.internal.discovery;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.heos.internal.configuration.BridgeConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
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
@Component(service = UpnpDiscoveryParticipant.class, configurationPid = "discovery.heos")
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
            Map<String, Object> properties = new HashMap<>();
            properties.put(Thing.PROPERTY_VENDOR, device.getDetails().getManufacturerDetails().getManufacturer());
            properties.put(Thing.PROPERTY_MODEL_ID, getModel(device.getDetails().getModelDetails()));
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getDetails().getSerialNumber());
            properties.put(BridgeConfiguration.IP_ADDRESS, device.getIdentity().getDescriptorURL().getHost());
            properties.put(PROP_NAME, device.getDetails().getFriendlyName());
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(" Bridge - " + device.getDetails().getFriendlyName())
                    .withRepresentationProperty(Thing.PROPERTY_VENDOR).build();
            logger.debug("Found HEOS device with UID: {}", uid.getAsString());
            return result;
        }
        return null;
    }

    private String getModel(ModelDetails modelDetails) {
        return String.format("%s (%s)", modelDetails.getModelName(), modelDetails.getModelNumber());
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        String modelName = details.getModelDetails().getModelName();
        String modelManufacturer = details.getManufacturerDetails().getManufacturer();
        if ("Denon".equals(modelManufacturer)
                && (modelName.startsWith("HEOS") || modelName.endsWith("H") || modelName.contains("Home"))) {
            String deviceType = device.getType().getType();
            if (deviceType.startsWith("ACT") || deviceType.startsWith("Aios")) {
                return new ThingUID(THING_TYPE_BRIDGE, device.getIdentity().getUdn().getIdentifierString());
            }
        }
        return null;
    }
}
