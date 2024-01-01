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
package org.openhab.binding.openweathermap.internal.handler;

import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapOneCallHistoryConfiguration;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallHistAPIData;
import org.openhab.binding.openweathermap.internal.dto.onecall.Precipitation;
import org.openhab.binding.openweathermap.internal.dto.onecallhist.Hourly;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenWeatherMapOneCallHistoryHandler} is responsible for handling commands, which are sent to one of
 * the channels.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapOneCallHistoryHandler extends AbstractOpenWeatherMapHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapOneCallHistoryHandler.class);

    private static final String CHANNEL_GROUP_HOURLY_FORECAST_PREFIX = "historyHours";
    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + "([0-9]*)");

    private @Nullable OpenWeatherMapOneCallHistAPIData weatherData;

    // the relative day in history.
    private int day = 0;

    public OpenWeatherMapOneCallHistoryHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        OpenWeatherMapOneCallHistoryConfiguration config = getConfigAs(OpenWeatherMapOneCallHistoryConfiguration.class);
        if (config.historyDay <= 0) {
            logger.warn("historyDay value of {} is not supported", config.historyDay);
            return;
        }
        /*
         * As of now, only 5 days in history are supported by the one call API. As this may change in the future,
         * we allow any value here and only log a warning if the value exceeds 5.
         */
        if (config.historyDay > 5) {
            logger.warn("History configuration of {} days may cause errors. You have been warned :-)",
                    config.historyDay);
        }
        day = config.historyDay;
        logger.debug("Initialize OpenWeatherMapOneCallHistoryHandler handler '{}' with historyDay {}.",
                getThing().getUID(), day);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected boolean requestData(OpenWeatherMapConnection connection)
            throws CommunicationException, ConfigurationException {
        logger.debug("Update weather and forecast data of thing '{}'.", getThing().getUID());
        try {
            weatherData = connection.getOneCallHistAPIData(location, day);
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
        switch (channelGroupId) {
            case CHANNEL_GROUP_ONECALL_HISTORY:
                updateHistoryCurrentChannel(channelUID);
                break;

            default:
                int i;
                Matcher hourlyForecastMatcher = CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (hourlyForecastMatcher.find() && (i = Integer.parseInt(hourlyForecastMatcher.group(1))) >= 1
                        && i <= 48) {
                    updateHourlyHistoryChannel(channelUID, i - 1);
                    break;
                }
                break;
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateHistoryCurrentChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapOneCallHistAPIData localWeatherData = weatherData;
        if (localWeatherData != null) {
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_STATION_LOCATION:
                    state = getPointTypeState(localWeatherData.getLat(), localWeatherData.getLon());
                    break;
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(localWeatherData.getCurrent().getDt());
                    break;
                case CHANNEL_SUNRISE:
                    state = getDateTimeTypeState(localWeatherData.getCurrent().getSunrise());
                    break;
                case CHANNEL_SUNSET:
                    state = getDateTimeTypeState(localWeatherData.getCurrent().getSunset());
                    break;
                case CHANNEL_CONDITION:
                    if (!localWeatherData.getCurrent().getWeather().isEmpty()) {
                        state = getStringTypeState(localWeatherData.getCurrent().getWeather().get(0).getDescription());
                    }
                    break;
                case CHANNEL_CONDITION_ID:
                    if (!localWeatherData.getCurrent().getWeather().isEmpty()) {
                        state = getStringTypeState(
                                Integer.toString(localWeatherData.getCurrent().getWeather().get(0).getId()));
                    }
                    break;
                case CHANNEL_CONDITION_ICON:
                    if (!localWeatherData.getCurrent().getWeather().isEmpty()) {
                        state = getRawTypeState(OpenWeatherMapConnection
                                .getWeatherIcon(localWeatherData.getCurrent().getWeather().get(0).getIcon()));
                    }
                    break;
                case CHANNEL_CONDITION_ICON_ID:
                    if (!localWeatherData.getCurrent().getWeather().isEmpty()) {
                        state = getStringTypeState(localWeatherData.getCurrent().getWeather().get(0).getIcon());
                    }
                    break;
                case CHANNEL_TEMPERATURE:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getTemp(), CELSIUS);
                    break;
                case CHANNEL_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getFeelsLike(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getHumidity(), PERCENT);
                    break;
                case CHANNEL_DEW_POINT:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getDewPoint(), CELSIUS);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getWindSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getWindDeg(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getWindGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getClouds(), PERCENT);
                    break;
                case CHANNEL_UVINDEX:
                    state = getDecimalTypeState(localWeatherData.getCurrent().getUvi());
                    break;
                case CHANNEL_RAIN:
                    Precipitation rain = localWeatherData.getCurrent().getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain.get1h(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Precipitation snow = localWeatherData.getCurrent().getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow.get1h(), MILLI(METRE));
                    break;
                case CHANNEL_VISIBILITY:
                    @Nullable
                    State tempstate = new QuantityType<>(localWeatherData.getCurrent().getVisibility(), METRE)
                            .toUnit(KILO(METRE));
                    state = (tempstate == null ? state : tempstate);
                    break;
                default:
                    // This should not happen
                    logger.warn("Unknown channel id {} in onecall current weather data", channelId);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count the number of the hour referenced by the channel
     */
    private void updateHourlyHistoryChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        logger.debug("Updating hourly history data for channel {}, group {}, count {}", channelId, channelGroupId,
                count);
        OpenWeatherMapOneCallHistAPIData localWeatherData = weatherData;
        if (localWeatherData != null && localWeatherData.getHourly().size() > count) {
            Hourly historyData = localWeatherData.getHourly().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(historyData.getDt());
                    break;
                case CHANNEL_CONDITION:
                    if (!historyData.getWeather().isEmpty()) {
                        state = getStringTypeState(historyData.getWeather().get(0).getDescription());
                    }
                    break;
                case CHANNEL_CONDITION_ID:
                    if (!historyData.getWeather().isEmpty()) {
                        state = getStringTypeState(Integer.toString(historyData.getWeather().get(0).getId()));
                    }
                    break;
                case CHANNEL_CONDITION_ICON:
                    if (!historyData.getWeather().isEmpty()) {
                        state = getRawTypeState(
                                OpenWeatherMapConnection.getWeatherIcon(historyData.getWeather().get(0).getIcon()));
                    }
                    break;
                case CHANNEL_CONDITION_ICON_ID:
                    if (!historyData.getWeather().isEmpty()) {
                        state = getStringTypeState(historyData.getWeather().get(0).getIcon());
                    }
                    break;
                case CHANNEL_TEMPERATURE:
                    state = getQuantityTypeState(historyData.getTemp(), CELSIUS);
                    break;
                case CHANNEL_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(historyData.getFeelsLike(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(historyData.getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(historyData.getHumidity(), PERCENT);
                    break;
                case CHANNEL_DEW_POINT:
                    state = getQuantityTypeState(historyData.getDewPoint(), CELSIUS);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(historyData.getWindSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(historyData.getWindDeg(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(historyData.getWindGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(historyData.getClouds(), PERCENT);
                    break;
                case CHANNEL_VISIBILITY:
                    @Nullable
                    State tempstate = new QuantityType<>(historyData.getVisibility(), METRE).toUnit(KILO(METRE));
                    state = (tempstate == null ? state : tempstate);
                case CHANNEL_RAIN:
                    Precipitation rain = historyData.getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain.get1h(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Precipitation snow = historyData.getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow.get1h(), MILLI(METRE));
                    break;
                default:
                    // This should not happen
                    logger.warn("Unknown channel id {} in onecall hourly weather data", channelId);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }
}
