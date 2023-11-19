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
package org.openhab.binding.opensmartcity.internal;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenSmartCityCityHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class OpenSmartCityCityHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set
            .of(org.openhab.binding.opensmartcity.internal.OpenSmartCityBindingConstants.THING_TYPE_WEATHER);

    private static final String PATH_AVAILABILITY = "/v1.1/Locations";

    private final Logger logger = LoggerFactory.getLogger(OpenSmartCityCityHandler.class);

    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> refreshJob;

    // keeps track of the parsed config
    private @Nullable OpenSmartCityConfiguration config;

    // TODO: Hardcoded for PoC
    private String basePath = "https://frost.solingen.de:8443/FROST-Server";

    public OpenSmartCityCityHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize OpenWeatherMap API handler '{}'.", getThing().getUID());
        config = getConfigAs(org.openhab.binding.opensmartcity.internal.OpenSmartCityConfiguration.class);

        boolean configValid = true;
        if (config.city.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "City parameter must be provided.");
            configValid = false;
        }
        int refreshInterval = config.refreshInterval;
        if (refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Minimal refresh interval is 60 seconds.");
            configValid = false;
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);

            ScheduledFuture<?> localRefreshJob = refreshJob;
            if (localRefreshJob == null || localRefreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} seconds.", refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::checkAvailability, 0, refreshInterval,
                        TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no channels to deal with

    }

    private void checkAvailability() {
        try {
            String url = basePath + PATH_AVAILABILITY;
            logger.debug("Requesting {}", url);
            Request request = httpClient.newRequest(basePath + PATH_AVAILABILITY);
            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                // TODO: check exact problem
                updateStatus(ThingStatus.OFFLINE);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose handler '{}'.", getThing().getUID());
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
            logger.debug("Stop refresh job.");
            if (localRefreshJob.cancel(true)) {
                refreshJob = null;
            }
        }
    }

}
