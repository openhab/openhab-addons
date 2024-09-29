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
package org.openhab.binding.loxone.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
 * The {@link LxDiscoveryParticipant} class creates Miniserver things.
 * It analyzes UPNP devices discovered by the framework and if Loxone Miniserver is found,
 * a new thing discovery is reported, which in turn will result in creating a {@link Thing}
 * and subsequently a new {@link LxServerHandler} object.
 *
 * @author Pawel Pieczul - Initial contribution
 *
 */
@Component
public class LxDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(LxDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return LxServerHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);

            // After correct Thing UID is created, we have confidence that all following parameters exist and we don't
            // need to check for null objects here in the device details
            DeviceDetails details = device.getDetails();
            String serial = details.getSerialNumber();
            String host = details.getPresentationURI().getHost();
            String label = details.getFriendlyName() + " @ " + host;
            int port = details.getPresentationURI().getPort();
            String vendor = details.getManufacturerDetails().getManufacturer();
            String model = details.getModelDetails().getModelName();

            logger.debug("Creating discovery result for serial {} label {} port {}", serial, label, port);
            properties.put(LxBindingConstants.MINISERVER_PARAM_HOST, host);
            properties.put(LxBindingConstants.MINISERVER_PARAM_PORT, port);
            properties.put(Thing.PROPERTY_VENDOR, vendor);
            properties.put(Thing.PROPERTY_MODEL_ID, model);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serial);

            return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        if (manufacturer != null && manufacturer.toLowerCase().contains("loxone")) {
            String model = device.getDetails().getModelDetails().getModelName();
            if (model != null && model.toLowerCase().contentEquals("loxone miniserver")) {
                String serial = device.getDetails().getSerialNumber();
                if (serial == null) {
                    serial = device.getIdentity().getUdn().getIdentifierString();
                }
                if (serial != null) {
                    return new ThingUID(LxBindingConstants.THING_TYPE_MINISERVER, serial);
                }
            }
        }
        return null;
    }
}
