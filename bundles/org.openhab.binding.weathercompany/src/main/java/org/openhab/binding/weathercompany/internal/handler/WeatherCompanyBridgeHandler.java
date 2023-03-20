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
package org.openhab.binding.weathercompany.internal.handler;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpResponseException;
import org.openhab.binding.weathercompany.internal.config.WeatherCompanyBridgeConfig;
import org.openhab.binding.weathercompany.internal.util.ExceptionUtils;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherCompanyBridgeHandler} is responsible for validating the API key
 * used to access the Weather Company API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class WeatherCompanyBridgeHandler extends BaseBridgeHandler {
    private static final long KEY_VALIDATION_FREQ_SECONDS = 120L;
    private static final String BASE_URL = "https://api.weather.com/v3/location/search?query=chicago&locationType=locid&language=en-US&format=json&apiKey=";

    private final Logger logger = LoggerFactory.getLogger(WeatherCompanyBridgeHandler.class);

    private @Nullable Future<?> validateApiKeyJob;

    private final Runnable validateApiKeyRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Bridge: Attempting to validate API key");
            try {
                String url = BASE_URL + getConfigAs(WeatherCompanyBridgeConfig.class).apiKey;
                String response = HttpUtil.executeUrl("GET", url, 10000);
                // If we get a response, we know the API key is valid
                logger.debug("Bridge: Got a successful response to key validation: '{}'", response);
                updateStatus(ThingStatus.ONLINE);
                cancelValidateApiKeyJob();
            } catch (IOException e) {
                Throwable rootcause = ExceptionUtils.getRootThrowable(e);
                if (rootcause instanceof HttpResponseException
                        && rootcause.getMessage().contains("Authentication challenge without")) {
                    logger.debug("Bridge: HttpResponseException: API key is not valid");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.config-error-invalid-api-key");
                } else {
                    logger.debug("Bridge: IOException trying to validate Api key: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
                }
            }
        }
    };

    public WeatherCompanyBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        scheduleValidateApiKeyJob();
    }

    @Override
    public void dispose() {
        cancelValidateApiKeyJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Bridge doesn't handle any commands
    }

    public @Nullable String getApiKey() {
        return getConfigAs(WeatherCompanyBridgeConfig.class).apiKey;
    }

    private void scheduleValidateApiKeyJob() {
        cancelValidateApiKeyJob();
        validateApiKeyJob = scheduler.scheduleWithFixedDelay(validateApiKeyRunnable, 0L, KEY_VALIDATION_FREQ_SECONDS,
                TimeUnit.SECONDS);
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
