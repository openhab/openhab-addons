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
package org.openhab.binding.magentatv.internal.discovery;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;
import static org.openhab.binding.magentatv.internal.MagentaTVUtil.*;
import static org.openhab.core.thing.Thing.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
 * The {@link MagentaTVDiscoveryParticipant} is responsible for discovering new
 * and removed MagentaTV receivers. It uses the central UpnpDiscoveryService.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class)
public class MagentaTVDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_RECEIVER);
    }

    /**
     * New discovered result.
     */
    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        DiscoveryResult result = null;
        try {
            String modelName = getString(device.getDetails().getModelDetails().getModelName()).toUpperCase();
            String manufacturer = getString(device.getDetails().getManufacturerDetails().getManufacturer())
                    .toUpperCase();
            logger.trace("Device discovered: {} - {}", manufacturer, modelName);

            ThingUID uid = getThingUID(device);
            if (uid != null) {
                logger.debug("Discovered a MagentaTV Media Receiver {}, UDN: {}, Model {}.{}",
                        device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString(),
                        modelName, device.getDetails().getModelDetails().getModelNumber());

                Map<String, Object> properties = new TreeMap<>();
                String descriptorURL = device.getIdentity().getDescriptorURL().toString();
                String port = substringBefore(substringAfterLast(descriptorURL, ":"), "/");
                String hex = device.getIdentity().getUdn().getIdentifierString()
                        .substring(device.getIdentity().getUdn().getIdentifierString().length() - 12);
                String mac = hex.substring(0, 2) + ":" + hex.substring(2, 4) + ":" + hex.substring(4, 6) + ":"
                        + hex.substring(6, 8) + ":" + hex.substring(8, 10) + ":" + hex.substring(10, 12);
                if (port.equals("49153")) { // MR400 reports the rong
                    port = MR400_DEF_REMOTE_PORT;
                }
                properties.put(PROPERTY_VENDOR, VENDOR + "(" + manufacturer + ")");
                properties.put(PROPERTY_MODEL_ID, modelName);
                properties.put(PROPERTY_HARDWARE_VERSION, device.getDetails().getModelDetails().getModelNumber());
                properties.put(PROPERTY_MAC_ADDRESS, mac);
                properties.put(PROPERTY_UDN, device.getIdentity().getUdn().getIdentifierString().toUpperCase());
                properties.put(PROPERTY_IP, substringBetween(descriptorURL, "http://", ":"));
                properties.put(PROPERTY_PORT, port);
                properties.put(PROPERTY_DESC_URL, substringAfterLast(descriptorURL, ":" + port));

                logger.debug("Create Thing for device {} with UDN {}, Model{}", device.getDetails().getFriendlyName(),
                        device.getIdentity().getUdn().getIdentifierString(), modelName);
                result = DiscoveryResultBuilder.create(uid).withLabel(device.getDetails().getFriendlyName())
                        .withProperties(properties).withRepresentationProperty(PROPERTY_MAC_ADDRESS).build();
            }
        } catch (RuntimeException e) {
            logger.debug("Unable to create thing for device {}/{} - {}", device.getDetails().getFriendlyName(),
                    device.getIdentity().getUdn().getIdentifierString(), e.getMessage());
        }
        return result;
    }

    /**
     * Get the UID for a device
     */
    @Override
    public @Nullable ThingUID getThingUID(@Nullable RemoteDevice device) {
        if (device != null) {
            String manufacturer = getString(device.getDetails().getManufacturerDetails().getManufacturer())
                    .toUpperCase();
            String model = device.getDetails().getModelDetails().getModelName().toUpperCase();
            if (manufacturer.contains(OEM_VENDOR) && ((model.contains(MODEL_MR400) || model.contains(MODEL_MR401B)
                    || model.contains(MODEL_MR601) || model.contains(MODEL_MR201)))) {
                return new ThingUID(THING_TYPE_RECEIVER, device.getIdentity().getUdn().getIdentifierString());
            }
        }
        return null;
    }

    private String getString(@Nullable String value) {
        return value != null ? value : "";
    }
}
