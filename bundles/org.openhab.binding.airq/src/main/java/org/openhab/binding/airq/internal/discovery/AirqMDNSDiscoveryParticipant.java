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
package org.openhab.binding.airq.internal.discovery;

import static org.openhab.binding.airq.internal.AirqBindingConstants.CONFIG_IP_ADDRESS;
import static org.openhab.binding.airq.internal.AirqBindingConstants.THING_TYPE_AIRQ;

import java.net.Inet4Address;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirqMDNSDiscoveryParticipant} discovers air-Q devices on the local network via mDNS.
 *
 * air-Q devices advertise themselves as {@code _http._tcp.local.} services with a TXT record property
 * {@code device=air-q}. This participant filters for that property to distinguish air-Q devices from
 * other HTTP services on the network.
 *
 * @author Renat Sibgatulin - Initial contribution
 */
@Component(configurationPid = "discovery.airq")
@NonNullByDefault
public class AirqMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_http._tcp.local.";
    private static final String MDNS_PROPERTY_DEVICE = "device";
    private static final String MDNS_PROPERTY_ID = "id";
    private static final String MDNS_PROPERTY_DEVICE_NAME = "devicename";
    private static final String DEVICE_TYPE = "air-q";

    private final Logger logger = LoggerFactory.getLogger(AirqMDNSDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_AIRQ);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        String device = serviceInfo.getPropertyString(MDNS_PROPERTY_DEVICE);
        if (device == null || !DEVICE_TYPE.equalsIgnoreCase(device)) {
            return null;
        }

        String id = serviceInfo.getPropertyString(MDNS_PROPERTY_ID);
        if (id == null || id.isBlank()) {
            logger.debug("Discovered air-Q device without id property, ignoring: {}", serviceInfo.getQualifiedName());
            return null;
        }

        return new ThingUID(THING_TYPE_AIRQ, id);
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        ThingUID thingUID = getThingUID(serviceInfo);
        if (thingUID == null) {
            return null;
        }

        Inet4Address[] addresses = serviceInfo.getInet4Addresses();
        Inet4Address ip4Address = null;
        for (Inet4Address address : addresses) {
            if (address != null) {
                ip4Address = address;
                break;
            }
        }
        if (ip4Address == null) {
            logger.debug("Discovered air-Q device without IPv4 address, ignoring: {}", serviceInfo.getQualifiedName());
            return null;
        }

        String ipAddress = ip4Address.getHostAddress();
        String deviceName = serviceInfo.getPropertyString(MDNS_PROPERTY_DEVICE_NAME);
        String label = deviceName != null && !deviceName.isBlank() ? "air-Q (%s)".formatted(deviceName) : "air-Q";
        String id = serviceInfo.getPropertyString(MDNS_PROPERTY_ID);

        logger.debug("Discovered air-Q device '{}' at {} with id {}", deviceName, ipAddress, id);

        return DiscoveryResultBuilder.create(thingUID).withProperties(Map.of(CONFIG_IP_ADDRESS, ipAddress))
                .withRepresentationProperty(CONFIG_IP_ADDRESS).withLabel(label).build();
    }
}
