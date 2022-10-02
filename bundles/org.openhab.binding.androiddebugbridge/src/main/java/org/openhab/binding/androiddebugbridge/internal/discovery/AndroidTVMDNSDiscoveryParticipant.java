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
package org.openhab.binding.androiddebugbridge.internal.discovery;

import static org.openhab.binding.androiddebugbridge.internal.AndroidDebugBridgeBindingConstants.*;

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
 * The {@link AndroidTVMDNSDiscoveryParticipant} is responsible for discovering new and removed Android TV devices. It
 * uses
 * the central {@link MDNSDiscoveryService}.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "discovery.androiddebugbridge")
@NonNullByDefault
public class AndroidTVMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_androidtvremote2._tcp.local.";
    private static final String MDNS_PROPERTY_MAC_ADDRESS = "bt";
    private final Logger logger = LoggerFactory.getLogger(AndroidTVMDNSDiscoveryParticipant.class);

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
        return SUPPORTED_THING_TYPES;
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
                String ip = service.getHostAddresses()[0];
                String macAddress = service.getPropertyString(MDNS_PROPERTY_MAC_ADDRESS);
                String friendlyName = String.format("%s (%s)", service.getName(), ip);
                return DiscoveryResultBuilder.create(uid) //
                        .withProperties(Map.of( //
                                PARAMETER_IP, ip, //
                                Thing.PROPERTY_MAC_ADDRESS, macAddress.toLowerCase())) //
                        .withLabel(friendlyName) //
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS) //
                        .build();
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String macAddress = service.getPropertyString(MDNS_PROPERTY_MAC_ADDRESS);
        if (macAddress != null && !macAddress.isBlank()) {
            return new ThingUID(THING_TYPE_ANDROID_DEVICE, macAddress.replaceAll(":", "").toLowerCase());
        }
        return null;
    }
}
