/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.magentatv.internal.MagentaTVHandler;
import org.openhab.binding.magentatv.internal.MagentaTVHandlerFactory;
import org.openhab.binding.magentatv.internal.utils.MagentaTVLogger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The {@link MagentaTVDiscoveryParticipant} is responsible for discovering new
 * and removed MagentaTV receivers. It uses the central UpnpDiscoveryService.
 *
 * @author Hans-Jörg Merk - Initial contribution (hmerck)
 * @author Markus Michels - Discovery more attributes, which allow to
 *         differentiate various models (markus7017)
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class MagentaTVDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final MagentaTVLogger logger = new MagentaTVLogger(MagentaTVHandler.class, "Discovery");
    private @Nullable MagentaTVHandlerFactory handlerFactory;

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_RECEIVER);
    }

    /**
     * New discovered result.
     */
    @Override
    @SuppressWarnings("null")
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        DiscoveryResult result = null;
        try {
            logger.trace("Device discovered: {0} - {1}", device.getDetails().getManufacturerDetails().getManufacturer(),
                    device.getDetails().getModelDetails().getModelName());
            if (handlerFactory == null) {
                logger.fatal("handlerFactory not yet initialized!");
                return null;
            }

            ThingUID uid = getThingUID(device);
            if (uid != null) {
                logger.debug("Discovered an MagentaTV Media Receiver {0}, UDN: {1}, Model {2}.{3}",
                        device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString(),
                        device.getDetails().getModelDetails().getModelName(),
                        device.getDetails().getModelDetails().getModelNumber());
                Map<String, Object> properties = new HashMap<>();
                properties.put(PROPERTY_VENDOR,
                        VENDOR + "(" + device.getDetails().getManufacturerDetails().getManufacturer() + ")");
                properties.put(PROPERTY_MODEL_ID, device.getDetails().getModelDetails().getModelName().toUpperCase());
                properties.put(PROPERTY_HARDWARE_VERSION, device.getDetails().getModelDetails().getModelNumber());
                properties.put(PROPERTY_SERIAL_NUMBER, device.getDetails().getSerialNumber());
                properties.put(PROPERTY_UDN, device.getIdentity().getUdn().getIdentifierString().toUpperCase());
                String descriptorURL = device.getIdentity().getDescriptorURL().toString();
                properties.put(PROPERTY_IP, StringUtils.substringBetween(descriptorURL, "http://", ":"));
                String port = StringUtils.substringBefore(StringUtils.substringAfterLast(descriptorURL, ":"), "/");
                properties.put(PROPERTY_PORT, port);
                properties.put(PROPERTY_DESC_URL, StringUtils.substringAfterLast(descriptorURL, ":" + port));

                String hex = device.getIdentity().getUdn().getIdentifierString()
                        .substring(device.getIdentity().getUdn().getIdentifierString().length() - 12);
                String mac = hex.substring(0, 2) + ":" + hex.substring(2, 4) + ":" + hex.substring(4, 6) + ":"
                        + hex.substring(6, 8) + ":" + hex.substring(8, 10) + ":" + hex.substring(10, 12);
                properties.put(PROPERTY_MAC_ADDRESS, mac);

                logger.debug("Create Thing for device {0} with UDN {1}, Model{2}",
                        device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString(),
                        device.getDetails().getModelDetails().getModelName());
                // TO-DO: Check if thing with this name already exist
                result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withLabel(device.getDetails().getFriendlyName()).withRepresentationProperty(PROPERTY_UDN)
                        .build();
                Validate.notNull(handlerFactory);
                handlerFactory.deviceDiscoverd(properties);
            }
        } catch (Exception e) {
            logger.exception(e, "Unable to create thing for device {0}/{1}", device.getDetails().getFriendlyName(),
                    device.getIdentity().getUdn().getIdentifierString());
        }
        return result;
    }

    /**
     * Get the UID for a device
     */
    @SuppressWarnings("null")
    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (device != null) {
            if (device.getDetails().getManufacturerDetails().getManufacturer() != null) {
                String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase();
                if (manufacturer.contains(OEM_VENDOR)) {
                    if (device.getDetails().getModelDetails().getModelName() != null) {
                        String model = device.getDetails().getModelDetails().getModelName().toUpperCase();
                        if (model.contains(MODEL_MR400) || model.contains(MODEL_MR401B)
                                || model.contains(MODEL_MR201)) {
                            return new ThingUID(THING_TYPE_RECEIVER,
                                    device.getIdentity().getUdn().getIdentifierString());
                        }
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("null")
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setMagentaTVHandlerFactory(MagentaTVHandlerFactory handlerFactory) {
        if (handlerFactory != null) {
            this.handlerFactory = handlerFactory;
            logger.debug("HandlerFactory bound to MagentaTVDiscoveryParticipant");
        }
    }

    public void unsetMagentaTVHandlerFactory(MagentaTVHandlerFactory handlerFactory) {
        this.handlerFactory = null;
    }
}
