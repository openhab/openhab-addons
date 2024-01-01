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
package org.openhab.binding.meteoblue.internal.handler;

import static org.openhab.core.library.unit.MetricPrefix.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openhab.binding.meteoblue.internal.Forecast;
import org.openhab.binding.meteoblue.internal.MeteoBlueConfiguration;
import org.openhab.binding.meteoblue.internal.json.JsonData;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link MeteoBlueHandler} is responsible for handling commands
 * sent to one of the channels.
 *
 * @author Chris Carman - Initial contribution
 */
public class MeteoBlueHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MeteoBlueHandler.class);

    private Bridge bridge;
    private Forecast[] forecasts;
    private Gson gson;
    private JsonData weatherData;
    private ScheduledFuture<?> refreshJob;
    private boolean properlyConfigured;

    public MeteoBlueHandler(Thing thing) {
        super(thing);
        gson = new Gson();
        forecasts = new Forecast[7];
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (properlyConfigured) {
            logger.debug("Received command '{}' for channel '{}'", command, channelUID);
            updateChannel(channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the meteoblue handler...");

        bridge = getBridge();
        if (bridge == null) {
            logger.warn("Unable to initialize meteoblue. No bridge was configured.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not configured.");
            return;
        }

        MeteoBlueConfiguration config = getConfigAs(MeteoBlueConfiguration.class);

        if (config.serviceType == null || config.serviceType.isBlank()) {
            config.serviceType = MeteoBlueConfiguration.SERVICETYPE_NONCOMM;
            logger.debug("Using default service type ({}).", config.serviceType);
            return;
        }

        if (config.location == null || config.location.isBlank()) {
            flagBadConfig("The location was not configured.");
            return;
        }

        config.parseLocation();

        if (config.latitude == null) {
            flagBadConfig(String.format("Could not determine latitude from the defined location setting (%s).",
                    config.location));
            return;
        }

        if (config.latitude > 90.0 || config.latitude < -90.0) {
            flagBadConfig(String.format("Specified latitude value (%d) is not valid.", config.latitude));
            return;
        }

        if (config.longitude == null) {
            flagBadConfig(String.format("Could not determine longitude from the defined location setting (%s).",
                    config.location));
            return;
        }

        if (config.longitude > 180.0 || config.longitude < -180.0) {
            flagBadConfig(String.format("Specified longitude value (%d) is not valid.", config.longitude));
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        startAutomaticRefresh(config);
        properlyConfigured = true;
    }

    /**
     * Marks the configuration as invalid.
     */
    private void flagBadConfig(String message) {
        properlyConfigured = false;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
    }

    /**
     * Schedule a job to periodically refresh the weather data.
     */
    private void startAutomaticRefresh(MeteoBlueConfiguration config) {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            logger.trace("Refresh job already exists.");
            return;
        }

        Runnable runnable = () -> {
            boolean updateSuccessful = false;

            try {
                // Request new weather data
                updateSuccessful = updateWeatherData();

                if (updateSuccessful) {
                    // build forecasts from the data
                    for (int i = 0; i < 7; i++) {
                        forecasts[i] = new Forecast(i, weatherData.getMetadata(), weatherData.getUnits(),
                                weatherData.getDataDay());
                    }

                    // Update all channels from the updated weather data
                    for (Channel channel : getThing().getChannels()) {
                        updateChannel(channel.getUID().getId());
                    }
                }
            } catch (Exception e) {
                logger.warn("Exception occurred during weather update: {}", e.getMessage(), e);
            }
        };

        int period = config.refresh != null ? config.refresh : MeteoBlueConfiguration.DEFAULT_REFRESH;
        refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, period, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing meteoblue handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    /**
     * Update the channel from the last weather data retrieved.
     *
     * @param channelId the id of the channel to be updated
     */
    private void updateChannel(String channelId) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null || !isLinked(channelId)) {
            logger.trace("Channel '{}' was null or not linked! Not updated.", channelId);
            return;
        }

        // get the set of channel parameters.
        // the first will be the forecast day (eg. forecastToday),
        // and the second will be the datapoint (eg. snowFraction)
        String[] channelParts = channelId.split("#");
        String forecastDay = channelParts[0];
        String datapointName = channelParts[1];
        if (channelParts.length != 2) {
            logger.debug("Skipped invalid channelId '{}'", channelId);
            return;
        }

        logger.debug("Updating channel '{}'", channelId);
        Forecast forecast = getForecast(forecastDay);
        if (forecast == null) {
            logger.debug("No forecast found for '{}'. Not updating.", forecastDay);
            return;
        }

        Object datapoint = forecast.getDatapoint(datapointName);
        logger.debug("Value for datapoint '{}' is '{}'", datapointName, datapoint);
        if (datapoint == null) {
            logger.debug("Couldn't get datapoint '{}' for '{}'. Not updating.", datapointName, forecastDay);
            return;
        }

        // Build a State from this value
        State state = null;
        if (datapoint instanceof Calendar calendar) {
            state = new DateTimeType(ZonedDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()));
        } else if (datapoint instanceof Integer) {
            state = getStateForType(channel.getAcceptedItemType(), (Integer) datapoint);
        } else if (datapoint instanceof Number) {
            BigDecimal decimalValue = new BigDecimal(datapoint.toString()).setScale(2, RoundingMode.HALF_UP);
            state = getStateForType(channel.getAcceptedItemType(), decimalValue);
        } else if (datapoint instanceof String) {
            state = new StringType(datapoint.toString());
        } else if (datapoint instanceof BufferedImage image) {
            ImageItem item = new ImageItem("rain area");
            state = new RawType(renderImage(image), "image/png");
            item.setState(state);
        } else {
            logger.debug("Unsupported value type {}", datapoint.getClass().getSimpleName());
        }

        // Update the channel
        if (state != null) {
            logger.trace("Updating channel with state value {}. (object type {})", state,
                    datapoint.getClass().getSimpleName());
            updateState(channelId, state);
        }
    }

    private State getStateForType(String type, Integer value) {
        return getStateForType(type, new BigDecimal(value));
    }

    private State getStateForType(String type, BigDecimal value) {
        State state = new DecimalType(value);

        if ("Number:Temperature".equals(type)) {
            state = new QuantityType<>(value, SIUnits.CELSIUS);
        } else if ("Number:Length".equals(type)) {
            state = new QuantityType<>(value, MILLI(SIUnits.METRE));
        } else if ("Number:Pressure".equals(type)) {
            state = new QuantityType<>(value, HECTO(SIUnits.PASCAL));
        } else if ("Number:Speed".equals(type)) {
            state = new QuantityType<>(value, Units.METRE_PER_SECOND);
        }

        return state;
    }

    // Request new weather data from the service
    private boolean updateWeatherData() {
        if (bridge == null) {
            logger.debug("Unable to update weather data. Bridge missing.");
            return false;
        }

        MeteoBlueBridgeHandler handler = (MeteoBlueBridgeHandler) bridge.getHandler();
        if (handler == null) {
            logger.debug("Unable to update weather data. Handler missing.");
            return false;
        }

        String apiKey = handler.getApiKey();

        logger.debug("Updating weather data...");
        MeteoBlueConfiguration config = getConfigAs(MeteoBlueConfiguration.class);
        config.parseLocation();
        String serviceType = config.serviceType;

        if (serviceType.equals(MeteoBlueConfiguration.SERVICETYPE_COMM)) {
            logger.debug("Fetching weather data using Commercial API.");
        } else {
            logger.debug("Fetching weather data using NonCommercial API.");
        }

        // get the base url for the HTTP query
        String url = MeteoBlueConfiguration.getURL(serviceType);
        url = url.replace("#API_KEY#", apiKey);
        url = url.replace("#LATITUDE#", String.valueOf(config.latitude)).replace("#LONGITUDE#",
                String.valueOf(config.longitude));

        // fill in any optional parameters for the HTTP query
        StringBuilder builder = new StringBuilder();

        if (config.altitude != null) {
            builder.append("&asl=" + config.altitude);
        }
        if (config.timeZone != null && !config.timeZone.isBlank()) {
            builder.append("&tz=" + config.timeZone);
        }
        url = url.replace("#FORMAT_PARAMS#", builder.toString());
        logger.trace("Using URL '{}'", url);

        // Run the HTTP request and get the JSON response
        String httpResponse = getWeatherData(url);
        if (httpResponse == null) {
            return false;
        }
        JsonData jsonResult = translateJson(httpResponse, serviceType);
        logger.trace("json object: {}", jsonResult);

        if (jsonResult == null) {
            logger.warn("No data was received from the weather service");
            return false;
        }

        String errorMessage = jsonResult.getErrorMessage();
        if (errorMessage != null) {
            if ("MB_REQUEST::DISPATCH: Invalid api key".equals(errorMessage)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid API Key");
            } else if ("MB_REQUEST::DISPATCH: This datafeed is not authorized for your api key".equals(errorMessage)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "API Key not authorized for this datafeed");
            } else {
                logger.warn("Failed to retrieve weather data due to unexpected error. Error message: {}", errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
            return false;
        }

        weatherData = jsonResult;
        updateStatus(ThingStatus.ONLINE);
        return true;
    }

    // Run the HTTP request and get the JSON response
    private String getWeatherData(String url) {
        try {
            String httpResponse = HttpUtil.executeUrl("GET", url, 30 * 1000);
            logger.trace("http response: {}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.debug("I/O Exception occurred while retrieving weather data.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "I/O Exception occurred while retrieving weather data.");
            return null;
        }
    }

    // Convert a json string response into a json data object
    private JsonData translateJson(String stringData, String serviceType) {
        // JsonData weatherData = null;

        // For now, no distinction is made between commercial and non-commercial data;
        // This may need to be changed later based on user feedback.
        /*
         * if (serviceType.equals(MeteoBlueConfiguration.SERVICETYPE_COMM)) {
         * weatherData = gson.fromJson(httpResponse, JsonCommercialData.class);
         * }
         * else {
         * weatherData = gson.fromJson(httpResponse, JsonNonCommercialData.class);
         * }
         */

        return gson.fromJson(stringData, JsonData.class);
    }

    private Forecast getForecast(String which) {
        switch (which) {
            case "forecastToday":
                return forecasts[0];
            case "forecastTomorrow":
                return forecasts[1];
            case "forecastDay2":
                return forecasts[2];
            case "forecastDay3":
                return forecasts[3];
            case "forecastDay4":
                return forecasts[4];
            case "forecastDay5":
                return forecasts[5];
            case "forecastDay6":
                return forecasts[6];
            default:
                return null;
        }
    }

    private byte[] renderImage(BufferedImage image) {
        byte[] data = null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            out.flush();
            data = out.toByteArray();
            out.close();
        } catch (IOException ioe) {
            logger.debug("I/O exception occurred converting image data", ioe);
        }

        return data;
    }
}
