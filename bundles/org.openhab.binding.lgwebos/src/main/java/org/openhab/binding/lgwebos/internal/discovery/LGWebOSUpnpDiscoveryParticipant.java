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
package org.openhab.binding.lgwebos.internal.discovery;

import static org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * This Upnp Discovery participant add the ability to auto discover LG Web OS devices on the network.
 * Some users choose to not use upnp. Therefore this can only play an optional role and help discover the device and its
 * ip.
 *
 * @author Sebastian Prehn - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, configurationPid = "discovery.lgwebos.upnp")
public class LGWebOSUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(LGWebOSUpnpDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID thingUID = getThingUID(device);

        if (thingUID == null) {
            return null;
        }

        String modelName = device.getDetails().getModelDetails().getModelName();
        if (device.getDetails().getModelDetails().getModelNumber() != null) {
            modelName += " " + device.getDetails().getModelDetails().getModelNumber();
        }

        return DiscoveryResultBuilder.create(thingUID).withLabel(device.getDetails().getFriendlyName())
                .withProperty(PROPERTY_DEVICE_ID, device.getIdentity().getUdn().getIdentifierString())
                .withProperty(CONFIG_HOST, device.getIdentity().getDescriptorURL().getHost())
                .withProperty(Thing.PROPERTY_MODEL_ID, modelName)
                .withProperty(Thing.PROPERTY_VENDOR, device.getDetails().getManufacturerDetails().getManufacturer())
                .withRepresentationProperty(PROPERTY_DEVICE_ID).withThingType(THING_TYPE_WEBOSTV).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        logger.trace("Discovered remote device {}", device);
        if (device.findService(UPNP_SERVICE_TYPE) != null) {
            logger.debug("Found LG WebOS TV: {}", device);
            return new ThingUID(THING_TYPE_WEBOSTV, device.getIdentity().getUdn().getIdentifierString());
        }
        return null;
    }
}
