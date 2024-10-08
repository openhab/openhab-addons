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

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DirigeraDiscoveryRunnable} dto for code response
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DirigeraDiscoveryRunnable implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(DirigeraDiscoveryRunnable.class);

    private DirigeraDiscoveryService discovery;
    private HttpClient httpClient;
    private String ipAddress;

    public DirigeraDiscoveryRunnable(DirigeraDiscoveryService discovery, String ipAddress, HttpClient httpClient) {
        this.discovery = discovery;
        this.ipAddress = ipAddress;
        this.httpClient = httpClient;
    }

    @Override
    public void run() {
        String homeUrl = String.format(HOME_URL, ipAddress);
        // logger.info("DIRIGERA DISCOVERY check {}", ipAddress);
        ContentResponse response;
        try {
            response = httpClient.newRequest(homeUrl).header(HttpHeader.WWW_AUTHENTICATE, "Basic")
                    .timeout(5, TimeUnit.SECONDS).send();
            // status shall be unauthorized, not anything else
            // logger.info("DIRIGERA DISCOVERY check {} responded {}", ipAddress, response.getStatus());
            if (response.getStatus() == 401) {
                Map<String, Object> properties = new HashMap<>();
                properties.put(PROPERTY_IP_ADDRESS, ipAddress);
                logger.info("DIRIGERA DISCOVERY possible candidate {}", homeUrl);
                discovery.gatewayDiscovered(ipAddress, properties);
                return;
            } else {
                // logger.info("DIRIGERA DISCOVERY discard candidate {}", ipAddress);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // logger.info("DIRIGERA DISCOVERY discard candidate {} {}", ipAddress, e.getMessage());
        }
    }
}
