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
package org.openhab.binding.lgwebos.internal.discovery;

import static org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.types.DeviceType;
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
@Component(service = UpnpDiscoveryParticipant.class, immediate = true, configurationPid = "discovery.lgwebos.upnp")
public class LGWebOSUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(LGWebOSUpnpDiscoveryParticipant.class);

    private static final DeviceType UPNP_MEDIARENDERER_DEVICE_TYPE = new DeviceType("schemas-upnp-org", "MediaRenderer",
            1);

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

        return DiscoveryResultBuilder.create(thingUID).withLabel(device.getDetails().getFriendlyName())
                .withProperty(PROPERTY_DEVICE_ID, device.getIdentity().getUdn().getIdentifierString())
                .withProperty(CONFIG_HOST, device.getIdentity().getDescriptorURL().getHost())
                .withLabel(device.getDetails().getFriendlyName())
                .withProperty(PROPERTY_MODEL_NAME, device.getDetails().getFriendlyName())
                .withProperty(PROPERTY_MANUFACTURER, device.getDetails().getManufacturerDetails().getManufacturer())
                .withRepresentationProperty(PROPERTY_DEVICE_ID).withThingType(THING_TYPE_WEBOSTV).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        logger.trace("Discovered remote device {}", device);
        if (UPNP_MEDIARENDERER_DEVICE_TYPE.equals(device.getType())
                && device.getDetails().getManufacturerDetails().getManufacturer() != null
                && device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase()
                        .contains("LG ELECTRONICS")
                && device.getDetails().getModelDetails().getModelDescription() != null
                && device.getDetails().getModelDetails().getModelDescription().toUpperCase().contains("WEBOSTV")) {
            logger.debug("Found LG WebOS TV: {}", device);
            return new ThingUID(THING_TYPE_WEBOSTV, device.getIdentity().getUdn().getIdentifierString());
        }
        return null;
    }

}
