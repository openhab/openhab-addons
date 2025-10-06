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
package org.openhab.binding.homekit.internal.discovery;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.AccessoryCategory;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovers new HomeKit server devices.
 * HomeKit devices advertise themselves using mDNS with the service type "_hap._tcp.local.".
 * Each device is identified by its MAC address, which is included in the mDNS properties.
 * The device category is also included, allowing differentiation between bridges and accessories.
 * The discovery participant creates a ThingUID based on the MAC address and device category.
 * Discovered devices are published as Things of type
 * {@link org.openhab.binding.homekit.internal.HomekitBindingConstants#THING_TYPE_ACCESSORY}
 * or {@link org.openhab.binding.homekit.internal.HomekitBindingConstants#THING_TYPE_BRIDGE}.
 * Discovered Things include properties such as model name, protocol version, and IP address.
 * This class does not perform active scanning; instead, it relies on the central mDNS discovery
 * service to notify it of new services.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
public class HomekitMdnsDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_hap._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_ACCESSORY);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            Map<String, String> properties = getProperties(service);

            String mac = properties.get("id"); // MAC address
            String host = service.getHostAddresses()[0]; // ipV4 address
            int port = service.getPort();
            if (port != 0) {
                host = host + ":" + port;
            }
            AccessoryCategory cat;
            try {
                String ci = properties.getOrDefault("ci", ""); // accessory category
                cat = AccessoryCategory.from(Integer.parseInt(ci));
            } catch (IllegalArgumentException e) {
                cat = null;
            }

            if (host != null && mac != null && cat != null) {
                DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(uid);
                builder.withLabel(THING_LABEL_FMT.formatted(service.getName(), host)) //
                        .withProperty(CONFIG_HOST, host) //
                        .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac) //
                        .withProperty(PROPERTY_ACCESSORY_CATEGORY, cat.toString()) //
                        .withProperty(PROPERTY_ACCESSORY_UID, new ThingUID(THING_TYPE_ACCESSORY, "1").toString()) //
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS);

                String model = properties.get("md");
                if (model != null) {
                    builder.withProperty(Thing.PROPERTY_MODEL_ID, model);
                }
                String serial = properties.get("s#");
                if (serial != null) {
                    builder.withProperty(Thing.PROPERTY_SERIAL_NUMBER, serial);
                }
                String protocolVersion = properties.get("pv");
                if (protocolVersion != null) {
                    builder.withProperty(PROPERTY_PROTOCOL_VERSION, protocolVersion);
                }

                return builder.build();
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        Map<String, String> properties = getProperties(service);

        String mac = properties.get("id"); // MAC address
        AccessoryCategory cat;
        try {
            String ci = properties.getOrDefault("ci", "");
            cat = AccessoryCategory.from(Integer.parseInt(ci));
        } catch (IllegalArgumentException e) {
            cat = null;
        }

        if (mac != null && cat != null) {
            return new ThingUID(AccessoryCategory.BRIDGE == cat ? THING_TYPE_BRIDGE : THING_TYPE_ACCESSORY,
                    mac.replace(":", "").toLowerCase()); // thing id example "a1b2c3d4e5f6"
        }

        return null;
    }

    /**
     * The JmDNS library getProperties() method has a bug whereby it fails to return any properties
     * in the case that the TXT record contains zero length parts. This is a drop in replacement.
     */
    private Map<String, String> getProperties(ServiceInfo service) {
        Map<String, String> map = new HashMap<>();
        byte[] bytes = service.getTextBytes();
        int i = 0;
        while (i < bytes.length) {
            int len = bytes[i++] & 0xFF;
            if (len == 0) { // skip zero length parts
                continue;
            }
            String[] parts = new String(bytes, i, len, StandardCharsets.UTF_8).split("=");
            map.put(parts[0], parts.length < 2 ? "" : parts[1].replaceFirst("\\u0000$", "")); // strip zero endings
            i += len;
        }
        return map;
    }
}
