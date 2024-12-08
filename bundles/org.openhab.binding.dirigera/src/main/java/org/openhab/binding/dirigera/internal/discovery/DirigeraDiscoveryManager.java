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

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.discovery.DiscoveryResult;
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

    private @Nullable DirigeraDiscoveryService discoveryService;
    private @Nullable HttpClient insecureClient;
    private @Nullable String ipAddress;

    @Activate
    public DirigeraDiscoveryManager() {
    }

    public void initialize(HttpClient httpClient, String ip) {
        logger.trace("DIRIGERA DISCOVERY IP address {}", ip);
        insecureClient = httpClient;
        ipAddress = ip;
    }

    public void setDiscoverService(DirigeraDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public void scanForHub() {
        DirigeraDiscoveryService currentDiscoveryService = discoveryService;
        HttpClient currentInsecureClient = insecureClient;
        String proxyIpAddress = ipAddress;

        if (currentDiscoveryService != null && currentInsecureClient != null && proxyIpAddress != null) {
            String ipAddress = proxyIpAddress;
            ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("dirigera-discovery");
            if (!ipAddress.isBlank()) {
                int splitIndex = ipAddress.lastIndexOf(".") + 1;
                String ipPart = ipAddress.substring(0, splitIndex);
                for (int i = 1; i < 256; i++) {
                    String investigateIp = ipPart + i;
                    DirigeraDiscoveryRunnable investigator = new DirigeraDiscoveryRunnable(currentDiscoveryService,
                            investigateIp, currentInsecureClient);
                    scheduler.execute(investigator);
                }
            } else {
                logger.debug("DIRIGERA DISCOVERY cannot obtain IP address");
            }
        } else {
            logger.debug("DIRIGERA DISCOVERY not ready yet");
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
}
