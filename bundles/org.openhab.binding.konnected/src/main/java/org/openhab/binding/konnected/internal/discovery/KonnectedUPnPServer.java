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
package org.openhab.binding.konnected.internal.discovery;

import static org.openhab.binding.konnected.internal.KonnectedBindingConstants.*;

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
 * The {@link KonnectedUPnPServer} is responsible for discovering new
 * Konnectedmodules modules. It uses the central {@link UpnpDiscoveryService}.
 *
 * @author Zachary Christainsen - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class)
public class KonnectedUPnPServer implements UpnpDiscoveryParticipant {
    private Logger logger = LoggerFactory.getLogger(KonnectedUPnPServer.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_MODULE);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(HOST, device.getDetails().getBaseURL());
            properties.put(MAC_ADDR, device.getDetails().getSerialNumber());
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getDetails().getFriendlyName()).withRepresentationProperty(MAC_ADDR).build();
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ModelDetails modelDetails = details.getModelDetails();

            if (modelDetails != null) {
                String modelName = modelDetails.getModelName();
                logger.debug("Model Details: {} Url: {} UDN: {}  Model Number: {}", modelName, details.getBaseURL(),
                        details.getSerialNumber(), modelDetails.getModelNumber());
                if (modelName != null) {
                    if (modelName.startsWith("Konnected")) {
                        return new ThingUID(THING_TYPE_MODULE, details.getSerialNumber());
                    }
                }
            }
        }
        return null;
    }
}
