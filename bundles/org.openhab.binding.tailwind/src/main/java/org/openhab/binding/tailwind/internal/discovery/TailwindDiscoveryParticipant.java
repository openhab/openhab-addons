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
package org.openhab.binding.tailwind.internal.discovery;

import static org.openhab.binding.tailwind.internal.TailwindBindingConstants.*;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tailwind.internal.TailwindDeviceId;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link TailwindDiscoveryParticipant} finds TailWind controllers, which announce their web server with mDNS.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class TailwindDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final String SERVICE_TYPE = "_http._tcp.local.";
    private static final String MDNS_VENDOR = "tailwind";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (!MDNS_VENDOR.equals(service.getPropertyString("vendor"))
                || !PRODUCT_IQ3.equalsIgnoreCase(service.getPropertyString("product"))) {
            return null;
        }
        String mac = TailwindDeviceId.toMacAddress(service.getPropertyString("device_id"));
        return mac == null ? null : new ThingUID(THING_TYPE_IQ3, mac);
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID thingUID = getThingUID(service);
        String hostname = hostname(service);
        if (thingUID == null || hostname == null) {
            return null;
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_HOSTNAME, hostname);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, thingUID.getId());
        properties.put(Thing.PROPERTY_VENDOR, VENDOR);
        properties.put(Thing.PROPERTY_MODEL_ID, PRODUCT_IQ3);
        String firmwareVersion = service.getPropertyString("SW ver");
        if (firmwareVersion != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        }
        String hardwareVersion = service.getPropertyString("HW ver");
        if (hardwareVersion != null) {
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwareVersion);
        }
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withLabel(VENDOR + " " + PRODUCT_IQ3).build();
    }

    /**
     * Returns the IPv4 address of the controller, or its host name if the service has no address yet.
     */
    private static @Nullable String hostname(ServiceInfo service) {
        Inet4Address[] addresses = service.getInet4Addresses();
        if (addresses.length > 0) {
            return addresses[0].getHostAddress();
        }
        String server = service.getServer();
        if (server == null || server.isBlank()) {
            return null;
        }
        return server.endsWith(".") ? server.substring(0, server.length() - 1) : server;
    }
}
