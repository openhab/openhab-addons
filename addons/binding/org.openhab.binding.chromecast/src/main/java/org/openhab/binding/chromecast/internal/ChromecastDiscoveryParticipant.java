/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.openhab.binding.chromecast.ChromecastBindingConstants;

/**
 * The {@link ChromecastDiscoveryParticipant} is responsible for discovering Chromecast devices through UPnP.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class ChromecastDiscoveryParticipant implements UpnpDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return ChromecastBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public DiscoveryResult createResult(final RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        final Map<String, Object> properties = new HashMap<>(2);
        properties.put(ChromecastBindingConstants.HOST, device.getDetails().getBaseURL().getHost());
        properties.put(ChromecastBindingConstants.SERIAL_NUMBER, device.getDetails().getSerialNumber());

        final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withThingType(getThingType(device))
                .withProperties(properties).withLabel(device.getDetails().getFriendlyName())
                .withRepresentationProperty(ChromecastBindingConstants.SERIAL_NUMBER).build();
        return result;
    }

    @Override
    public ThingUID getThingUID(final RemoteDevice device) {
        final RemoteDeviceIdentity identity = device.getIdentity();
        final DeviceDetails deviceDetails = device.getDetails();
        final ManufacturerDetails manufacturerDetails = deviceDetails.getManufacturerDetails();

        if (manufacturerDetails == null || !manufacturerDetails.getManufacturer().equals("Google Inc.")) {
            return null;
        }

        ThingTypeUID thingTypeUID = getThingType(device);
        if (thingTypeUID != null) {
            return new ThingUID(thingTypeUID, identity.getUdn().getIdentifierString());
        } else {
            return null;
        }
    }

    private ThingTypeUID getThingType(final RemoteDevice device) {
        final DeviceDetails deviceDetails = device.getDetails();
        if (deviceDetails != null) {
            final ModelDetails modelDetails = deviceDetails.getModelDetails();
            if (modelDetails != null) {
                if (modelDetails.getModelName().equals("Chromecast Audio")) {
                    return ChromecastBindingConstants.THING_TYPE_AUDIO;
                } else if (modelDetails.getModelName().equals("Eureka Dongle")) {
                    return ChromecastBindingConstants.THING_TYPE_CHROMECAST;
                }
            }
        }
        return null;
    }

}
