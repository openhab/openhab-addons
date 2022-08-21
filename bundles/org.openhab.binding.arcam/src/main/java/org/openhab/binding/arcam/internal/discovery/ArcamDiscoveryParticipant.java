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
package org.openhab.binding.arcam.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.arcam.internal.ArcamBindingConstants;
import org.openhab.binding.arcam.internal.config.ArcamConfiguration;
import org.openhab.binding.arcam.internal.devices.ArcamDeviceUtil;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The{@link ArcamDiscoveryParticipant} is responsible processing the
 * results of searches for UPNP devices
 *
 * @author Joep Admiraal - Initial contribution
 */
@Component
@NonNullByDefault
public class ArcamDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ArcamDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return ArcamBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    @Nullable
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        Map<String, Object> properties = new HashMap<>(3);
        String model = device.getDetails().getModelDetails().getModelName();
        String ipAddress = device.getIdentity().getDescriptorURL().getHost();
        String serial = device.getDetails().getSerialNumber();
        String name = device.getDetails().getFriendlyName();

        properties.put(ArcamConfiguration.HOSTNAME, ipAddress);
        properties.put(ArcamConfiguration.SERIAL, serial);
        properties.put(ArcamConfiguration.NAME, name);
        properties.put(ArcamConfiguration.MODEL, model);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(name)
                .withRepresentationProperty(ArcamConfiguration.SERIAL).build();

        logger.debug("Created a DiscoveryResult for device {} with UDN {}, IP: {}, serial: {}",
                device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString(), ipAddress,
                serial);
        return result;
    }

    @Override
    @Nullable
    public ThingUID getThingUID(RemoteDevice device) {
        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        if (manufacturer == null || !manufacturer.toUpperCase().equals("HARMAN LUXURY AUDIO")) {
            return null;
        }

        String modelName = device.getDetails().getModelDetails().getModelName();
        ThingTypeUID thingTypeUID = ArcamDeviceUtil.getThingTypeUIDFromModelName(modelName);
        if (thingTypeUID == null) {
            return null;
        }

        String serial = device.getDetails().getSerialNumber();

        logger.debug("Discovered an Arcam '{}' thing with serial {}", thingTypeUID, serial);
        return new ThingUID(thingTypeUID, serial);
    }
}
