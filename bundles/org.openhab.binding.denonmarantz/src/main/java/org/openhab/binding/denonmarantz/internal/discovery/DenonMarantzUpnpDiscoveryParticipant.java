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
package org.openhab.binding.denonmarantz.internal.discovery;

import static org.openhab.binding.denonmarantz.internal.DenonMarantzBindingConstants.*;

import java.net.URL;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DenonMarantzUpnpDiscoveryParticipant} is responsible for discovering Denon AV Receivers.
 * It uses the central {@link org.openhab.core.config.discovery.upnp.internal.UpnpDiscoveryService}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@Component(configurationPid = "discovery.denonmarantz")
@NonNullByDefault
public class DenonMarantzUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(DenonMarantzUpnpDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_AVR);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        DeviceDetails details = device.getDetails();

        if (!VENDOR_DENON.equalsIgnoreCase(details.getManufacturerDetails().getManufacturer())) {
            return null;
        }

        URL baseUrl = details.getBaseURL();
        if (baseUrl == null) {
            logger.debug("Discovered {}, but base URL is missing", device.getDisplayString());
            return null;
        }

        String serialNumber = details.getSerialNumber();
        if (serialNumber == null) {
            logger.debug("Discovered {}, but serial number is missing", device.getDisplayString());
            return null;
        }

        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }

        String host = baseUrl.getHost();
        String model = details.getModelDetails().getModelName();

        logger.debug("Discovered {}", device.getDisplayString());

        Map<String, Object> properties = new HashMap<>(4);
        properties.put(PARAMETER_HOST, host);
        properties.put(Thing.PROPERTY_VENDOR, VENDOR_DENON);
        properties.put(Thing.PROPERTY_MODEL_ID, model);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber.toLowerCase());

        return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(VENDOR_DENON + " " + model)
                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (!VENDOR_DENON.equalsIgnoreCase(details.getManufacturerDetails().getManufacturer())) {
            return null;
        }
        String serialNumber = details.getSerialNumber();
        if (serialNumber == null) {
            return null;
        }
        return new ThingUID(THING_TYPE_AVR, serialNumber.toLowerCase());
    }
}
