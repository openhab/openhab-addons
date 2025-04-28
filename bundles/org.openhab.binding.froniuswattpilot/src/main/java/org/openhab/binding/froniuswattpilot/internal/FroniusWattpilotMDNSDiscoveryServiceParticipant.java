/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.froniuswattpilot.internal;

import static org.openhab.binding.froniuswattpilot.internal.FroniusWattpilotBindingConstants.*;
import static org.openhab.binding.froniuswattpilot.internal.FroniusWattpilotHandlerFactory.SUPPORTED_THING_TYPES_UIDS;

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

/**
 * The {@link FroniusWattpilotMDNSDiscoveryServiceParticipant} is responsible for discovering new and removed Wattpilot
 * wallboxes through mDNS. It uses the central
 * {@link org.openhab.core.config.discovery.mdns.internal.MDNSDiscoveryService}.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "discovery.froniuswattpilot")
@NonNullByDefault
public class FroniusWattpilotMDNSDiscoveryServiceParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_Fronius-SE-Wattpilot._tcp.local.";

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
                String[] addresses = service.getHostAddresses();
                if (addresses == null || addresses.length == 0) {
                    return null;
                }
                String ip = addresses[0];
                return DiscoveryResultBuilder.create(uid) //
                        .withProperties(Map.of( //
                                FroniusWattpilotBindingConstants.HOSTNAME_CONFIGURATION_KEY, ip)) //
                        .withLabel(service.getName()) //
                        .withRepresentationProperty(FroniusWattpilotBindingConstants.HOSTNAME_CONFIGURATION_KEY) //
                        .build();
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String name = service.getName();
        if (name != null && !name.isBlank()) {
            return new ThingUID(THING_TYPE_WATTPILOT, name.replace("_", "-").replace(" ", "-").toLowerCase());
        }
        return null;
    }
}
