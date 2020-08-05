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
package org.openhab.binding.openweathermap.internal.handler;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.eclipse.smarthome.core.library.unit.SIUnits.*;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapWeatherAndForecastConfiguration;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapCommunicationException;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConfigurationException;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonOneCallAPIData;
import org.openhab.binding.openweathermap.internal.dto.base.Rain;
import org.openhab.binding.openweathermap.internal.dto.base.Snow;
import org.openhab.binding.openweathermap.internal.dto.forecast.daily.DailyForecast;
import org.openhab.binding.openweathermap.internal.dto.forecast.daily.FeelsLikeTemp;
import org.openhab.binding.openweathermap.internal.dto.forecast.hourly.HourlyForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenWeatherMapWeatherAndForecastHandler} is responsible for handling commands, which are sent to one of
 * the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapWeatherAndForecastHandler extends AbstractOpenWeatherMapHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapWeatherAndForecastHandler.class);

    private static final String CHANNEL_GROUP_HOURLY_FORECAST_PREFIX = "forecastHours";
    private static final String CHANNEL_GROUP_DAILY_FORECAST_PREFIX = "forecastDay";
    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + "([0-9]*)");

    // keeps track of the parsed counts
    private int forecastHours = 24;
    private int forecastDays = 6;

    private @Nullable OpenWeatherMapJsonOneCallAPIData oneCallData;

    public OpenWeatherMapWeatherAndForecastHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Initialize OpenWeatherMapWeatherAndForecastHandler handler '{}'.", getThing().getUID());
        OpenWeatherMapWeatherAndForecastConfiguration config = getConfigAs(
                OpenWeatherMapWeatherAndForecastConfiguration.class);

        boolean configValid = true;
        int newForecastHours = config.forecastHours;
        if (newForecastHours < 0 || newForecastHours > 120 || newForecastHours % 3 != 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-number-of-hours");
            configValid = false;
        }
        int newForecastDays = config.forecastDays;
        if (newForecastDays < 0 || newForecastDays > 16) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-number-of-days");
            configValid = false;
        }

        if (configValid) {
            logger.debug("Rebuilding thing '{}'.", getThing().getUID());
            List<Channel> toBeAddedChannels = new ArrayList<>();
            List<Channel> toBeRemovedChannels = new ArrayList<>();
            if (forecastHours != newForecastHours) {
                logger.debug("Rebuilding hourly forecast channel groups.");
                if (forecastHours > newForecastHours) {
                    for (int i = newForecastHours + 3; i <= forecastHours; i += 3) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i)));
                    }
                } else {
                    for (int i = forecastHours + 3; i <= newForecastHours; i += 3) {
                        toBeAddedChannels.addAll(createChannelsForGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i),
                                CHANNEL_GROUP_TYPE_HOURLY_FORECAST));
                    }
                }
                forecastHours = newForecastHours;
            }
            if (forecastDays != newForecastDays) {
                logger.debug("Rebuilding daily forecast channel groups.");
                if (forecastDays > newForecastDays) {
                    if (newForecastDays < 1) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(CHANNEL_GROUP_FORECAST_TODAY));
                    }
                    if (newForecastDays < 2) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(CHANNEL_GROUP_FORECAST_TOMORROW));
                    }
                    for (int i = newForecastDays; i < forecastDays; ++i) {
                        toBeRemovedChannels.addAll(
                                removeChannelsOfGroup(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + Integer.toString(i)));
                    }
                } else {
                    if (forecastDays == 0 && newForecastDays > 0) {
                        toBeAddedChannels.addAll(createChannelsForGroup(CHANNEL_GROUP_FORECAST_TODAY,
                                CHANNEL_GROUP_TYPE_DAILY_FORECAST));
                    }
                    if (forecastDays <= 1 && newForecastDays > 1) {
                        toBeAddedChannels.addAll(createChannelsForGroup(CHANNEL_GROUP_FORECAST_TOMORROW,
                                CHANNEL_GROUP_TYPE_DAILY_FORECAST));
                    }
                    for (int i = (forecastDays < 2) ? 2 : forecastDays; i < newForecastDays; ++i) {
                        toBeAddedChannels.addAll(
                                createChannelsForGroup(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + Integer.toString(i),
                                        CHANNEL_GROUP_TYPE_DAILY_FORECAST));
                    }
                }
                forecastDays = newForecastDays;
            }
            ThingBuilder builder = editThing().withoutChannels(toBeRemovedChannels);
            for (Channel channel : toBeAddedChannels) {
                builder.withChannel(channel);
            }
            updateThing(builder.build());
        }
    }

    @Override
    protected boolean requestData(OpenWeatherMapConnection connection)
            throws OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        logger.debug("Update weather and forecast data of thing '{}'.", getThing().getUID());
        try {
            oneCallData = connection.getOneCallData(location);
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
        if (channelGroupId != null) {
            switch (channelGroupId) {
                case CHANNEL_GROUP_STATION:
                case CHANNEL_GROUP_CURRENT_WEATHER:
                    updateCurrentChannel(channelUID);
                    break;
                case CHANNEL_GROUP_FORECAST_TODAY:
                    updateDailyForecastChannel(channelUID, 0);
                    break;
                case CHANNEL_GROUP_FORECAST_TOMORROW:
                    updateDailyForecastChannel(channelUID, 1);
                    break;
                default:
                    int i;
                    Matcher hourlyForecastMatcher = CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN
                            .matcher(channelGroupId);
                    if (hourlyForecastMatcher.find() && (i = Integer.parseInt(hourlyForecastMatcher.group(1))) >= 3
                            && i <= 120) {
                        updateHourlyForecastChannel(channelUID, (i / 3) - 1);
                        break;
                    }
                    Matcher dailyForecastMatcher = CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                    if (dailyForecastMatcher.find() && (i = Integer.parseInt(dailyForecastMatcher.group(1))) > 1
                            && i <= 16) {
                        updateDailyForecastChannel(channelUID, i);
                        break;
                    }
                    break;
            }
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateCurrentChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapJsonOneCallAPIData localWeatherData = oneCallData;
        if (localWeatherData != null) {
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_STATION_LOCATION:
                    state = getPointTypeState(localWeatherData.getLat(), localWeatherData.getLon());
                    break;
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(localWeatherData.getCurrent().getDt());
                    break;
                case CHANNEL_CONDITION:
                    if (!localWeatherData.getCurrent().getWeather().isEmpty()) {
                        state = getStringTypeState(localWeatherData.getCurrent().getWeather().get(0).getDescription());
                    }
                    break;
                case CHANNEL_CONDITION_ID:
                    if (!localWeatherData.getCurrent().getWeather().isEmpty()) {
                        state = getStringTypeState(
                                localWeatherData.getCurrent().getWeather().get(0).getId().toString());
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
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getWindSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getWindGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getWindDeg(), DEGREE_ANGLE);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(localWeatherData.getCurrent().getClouds(), PERCENT);
                    break;
                case CHANNEL_RAIN:
                    Rain rain = localWeatherData.getCurrent().getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain.getVolume(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Snow snow = localWeatherData.getCurrent().getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow.getVolume(), MILLI(METRE));
                    break;
                case CHANNEL_VISIBILITY:
                    Integer localVisibility = localWeatherData.getCurrent().getVisibility();
                    state = localVisibility == null ? UnDefType.UNDEF
                            : new QuantityType<>(localVisibility, METRE).toUnit(KILO(METRE));
                    if (state == null) {
                        logger.debug("State conversion failed, cannot update state.");
                        return;
                    }
                    break;
                case CHANNEL_UVINDEX:
                    state = getDecimalTypeState(localWeatherData.getCurrent().getUvi());
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
     * @param count
     */
    private void updateHourlyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapJsonOneCallAPIData localWeatherData = oneCallData;
        if (localWeatherData != null && localWeatherData.getHourly() != null
                && localWeatherData.getHourly().size() > count) {
            HourlyForecast forecastData = localWeatherData.getHourly().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getDt());
                    break;
                case CHANNEL_CONDITION:
                    if (!forecastData.getWeather().isEmpty()) {
                        state = getStringTypeState(forecastData.getWeather().get(0).getDescription());
                    }
                    break;
                case CHANNEL_CONDITION_ID:
                    if (!forecastData.getWeather().isEmpty()) {
                        state = getStringTypeState(forecastData.getWeather().get(0).getId().toString());
                    }
                    break;
                case CHANNEL_CONDITION_ICON:
                    if (!forecastData.getWeather().isEmpty()) {
                        state = getRawTypeState(
                                OpenWeatherMapConnection.getWeatherIcon(forecastData.getWeather().get(0).getIcon()));
                    }
                    break;
                case CHANNEL_CONDITION_ICON_ID:
                    if (!forecastData.getWeather().isEmpty()) {
                        state = getStringTypeState(forecastData.getWeather().get(0).getIcon());
                    }
                    break;
                case CHANNEL_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getTemp(), CELSIUS);
                    break;
                case CHANNEL_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getFeelsLike(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(forecastData.getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(forecastData.getHumidity(), PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(forecastData.getWindSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(forecastData.getWindDeg(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(forecastData.getGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(forecastData.getClouds(), PERCENT);
                    break;
                case CHANNEL_RAIN:
                    Rain rain = forecastData.getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain.getVolume(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Snow snow = forecastData.getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow.getVolume(), MILLI(METRE));
                    break;
                case CHANNEL_VISIBILITY:
                    Integer localVisibility = forecastData.getVisibility();
                    state = localVisibility == null ? UnDefType.UNDEF
                            : new QuantityType<>(localVisibility, METRE).toUnit(KILO(METRE));
                    if (state == null) {
                        logger.debug("State conversion failed, cannot update state.");
                        return;
                    }
                    break;
                case CHANNEL_POP:
                    state = getQuantityTypeState(forecastData.getPop() * 100, PERCENT);
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
     * @param count
     */
    private void updateDailyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapJsonOneCallAPIData localDailyForecastData = oneCallData;
        if (localDailyForecastData != null && localDailyForecastData.getDaily().size() > count) {
            DailyForecast forecastData = localDailyForecastData.getDaily().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getDt());
                    break;
                case CHANNEL_CONDITION:
                    if (!forecastData.getWeather().isEmpty()) {
                        state = getStringTypeState(forecastData.getWeather().get(0).getDescription());
                    }
                    break;
                case CHANNEL_CONDITION_ID:
                    if (!forecastData.getWeather().isEmpty()) {
                        state = getStringTypeState(forecastData.getWeather().get(0).getId().toString());
                    }
                    break;
                case CHANNEL_CONDITION_ICON:
                    if (!forecastData.getWeather().isEmpty()) {
                        state = getRawTypeState(
                                OpenWeatherMapConnection.getWeatherIcon(forecastData.getWeather().get(0).getIcon()));
                    }
                    break;
                case CHANNEL_CONDITION_ICON_ID:
                    if (!forecastData.getWeather().isEmpty()) {
                        state = getStringTypeState(forecastData.getWeather().get(0).getIcon());
                    }
                    break;
                case CHANNEL_MIN_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getTemp().getMin(), CELSIUS);
                    break;
                case CHANNEL_MAX_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getTemp().getMax(), CELSIUS);
                    break;
                case CHANNEL_APPARENT_TEMPERATURE:
                    FeelsLikeTemp feelsLikeTemp = forecastData.getFeelsLike();
                    if (feelsLikeTemp != null) {
                        state = getQuantityTypeState(feelsLikeTemp.getDay(), CELSIUS);
                    }
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(forecastData.getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(forecastData.getHumidity(), PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(forecastData.getWindSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(forecastData.getWindDeg(), DEGREE_ANGLE);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(forecastData.getClouds(), PERCENT);
                    break;
                case CHANNEL_RAIN:
                    double rain = forecastData.getRain();
                    state = getQuantityTypeState(rain, MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    double snow = forecastData.getSnow();
                    state = getQuantityTypeState(snow, MILLI(METRE));
                    break;
                case CHANNEL_POP:
                    state = getQuantityTypeState(forecastData.getPop() * 100, PERCENT);
                    break;
                case CHANNEL_UVINDEX:
                    state = getDecimalTypeState(forecastData.getUvi());
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }
}
