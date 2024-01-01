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
package org.openhab.binding.nzwateralerts.internal.api;

import static org.openhab.binding.nzwateralerts.internal.NZWaterAlertsBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WaterAlertWebClient} class contains the logic to get data from a URL.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class WaterAlertWebClient {
    private final Logger logger = LoggerFactory.getLogger(WaterAlertWebClient.class);

    private static final int REQUEST_TIMEOUT = 10;

    private final HttpClient httpClient;
    private final String webService;
    private final String region;
    private final String area;

    private @Nullable WaterWebService service = null;

    public WaterAlertWebClient(final HttpClient httpClient, final String location) {
        this.httpClient = httpClient;

        final String[] locationSegmented = location.split(":", 3);
        webService = locationSegmented[0];
        region = locationSegmented[1];
        area = locationSegmented[2];

        for (final WaterWebService srv : WATER_WEB_SERVICES) {
            logger.trace("Checking service {}", srv.service());
            if (locationSegmented[0].equalsIgnoreCase(srv.service())) {
                logger.trace("Found service {}", srv.service());
                service = srv;
            }
        }

        if (service == null) {
            logger.debug("Service could not be found for {}", locationSegmented[0]);
        }
    }

    public @Nullable Integer getLevel() {
        ContentResponse response;
        final WaterWebService localService = service;
        try {
            if (localService != null) {
                logger.debug("Getting Water Level from service {} region {} area {}", webService, region, area);

                final String endpoint = localService.endpoint(region);
                logger.trace("Getting data from endpoint {} with timeout {}", endpoint, REQUEST_TIMEOUT);
                response = httpClient.newRequest(endpoint).timeout(REQUEST_TIMEOUT, TimeUnit.SECONDS).send();

                final int waterLevel = localService.findWaterLevel(response.getContentAsString(), area);
                logger.debug("Got water level {}", waterLevel);
                return waterLevel;
            } else {
                logger.debug("Service, region is null");
                return null;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Error when attempting to get Water Level {}", e.getMessage());
            return null;
        }
    }
}
