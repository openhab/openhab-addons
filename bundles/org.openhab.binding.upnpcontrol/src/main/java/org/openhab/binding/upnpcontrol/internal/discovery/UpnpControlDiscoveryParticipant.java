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
package org.openhab.binding.upnpcontrol.internal.discovery;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
@Component(service = { UpnpDiscoveryParticipant.class })
@NonNullByDefault
public class UpnpControlDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        DiscoveryResult result = null;
        ThingUID thingUid = getThingUID(device);
        if (thingUid != null) {
            String label = device.getDetails().getFriendlyName().isEmpty() ? device.getDisplayString()
                    : device.getDetails().getFriendlyName();
            Map<String, Object> properties = new HashMap<>();
            properties.put("ipAddress", device.getIdentity().getDescriptorURL().getHost());
            properties.put("udn", device.getIdentity().getUdn().getIdentifierString());
            result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withProperties(properties)
                    .withRepresentationProperty("udn").build();
        }
        return result;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        ThingUID result = null;
        String deviceType = device.getType().getType();
        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        String model = device.getDetails().getModelDetails().getModelName();
        String serialNumber = device.getDetails().getSerialNumber();

        logger.debug("Device type {}, manufacturer {}, model {}, SN# {}", deviceType, manufacturer, model,
                serialNumber);

        if (deviceType.equalsIgnoreCase("MediaRenderer")) {
            this.logger.debug("Media renderer found: {}, {}", manufacturer, model);
            ThingTypeUID thingTypeUID = THING_TYPE_RENDERER;
            result = new ThingUID(thingTypeUID, device.getIdentity().getUdn().getIdentifierString());
        } else if (deviceType.equalsIgnoreCase("MediaServer")) {
            this.logger.debug("Media server found: {}, {}", manufacturer, model);
            ThingTypeUID thingTypeUID = THING_TYPE_SERVER;
            result = new ThingUID(thingTypeUID, device.getIdentity().getUdn().getIdentifierString());
        }
        return result;
    }
}
