/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.handler;

import static org.openhab.binding.airquality.AirQualityBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
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
import org.openhab.binding.airquality.internal.AirQualityConfiguration;
import org.openhab.binding.airquality.json.AirQualityJsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AirQualityHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kuba Wolanin - Initial contribution
 * @author Łukasz Dywicki - Initial contribution
 */
public class AirQualityHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(AirQualityHandler.class);

    private static final String URL = "http://api.waqi.info/feed/%QUERY%/?token=%apikey%";

    private static final int DEFAULT_REFRESH_PERIOD = 30;

    private ScheduledFuture<?> refreshJob;

    private AirQualityJsonResponse aqiResponse;

    private Gson gson;

    private int retryCounter = 0;

    public AirQualityHandler(Thing thing) {
        super(thing);
        gson = new Gson();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Air Quality handler.");

        AirQualityConfiguration config = getConfigAs(AirQualityConfiguration.class);
        logger.debug("config apikey = (omitted from logging)");
        logger.debug("config location = {}", config.location);
        logger.debug("config stationId = {}", config.stationId);
        logger.debug("config refresh = {}", config.refresh);

        String errorMsg = null;

        if (StringUtils.trimToNull(config.apikey) == null) {
            errorMsg = "Parameter 'apikey' is mandatory and must be configured";
        }
        if (StringUtils.trimToNull(config.location) == null && config.stationId == null) {
            errorMsg = "Parameter 'location' or 'stationId' is mandatory and must be configured";
        }
        if (config.refresh != null && config.refresh < 5) {
            errorMsg = "Parameter 'refresh' must be at least 5 minutes";
        }

        if (errorMsg == null) {
            updateStatus(ThingStatus.ONLINE);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job refreshing the Air Quality data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                try {
                    // Request new air quality data to the aqicn.org service
                    retryCounter = 0;
                    aqiResponse = getAirQualityData();

                    // Update all channels from the updated AQI data
                    for (Channel channel : getThing().getChannels()) {
                        updateChannel(channel.getUID().getId(), aqiResponse);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                }
            };

            AirQualityConfiguration config = getConfigAs(AirQualityConfiguration.class);
            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Air Quality handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId(), aqiResponse);
        } else {
            logger.debug("The Air Quality binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * Update the channel from the last Air Quality data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId, AirQualityJsonResponse aqiResponse) {
        if (isLinked(channelId)) {
            Object value;
            try {
                value = getValue(channelId, aqiResponse);
            } catch (Exception e) {
                logger.debug("Station doesn't provide {} measurement", channelId.toUpperCase());
                return;
            }

            State state = null;
            if (value == null) {
                state = UnDefType.UNDEF;
            } else if (value instanceof PointType) {
                state = (PointType) value;
            } else if (value instanceof ZonedDateTime) {
                state = new DateTimeType((ZonedDateTime) value);
            } else if (value instanceof QuantityType<?>) {
                state = (QuantityType<?>) value;
            } else if (value instanceof BigDecimal) {
                state = new DecimalType((BigDecimal) value);
            } else if (value instanceof Integer) {
                state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
            } else if (value instanceof String) {
                state = new StringType(value.toString());
            } else {
                logger.warn("Update channel {}: Unsupported value type {}", channelId,
                        value.getClass().getSimpleName());
            }
            logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                    (value == null) ? "null" : value.getClass().getSimpleName());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            }
        }
    }

    /**
     * Build request URL from configuration data
     *
     * @return a valid URL for the aqicn.org service
     */
    private String buildRequestURL() {
        AirQualityConfiguration config = getConfigAs(AirQualityConfiguration.class);

        String location = StringUtils.trimToEmpty(config.location);
        Integer stationId = config.stationId;

        String geoStr = "geo:";
        geoStr += location.replace(" ", "").replace(",", ";").replace("\"", "").replace("'", "").trim();

        String urlStr = URL.replace("%apikey%", StringUtils.trimToEmpty(config.apikey));

        if (stationId == null) {
            urlStr = urlStr.replace("%QUERY%", geoStr);
        } else {
            urlStr = urlStr.replace("%QUERY%", "@" + stationId);
        }

        return urlStr;
    }

    /**
     * Request new air quality data to the aqicn.org service
     *
     * @param location geo-coordinates from config
     * @param stationId station ID from config
     * @return the air quality data object mapping the JSON response or null in case of error
     */
    private AirQualityJsonResponse getAirQualityData() {
        AirQualityJsonResponse result = null;
        String errorMsg = null;

        String urlStr = buildRequestURL();
        logger.debug("URL = {}", urlStr);

        try {
            // Run the HTTP request and get the JSON response from aqicn.org
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();

            try {
                String response = IOUtils.toString(connection.getInputStream());
                logger.debug("aqiResponse = {}", response);

                // Map the JSON response to an object
                result = gson.fromJson(response, AirQualityJsonResponse.class);
            } finally {
                IOUtils.closeQuietly(connection.getInputStream());
            }

            if (result.getData() != null && result.getStatus() != "error") {
                String attributions = result.getData().getAttributions();
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, attributions);
                return result;
            } else {
                retryCounter++;
                if (retryCounter == 1) {
                    logger.warn("Error in aqicn.org (Air Quality), retrying once");
                    return getAirQualityData();
                }
                errorMsg = "missing data sub-object";
                logger.warn("Error in aqicn.org (Air Quality) response: {}", errorMsg);
            }

        } catch (MalformedURLException e) {
            errorMsg = e.getMessage();
            logger.warn("Constructed url is not valid: {}", errorMsg);
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.warn("Error running aqicn.org (Air Quality) request: {}", errorMsg);
        } catch (IOException | IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        return null;
    }

    public static Object getValue(String channelId, AirQualityJsonResponse data) throws Exception {
        String[] fields = StringUtils.split(channelId, "#");

        if (data != null) {
            switch (fields[0]) {
                case AQI:
                    return data.getData().getAqi();
                case AQIDESCRIPTION:
                    return data.getData().getAqiDescription();
                case PM25:
                    return data.getData().getIaqi().getPm25();
                case PM10:
                    return data.getData().getIaqi().getPm10();
                case O3:
                    return data.getData().getIaqi().getO3();
                case NO2:
                    return data.getData().getIaqi().getNo2();
                case CO:
                    return data.getData().getIaqi().getCo();
                case LOCATIONNAME:
                    return data.getData().getCity().getName();
                case STATIONID:
                    return data.getData().getStationId();
                case STATIONLOCATION:
                    return new PointType(data.getData().getCity().getGeo());
                case OBSERVATIONTIME:
                    return data.getData().getTime().getObservationTime();
                case TEMPERATURE:
                    return new QuantityType<Temperature>(data.getData().getIaqi().getT(), API_TEMPERATURE_UNIT);
                case PRESSURE:
                    return new QuantityType<Pressure>(data.getData().getIaqi().getP(), API_PRESSURE_UNIT);
                case HUMIDITY:
                    return new QuantityType<>(data.getData().getIaqi().getH(), API_HUMIDITY_UNIT);
                case DOMINENTPOL:
                    return data.getData().getDominentPol();
            }
        }

        return null;
    }

}
