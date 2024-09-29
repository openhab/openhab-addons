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
package org.openhab.binding.yamahareceiver.internal.discovery;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Configs.CONFIG_HOST_NAME;

import java.net.URL;
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
 * The {@link YamahaDiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Introduced config object, migrated to newer UPnP api
 */
@Component
@NonNullByDefault
public class YamahaDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(YamahaDiscoveryParticipant.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(BRIDGE_THING_TYPE);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        Map<String, Object> properties = new HashMap<>(3);

        String label = "Yamaha Receiver";
        try {
            label += " " + device.getDetails().getModelDetails().getModelName();
        } catch (Exception e) {
            // ignore and use the default label
        }

        URL url = device.getIdentity().getDescriptorURL();
        properties.put(CONFIG_HOST_NAME, url.getHost());

        // The port via UPNP is unreliable, sometimes it is 8080, on some models 49154.
        // But so far the API was always reachable via port 80.
        // We provide the port config therefore, if the user ever needs to adjust the port.
        // Note the port is set in the thing-types.xml to 80 by default.

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withTTL(MIN_MAX_AGE_SECS).withProperties(properties)
                .withLabel(label).withRepresentationProperty(CONFIG_HOST_NAME).build();

        logger.debug("Discovered a Yamaha Receiver '{}' model '{}' thing with UDN '{}'",
                device.getDetails().getFriendlyName(), device.getDetails().getModelDetails().getModelName(),
                device.getIdentity().getUdn().getIdentifierString());

        return result;
    }

    public static @Nullable ThingUID getThingUID(@Nullable String manufacturer, @Nullable String deviceType,
            String udn) {
        if (manufacturer == null || deviceType == null) {
            return null;
        }

        if (manufacturer.toUpperCase().contains(UPNP_MANUFACTURER) && deviceType.equals(UPNP_TYPE)) {
            return new ThingUID(BRIDGE_THING_TYPE, udn);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        String deviceType = device.getType().getType();

        // UDN shouldn't contain '-' characters.
        return getThingUID(manufacturer, deviceType,
                device.getIdentity().getUdn().getIdentifierString().replace("-", "_"));
    }
}
