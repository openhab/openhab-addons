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

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.AccessoryType;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(HomekitMdnsDiscoveryParticipant.class);

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
            String host = service.getHostAddresses()[0];
            String macAddress = service.getPropertyString("id"); // HomeKit device ID is the MAC address
            String modelName = service.getPropertyString("md"); // HomeKit device model name
            String deviceCategory = service.getPropertyString("ci"); // HomeKit device category
            String protocolVersion = service.getPropertyString("pv"); // HomeKit protocol version

            DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(uid);
            builder.withLabel(THING_LABEL_FMT.formatted(service.getName(), host)) //
                    .withProperty(CONFIG_HOST, host) //
                    .withProperty(Thing.PROPERTY_MODEL_ID, modelName) //
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, macAddress) //
                    .withProperty(PROPERTY_PROTOCOL_VERSION, protocolVersion) //
                    .withProperty(PROPERTY_DEVICE_CATEGORY, deviceCategory) //
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS);

            if (!isBridge(service)) {
                // '1' means we shall use the first (and only) accessory
                ThingUID accessoryUid = new ThingUID(THING_TYPE_ACCESSORY, "1");
                builder.withProperty(PROPERTY_ACCESSORY_UID, accessoryUid.toString());
            }

            return builder.build();
        }
        logger.debug("Ignoring discovered HAP service {} with bad properties", service.getName());
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String macAddress = service.getPropertyString("id");
        if (macAddress != null) {
            String id = macAddress.replace(":", "").replace("-", "").toLowerCase(); // e.g. "a1b2c3d4e5f6"
            return isBridge(service) ? new ThingUID(THING_TYPE_BRIDGE, id) : new ThingUID(THING_TYPE_ACCESSORY, id);
        }
        return null;
    }

    private boolean isBridge(ServiceInfo service) {
        String ci = service.getPropertyString("ci"); // accessory type i.e. 'category'
        try {
            return AccessoryType.BRIDGE == AccessoryType.from(Integer.parseInt(ci));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to parse accessory category '{}' for HAP service '{}'", ci, service.getName());
        }
        return false;
    }
}
