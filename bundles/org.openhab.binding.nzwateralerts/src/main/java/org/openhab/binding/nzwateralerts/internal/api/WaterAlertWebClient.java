/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.nzwateralerts.internal.NZWaterAlertsBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * The {@link WebClient} class contains the logic to get data from a URL.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class WaterAlertWebClient {
    private final Logger logger = LoggerFactory.getLogger(WaterAlertWebClient.class);

    private @Nullable HttpClient httpClient = null;
    private @Nullable WaterWebService service = null;
    
    private String webService = "";
    private String region = "";
    private String area = "";

    public WaterAlertWebClient(HttpClient httpClient, String location) {
        this.httpClient = httpClient;

        String[] locationSegmented = location.split(":", 3);
        webService = locationSegmented[0];
        region = locationSegmented[1];
        area = locationSegmented[2];

        for (WaterWebService srv : WATER_WEB_SERVICES) {
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
        try {
            if (service != null) {
                logger.debug("Getting Water Level from service {} region {} area {}", webService,
                region, area);
                String endpoint = service.endpoint(region);
                logger.trace("Getting data from endpoint {}", endpoint);
                response = httpClient.GET(endpoint);

                int waterLevel = service.findWaterLevel(response.getContentAsString(), area);
                logger.debug("Got water level {}", waterLevel);
                return waterLevel;
            } else {
                logger.debug("Service is null");
                return null;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Error when attempting to get Water Level {}", e.getMessage());
            return null;
        }
    }
}
