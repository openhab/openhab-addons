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
package org.openhab.binding.weatherunderground.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.weatherunderground.internal.WeatherUndergroundBindingConstants;
import org.openhab.binding.weatherunderground.internal.json.WeatherUndergroundJsonData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WeatherUndergroundBridgeHandler} is responsible for handling the
 * bridge things created to use the Weather Underground Service. This way, the
 * API key may be entered only once.
 *
 * @author Theo Giovanna - Initial Contribution
 * @author Laurent Garnier - refactor bridge/thing handling
 */
@NonNullByDefault
public class WeatherUndergroundBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(WeatherUndergroundBridgeHandler.class);
    private final Gson gson;
    private static final String URL = "http://api.wunderground.com/api/%APIKEY%/";
    public static final int FETCH_TIMEOUT_MS = 30000;

    @Nullable
    private ScheduledFuture<?> controlApiKeyJob;

    private String apikey = "";

    public WeatherUndergroundBridgeHandler(Bridge bridge) {
        super(bridge);
        gson = new Gson();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing weatherunderground bridge handler.");
        Configuration config = getThing().getConfiguration();

        // Check if an api key has been provided during the bridge creation
        Object configApiKey = config.get(WeatherUndergroundBindingConstants.APIKEY);
        if (!(configApiKey instanceof String) || ((String) configApiKey).trim().isEmpty()) {
            logger.debug("Setting thing '{}' to OFFLINE: Parameter 'apikey' must be configured.", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-apikey");
        } else {
            apikey = ((String) configApiKey).trim();
            updateStatus(ThingStatus.UNKNOWN);
            startControlApiKeyJob();
        }
    }

    /**
     * Start the job controlling the API key
     */
    private void startControlApiKeyJob() {
        if (controlApiKeyJob == null || controlApiKeyJob.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    WeatherUndergroundJsonData result = null;
                    String errorDetail = null;
                    String error = null;
                    String statusDescr = null;
                    boolean resultOk = false;

                    // Check if the provided api key is valid for use with the weatherunderground service
                    try {
                        String urlStr = URL.replace("%APIKEY%", getApikey());
                        // Run the HTTP request and get the JSON response from Weather Underground
                        String response = null;
                        try {
                            response = HttpUtil.executeUrl("GET", urlStr, FETCH_TIMEOUT_MS);
                            logger.debug("apiResponse = {}", response);
                        } catch (IllegalArgumentException e) {
                            // Catch Illegal character in path at index XX: http://api.wunderground.com/...
                            error = "Error creating URI";
                            errorDetail = e.getMessage();
                            statusDescr = "@text/offline.uri-error";
                        }
                        // Map the JSON response to an object
                        result = gson.fromJson(response, WeatherUndergroundJsonData.class);
                        if (result.getResponse() == null) {
                            error = "Error in Weather Underground response";
                            errorDetail = "missing response sub-object";
                            statusDescr = "@text/offline.comm-error-response";
                        } else if (result.getResponse().getErrorDescription() != null) {
                            if ("keynotfound".equals(result.getResponse().getErrorType())) {
                                error = "API key has to be fixed";
                                errorDetail = result.getResponse().getErrorDescription();
                                statusDescr = "@text/offline.comm-error-invalid-api-key";
                            } else if ("invalidquery".equals(result.getResponse().getErrorType())) {
                                // The API key provided is valid
                                resultOk = true;
                            } else {
                                error = "Error in Weather Underground response";
                                errorDetail = result.getResponse().getErrorDescription();
                                statusDescr = "@text/offline.comm-error-response";
                            }
                        } else {
                            resultOk = true;
                        }
                    } catch (IOException e) {
                        error = "Error running Weather Underground request";
                        errorDetail = e.getMessage();
                        statusDescr = "@text/offline.comm-error-running-request";
                    } catch (JsonSyntaxException e) {
                        error = "Error parsing Weather Underground response";
                        errorDetail = e.getMessage();
                        statusDescr = "@text/offline.comm-error-parsing-response";
                    }

                    // Update the thing status
                    if (resultOk) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        logger.debug("Setting thing '{}' to OFFLINE: Error '{}': {}", getThing().getUID(), error,
                                errorDetail);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, statusDescr);
                    }
                }
            };
            controlApiKeyJob = scheduler.schedule(runnable, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing weatherunderground bridge handler.");

        if (controlApiKeyJob != null && !controlApiKeyJob.isCancelled()) {
            controlApiKeyJob.cancel(true);
            controlApiKeyJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    public String getApikey() {
        return apikey;
    }
}
