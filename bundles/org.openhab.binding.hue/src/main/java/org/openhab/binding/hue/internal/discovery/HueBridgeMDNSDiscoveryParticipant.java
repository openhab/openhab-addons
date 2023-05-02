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
package org.openhab.binding.hue.internal.discovery;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.config.discovery.mdns.internal.MDNSDiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueBridgeMDNSDiscoveryParticipant} is responsible for discovering new and removed Hue Bridges. It uses the
 * central {@link MDNSDiscoveryService}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Thomas Höfer - Added representation
 * @author Christoph Weitkamp - Change discovery protocol to mDNS
 */
@Component(configurationPid = "discovery.hue")
@NonNullByDefault
public class HueBridgeMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_hue._tcp.local.";
    private static final String MDNS_PROPERTY_BRIDGE_ID = "bridgeid";
    private static final String MDNS_PROPERTY_MODEL_ID = "modelid";

    private final Logger logger = LoggerFactory.getLogger(HueBridgeMDNSDiscoveryParticipant.class);

    private long removalGracePeriod = 0L;

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
        String removalGracePeriodPropertyValue = (String) properties.get(REMOVAL_GRACE_PERIOD);
        if (removalGracePeriodPropertyValue != null && !removalGracePeriodPropertyValue.isBlank()) {
            try {
                removalGracePeriod = Long.parseLong(removalGracePeriodPropertyValue);
            } catch (NumberFormatException e) {
                logger.warn("Configuration property '{}' has invalid value: {}", REMOVAL_GRACE_PERIOD,
                        removalGracePeriodPropertyValue);
            }
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return HueBridgeHandler.SUPPORTED_THING_TYPES;
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
                String host = service.getHostAddresses()[0];
                String id = service.getPropertyString(MDNS_PROPERTY_BRIDGE_ID);
                String friendlyName = String.format(DISCOVERY_LABEL_PATTERN, host);
                return DiscoveryResultBuilder.create(uid) //
                        .withProperties(Map.of( //
                                HOST, host, //
                                Thing.PROPERTY_MODEL_ID, service.getPropertyString(MDNS_PROPERTY_MODEL_ID), //
                                Thing.PROPERTY_SERIAL_NUMBER, id.toLowerCase())) //
                        .withLabel(friendlyName) //
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER) //
                        .withTTL(120L) //
                        .build();
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String id = service.getPropertyString(MDNS_PROPERTY_BRIDGE_ID);
        if (id != null && !id.isBlank()) {
            return new ThingUID(THING_TYPE_BRIDGE, id.toLowerCase());
        }
        return null;
    }

    @Override
    public long getRemovalGracePeriodSeconds(ServiceInfo service) {
        return removalGracePeriod;
    }
}
