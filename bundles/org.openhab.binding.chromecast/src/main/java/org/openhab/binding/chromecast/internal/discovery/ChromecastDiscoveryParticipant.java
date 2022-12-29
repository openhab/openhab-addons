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
package org.openhab.binding.chromecast.internal.discovery;

import static org.openhab.binding.chromecast.internal.ChromecastBindingConstants.*;

import java.net.Inet4Address;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChromecastDiscoveryParticipant} is responsible for discovering Chromecast devices through mDNS.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Daniel Walters - Change discovery protocol to mDNS
 * @author Christoph Weitkamp - Use "discovery.chromecast:background=false" to disable discovery service
 */
@Component(configurationPid = "discovery.chromecast")
@NonNullByDefault
public class ChromecastDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ChromecastDiscoveryParticipant.class);

    private static final String PROPERTY_MODEL = "md";
    private static final String PROPERTY_FRIENDLY_NAME = "fn";
    private static final String PROPERTY_DEVICE_ID = "id";
    private static final String SERVICE_TYPE = "_googlecast._tcp.local.";

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
        String autoDiscoveryPropertyValue = (String) properties
                .get(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY);
        if (autoDiscoveryPropertyValue != null && !autoDiscoveryPropertyValue.isBlank()) {
            isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        if (isAutoDiscoveryEnabled) {
            ThingUID uid = getThingUID(service);
            if (uid != null) {
                Inet4Address[] addresses = service.getInet4Addresses();
                if (addresses.length == 0) {
                    return null;
                }
                String host = addresses[0].getHostAddress();
                int port = service.getPort();
                logger.debug("Chromecast Found: {} {}", host, port);
                String id = service.getPropertyString(PROPERTY_DEVICE_ID);
                String friendlyName = service.getPropertyString(PROPERTY_FRIENDLY_NAME); // friendly name;
                return DiscoveryResultBuilder.create(uid).withThingType(getThingType(service))
                        .withProperties(Map.of(HOST, host, PORT, port, DEVICE_ID, id))
                        .withRepresentationProperty(DEVICE_ID).withLabel(friendlyName).build();
            }
        }
        return null;
    }

    private @Nullable ThingTypeUID getThingType(final ServiceInfo service) {
        String model = service.getPropertyString(PROPERTY_MODEL); // model
        logger.debug("Chromecast Type: {}", model);
        if (model == null) {
            return null;
        }
        switch (model) {
            case "Chromecast Audio":
                return THING_TYPE_AUDIO;
            case "Google Cast Group":
                return THING_TYPE_AUDIOGROUP;
            default:
                return THING_TYPE_CHROMECAST;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        ThingTypeUID thingTypeUID = getThingType(service);
        if (thingTypeUID != null) {
            String id = service.getPropertyString(PROPERTY_DEVICE_ID); // device id
            return new ThingUID(thingTypeUID, id);
        }
        return null;
    }
}
