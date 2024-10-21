/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.discovery;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DirigeraDiscoveryManager} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(service = DirigeraDiscoveryManager.class)
public class DirigeraDiscoveryManager implements DiscoveryListener {
    private final Logger logger = LoggerFactory.getLogger(DirigeraDiscoveryManager.class);

    private @Nullable DirigeraDiscoveryService discoveryService;
    private @Nullable HttpClient insecureClient;
    private @Nullable String ipAddress;

    @Activate
    public DirigeraDiscoveryManager() {
        logger.info("DIRIGERA Manager constructor");
    }

    public void initialize(HttpClient httpClient, String ip) {
        logger.info("DIRIGERA Manager received {}", ip);
        insecureClient = httpClient;
        ipAddress = ip;
    }

    public void setDiscoverService(DirigeraDiscoveryService discoveryService) {
        logger.info("DIRIGERA DiscoveryService registered");
        this.discoveryService = discoveryService;
    }

    public void scanForHub() {
        DirigeraDiscoveryService currentDiscoveryService = discoveryService;
        HttpClient currentInsecureClient = insecureClient;
        String proxyIpAddress = ipAddress;

        if (currentDiscoveryService != null && currentInsecureClient != null && proxyIpAddress != null) {
            Instant startTime = Instant.now();
            String ipAddress = proxyIpAddress;
            ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("dirigera-discovery");
            if (!ipAddress.isBlank()) {
                int splitIndex = ipAddress.lastIndexOf(".") + 1;
                String ipPart = ipAddress.substring(0, splitIndex);
                for (int i = 1; i < 256; i++) {
                    String investigateIp = ipPart + i;
                    DirigeraDiscoveryRunnable investigator = new DirigeraDiscoveryRunnable(currentDiscoveryService,
                            investigateIp, currentInsecureClient);
                    scheduler.schedule(investigator, 0, TimeUnit.SECONDS);
                }
                logger.info("DIRIGERA DISCOVERY scan finished in {} seconds",
                        Duration.between(startTime, Instant.now()).getSeconds());
            } else {
                logger.info("DIRIGERA DISCOVERY cannot obtain IP address");
            }
        } else {
            logger.info("DIRIGERA DISCOVERY not ready yet");
        }
    }

    public void thingDiscovered(final DiscoveryResult discoveryResult) {
        DirigeraDiscoveryService proxyDiscovery = discoveryService;
        if (proxyDiscovery != null) {
            proxyDiscovery.deviceDiscovered(discoveryResult);
        }
    }

    public void thingRemoved(final DiscoveryResult discoveryResult) {
        DirigeraDiscoveryService proxyDiscovery = discoveryService;
        if (proxyDiscovery != null) {
            proxyDiscovery.deviceRemoved(discoveryResult);
        }
    }

    @Override
    public void thingDiscovered(@Nullable DiscoveryService source, DiscoveryResult result) {
        // TODO Auto-generated method stub
    }

    @Override
    public void thingRemoved(@Nullable DiscoveryService source, ThingUID thingUID) {
        // TODO Auto-generated method stub
    }

    @Override
    public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
        // TODO Auto-generated method stub
        return null;
    }
}
