/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.transitapp.internal.handler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.transitapp.internal.config.TransitAppBridgeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitAppBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppBridgeHandler.class);
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public TransitAppBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        String apiKey = config.apiKey;

        if (apiKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key is missing");
            return;
        }

        logger.debug("API Key loaded successfully. Verifying connection...");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Verifying API Key...");

        String url = "https://external.transitapp.com/v4/public/stop_departures?global_stop_id=test";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("apiKey", apiKey).GET().build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                logger.info("Transit API connection verified successfully! Status code: {}.", statusCode);
                updateStatus(ThingStatus.ONLINE);
            } else if (statusCode == 401 || statusCode == 403) {
                logger.error("API Authentication failed with status code {}.", statusCode);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "API Authentication Failed (Status: " + statusCode + ")");
            } else {
                logger.warn("Transit API verification returned status {}. Treating bridge as ONLINE.", statusCode);
                updateStatus(ThingStatus.ONLINE);
            }
        }).exceptionally(e -> {
            logger.error("Failed to connect to Transit API: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection Failed: " + e.getMessage());
            return null;
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
