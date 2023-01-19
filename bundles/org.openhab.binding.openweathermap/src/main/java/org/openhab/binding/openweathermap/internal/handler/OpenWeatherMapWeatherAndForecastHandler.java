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
package org.openhab.binding.openweathermap.internal.handler;

import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpResponseException;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapWeatherAndForecastConfiguration;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonDailyForecastData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonHourlyForecastData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonWeatherData;
import org.openhab.binding.openweathermap.internal.dto.base.Precipitation;
import org.openhab.binding.openweathermap.internal.dto.forecast.daily.FeelsLikeTemp;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

    private @Nullable OpenWeatherMapJsonWeatherData weatherData;
    private @Nullable OpenWeatherMapJsonHourlyForecastData hourlyForecastData;
    private @Nullable OpenWeatherMapJsonDailyForecastData dailyForecastData;

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
            throws CommunicationException, ConfigurationException {
        logger.debug("Update weather and forecast data of thing '{}'.", getThing().getUID());
        try {
            weatherData = connection.getWeatherData(location);
            if (forecastHours > 0) {
                hourlyForecastData = connection.getHourlyForecastData(location, forecastHours / 3);
            }
            if (forecastDays > 0) {
                try {
                    dailyForecastData = connection.getDailyForecastData(location, forecastDays);
                } catch (ConfigurationException e) {
                    if (e.getCause() instanceof HttpResponseException) {
                        forecastDays = 0;
                        Configuration editConfig = editConfiguration();
                        editConfig.put(CONFIG_FORECAST_DAYS, 0);
                        updateConfiguration(editConfig);
                        logger.debug("Removing daily forecast channel groups.");
                        List<Channel> channels = getThing().getChannels().stream()
                                .filter(c -> CHANNEL_GROUP_FORECAST_TODAY.equals(c.getUID().getGroupId())
                                        || CHANNEL_GROUP_FORECAST_TOMORROW.equals(c.getUID().getGroupId())
                                        || c.getUID().getGroupId().startsWith(CHANNEL_GROUP_DAILY_FORECAST_PREFIX))
                                .collect(Collectors.toList());
                        updateThing(editThing().withoutChannels(channels).build());
                    } else {
                        throw e;
                    }
                }
            }
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
                Matcher hourlyForecastMatcher = CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
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

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateCurrentChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapJsonWeatherData localWeatherData = weatherData;
        if (localWeatherData != null) {
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_STATION_ID:
                    state = getStringTypeState(localWeatherData.getId().toString());
                    break;
                case CHANNEL_STATION_NAME:
                    state = getStringTypeState(localWeatherData.getName());
                    break;
                case CHANNEL_STATION_LOCATION:
                    state = getPointTypeState(localWeatherData.getCoord().getLat(),
                            localWeatherData.getCoord().getLon());
                    break;
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(localWeatherData.getDt());
                    break;
                case CHANNEL_CONDITION:
                    if (!localWeatherData.getWeather().isEmpty()) {
                        state = getStringTypeState(localWeatherData.getWeather().get(0).getDescription());
                    }
                    break;
                case CHANNEL_CONDITION_ID:
                    if (!localWeatherData.getWeather().isEmpty()) {
                        state = getStringTypeState(localWeatherData.getWeather().get(0).getId().toString());
                    }
                    break;
                case CHANNEL_CONDITION_ICON:
                    if (!localWeatherData.getWeather().isEmpty()) {
                        state = getRawTypeState(OpenWeatherMapConnection
                                .getWeatherIcon(localWeatherData.getWeather().get(0).getIcon()));
                    }
                    break;
                case CHANNEL_CONDITION_ICON_ID:
                    if (!localWeatherData.getWeather().isEmpty()) {
                        state = getStringTypeState(localWeatherData.getWeather().get(0).getIcon());
                    }
                    break;
                case CHANNEL_TEMPERATURE:
                    state = getQuantityTypeState(localWeatherData.getMain().getTemp(), CELSIUS);
                    break;
                case CHANNEL_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(localWeatherData.getMain().getFeelsLikeTemp(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(localWeatherData.getMain().getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(localWeatherData.getMain().getHumidity(), PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(localWeatherData.getWind().getSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(localWeatherData.getWind().getDeg(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(localWeatherData.getWind().getGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(localWeatherData.getClouds().getAll(), PERCENT);
                    break;
                case CHANNEL_RAIN:
                    Precipitation rain = localWeatherData.getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain.getVolume(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Precipitation snow = localWeatherData.getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow.getVolume(), MILLI(METRE));
                    break;
                case CHANNEL_VISIBILITY:
                    Integer localVisibility = localWeatherData.getVisibility();
                    state = localVisibility == null ? UnDefType.UNDEF
                            : new QuantityType<>(localVisibility, METRE).toUnit(KILO(METRE));
                    if (state == null) {
                        logger.debug("State conversion failed, cannot update state.");
                        return;
                    }
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
        OpenWeatherMapJsonHourlyForecastData localHourlyForecastData = hourlyForecastData;
        if (localHourlyForecastData != null && localHourlyForecastData.getList().size() > count) {
            org.openhab.binding.openweathermap.internal.dto.forecast.hourly.List forecastData = localHourlyForecastData
                    .getList().get(count);
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
                    state = getQuantityTypeState(forecastData.getMain().getTemp(), CELSIUS);
                    break;
                case CHANNEL_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getMain().getFeelsLikeTemp(), CELSIUS);
                    break;
                case CHANNEL_MIN_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getMain().getTempMin(), CELSIUS);
                    break;
                case CHANNEL_MAX_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getMain().getTempMax(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(forecastData.getMain().getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(forecastData.getMain().getHumidity(), PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(forecastData.getWind().getSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(forecastData.getWind().getDeg(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(forecastData.getWind().getGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(forecastData.getClouds().getAll(), PERCENT);
                    break;
                case CHANNEL_RAIN:
                    Precipitation rain = forecastData.getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain.getVolume(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Precipitation snow = forecastData.getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow.getVolume(), MILLI(METRE));
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
        OpenWeatherMapJsonDailyForecastData localDailyForecastData = dailyForecastData;
        if (localDailyForecastData != null && localDailyForecastData.getList().size() > count) {
            org.openhab.binding.openweathermap.internal.dto.forecast.daily.List forecastData = localDailyForecastData
                    .getList().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getDt());
                    break;
                case CHANNEL_SUNRISE:
                    state = getDateTimeTypeState(forecastData.getSunrise());
                    break;
                case CHANNEL_SUNSET:
                    state = getDateTimeTypeState(forecastData.getSunset());
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
                    state = getQuantityTypeState(forecastData.getSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(forecastData.getDeg(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(forecastData.getGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(forecastData.getClouds(), PERCENT);
                    break;
                case CHANNEL_RAIN:
                    Double rain = forecastData.getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain, MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Double snow = forecastData.getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow, MILLI(METRE));
                    break;
                case CHANNEL_PRECIP_PROBABILITY:
                    Double probability = forecastData.getPop();
                    state = getQuantityTypeState(probability == null ? 0 : probability * 100.0, PERCENT);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }
}
