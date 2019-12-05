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
package org.openhab.binding.philipsair.internal.discovery;

import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.THING_TYPE_AC2889_10;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.THING_TYPE_UNIVERSAL;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.osgi.service.component.ComponentContext;
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
        if(!isAutoDiscoveryEnabled) {
            return null;
        }
        
        logger.info("Attempt to create things");
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            logger.info("Creating with uid {}", uid.getAsString());
            Map<String, Object> properties = new HashMap<>();
            properties.put(PhilipsAirConfiguration.CONFIG_HOST, device.getIdentity().getDescriptorURL().getHost());
            properties.put(PhilipsAirConfiguration.CONFIG_DEF_DEVICE_UUID,
                    device.getIdentity().getUdn().getIdentifierString());

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel("Philips " + device.getDetails().getModelDetails().getModelName() + " "
                            + device.getDetails().getModelDetails().getModelNumber())
                    .withRepresentationProperty(PhilipsAirConfiguration.CONFIG_DEF_DEVICE_UUID).build();

            logger.info("DiscoveryResult with uid {}", result.getThingUID().getAsString());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        ModelDetails modelDetails = null;
        String modelName = null;
        logger.info("Attempt to create things {}", device.toString());
        if (details == null || (modelDetails = details.getModelDetails()) == null
                || !PhilipsAirBindingConstants.DISCOVERY_UPNP_MODEL
                        .equalsIgnoreCase(modelName = modelDetails.getModelName())) {
            logger.info("Device not recognized {}", device.toString());
            return null;
        }

        if (PhilipsAirBindingConstants.SUPPORTED_MODEL_NUMBER_AC2889_10
                .startsWith(modelDetails.getModelNumber().toLowerCase())) {
            logger.info("Attempt to create Philips Air things {} {}", modelName, modelDetails.getModelNumber());
            return new ThingUID(THING_TYPE_AC2889_10, device.getIdentity().getUdn().getIdentifierString());
        } else {
            logger.info("Attempt to create Philips Air things {} {}", modelName, modelDetails.getModelNumber());
            return new ThingUID(THING_TYPE_UNIVERSAL, device.getIdentity().getUdn().getIdentifierString());
        }
    }
}
