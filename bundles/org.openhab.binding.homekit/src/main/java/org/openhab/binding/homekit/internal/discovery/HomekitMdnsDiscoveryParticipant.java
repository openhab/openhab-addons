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

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.THING_TYPE_DEVICE;

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link HomekitMdnsDiscoveryParticipant} is responsible for discovering new HomeKit server devices.
 * It uses the central {@link org.openhab.core.config.discovery.mdns.internal.MDNSDiscoveryService}.
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
        return Set.of(THING_TYPE_DEVICE);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            String ipV4Address = service.getHostAddresses()[0];
            String macAddress = service.getPropertyString("id"); // HomeKit device ID is the MAC address
            String modelName = service.getPropertyString("md"); // HomeKit device model name
            String deviceCategory = service.getPropertyString("ci"); // HomeKit device category
            String protocolVersion = service.getPropertyString("pv"); // HomeKit protocol version

            return DiscoveryResultBuilder.create(uid) //
                    .withLabel("%s on (%s)".formatted(modelName, ipV4Address)) //
                    .withProperty(Thing.PROPERTY_MODEL_ID, modelName) //
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, macAddress) //
                    .withProperty("protocolVersion", protocolVersion) //
                    .withProperty("ipV4Address", ipV4Address) //
                    .withProperty("deviceCategory", deviceCategory) //
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String macAddress = service.getPropertyString("id");
        if (macAddress != null) {
            return new ThingUID(THING_TYPE_DEVICE, macAddress.replace(":", "-").toLowerCase());
        } else {
            logger.warn("Discovered HomeKit device without MAC address property - ignoring");
            return null;
        }
    }
}
