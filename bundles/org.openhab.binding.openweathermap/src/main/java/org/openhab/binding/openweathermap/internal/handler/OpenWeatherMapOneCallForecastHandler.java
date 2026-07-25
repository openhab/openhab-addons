/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.openweathermap.internal.handler;

import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;
import static org.openhab.core.types.TimeSeries.Policy.REPLACE;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonHourlyForecastData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallAPIData;
import org.openhab.binding.openweathermap.internal.dto.base.Clouds;
import org.openhab.binding.openweathermap.internal.dto.base.Precipitation;
import org.openhab.binding.openweathermap.internal.dto.base.Wind;
import org.openhab.binding.openweathermap.internal.dto.onecall.Hourly;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenWeatherMapOneCallForecastHandler} provides a unified 5-day weather forecast
 * TimeSeries by merging data from two APIs:
 * <ul>
 * <li>One Call API 3.0 — current conditions + hourly forecast (0–48 h)</li>
 * <li>Forecast5 API — 3-hourly forecast slots (0–120 h, free tier)</li>
 * </ul>
 *
 * <p>
 * The merge strategy appends Forecast5 slots whose {@code dt} is strictly greater than the last
 * One Call hourly slot's {@code dt}, producing a seamless 5-day TimeSeries.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapOneCallForecastHandler extends AbstractOpenWeatherMapHandler {

    /** Maximum number of Forecast5 slots to request (40 × 3 h = 120 h). */
    private static final int FORECAST5_SLOT_COUNT = 40;

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapOneCallForecastHandler.class);

    private @Nullable OpenWeatherMapOneCallAPIData oneCallData;
    private @Nullable OpenWeatherMapJsonHourlyForecastData forecast5Data;

    public OpenWeatherMapOneCallForecastHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Initialize OpenWeatherMapOneCallForecastHandler for thing '{}'.", getThing().getUID());
    }

    @Override
    protected boolean requestData(OpenWeatherMapConnection connection)
            throws CommunicationException, ConfigurationException {
        logger.debug("Update onecall-forecast data of thing '{}'.", getThing().getUID());
        try {
            // One Call 3.0: exclude minutely, daily and alerts — only current + hourly needed
            oneCallData = connection.getOneCallAPIData(location, true, false, true, true);
            // Forecast5: 40 slots × 3 h = 120 h coverage; slots beyond 48 h extend the TimeSeries
            forecast5Data = connection.getHourlyForecastData(location, FORECAST5_SLOT_COUNT);
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
        if (channelGroupId == null) {
            logger.debug("Cannot update {} as it has no GroupId", channelUID);
            return;
        }
        logger.debug("OneCallForecastHandler: updateChannel {}, groupID {}", channelUID, channelGroupId);
        switch (channelGroupId) {
            case CHANNEL_GROUP_ONECALL_CURRENT:
                updateCurrentChannel(channelUID);
                break;
            case CHANNEL_GROUP_FORECAST:
                updateForecastTimeSeries(channelUID);
                break;
            default:
                logger.warn("Unknown channel group '{}' for onecall-forecast thing.", channelGroupId);
                break;
        }
    }

    /**
     * Updates a channel in the {@code current} channel group from the One Call API current data.
     *
     * @param channelUID the channel to update
     */
    private void updateCurrentChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapOneCallAPIData localData = oneCallData;
        if (localData == null) {
            logger.debug("No One Call data available to update current channel '{}' of group '{}'.", channelId,
                    channelGroupId);
            return;
        }
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_STATION_LOCATION:
                state = getPointTypeState(localData.getLat(), localData.getLon());
                break;
            case CHANNEL_TIME_STAMP:
                state = getDateTimeTypeState(localData.getCurrent().getDt());
                break;
            case CHANNEL_SUNRISE:
                state = getDateTimeTypeState(localData.getCurrent().getSunrise());
                break;
            case CHANNEL_SUNSET:
                state = getDateTimeTypeState(localData.getCurrent().getSunset());
                break;
            case CHANNEL_CONDITION:
                if (!localData.getCurrent().getWeather().isEmpty()) {
                    state = getStringTypeState(localData.getCurrent().getWeather().get(0).getDescription());
                }
                break;
            case CHANNEL_CONDITION_ID:
                if (!localData.getCurrent().getWeather().isEmpty()) {
                    state = getStringTypeState(Integer.toString(localData.getCurrent().getWeather().get(0).getId()));
                }
                break;
            case CHANNEL_CONDITION_ICON:
                if (!localData.getCurrent().getWeather().isEmpty()) {
                    state = getRawTypeState(OpenWeatherMapConnection
                            .getWeatherIcon(localData.getCurrent().getWeather().get(0).getIcon()));
                }
                break;
            case CHANNEL_CONDITION_ICON_ID:
                if (!localData.getCurrent().getWeather().isEmpty()) {
                    state = getStringTypeState(localData.getCurrent().getWeather().get(0).getIcon());
                }
                break;
            case CHANNEL_TEMPERATURE:
                state = getQuantityTypeState(localData.getCurrent().getTemp(), CELSIUS);
                break;
            case CHANNEL_APPARENT_TEMPERATURE:
                state = getQuantityTypeState(localData.getCurrent().getFeelsLike(), CELSIUS);
                break;
            case CHANNEL_PRESSURE:
                state = getQuantityTypeState(localData.getCurrent().getPressure(), HECTO(PASCAL));
                break;
            case CHANNEL_HUMIDITY:
                state = getQuantityTypeState(localData.getCurrent().getHumidity(), PERCENT);
                break;
            case CHANNEL_DEW_POINT:
                state = getQuantityTypeState(localData.getCurrent().getDewPoint(), CELSIUS);
                break;
            case CHANNEL_WIND_SPEED:
                state = getQuantityTypeState(localData.getCurrent().getWindSpeed(), METRE_PER_SECOND);
                break;
            case CHANNEL_WIND_DIRECTION:
                state = getQuantityTypeState(localData.getCurrent().getWindDeg(), DEGREE_ANGLE);
                break;
            case CHANNEL_GUST_SPEED:
                state = getQuantityTypeState(localData.getCurrent().getWindGust(), METRE_PER_SECOND);
                break;
            case CHANNEL_CLOUDINESS:
                state = getQuantityTypeState(localData.getCurrent().getClouds(), PERCENT);
                break;
            case CHANNEL_UVINDEX:
                state = getDecimalTypeState(localData.getCurrent().getUvi());
                break;
            case CHANNEL_RAIN:
                org.openhab.binding.openweathermap.internal.dto.onecall.Precipitation rain = localData.getCurrent()
                        .getRain();
                state = getQuantityTypeState(rain == null ? 0 : rain.get1h(), MILLI(METRE));
                break;
            case CHANNEL_SNOW:
                org.openhab.binding.openweathermap.internal.dto.onecall.Precipitation snow = localData.getCurrent()
                        .getSnow();
                state = getQuantityTypeState(snow == null ? 0 : snow.get1h(), MILLI(METRE));
                break;
            case CHANNEL_VISIBILITY:
                State visState = new QuantityType<>(localData.getCurrent().getVisibility(), METRE).toUnit(KILO(METRE));
                state = (visState == null ? state : visState);
                break;
            default:
                logger.warn("Unknown channel id '{}' in onecall-forecast current data.", channelId);
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }

    /**
     * Builds and sends the unified forecast {@link TimeSeries} for a single channel in the
     * {@code forecast} channel group.
     *
     * <p>
     * Assembly order:
     * <ol>
     * <li>All One Call 3.0 hourly slots (0–48 h) are added first.</li>
     * <li>Forecast5 slots whose {@code dt} is strictly greater than the last One Call hourly
     * slot's {@code dt} are appended (48–120 h).</li>
     * </ol>
     *
     * @param channelUID the channel to update
     */
    private void updateForecastTimeSeries(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();

        if (CHANNEL_TIME_STAMP.equals(channelId)) {
            logger.debug("Channel '{}' of group '{}' is not a supported TimeSeries channel.", channelId,
                    channelGroupId);
            return;
        }

        TimeSeries timeSeries = new TimeSeries(REPLACE);
        long cutoffDt = Long.MIN_VALUE;

        // --- One Call 3.0 hourly data (0–48 h) ---
        OpenWeatherMapOneCallAPIData localOneCall = oneCallData;
        if (localOneCall != null && !localOneCall.getHourly().isEmpty()) {
            List<Hourly> hourlyList = localOneCall.getHourly();
            for (Hourly slot : hourlyList) {
                timeSeries.add(Instant.ofEpochSecond(slot.getDt()), getOneCallHourlyState(channelId, slot));
            }
            cutoffDt = hourlyList.get(hourlyList.size() - 1).getDt();
        }

        // --- Forecast5 data (48–120 h) ---
        OpenWeatherMapJsonHourlyForecastData localForecast5 = forecast5Data;
        if (localForecast5 != null && localForecast5.getList() != null) {
            final long cutoff = cutoffDt;
            localForecast5.getList().stream().filter(slot -> slot.getDt() > cutoff).forEach(slot -> {
                timeSeries.add(Instant.ofEpochSecond(slot.getDt()), getForecast5State(channelId, slot));
            });
        }

        logger.debug("Update channel '{}' of group '{}' with new TimeSeries '{}'.", channelId, channelGroupId,
                timeSeries);
        sendTimeSeries(channelUID, timeSeries);
    }

    /**
     * Extracts the state for a given channel from a One Call API hourly slot.
     *
     * @param channelId the channel identifier
     * @param slot the One Call hourly DTO
     * @return the channel state, or {@link UnDefType#UNDEF} if not available
     */
    private State getOneCallHourlyState(String channelId, Hourly slot) {
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_CONDITION:
                if (!slot.getWeather().isEmpty()) {
                    state = getStringTypeState(slot.getWeather().get(0).getDescription());
                }
                break;
            case CHANNEL_CONDITION_ID:
                if (!slot.getWeather().isEmpty()) {
                    state = getStringTypeState(Integer.toString(slot.getWeather().get(0).getId()));
                }
                break;
            case CHANNEL_CONDITION_ICON:
                if (!slot.getWeather().isEmpty()) {
                    state = getRawTypeState(
                            OpenWeatherMapConnection.getWeatherIcon(slot.getWeather().get(0).getIcon()));
                }
                break;
            case CHANNEL_CONDITION_ICON_ID:
                if (!slot.getWeather().isEmpty()) {
                    state = getStringTypeState(slot.getWeather().get(0).getIcon());
                }
                break;
            case CHANNEL_TEMPERATURE:
                state = getQuantityTypeState(slot.getTemp(), CELSIUS);
                break;
            case CHANNEL_APPARENT_TEMPERATURE:
                state = getQuantityTypeState(slot.getFeelsLike(), CELSIUS);
                break;
            case CHANNEL_PRESSURE:
                state = getQuantityTypeState(slot.getPressure(), HECTO(PASCAL));
                break;
            case CHANNEL_HUMIDITY:
                state = getQuantityTypeState(slot.getHumidity(), PERCENT);
                break;
            case CHANNEL_DEW_POINT:
                state = getQuantityTypeState(slot.getDewPoint(), CELSIUS);
                break;
            case CHANNEL_UVINDEX:
                state = getDecimalTypeState(slot.getUvi());
                break;
            case CHANNEL_WIND_SPEED:
                state = getQuantityTypeState(slot.getWindSpeed(), METRE_PER_SECOND);
                break;
            case CHANNEL_WIND_DIRECTION:
                state = getQuantityTypeState(slot.getWindDeg(), DEGREE_ANGLE);
                break;
            case CHANNEL_GUST_SPEED:
                state = getQuantityTypeState(slot.getWindGust(), METRE_PER_SECOND);
                break;
            case CHANNEL_CLOUDINESS:
                state = getQuantityTypeState(slot.getClouds(), PERCENT);
                break;
            case CHANNEL_PRECIP_PROBABILITY:
                state = getQuantityTypeState(slot.getPop() * 100.0, PERCENT);
                break;
            case CHANNEL_RAIN:
                org.openhab.binding.openweathermap.internal.dto.onecall.Precipitation rain = slot.getRain();
                state = getQuantityTypeState(rain == null ? 0 : rain.get1h(), MILLI(METRE));
                break;
            case CHANNEL_SNOW:
                org.openhab.binding.openweathermap.internal.dto.onecall.Precipitation snow = slot.getSnow();
                state = getQuantityTypeState(snow == null ? 0 : snow.get1h(), MILLI(METRE));
                break;
            case CHANNEL_VISIBILITY:
                State visState = new QuantityType<>(slot.getVisibility(), METRE).toUnit(KILO(METRE));
                state = (visState == null ? state : visState);
                break;
            default:
                logger.warn("Unknown channel id '{}' in onecall-forecast One Call hourly data.", channelId);
                break;
        }
        return state;
    }

    /**
     * Extracts the state for a given channel from a Forecast5 slot.
     *
     * <p>
     * Channels {@code dew-point} and {@code uvindex} are not provided by Forecast5 and will
     * return {@link UnDefType#UNDEF}.
     *
     * @param channelId the channel identifier
     * @param slot the Forecast5 list-item DTO
     * @return the channel state, or {@link UnDefType#UNDEF} if not available
     */
    private State getForecast5State(String channelId,
            org.openhab.binding.openweathermap.internal.dto.forecast.hourly.List slot) {
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_CONDITION:
                if (!slot.getWeather().isEmpty()) {
                    state = getStringTypeState(slot.getWeather().get(0).getDescription());
                }
                break;
            case CHANNEL_CONDITION_ID:
                if (!slot.getWeather().isEmpty()) {
                    state = getStringTypeState(Integer.toString(slot.getWeather().get(0).getId()));
                }
                break;
            case CHANNEL_CONDITION_ICON:
                if (!slot.getWeather().isEmpty()) {
                    state = getRawTypeState(
                            OpenWeatherMapConnection.getWeatherIcon(slot.getWeather().get(0).getIcon()));
                }
                break;
            case CHANNEL_CONDITION_ICON_ID:
                if (!slot.getWeather().isEmpty()) {
                    state = getStringTypeState(slot.getWeather().get(0).getIcon());
                }
                break;
            case CHANNEL_TEMPERATURE:
                state = getQuantityTypeState(slot.getMain().getTemp(), CELSIUS);
                break;
            case CHANNEL_APPARENT_TEMPERATURE:
                Double feelsLike = slot.getMain().getFeelsLikeTemp();
                if (feelsLike != null) {
                    state = getQuantityTypeState(feelsLike, CELSIUS);
                }
                break;
            case CHANNEL_PRESSURE:
                state = getQuantityTypeState(slot.getMain().getPressure(), HECTO(PASCAL));
                break;
            case CHANNEL_HUMIDITY:
                state = getQuantityTypeState(slot.getMain().getHumidity(), PERCENT);
                break;
            case CHANNEL_DEW_POINT:
                // Not available in Forecast5 API — returns UNDEF
                break;
            case CHANNEL_UVINDEX:
                // Not available in Forecast5 API — returns UNDEF
                break;
            case CHANNEL_WIND_SPEED:
                Wind wind = slot.getWind();
                state = getQuantityTypeState(wind.getSpeed(), METRE_PER_SECOND);
                break;
            case CHANNEL_WIND_DIRECTION:
                Wind windDir = slot.getWind();
                state = getQuantityTypeState(windDir.getDeg(), DEGREE_ANGLE);
                break;
            case CHANNEL_GUST_SPEED:
                Double gust = slot.getWind().getGust();
                if (gust != null) {
                    state = getQuantityTypeState(gust, METRE_PER_SECOND);
                }
                break;
            case CHANNEL_CLOUDINESS:
                Clouds clouds = slot.getClouds();
                state = getQuantityTypeState(clouds.getAll(), PERCENT);
                break;
            case CHANNEL_PRECIP_PROBABILITY:
                Double pop = slot.getPop();
                state = getQuantityTypeState(pop == null ? 0 : pop * 100.0, PERCENT);
                break;
            case CHANNEL_RAIN:
                Precipitation rain = slot.getRain();
                state = getQuantityTypeState(rain == null ? 0 : rain.getVolume(), MILLI(METRE));
                break;
            case CHANNEL_SNOW:
                Precipitation snow = slot.getSnow();
                state = getQuantityTypeState(snow == null ? 0 : snow.getVolume(), MILLI(METRE));
                break;
            case CHANNEL_VISIBILITY:
                Integer visibility = slot.getVisibility();
                if (visibility != null) {
                    State visState = new QuantityType<>(visibility, METRE).toUnit(KILO(METRE));
                    state = (visState == null ? state : visState);
                }
                break;
            default:
                logger.warn("Unknown channel id '{}' in onecall-forecast Forecast5 data.", channelId);
                break;
        }
        return state;
    }
}
