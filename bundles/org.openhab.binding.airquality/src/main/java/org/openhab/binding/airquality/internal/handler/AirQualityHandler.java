/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airquality.internal.AirQualityConfiguration;
import org.openhab.binding.airquality.internal.json.AirQualityData;
import org.openhab.binding.airquality.internal.json.AirQualityData.AlertLevel;
import org.openhab.binding.airquality.internal.json.AirQualityResponse;
import org.openhab.binding.airquality.internal.json.AirQualityResponse.ResponseStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    private static final String URL = "http://api.waqi.info/feed/%query%/?token=%apikey%";
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);

    private static final Map<AlertLevel, State> ALERT_COLORS = Map.of(AlertLevel.HAZARDOUS, HSBType.fromRGB(126, 0, 35),
            AlertLevel.VERY_UNHEALTHY, HSBType.fromRGB(143, 63, 151), AlertLevel.UNHEALTHY, HSBType.fromRGB(255, 0, 0),
            AlertLevel.UNHEALTHY_FOR_SENSITIVE, HSBType.fromRGB(255, 126, 0), AlertLevel.MODERATE,
            HSBType.fromRGB(255, 255, 0), AlertLevel.GOOD, HSBType.fromRGB(0, 228, 0));

    private final Logger logger = LoggerFactory.getLogger(AirQualityHandler.class);
    private final Gson gson;
    private final TimeZoneProvider timeZoneProvider;
    private final LocationProvider locationProvider;

    private @Nullable ScheduledFuture<?> refreshJob;
    private int retryCounter = 0;

    public AirQualityHandler(Thing thing, Gson gson, TimeZoneProvider timeZoneProvider,
            LocationProvider locationProvider) {
        super(thing);
        this.gson = gson;
        this.timeZoneProvider = timeZoneProvider;
        this.locationProvider = locationProvider;
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
        if (config.location.trim().isEmpty() && config.stationId == 0) {
            errorMsg.add("Parameter 'location' or 'stationId' is mandatory and must be configured");
        }
        if (config.refresh < 30) {
            errorMsg.add("Parameter 'refresh' must be at least 30 minutes");
        }

        if (errorMsg.isEmpty()) {
            if (thing.getProperties().isEmpty()) {
                Map<String, String> properties = discoverAttributes(config);
                updateProperties(properties);
            }

            ScheduledFuture<?> job = this.refreshJob;
            if (job == null || job.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublishData, 0, config.refresh,
                        TimeUnit.MINUTES);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.join(", ", errorMsg));
        }
    }

    private Map<String, String> discoverAttributes(AirQualityConfiguration config) {
        Map<String, String> properties = new HashMap<>();

        getAirQualityData().ifPresent(data -> {
            ThingBuilder thingBuilder = editThing();
            List<Channel> channels = new ArrayList<>(getThing().getChannels());
            PointType stationLocation = new PointType(data.getCity().getGeo());

            Configuration thingConfig = editConfiguration();
            thingConfig.put(AirQualityConfiguration.STATION_ID, data.getStationId());
            thingConfig.put(AirQualityConfiguration.LOCATION, stationLocation.toString());
            updateConfiguration(thingConfig);

            properties.put(ATTRIBUTIONS, data.getAttributions());
            properties.put(CITY, data.getCity().getName());
            PointType serverLocation = locationProvider.getLocation();
            if (serverLocation != null) {
                DecimalType distance = serverLocation.distanceFrom(stationLocation);
                properties.put(DISTANCE, new QuantityType<>(distance, SIUnits.METRE).toString());
            }

            POLLUTOR_CHANNEL_IDS.forEach(id -> {
                double value = data.getIaqiValue(id);
                if (value == -1) {
                    channels.removeIf(channel -> channel.getUID().getId().contains(id));
                }
            });

            thingBuilder.withChannels(channels);
            updateThing(thingBuilder.build());

        });
        return properties;
    }

    private void updateAndPublishData() {
        retryCounter = 0;
        getAirQualityData().ifPresent(data -> {
            // Update all channels from the updated AQI data
            getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID().getId())).forEach(channel -> {
                String channelId = channel.getUID().getIdWithoutGroup();
                State state = getValue(channelId, data);
                updateState(channelId, state);
            });
        });
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
            return;
        }
        logger.debug("The Air Quality binding is read-only and can not handle command {}", command);
    }

    /**
     * Build request URL from configuration data
     *
     * @return a valid URL for the aqicn.org service
     */
    private String buildRequestURL() {
        AirQualityConfiguration config = getConfigAs(AirQualityConfiguration.class);

        Integer stationId = config.stationId;

        String geoStr = stationId == 0
                ? String.format("geo:%s",
                        config.location.replace(" ", "").replace(",", ";").replace("\"", "").replace("'", "").trim())
                : String.format("@%d", stationId);

        return URL.replace("%apikey%", config.apikey.trim()).replace("%query%", geoStr);
    }

    /**
     * Request new air quality data to the aqicn.org service
     *
     * @return an optional air quality data object mapping the JSON response
     */
    private Optional<AirQualityData> getAirQualityData() {
        String errorMsg = "";

        String urlStr = buildRequestURL();
        logger.debug("URL = {}", urlStr);

        try {
            String response = HttpUtil.executeUrl("GET", urlStr, null, null, null, REQUEST_TIMEOUT_MS);
            logger.debug("aqiResponse = {}", response);
            AirQualityResponse result = gson.fromJson(response, AirQualityResponse.class);
            if (result != null && result.getStatus() == ResponseStatus.OK) {
                updateStatus(ThingStatus.ONLINE);
                return Optional.of(result.getData());
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
        return Optional.empty();
    }

    private State getValue(String channelId, AirQualityData aqiResponse) {
        switch (channelId) {
            case ALERT:
                return new DecimalType(aqiResponse.getAlertLevel().ordinal());
            case AQI:
                return new DecimalType(aqiResponse.getAqi());
            case AQIDESCRIPTION:
                return new StringType(aqiResponse.getAlertLevel().name());
            case PM25:
            case PM10:
            case O3:
            case NO2:
            case CO:
            case SO2:
                double value = aqiResponse.getIaqiValue(channelId);
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
            case OBSERVATIONTIME:
                return new DateTimeType(
                        aqiResponse.getTime().getObservationTime().withZoneSameLocal(timeZoneProvider.getTimeZone()));
            case DOMINENTPOL:
                return new StringType(aqiResponse.getDominentPol());
            case AQI_COLOR:
                return ALERT_COLORS.getOrDefault(aqiResponse.getAlertLevel(), UnDefType.UNDEF);
            default:
                return UnDefType.UNDEF;
        }
    }
}
