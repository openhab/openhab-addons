/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal.discovery;

import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhilipsAirUpnpDiscoveryParticipantTest} is responsible for discovering
 * new Philips Air Purifier things
 *
 * @author Michał Boroński - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class PhilipsAirUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirUpnpDiscoveryParticipant.class);
    private boolean isAutoDiscoveryEnabled = true;

    @Activate
    protected void activate(ComponentContext componentContext) {
        activateOrModifyService(componentContext);
    }

    @Modified
    protected void modified(ComponentContext componentContext) {
        activateOrModifyService(componentContext);
    }

    private void activateOrModifyService(ComponentContext componentContext) {
        Dictionary<String, @Nullable Object> properties = componentContext.getProperties();
        String autoDiscoveryPropertyValue = (String) properties.get("enableAutoDiscovery");
        if (autoDiscoveryPropertyValue != null && autoDiscoveryPropertyValue.length() != 0) {
            isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        if (!isAutoDiscoveryEnabled) {
            return null;
        }

        ThingUID uid = getThingUID(device);
        if (uid != null) {
            logger.trace("Creating with uid {}", uid.getAsString());
            Map<String, Object> properties = new HashMap<>();
            RemoteDeviceIdentity identity = device.getIdentity();
            if (identity != null) {
                addProperty(properties, PhilipsAirConfiguration.CONFIG_HOST, identity.getDescriptorURL().getHost());
                String idString = identity.getUdn().getIdentifierString();
                if (idString != null) {
                    addProperty(properties, PhilipsAirConfiguration.CONFIG_DEF_DEVICE_UUID, idString);
                    int macIndex = idString.lastIndexOf('-');
                    if (macIndex > 0) {
                        addProperty(properties, PROPERTY_MAC_ADDRESS,
                                idString.substring(idString.lastIndexOf('-') + 1));
                    }
                }
            }

            DeviceDetails details = device.getDetails();
            String label = "Philips AirPurifier";
            if (details != null) {
                addProperty(properties, PROPERTY_MANUFACTURER, details.getManufacturerDetails().getManufacturer());
                ModelDetails modelDetails = device.getDetails().getModelDetails();
                if (modelDetails != null) {
                    addProperty(properties, PROPERTY_VENDOR, PhilipsAirBindingConstants.VENDOR);
                    addProperty(properties, PROPERTY_MODEL_ID, modelDetails.getModelNumber());
                    addProperty(properties, PROPERTY_DEV_TYPE, modelDetails.getModelName());
                    label = String.format("Philips %s %s", modelDetails.getModelName(), modelDetails.getModelNumber());
                }
            }

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(PhilipsAirConfiguration.CONFIG_DEF_DEVICE_UUID).build();

            logger.debug("DiscoveryResult with uid {} label : {} ", result.getThingUID().getAsString(),
                    result.getLabel());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        ModelDetails modelDetails = details != null ? details.getModelDetails() : null;
        String modelName = modelDetails != null ? modelDetails.getModelName().toLowerCase() : null;

        logger.info("Device {} {}  ?recognized {}", modelDetails, modelName, device.toString());

        if (details == null || modelDetails == null
                || !PhilipsAirBindingConstants.DISCOVERY_UPNP_MODEL.equalsIgnoreCase(modelName)) {

            logger.trace("Device not recognized {}", device.toString());
            return null;
        }

        String modelNumber = modelDetails.getModelNumber().toLowerCase();
        ThingTypeUID thingType = THING_TYPE_UNIVERSAL;
        if (PhilipsAirBindingConstants.SUPPORTED_MODEL_NUMBER_AC2889_10.startsWith(modelNumber)) {
            thingType = THING_TYPE_AC2889_10;
        } else if (PhilipsAirBindingConstants.SUPPORTED_MODEL_NUMBER_AC1214_10.startsWith(modelNumber)) {
            thingType = THING_TYPE_AC1214_10;
        } else if (PhilipsAirBindingConstants.SUPPORTED_MODEL_NUMBER_AC2729.startsWith(modelNumber)) {
            thingType = THING_TYPE_AC2729;
        } else if (PhilipsAirBindingConstants.SUPPORTED_MODEL_NUMBER_AC3829_10.startsWith(modelNumber)) {
            thingType = THING_TYPE_AC3829_10;
        } else {
            thingType = THING_TYPE_UNIVERSAL;
        }

        logger.debug("Attempt to create Philips Air things {} {}", modelName, modelDetails.getModelNumber());
        return new ThingUID(thingType, device.getIdentity().getUdn().getIdentifierString());
    }

    private static void addProperty(Map<String, Object> properties, String key, @Nullable String value) {
        properties.put(key, value != null ? value : "");
    }
}
