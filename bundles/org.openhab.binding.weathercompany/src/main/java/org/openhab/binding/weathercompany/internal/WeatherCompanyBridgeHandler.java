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
package org.openhab.binding.weathercompany.internal;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.weathercompany.internal.config.WeatherCompanyBridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherCompanyBridgeHandler} is responsible for
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class WeatherCompanyBridgeHandler extends BaseBridgeHandler {
    private static final String BASE_URL = "https://api.weather.com/v3/location/search?query=chicago&locationType=locid&language=en-US&format=json&apiKey=";

    private final Logger logger = LoggerFactory.getLogger(WeatherCompanyBridgeHandler.class);

    // Thing configuration
    private @Nullable String apiKey;

    // Job to validate the API Key
    private @Nullable Future<?> validateApiKeyJob;

    private Runnable validateApiKeyRunnable = new Runnable() {
        @Override
        public void run() {
            validateApiKey();
        }
    };

    public WeatherCompanyBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // Get the configuration
        WeatherCompanyBridgeConfig config = getConfigAs(WeatherCompanyBridgeConfig.class);
        apiKey = config.apiKey;

        updateStatus(ThingStatus.OFFLINE);
        scheduleValidateApiKeyJob();
    }

    @Override
    public void dispose() {
        cancelValidateApiKeyJob();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo status) {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Bridge doesn't handle any commands
    }

    public @Nullable String getApiKey() {
        return apiKey;
    }

    private void validateApiKey() {
        logger.debug("Bridge: Validating API key");
        try {
            String url = BASE_URL + apiKey;
            String response = HttpUtil.executeUrl("GET", url, 10000);
            logger.debug("Bridge: Response to key validation is '{}'", response);
            updateStatus(ThingStatus.ONLINE);
            cancelValidateApiKeyJob();
        } catch (IOException e) {
            Throwable rootcause = ExceptionUtils.getRootCause(e);
            if (rootcause instanceof HttpResponseException
                    && rootcause.getMessage().contains("Authentication challenge without")) {
                logger.debug("Bridge: HttpResponseException: API key is not valid");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API key is invalid");
            } else {
                logger.info("Bridge: IOException trying to validate Api key: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            }
        }
    }

    /*
     * The refresh job updates the daily forecast and the PWS current
     * observations on the refresh interval set in the thing config
     */
    private void scheduleValidateApiKeyJob() {
        cancelValidateApiKeyJob();
        validateApiKeyJob = scheduler.scheduleWithFixedDelay(validateApiKeyRunnable, 0L, 60, TimeUnit.SECONDS);
        logger.debug("Bridge: Scheduling job to validate API key");
    }

    private void cancelValidateApiKeyJob() {
        if (validateApiKeyJob != null) {
            validateApiKeyJob.cancel(true);
            validateApiKeyJob = null;
            logger.debug("Bridge: Canceling job to validate API key");
        }
    }
}
