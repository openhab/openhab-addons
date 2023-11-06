/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.discovery;

import static org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.lametrictime.internal.config.LaMetricTimeConfiguration.HOST;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LaMetricTimeDiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author Gregory Moyer - Initial contribution
 */
@Component
@NonNullByDefault
public class LaMetricTimeDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(LaMetricTimeDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_DEVICE);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(HOST, device.getIdentity().getDescriptorURL().getHost());

        String friendlyName = device.getDetails().getFriendlyName();
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(friendlyName)
                .build();

        logger.debug("Created a DiscoveryResult for device '{}' with serial number '{}'",
                device.getDetails().getModelDetails().getModelName(), device.getDetails().getSerialNumber());

        return result;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        try {
            String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
            String modelName = device.getDetails().getModelDetails().getModelName();

            if (!manufacturer.toUpperCase().contains("LAMETRIC")
                    || !modelName.toUpperCase().contains("LAMETRIC TIME")) {
                return null;
            }

            String serialNumber = device.getDetails().getSerialNumber();
            logger.debug("Discovered '{}' model '{}' thing with serial number '{}'",
                    device.getDetails().getFriendlyName(), modelName, serialNumber);

            return new ThingUID(THING_TYPE_DEVICE, serialNumber);
        } catch (Exception e) {
            // device was not what we expected
            logger.debug("Discovery hit an unexpected error", e);
            return null;
        }
    }
}
