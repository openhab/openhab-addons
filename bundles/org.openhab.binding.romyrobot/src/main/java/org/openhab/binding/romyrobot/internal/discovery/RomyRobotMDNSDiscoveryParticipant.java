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
package org.openhab.binding.romyrobot.internal.discovery;

import static org.openhab.binding.romyrobot.internal.RomyRobotBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.util.HashSet;
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
 * This is a discovery participant which finds RomyRobots on the local network
 * through their mDNS announcements.
 *
 * @author Manuel Dipolt - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class RomyRobotMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(RomyRobotMDNSDiscoveryParticipant.class);

    @Override
    public String getServiceType() {
        return "_aicu-http._tcp.local.";
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        Set<ThingTypeUID> supportedThingTypeUIDs = new HashSet<>();
        supportedThingTypeUIDs.add(ROMYROBOT_DEVICE);
        return supportedThingTypeUIDs;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service != null) {
            return new ThingUID(ROMYROBOT_DEVICE, service.getName());
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {

        // logger.error("~~~>>> {})", service.getName());

        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            logger.info("Discovered ROMY vacuum cleaner robot: {}", service);
            return DiscoveryResultBuilder.create(thingUID).withProperty(PROPERTY_SERIAL_NUMBER, service.getName())
                    .withLabel("@text/discovery.bridge.label").withRepresentationProperty(PROPERTY_SERIAL_NUMBER)
                    .build();
        }
        return null;
    }
}
