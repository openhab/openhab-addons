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
import org.openhab.binding.romyrobot.internal.RomyRobotConfiguration;
import org.openhab.binding.romyrobot.internal.api.RomyApi;
import org.openhab.binding.romyrobot.internal.api.RomyApiFactory;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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

    private RomyApiFactory apiFactory;
    // private RomyRobotConfiguration config;
    // private RomyApi romyDevice;
    // private final HttpClient httpClient;

    @Activate
    public RomyRobotMDNSDiscoveryParticipant(
            @Reference RomyApiFactory apiFactory /* @Reference HttpClientFactory httpClientFactory */) {
        logger.debug("Activating ROMY Discovery service");
        this.apiFactory = apiFactory;
        // this.httpClient = httpClientFactory.getCommonHttpClient();
    }

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
        return new ThingUID(ROMYROBOT_DEVICE, service.getName());
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {

        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            logger.info("Discovered ROMY vacuum cleaner robot: {}", service);

            // get IP address
            String address = "";
            String[] hostAddresses = service.getHostAddresses();
            // logger.info("hostAddresses: {}", hostAddresses);
            if ((hostAddresses != null) && (hostAddresses.length > 0)) {
                address = hostAddresses[0];
            }
            // logger.info("address: {}", address);
            if (address.isEmpty()) {
                // logger.error("ROMY discovered empty IP via MDNS!");
                return null;
            }

            try {
                RomyRobotConfiguration config = new RomyRobotConfiguration();
                config.hostname = address;
                RomyApi romyDevice = apiFactory.getHttpApi(config);
                romyDevice.refresh();
                logger.error("new ROMY discovered: {}", romyDevice.getName());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error setting up ROMY api: {}", e.getMessage());
                return null;
            }

            return DiscoveryResultBuilder.create(thingUID).withProperty(PROPERTY_SERIAL_NUMBER, service.getName())
                    .withLabel("@text/discovery.bridge.label").withRepresentationProperty(PROPERTY_SERIAL_NUMBER)
                    .build();
        }
        return null;
    }
}
