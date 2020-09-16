/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.handler;

import static org.openhab.binding.airquality.internal.AirQualityBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
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
import org.openhab.binding.airquality.internal.AirQualityConfiguration;
import org.openhab.binding.airquality.internal.json.AirQualityJsonData;
import org.openhab.binding.airquality.internal.json.AirQualityJsonResponse;
import org.openhab.binding.airquality.internal.json.AirQualityJsonResponse.ResponseStatus;
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
@NonNullByDefault
public class AirQualityHandler extends BaseThingHandler {
    private static final String URL = "http://api.waqi.info/feed/%QUERY%/?token=%apikey%";
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);
    private final Logger logger = LoggerFactory.getLogger(AirQualityHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;

    private final Gson gson;

    private int retryCounter = 0;
    private final TimeZoneProvider timeZoneProvider;

    public AirQualityHandler(Thing thing, Gson gson, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.gson = gson;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Air Quality handler.");

        AirQualityConfiguration config = getConfigAs(AirQualityConfiguration.class);
        logger.debug("config apikey = (omitted from logging)");
        logger.debug("config location = {}", config.location);
        logger.debug("config stationId = {}", config.stationId);
        logger.debug("config refresh = {}", config.refresh);

        List<String> errorMsg = new ArrayList<>();

        if (config.apikey.trim().isEmpty()) {
            errorMsg.add("Parameter 'apikey' is mandatory and must be configured");
        }
        if (config.location.trim().isEmpty() && config.stationId == null) {
            errorMsg.add("Parameter 'location' or 'stationId' is mandatory and must be configured");
        }
        if (config.refresh < 30) {
            errorMsg.add("Parameter 'refresh' must be at least 30 minutes");
        }

        if (errorMsg.isEmpty()) {
            ScheduledFuture<?> job = this.refreshJob;
            if (job == null || job.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublishData, 0, config.refresh,
                        TimeUnit.MINUTES);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.join(", ", errorMsg));
        }
    }

    private void updateAndPublishData() {
        retryCounter = 0;
        AirQualityJsonData aqiResponse = getAirQualityData();
        if (aqiResponse != null) {
            // Update all channels from the updated AQI data
            getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID().getId())).forEach(channel -> {
                String channelId = channel.getUID().getId();
                State state = getValue(channelId, aqiResponse);
                updateState(channelId, state);
            });
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Air Quality handler.");
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublishData();
        } else {
            logger.debug("The Air Quality binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * Build request URL from configuration data
     *
     * @return a valid URL for the aqicn.org service
     */
    private String buildRequestURL() {
        AirQualityConfiguration config = getConfigAs(AirQualityConfiguration.class);

        String location = config.location.trim();
        Integer stationId = config.stationId;

        String geoStr = "geo:" + location.replace(" ", "").replace(",", ";").replace("\"", "").replace("'", "").trim();

        String urlStr = URL.replace("%apikey%", config.apikey.trim());

        return urlStr.replace("%QUERY%", stationId == null ? geoStr : "@" + stationId);
    }

    /**
     * Request new air quality data to the aqicn.org service
     *
     * @param location geo-coordinates from config
     * @param stationId station ID from config
     * @return the air quality data object mapping the JSON response or null in case of error
     */
    private @Nullable AirQualityJsonData getAirQualityData() {
        String errorMsg;

        String urlStr = buildRequestURL();
        logger.debug("URL = {}", urlStr);

        try {
            String response = HttpUtil.executeUrl("GET", urlStr, null, null, null, REQUEST_TIMEOUT_MS);
            logger.debug("aqiResponse = {}", response);
            AirQualityJsonResponse result = gson.fromJson(response, AirQualityJsonResponse.class);
            if (result.getStatus() == ResponseStatus.OK) {
                AirQualityJsonData data = result.getData();
                String attributions = data.getAttributions();
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, attributions);
                return data;
            } else {
                retryCounter++;
                if (retryCounter == 1) {
                    logger.warn("Error in aqicn.org, retrying once");
                    return getAirQualityData();
                }
                errorMsg = "Missing data sub-object";
                logger.warn("Error in aqicn.org response: {}", errorMsg);
            }
        } catch (IOException e) {
            errorMsg = e.getMessage();
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.warn("Error running aqicn.org request: {}", errorMsg);
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        return null;
    }

    public State getValue(String channelId, AirQualityJsonData aqiResponse) {
        String[] fields = channelId.split("#");

        switch (fields[0]) {
            case AQI:
                return new DecimalType(aqiResponse.getAqi());
            case AQIDESCRIPTION:
                return getAqiDescription(aqiResponse.getAqi());
            case PM25:
            case PM10:
            case O3:
            case NO2:
            case CO:
            case SO2:
                double value = aqiResponse.getIaqiValue(fields[0]);
                return value != -1 ? new DecimalType(value) : UnDefType.UNDEF;
            case TEMPERATURE:
                double temp = aqiResponse.getIaqiValue("t");
                return temp != -1 ? new QuantityType<>(temp, API_TEMPERATURE_UNIT) : UnDefType.UNDEF;
            case PRESSURE:
                double press = aqiResponse.getIaqiValue("p");
                return press != -1 ? new QuantityType<>(press, API_PRESSURE_UNIT) : UnDefType.UNDEF;
            case HUMIDITY:
                double hum = aqiResponse.getIaqiValue("h");
                return hum != -1 ? new QuantityType<>(hum, API_HUMIDITY_UNIT) : UnDefType.UNDEF;
            case LOCATIONNAME:
                return new StringType(aqiResponse.getCity().getName());
            case STATIONID:
                return new DecimalType(aqiResponse.getStationId());
            case STATIONLOCATION:
                return new PointType(aqiResponse.getCity().getGeo());
            case OBSERVATIONTIME:
                return new DateTimeType(
                        aqiResponse.getTime().getObservationTime().withZoneSameLocal(timeZoneProvider.getTimeZone()));
            case DOMINENTPOL:
                return new StringType(aqiResponse.getDominentPol());
            case AQI_COLOR:
                return getAsHSB(aqiResponse.getAqi());
            default:
                return UnDefType.UNDEF;
        }
    }

    /**
     * Interprets the current aqi value within the ranges;
     * Returns AQI in a human readable format
     *
     * @return
     */
    public State getAqiDescription(int index) {
        if (index >= 300) {
            return HAZARDOUS;
        } else if (index >= 201) {
            return VERY_UNHEALTHY;
        } else if (index >= 151) {
            return UNHEALTHY;
        } else if (index >= 101) {
            return UNHEALTHY_FOR_SENSITIVE;
        } else if (index >= 51) {
            return MODERATE;
        } else if (index > 0) {
            return GOOD;
        }
        return UnDefType.UNDEF;
    }

    private State getAsHSB(int index) {
        State state = getAqiDescription(index);
        if (state == HAZARDOUS) {
            return HSBType.fromRGB(343, 100, 49);
        } else if (state == VERY_UNHEALTHY) {
            return HSBType.fromRGB(280, 100, 60);
        } else if (state == UNHEALTHY) {
            return HSBType.fromRGB(345, 100, 80);
        } else if (state == UNHEALTHY_FOR_SENSITIVE) {
            return HSBType.fromRGB(30, 80, 100);
        } else if (state == MODERATE) {
            return HSBType.fromRGB(50, 80, 100);
        } else if (state == GOOD) {
            return HSBType.fromRGB(160, 100, 60);
        }
        return UnDefType.UNDEF;
    }
}
