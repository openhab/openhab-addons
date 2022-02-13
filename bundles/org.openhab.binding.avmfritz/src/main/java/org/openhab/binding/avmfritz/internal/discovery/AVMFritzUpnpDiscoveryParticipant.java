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
package org.openhab.binding.avmfritz.internal.discovery;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;
import static org.openhab.core.thing.Thing.PROPERTY_VENDOR;

import java.util.Dictionary;
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
import org.openhab.core.config.discovery.upnp.internal.UpnpDiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AVMFritzUpnpDiscoveryParticipant} is responsible for discovering new and removed FRITZ!Box devices. It
 * uses the central {@link UpnpDiscoveryService}.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 * @author Christoph Weitkamp - Use "discovery.avmfritz:background=false" to disable discovery service
 */
@Component(configurationPid = "discovery.avmfritz")
@NonNullByDefault
public class AVMFritzUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzUpnpDiscoveryParticipant.class);

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
        String autoDiscoveryPropertyValue = (String) properties.get("background");
        if (autoDiscoveryPropertyValue != null && autoDiscoveryPropertyValue.length() != 0) {
            isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        if (isAutoDiscoveryEnabled) {
            ThingUID uid = getThingUID(device);
            if (uid != null) {
                logger.debug("discovered: {} ({}) at {}", device.getDisplayString(),
                        device.getDetails().getFriendlyName(), device.getIdentity().getDescriptorURL().getHost());
                return DiscoveryResultBuilder.create(uid)
                        .withProperties(Map.of(CONFIG_IP_ADDRESS, device.getIdentity().getDescriptorURL().getHost(),
                                PROPERTY_VENDOR, device.getDetails().getManufacturerDetails().getManufacturer()))
                        .withLabel(device.getDetails().getFriendlyName()).withRepresentationProperty(CONFIG_IP_ADDRESS)
                        .build();
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        // newer FRITZ!OS versions return several upnp services (e.g. Mediaserver)
        if (device.getType().getType().equals(BRIDGE_FRITZBOX)) {
            DeviceDetails details = device.getDetails();
            if (details != null) {
                ModelDetails modelDetails = details.getModelDetails();
                if (modelDetails != null) {
                    String modelName = modelDetails.getModelName();
                    if (modelName != null) {
                        // It would be better to use udn but in my case FB is discovered twice
                        // .getIdentity().getUdn().getIdentifierString()
                        String id = device.getIdentity().getDescriptorURL().getHost().replaceAll(INVALID_PATTERN, "_");
                        if (modelName.startsWith(BOX_MODEL_NAME)) {
                            logger.debug("discovered on {}", device.getIdentity().getDiscoveredOnLocalAddress());
                            return new ThingUID(BRIDGE_THING_TYPE, id);
                        } else if (POWERLINE546E_MODEL_NAME.equals(modelName)) {
                            logger.debug("discovered on {}", device.getIdentity().getDiscoveredOnLocalAddress());
                            return new ThingUID(POWERLINE546E_STANDALONE_THING_TYPE, id);
                        }
                    }
                }
            }
        }
        return null;
    }
}
