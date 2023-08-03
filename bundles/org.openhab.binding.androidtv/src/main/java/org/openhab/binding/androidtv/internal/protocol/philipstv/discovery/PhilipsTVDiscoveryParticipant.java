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
package org.openhab.binding.androidtv.internal.protocol.philipstv.discovery;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.DEFAULT_PORT;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.HOST;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.PORT;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.THING_TYPE_PHILIPS_TV;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
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
 * The {@link PhilipsTVDiscoveryParticipant} is responsible for discovering Philips TV devices through UPnP.
 *
 * @author Benjamin Meyer - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class PhilipsTVDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_PHILIPS_TV);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        final Map<String, Object> properties = new HashMap<>(2);
        String host = device.getIdentity().getDescriptorURL().getHost();
        properties.put(HOST, host);
        properties.put(PORT, DEFAULT_PORT);
        logger.debug("Philips TV Found: {}, using default port {}", host, DEFAULT_PORT);
        String friendlyName = device.getDetails().getFriendlyName();
        if (friendlyName.length() > 0 && Character.isDigit(friendlyName.charAt(0))) {
            friendlyName = "_" + friendlyName; // label must not start with a digit
        }

        return DiscoveryResultBuilder.create(uid).withThingType(THING_TYPE_PHILIPS_TV).withProperties(properties)
                .withLabel(friendlyName).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ModelDetails modelDetails = details.getModelDetails();
            if (modelDetails != null) {
                String modelName = modelDetails.getModelName();
                String modelDescription = modelDetails.getModelDescription();
                if (modelName != null && modelDescription != null) {
                    if (modelName.contains("Philips TV")) {
                        logger.debug("Device found: {} with desc {}", modelName, modelDescription);
                        // One Philips TV contains several UPnP devices.
                        // Create unique Philips TV thing for every Media Renderer
                        // device and ignore rest of the UPnP devices.
                        if (modelDescription.contains("Media")) {
                            // UDN shouldn't contain '-' characters.
                            String udn = device.getIdentity().getUdn().getIdentifierString().replace("-", "_");
                            logger.debug("Discovered a Philips TV '{}' model '{}' thing with UDN '{}'",
                                    device.getDetails().getFriendlyName(), modelName, udn);

                            return new ThingUID(THING_TYPE_PHILIPS_TV, udn);
                        }
                    }
                }
            }
        }
        return null;
    }
}
