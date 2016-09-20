/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.sony.internal.bravia.BraviaConstants;

/**
 * This implementation of the {@link UpnpDiscoveryParticipant} provides discovery of Sony Bravia TVs
 *
 * @author Tim Roberts
 * @version $Id: $Id
 */
public class BraviaDiscoveryParticipant implements UpnpDiscoveryParticipant {

    /**
     * Returns the set of supported things {@link BraviaConstants#THING_TYPE_BRAVIA}
     *
     * @return a singleton set to the bravia thing type
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(BraviaConstants.THING_TYPE_BRAVIA);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);
            properties.put("ipAddress", device.getIdentity().getDescriptorURL().getHost());

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getDetails().getFriendlyName()).build();
            return result;
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link ThingUID} representing a Sony Bravia device or null if the {@link RemoteDevice} is not a Sony
     * Bravia device
     *
     * @param device a possibly null device to check
     * @return a {@link ThingUID} for the device or null if it's not a sony bravia device
     */
    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device == null) {
            return null;
        }

        final DeviceDetails details = device.getDetails();

        if (details != null) {
            final String manufacturer = details.getManufacturerDetails() == null ? null
                    : details.getManufacturerDetails().getManufacturer();
            final String modelDescription = details.getModelDetails() == null ? null
                    : details.getModelDetails().getModelDescription();

            if (manufacturer == null || modelDescription == null) {
                return null;
            }

            if (manufacturer.toLowerCase().contains("sony") && modelDescription.toLowerCase().equals("bravia")) {
                final String identity = device.getIdentity() == null ? "" : device.getIdentity().toString();
                final int idx = identity.indexOf("uuid:");
                final int idx2 = idx < 0 ? -1 : identity.indexOf(",", idx + 5);
                if (idx2 > 0) {
                    return new ThingUID(BraviaConstants.THING_TYPE_BRAVIA, identity.substring(idx + 5, idx2));
                }
            }
        }
        return null;
    }

}
