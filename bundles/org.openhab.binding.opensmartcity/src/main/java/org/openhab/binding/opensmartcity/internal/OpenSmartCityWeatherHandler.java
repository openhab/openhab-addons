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

import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link OpenSmartCityWeatherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class OpenSmartCityWeatherHandler extends BaseThingHandler {

    private static final String QUERY_PATH = "/v1.1/Datastreams?%24top=1&%24expand=Observations(%24orderby%3DphenomenonTime%20desc%3B%24filter%3Dday(now())%20sub%20day(phenomenonTime)%20le%201%20and%20month(now())%20eq%20month(phenomenonTime))&%24filter=substringof(%27lufttemperatur%27%2Cname)%20and%20Thing%2Fproperties%2Fstatus%20eq%20%27online%27%20and%20Thing%2FLocations%2Fname%20eq%20%27Dorper%20Stra%C3%9Fe%20%2F%20Goerdeler%20Stra%C3%9Fe%27";

    private final Logger logger = LoggerFactory.getLogger(OpenSmartCityWeatherHandler.class);

    private @NonNullByDefault({}) OpenSmartCityWeatherConfiguration config;

    private @Nullable ScheduledFuture<?> refreshJob;

    private @NonNullByDefault({}) OpenSmartCityCityHandler bridgeHandler;

    private Gson gson = new Gson();

    public OpenSmartCityWeatherHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateSensorValues();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(OpenSmartCityWeatherConfiguration.class);

        bridgeHandler = (OpenSmartCityCityHandler) getBridge().getHandler();

        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            logger.debug("Start refresh job at interval {} seconds.", bridgeHandler.config.refreshInterval);
            refreshJob = scheduler.scheduleWithFixedDelay(this::updateSensorValues, 0,
                    bridgeHandler.config.refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void updateSensorValues() {
        try {
            String url = bridgeHandler.basePath + QUERY_PATH;
            logger.debug("Requesting {}", url);

            Request request = bridgeHandler.httpClient.newRequest(url);

            // TODO: remove hard coded credentials
            request.getHeaders().add("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(("smarthomeuser:Solingen2030!").getBytes()));
            ContentResponse response = request.send();

            if (response.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);

                String content = response.getContentAsString();
                JsonObject jsonResponse = gson.fromJson(content, JsonObject.class);
                logger.debug("Response: {}", jsonResponse.toString());
                // JsonElement observations = jsonResponse.get("value").getAsJsonArray().get(0);
                Double temperature = 11.3;
                Double humidity = 0.85;

                updateState(OpenSmartCityBindingConstants.CHANNEL_TEMPERATURE,
                        new QuantityType<Temperature>(temperature, SIUnits.CELSIUS));
                updateState(OpenSmartCityBindingConstants.CHANNEL_HUMIDITY, new DecimalType(humidity));

            } else {
                // TODO: check exact problem
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("HTTP request failed with response code {}: {}", response.getStatus(),
                        response.getReason());
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

    public class JsonResponse {
    }
}
