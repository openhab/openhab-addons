/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhilipsAirUpnpDiscoveryParticipant} is responsible for discovering
 * new Philips Air Purifier things
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Match model numbers with region suffix
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, configurationPid = "discovery.philipsair", immediate = true)
public class PhilipsAirUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirUpnpDiscoveryParticipant.class);
    private boolean isAutoDiscoveryEnabled = true;

    @Activate
    protected void activate(Map<String, Object> properties) {
        activateOrModifyService(properties);
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        activateOrModifyService(properties);
    }

    private void activateOrModifyService(Map<String, Object> properties) {
        // 'enableAutoDiscovery' is kept for backwards compatibility
        Boolean legacyEnabled = ConfigParser.valueAs(properties.get("enableAutoDiscovery"), Boolean.class);
        isAutoDiscoveryEnabled = ConfigParser.valueAsOrElse(
                properties.get(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY), Boolean.class,
                legacyEnabled != null ? legacyEnabled : true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_UPNP_THING_TYPES_UIDS;
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
        String modelName = modelDetails != null ? modelDetails.getModelName() : null;

        if (modelDetails == null || !PhilipsAirBindingConstants.DISCOVERY_UPNP_MODEL.equalsIgnoreCase(modelName)) {
            logger.trace("Device not recognized {}", device.toString());
            return null;
        }

        String modelNumber = modelDetails.getModelNumber();
        ThingTypeUID thingType = getThingType(modelNumber);
        logger.debug("Attempt to create Philips Air things {} {}", modelName, modelNumber);
        return new ThingUID(thingType, device.getIdentity().getUdn().getIdentifierString());
    }

    /**
     * Maps the UPnP model number to a thing type. Devices report the model number with or without the region suffix
     * (e.g. 'AC2889' or 'AC2889/10'), so both are matched against the thing type id (e.g. 'ac2889-10').
     */
    static ThingTypeUID getThingType(@Nullable String modelNumber) {
        String model = modelNumber != null ? modelNumber.toLowerCase(Locale.ROOT).replace('/', '-') : "";
        if (!model.isEmpty()) {
            for (ThingTypeUID thingType : List.of(THING_TYPE_AC2889_10, THING_TYPE_AC1214_10, THING_TYPE_AC2729,
                    THING_TYPE_AC3829_10)) {
                String thingTypeId = thingType.getId();
                if (thingTypeId.startsWith(model) || model.startsWith(thingTypeId)) {
                    return thingType;
                }
            }
        }
        return THING_TYPE_UNIVERSAL;
    }

    private static void addProperty(Map<String, Object> properties, String key, @Nullable String value) {
        properties.put(key, value != null ? value : "");
    }
}
