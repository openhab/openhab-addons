/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.co2signal.handler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.co2signal.CO2SignalBindingConstants;
import org.openhab.binding.co2signal.internal.CO2SignalConfiguration;
import org.openhab.binding.co2signal.internal.json.CO2SignalJsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link CO2SignalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jens Viebig - Initial contribution
 */
public class CO2SignalHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(CO2SignalHandler.class);

    private static final String URL = "https://api.co2signal.com/v1/latest";

    private static final int DEFAULT_REFRESH_MINUTES = 10;

    private ScheduledFuture<?> refreshJob;

    private CO2SignalJsonResponse co2Response;

    private final Gson gson = new Gson();

    public CO2SignalHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing CO2 Signal handler.");

        CO2SignalConfiguration config = getConfigAs(CO2SignalConfiguration.class);
        logger.debug("config apikey = (omitted from logging)");
        logger.debug("config location = {}", config.location);
        logger.debug("config countryCode = {}", config.countryCode);
        logger.debug("config refresh = {}", config.refresh);

        boolean validConfig = true;
        List<String> errorMsg = new ArrayList<>();

        if (StringUtils.trimToNull(config.apikey) == null) {
            errorMsg.add("Parameter 'apikey' is mandatory and must be configured");
            validConfig = false;
        }
        if (StringUtils.trimToNull(config.location) == null && config.countryCode == null) {
            errorMsg.add("Parameter 'location' or 'stationId' is mandatory and must be configured");
            validConfig = false;
        }
        if (config.refresh != null && config.refresh < 1) {
            errorMsg.add("Parameter 'refresh' is mandatory and must be at least 1 minute");
            validConfig = false;
        }

        if (validConfig) {
            updateStatus(ThingStatus.ONLINE);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.join(",", errorMsg));
        }
    }

    /**
     * Start the job refreshing the CO2 Signal data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {

            Runnable runnable = () -> {
                try {
                    // Request new CO2 Signal data to the co2cn.org service
                    co2Response = updateCO2SignalData();

                    // Update all channels from the updated co2 data
                    for (Channel channel : getThing().getChannels()) {
                        updateChannel(channel.getUID().getId(), co2Response);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                }
            };

            CO2SignalConfiguration config = getConfigAs(CO2SignalConfiguration.class);
            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_MINUTES;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the CO2 Signal handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId(), co2Response);
        } else {
            logger.debug("The CO2 Signal binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * Update the channel from the last CO2 Signal data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId, CO2SignalJsonResponse co2Response) {
        if (isLinked(channelId)) {
            Object value;
            try {
                value = getValue(channelId, co2Response);
            } catch (Exception e) {
                logger.debug("Station doesn't provide {} measurement", channelId.toUpperCase());
                return;
            }

            State state = null;
            if (value == null) {
                state = UnDefType.UNDEF;
            } else if (value instanceof Double) {
                state = new DecimalType((Double) value);
            } else if (value instanceof String) {
                state = new StringType((String) value);
            } else {
                logger.warn("Update channel {}: Unsupported value type {}", channelId,
                        value.getClass().getSimpleName());
            }
            logger.debug("Update channel {} with state {} ({})", channelId, state,
                    (value == null) ? "null" : value.getClass().getSimpleName());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            }
        }
    }

    /**
     * Get new data from co2cn.org service
     *
     * @return {CO2SignalJsonResponse}
     */
    private CO2SignalJsonResponse updateCO2SignalData() {
        CO2SignalConfiguration config = getConfigAs(CO2SignalConfiguration.class);
        return getCO2SignalData(config);
    }

    /**
     * Request new CO2 Signal data to the co2signal.com service
     *
     * @param location geo-coordinates from config
     * @param countryCode station ID from config
     *
     * @return the CO2 Signal data object mapping the JSON response or null in case of error
     */
    private CO2SignalJsonResponse getCO2SignalData(CO2SignalConfiguration config) {
        CO2SignalJsonResponse result = null;
        boolean resultOk = false;
        String errorMsg = null;

        try {
            String urlStr = buildCO2SignalUrl(config);

            logger.debug("URL = {}", urlStr);

            Properties httpHeaders = new Properties();
            httpHeaders.put("User-Agent", "Mozilla/5.0");
            httpHeaders.put("auth-token", config.apikey);

            String response = HttpUtil.executeUrl("GET", urlStr, httpHeaders, null, null, 5000);
            logger.debug("co2Response = {}", response);

            // Map the JSON response to an object
            result = gson.fromJson(response, CO2SignalJsonResponse.class);

            if (result == null) {
                errorMsg = "no data returned";
            } else if (result.getData() != null && result.getStatus() != "error") {
                resultOk = true;
            } else {
                errorMsg = "missing data sub-object";
            }

            if (!resultOk) {
                logger.warn("Error in co2cn.org (CO2 Signal) response: {}", errorMsg);
            }
        } catch (MalformedURLException e) {
            errorMsg = e.getMessage();
            logger.warn("Constructed url is not valid: {}", errorMsg);
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.warn("Error running co2cn.org (CO2 Signal) request: {}", errorMsg);
        } catch (IOException | IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        // Update the thing status
        if (resultOk) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "OK");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        }

        return resultOk ? result : null;
    }

    private String buildCO2SignalUrl(CO2SignalConfiguration config) {
        UriBuilder builder = UriBuilder.fromPath(URL);

        String location = StringUtils.trimToEmpty(config.location);
        String countryCode = StringUtils.trimToEmpty(config.countryCode);

        if (location != null && !"".equals(location)) {
            location = location.replace(" ", "").replace("\"", "").replace("'", "").trim();
            String[] latLon = location.split(",");
            builder = builder.queryParam("lat", latLon[0]).queryParam("lon", latLon[1]);
        } else {
            builder = builder.queryParam("countryCode", countryCode);
        }
        String urlStr = builder.build().toString();
        return urlStr;
    }

    public static Object getValue(String channelId, CO2SignalJsonResponse data) throws Exception {
        String[] fields = StringUtils.split(channelId, "#");

        if (data == null) {
            return null;
        }

        String fieldName = fields[0];

        switch (fieldName) {
            case CO2SignalBindingConstants.COUNTRYCODE:
                return data.getCountryCode();
            case CO2SignalBindingConstants.CARBONINTENSITY:
                if (data.getData() == null) {
                    return null;
                }
                return data.getData().getCarbonIntensity();
            case CO2SignalBindingConstants.FOSSILFUELPERCENTAGE:
                if (data.getData() == null) {
                    return null;
                }
                return data.getData().getFossilFuelPercentage();
        }

        return null;
    }

}
