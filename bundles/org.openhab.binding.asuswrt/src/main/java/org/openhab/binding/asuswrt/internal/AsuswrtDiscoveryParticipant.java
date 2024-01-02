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
package org.openhab.binding.asuswrt.internal;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.PROPERTY_HOSTNAME;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.THING_TYPE_ROUTER;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link AsuswrtDiscoveryParticipant} discovers the ASUS routers using UPnP.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, configurationPid = "discovery.asuswrt")
public class AsuswrtDiscoveryParticipant implements UpnpDiscoveryParticipant {
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_ROUTER);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        DeviceDetails details = device.getDetails();

        String host = details.getPresentationURI().getHost();
        String model = details.getModelDetails().getModelNumber();

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_HOSTNAME, host);
        properties.put(Thing.PROPERTY_VENDOR, details.getManufacturerDetails().getManufacturer());
        properties.put(Thing.PROPERTY_MODEL_ID, model);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, details.getSerialNumber());

        String label = String.format("ASUS %s Wireless Router (%s)", model, host);

        return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        String modelName = details.getModelDetails().getModelName();
        String modelManufacturer = details.getManufacturerDetails().getManufacturer();
        if (modelManufacturer.toUpperCase().contains("ASUS") && ("ASUS Wireless Router".equalsIgnoreCase(modelName))) {
            return new ThingUID(THING_TYPE_ROUTER, details.getSerialNumber().replace(':', '-'));
        }
        return null;
    }
}
