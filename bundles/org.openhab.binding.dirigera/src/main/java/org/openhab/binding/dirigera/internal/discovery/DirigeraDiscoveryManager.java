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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.common.ThreadPoolManager;
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
public class DirigeraDiscoveryManager {
    private final Logger logger = LoggerFactory.getLogger(DirigeraDiscoveryManager.class);

    private List<String> foundGatewayAddresses = new ArrayList<>();
    private List<String> foundDeviceIds = new ArrayList<>();

    private @Nullable DirigeraDiscoveryService discoveryService;
    private @Nullable HttpClient insecureClient;
    private @Nullable String ipAddress;

    @Activate
    public DirigeraDiscoveryManager() {
        logger.info("DIRIGERA Manager constructor");
    }

    public void initialize(HttpClient httpClient, String ipAddress) {
        logger.info("DIRIGERA Manager received {}", ipAddress);
        insecureClient = httpClient;
        this.ipAddress = ipAddress;
        // scanForHub();
    }

    public void setDiscoverService(DirigeraDiscoveryService discoveryService) {
        logger.info("DIRIGERA DiscoveryService registered");
        this.discoveryService = discoveryService;
        // scanForHub();
    }

    public void scanForHub() {
        DirigeraDiscoveryService currentDiscoveryService = discoveryService;
        HttpClient currentInsecureClient = insecureClient;
        String currentIpAddress = ipAddress;

        if (currentDiscoveryService != null && currentInsecureClient != null && currentIpAddress != null) {
            Instant startTime = Instant.now();
            ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("dirigera-discovery");
            if (!currentIpAddress.isBlank()) {
                int splitIndex = currentIpAddress.lastIndexOf(".") + 1;
                String ipPart = currentIpAddress.substring(0, splitIndex);
                ScheduledFuture future = null;
                for (int i = 1; i < 256; i++) {
                    String investigateIp = ipPart + i;
                    if (!foundGatewayAddresses.contains(investigateIp)) {
                        DirigeraDiscoveryRunnable investigator = new DirigeraDiscoveryRunnable(currentDiscoveryService,
                                investigateIp, currentInsecureClient);
                        future = scheduler.schedule(investigator, 0, TimeUnit.SECONDS);
                    } else {
                        logger.info("DIRIGERA DISCOVERY IP Address {} already has a Handler instance", investigateIp);
                    }
                }
                // logger.info("DIRIGERA DISCOVERY wait for termination");
                // if (future != null) {
                // while (!future.isDone()) {
                // try {
                // Thread.sleep(1000);
                // } catch (InterruptedException e) {
                // logger.info("DIRIGERA DISCOVERY wait for termination interrupted");
                // }
                // }
                // }
                logger.info("DIRIGERA DISCOVERY scan finished in {} seconds",
                        Duration.between(startTime, Instant.now()).getSeconds());
            } else {
                logger.info("DIRIGERA DISCOVERY cannot obtain IP address");
            }
        } else {
            logger.info("DIRIGERA DISCOVERY not ready yet");
        }
    }

    public void ignoreGateway(String ipAddress) {
        foundGatewayAddresses.add(ipAddress);
    }

    public void ignoreDevice(String id) {
        foundDeviceIds.add(id);
    }
}
