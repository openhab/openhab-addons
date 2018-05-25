/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv.handler;

import static org.openhab.binding.openuv.OpenUVBindingConstants.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.openuv.internal.OpenUVConfiguration;
import org.openhab.binding.openuv.json.OpenUVJsonResponse;
import org.openhab.binding.openuv.json.OpenUVJsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link OpenUVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class OpenUVHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(OpenUVHandler.class);
    private static final int DEFAULT_REFRESH_PERIOD = 30;

    OpenUVJsonResult openUVData;

    private ScheduledFuture<?> refreshJob;
    private ScheduledFuture<?> uvMaxJob;

    private Gson gson;

    public OpenUVHandler(Thing thing) {
        super(thing);
        gson = new GsonBuilder()
                .registerTypeAdapter(DecimalType.class,
                        (JsonDeserializer<DecimalType>) (json, type, jsonDeserializationContext) -> DecimalType
                                .valueOf(json.getAsJsonPrimitive().getAsString()))
                .registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type,
                        jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()))
                .create();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenUV handler.");

        OpenUVConfiguration config = getConfigAs(OpenUVConfiguration.class);
        logger.debug("config apikey = (omitted from logging)");
        logger.debug("config refresh = {}", config.refresh);

        String errorMsg = null;

        if (StringUtils.trimToNull(config.apikey) == null) {
            errorMsg = "Parameter 'apikey' is mandatory and must be configured";
        }
        if (config.refresh != null && config.refresh < 3) {
            errorMsg = "Parameter 'refresh' must be at least 3 minutes to stay in free API plan";
        }

        if (errorMsg == null) {
            updateStatus(ThingStatus.ONLINE);
            startAutomaticRefresh();
            startUVMaxScreening();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job screening UV Max reached
     */
    private void startUVMaxScreening() {
        if (uvMaxJob == null || uvMaxJob.isCancelled()) {
            Runnable runnable = () -> {
                try {
                    if (openUVData != null) {
                        long timeDiff = ChronoUnit.MINUTES.between(ZonedDateTime.now(ZoneId.systemDefault()),
                                openUVData.getUVMaxTimeAsZDT());
                        if (timeDiff == 0 || timeDiff == 1) {
                            triggerChannel(UVMAXEVENT);
                        }

                        logger.debug("Time to Max UV : {} min", timeDiff);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                }
            };

            uvMaxJob = scheduler.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * Start the job refreshing the data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                try {
                    getOpenUVData();

                    for (Channel channel : getThing().getChannels()) {
                        updateChannel(channel.getUID());
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                }
            };

            OpenUVConfiguration config = getConfigAs(OpenUVConfiguration.class);
            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the OpenUV handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }

        if (uvMaxJob != null && !uvMaxJob.isCancelled()) {
            uvMaxJob.cancel(true);
            uvMaxJob = null;
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID);
        } else {
            logger.debug("The OpenUV binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * Update the channel from the last OpenUV data retrieved
     *
     * @param channelUID the id identifying the channel to be updated
     *
     */
    private void updateChannel(@NonNull ChannelUID channelUID) {
        if (openUVData != null) {
            switch (channelUID.getId()) {
                case UVINDEX:
                    updateState(channelUID, openUVData.getUv());
                    break;
                case UVCOLOR:
                    updateState(channelUID, getAsRGB(openUVData.getUv().floatValue()));
                    break;
                case UVMAX:
                    updateState(channelUID, openUVData.getUvMax());
                    break;
                case OZONE:
                    updateState(channelUID, openUVData.getOzone());
                    break;
                case OZONETIME:
                    updateState(channelUID, openUVData.getOzoneTime());
                    break;
                case UVMAXTIME:
                    updateState(channelUID, openUVData.getUVMaxTime());
                    break;
                case UVTIME:
                    updateState(channelUID, openUVData.getUVTime());
                    break;
                case SAFEEXPOSURE:
                    Channel channel = getThing().getChannel(channelUID.getId());
                    if (channel != null) {
                        Configuration configuration = channel.getConfiguration();
                        int index = Integer.parseInt(configuration.get(PROPERTY_INDEX).toString());
                        updateState(channelUID, openUVData.getSafeExposureTime().getSafeExposure(index));
                    }
                    break;
            }
        }
    }

    private @NonNull State getAsRGB(float uv) {
        if (uv >= 11) {
            return HSBType.fromRGB(106, 27, 154);
        } else if (uv >= 8) {
            return HSBType.fromRGB(183, 28, 28);
        } else if (uv >= 6) {
            return HSBType.fromRGB(239, 108, 0);
        } else if (uv >= 3) {
            return HSBType.fromRGB(249, 168, 37);
        } else {
            return HSBType.fromRGB(85, 139, 47);
        }
    }

    /**
     * Build request URL from configuration data
     *
     * @return a valid URL for the OpenUV service
     */
    private String buildRequestURL() {
        OpenUVConfiguration config = getConfigAs(OpenUVConfiguration.class);

        String urlStr = "https://api.openuv.io/api/v1/uv?lat=" + config.getLatitude();
        urlStr += "&lng=" + config.getLongitude();

        if (config.getAltitude() != null) {
            urlStr += "&alt=" + config.getAltitude();
        }

        return urlStr;
    }

    /**
     * Request new UV Index data to the service
     *
     */
    private void getOpenUVData() {
        OpenUVConfiguration config = getConfigAs(OpenUVConfiguration.class);

        Properties header = new Properties();
        header.put("x-access-token", config.apikey);

        try {
            String jsonData = HttpUtil.executeUrl("GET", buildRequestURL(), header, null, null, 2000);

            logger.debug("URL = {}", jsonData);
            OpenUVJsonResponse result = gson.fromJson(jsonData, OpenUVJsonResponse.class);

            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            openUVData = result.getResult();

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }

    }

}
