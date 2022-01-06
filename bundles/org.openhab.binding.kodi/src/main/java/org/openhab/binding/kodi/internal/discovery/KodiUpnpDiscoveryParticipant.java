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
package org.openhab.binding.kodi.internal.discovery;

import static org.openhab.binding.kodi.internal.KodiBindingConstants.*;

import java.util.Dictionary;
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
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An UpnpDiscoveryParticipant which allows to discover Kodi AVRs.
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Use "discovery.kodi:background=false" to disable discovery service
 */
@Component(configurationPid = "discovery.kodi")
@NonNullByDefault
public class KodiUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(KodiUpnpDiscoveryParticipant.class);

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
        if (autoDiscoveryPropertyValue != null && !autoDiscoveryPropertyValue.isEmpty()) {
            isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        if (isAutoDiscoveryEnabled) {
            ThingUID thingUid = getThingUID(device);
            if (thingUid != null) {
                String friendlyName = device.getDetails().getFriendlyName();
                String label = friendlyName == null || friendlyName.isEmpty() ? device.getDisplayString()
                        : friendlyName;
                Map<String, Object> properties = new HashMap<>();
                properties.put(HOST_PARAMETER, device.getIdentity().getDescriptorURL().getHost());

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUid).withLabel(label)
                        .withProperties(properties).withRepresentationProperty(HOST_PARAMETER).build();

                return result;
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        if (containsIgnoreCase(manufacturer, MANUFACTURER)) {
            logger.debug("Manufacturer matched: search: {}, device value: {}.", MANUFACTURER, manufacturer);
            String type = device.getType().getType();
            if (containsIgnoreCase(type, UPNP_DEVICE_TYPE)) {
                logger.debug("Device type matched: search: {}, device value: {}.", UPNP_DEVICE_TYPE, type);
                return new ThingUID(THING_TYPE_KODI, device.getIdentity().getUdn().getIdentifierString());
            }
        }
        return null;
    }

    private boolean containsIgnoreCase(final @Nullable String str, final String searchStr) {
        return str != null && str.toLowerCase().contains(searchStr.toLowerCase());
    }
}
