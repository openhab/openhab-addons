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
import static org.openhab.core.types.TimeSeries.Policy.REPLACE;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapOneCallConfiguration;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallAPIData;
import org.openhab.binding.openweathermap.internal.dto.forecast.daily.FeelsLikeTemp;
import org.openhab.binding.openweathermap.internal.dto.forecast.daily.Temp;
import org.openhab.binding.openweathermap.internal.dto.onecall.Alert;
import org.openhab.binding.openweathermap.internal.dto.onecall.Daily;
import org.openhab.binding.openweathermap.internal.dto.onecall.Hourly;
import org.openhab.binding.openweathermap.internal.dto.onecall.Precipitation;
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
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenWeatherMapOneCallHandler} is responsible for handling commands, which are sent to one of
 * the channels.
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Christoph Weitkamp - Added weather alerts
 * @author Florian Hotze - Added support for persisting forecasts
 */
@NonNullByDefault
public class OpenWeatherMapOneCallHandler extends AbstractOpenWeatherMapHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapOneCallHandler.class);

    private static final String CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX = "forecastMinutes";
    private static final String CHANNEL_GROUP_MINUTELY_TIMESERIES_PREFIX = "forecastMinutely";
    private static final String CHANNEL_GROUP_HOURLY_FORECAST_PREFIX = "forecastHours";
    private static final String CHANNEL_GROUP_HOURLY_TIMESERIES_PREFIX = "forecastHourly";
    private static final String CHANNEL_GROUP_DAILY_FORECAST_PREFIX = "forecastDay";
    private static final String CHANNEL_GROUP_DAILY_TIMESERIES_PREFIX = "forecastDaily";
    private static final String CHANNEL_GROUP_ALERTS_PREFIX = "alerts";
    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_ALERTS_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_ALERTS_PREFIX + "([0-9]*)");

    private @Nullable OpenWeatherMapOneCallAPIData weatherData;

    // forecastMinutes, -Hours and -Days determine the number of channel groups to create for each type
    private int forecastMinutes = 60;
    private int forecastHours = 48;
    private int forecastDays = 8;
    private int numberOfAlerts = 0;

    public OpenWeatherMapOneCallHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Initialize OpenWeatherMapOneCallHandler handler '{}'.", getThing().getUID());
        OpenWeatherMapOneCallConfiguration config = getConfigAs(OpenWeatherMapOneCallConfiguration.class);

        boolean configValid = true;
        int newForecastMinutes = config.forecastMinutes;
        if (newForecastMinutes < 0 || newForecastMinutes > 60) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-onecall-number-of-minutes");
            configValid = false;
        }
        int newForecastHours = config.forecastHours;
        if (newForecastHours < 0 || newForecastHours > 48) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-onecall-number-of-hours");
            configValid = false;
        }
        int newForecastDays = config.forecastDays;
        if (newForecastDays < 0 || newForecastDays > 8) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-onecall-number-of-days");
            configValid = false;
        }
        int newNumberOfAlerts = config.numberOfAlerts;
        if (newNumberOfAlerts < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-onecall-number-of-alerts");
            configValid = false;
        }

        if (configValid) {
            logger.debug("Rebuilding thing '{}'.", getThing().getUID());
            List<Channel> toBeAddedChannels = new ArrayList<>();
            List<Channel> toBeRemovedChannels = new ArrayList<>();
            toBeAddedChannels
                    .addAll(createChannelsForGroup(CHANNEL_GROUP_ONECALL_CURRENT, CHANNEL_GROUP_TYPE_ONECALL_CURRENT));
            if (forecastMinutes != newForecastMinutes) {
                logger.debug("forecastMinutes changed from {} to {}. Rebuilding minutely forecast channel groups.",
                        forecastMinutes, newForecastMinutes);
                if (forecastMinutes > newForecastMinutes) {
                    for (int i = newForecastMinutes + 1; i <= forecastMinutes; i++) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(
                                CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i)));
                    }
                } else {
                    for (int i = forecastMinutes + 1; i <= newForecastMinutes; i++) {
                        toBeAddedChannels.addAll(createChannelsForGroup(
                                CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i),
                                CHANNEL_GROUP_TYPE_ONECALL_MINUTELY_FORECAST));
                    }
                }
                forecastMinutes = newForecastMinutes;
            }
            if (forecastHours != newForecastHours) {
                logger.debug("ForecastHours changed from {} to {}. Rebuilding hourly forecast channel groups.",
                        forecastHours, newForecastHours);
                if (forecastHours > newForecastHours) {
                    for (int i = newForecastHours + 1; i <= forecastHours; i++) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i)));
                    }
                } else {
                    for (int i = forecastHours + 1; i <= newForecastHours; i++) {
                        toBeAddedChannels.addAll(createChannelsForGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i),
                                CHANNEL_GROUP_TYPE_ONECALL_HOURLY_FORECAST));
                    }
                }
                forecastHours = newForecastHours;
            }
            if (forecastDays != newForecastDays) {
                logger.debug("ForecastDays changed from {} to {}. Rebuilding daily forecast channel groups.",
                        forecastDays, newForecastDays);
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
                                CHANNEL_GROUP_TYPE_ONECALL_DAILY_FORECAST));
                    }
                    if (forecastDays <= 1 && newForecastDays > 1) {
                        toBeAddedChannels.addAll(createChannelsForGroup(CHANNEL_GROUP_FORECAST_TOMORROW,
                                CHANNEL_GROUP_TYPE_ONECALL_DAILY_FORECAST));
                    }
                    for (int i = Math.max(forecastDays, 2); i < newForecastDays; ++i) {
                        toBeAddedChannels.addAll(
                                createChannelsForGroup(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + Integer.toString(i),
                                        CHANNEL_GROUP_TYPE_ONECALL_DAILY_FORECAST));
                    }
                }
                forecastDays = newForecastDays;
                if (numberOfAlerts != newNumberOfAlerts) {
                    logger.debug("Rebuilding alerts channel groups.");
                    if (numberOfAlerts > newNumberOfAlerts) {
                        for (int i = newNumberOfAlerts + 1; i <= numberOfAlerts; ++i) {
                            toBeRemovedChannels
                                    .addAll(removeChannelsOfGroup(CHANNEL_GROUP_ALERTS_PREFIX + Integer.toString(i)));
                        }
                    } else {
                        for (int i = numberOfAlerts + 1; i <= newNumberOfAlerts; ++i) {
                            toBeAddedChannels
                                    .addAll(createChannelsForGroup(CHANNEL_GROUP_ALERTS_PREFIX + Integer.toString(i),
                                            CHANNEL_GROUP_TYPE_ONECALL_ALERTS));
                        }
                    }
                    numberOfAlerts = newNumberOfAlerts;
                }
            }
            logger.debug("toBeRemovedChannels: {}. toBeAddedChannels: {}", toBeRemovedChannels, toBeAddedChannels);
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
            // Include minutely, hourly and daily data as this is needed for the time series channels
            weatherData = connection.getOneCallAPIData(location, false, false, false, numberOfAlerts == 0);
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
        logger.debug("OneCallHandler: updateChannel {}, groupID {}", channelUID, channelGroupId);
        switch (channelGroupId) {
            case CHANNEL_GROUP_ONECALL_CURRENT:
                updateCurrentChannel(channelUID);
                break;
            case CHANNEL_GROUP_ONECALL_TODAY:
                updateDailyForecastChannel(channelUID, 0);
                break;
            case CHANNEL_GROUP_ONECALL_TOMORROW:
                updateDailyForecastChannel(channelUID, 1);
                break;
            case CHANNEL_GROUP_MINUTELY_TIMESERIES_PREFIX:
                updateMinutelyForecastTimeSeries(channelUID);
                break;
            case CHANNEL_GROUP_HOURLY_TIMESERIES_PREFIX:
                updateHourlyForecastTimeSeries(channelUID);
                break;
            case CHANNEL_GROUP_DAILY_TIMESERIES_PREFIX:
                updateDailyForecastTimeSeries(channelUID);
                break;
            default:
                int i;
                Matcher hourlyForecastMatcher = CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (hourlyForecastMatcher.find() && (i = Integer.parseInt(hourlyForecastMatcher.group(1))) >= 1
                        && i <= 48) {
                    updateHourlyForecastChannel(channelUID, (i - 1));
                    break;
                }
                Matcher dailyForecastMatcher = CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (dailyForecastMatcher.find() && (i = Integer.parseInt(dailyForecastMatcher.group(1))) >= 1
                        && i <= 7) {
                    updateDailyForecastChannel(channelUID, i);
                    break;
                }
                Matcher minutelyForecastMatcher = CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX_PATTERN
                        .matcher(channelGroupId);
                if (minutelyForecastMatcher.find() && (i = Integer.parseInt(minutelyForecastMatcher.group(1))) >= 1
                        && i <= 60) {
                    updateMinutelyForecastChannel(channelUID, i - 1);
                    break;
                }
                Matcher alertsMatcher = CHANNEL_GROUP_ALERTS_PREFIX_PATTERN.matcher(channelGroupId);
                if (alertsMatcher.find() && (i = Integer.parseInt(alertsMatcher.group(1))) >= 1) {
                    updateAlertsChannel(channelUID, i - 1);
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
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
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
     * @param count the index of the minutely data referenced by the channel (minute 1 is count 0)
     */
    private void updateMinutelyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (forecastMinutes == 0) {
            logger.warn(
                    "Can't update channel group {} because forecastMinutes is set to '0'. Please adjust config accordingly",
                    channelGroupId);
            return;
        }
        if (localWeatherData != null && localWeatherData.getMinutely() != null
                && localWeatherData.getMinutely().size() > count) {
            org.openhab.binding.openweathermap.internal.dto.onecall.Minutely forecastData = localWeatherData
                    .getMinutely().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getDt());
                    break;
                case CHANNEL_PRECIPITATION:
                    double precipitation = forecastData.getPrecipitation();
                    state = getQuantityTypeState(precipitation, MILLI(METRE));
                    break;
                default:
                    // This should not happen
                    logger.warn("Unknown channel id {} in onecall minutely weather data", channelId);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    private void updateMinutelyForecastTimeSeries(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (channelId.equals(CHANNEL_TIME_STAMP)) {
            logger.debug("Channel `{}` of group '{}' is no supported time-series channel.", channelId, channelGroupId);
            return;
        }
        if (localWeatherData != null && !localWeatherData.getMinutely().isEmpty()) {
            List<org.openhab.binding.openweathermap.internal.dto.onecall.Minutely> forecastData = localWeatherData
                    .getMinutely();
            TimeSeries timeSeries = new TimeSeries(REPLACE);
            forecastData.forEach((m) -> {
                if (channelId.equals(CHANNEL_PRECIPITATION)) {
                    State state = UnDefType.UNDEF;
                    Instant timestamp = Instant.ofEpochSecond(m.getDt());
                    double precipitation = m.getPrecipitation();
                    state = getQuantityTypeState(precipitation, MILLI(METRE));
                    timeSeries.add(timestamp, state);
                } else {
                    // This should not happen
                    logger.warn("Unknown channel id {} in onecall minutely weather data", channelId);
                    return;
                }
            });
            logger.debug("Update channel '{}' of group '{}' with new time-series '{}'.", channelId, channelGroupId,
                    timeSeries);
            sendTimeSeries(channelUID, timeSeries);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    /**
     * Update the hourly forecast channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count the index of the hourly data referenced by the channel (hour 1 is count 0)
     */
    private void updateHourlyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (forecastHours == 0) {
            logger.warn(
                    "Can't update channel group {} because forecastHours is set to '0'. Please adjust config accordingly",
                    channelGroupId);
            return;
        }
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (localWeatherData != null && localWeatherData.getHourly().size() > count) {
            org.openhab.binding.openweathermap.internal.dto.onecall.Hourly forecastData = localWeatherData.getHourly()
                    .get(count);
            State state = getHourlyForecastState(channelId, forecastData, localWeatherData);
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    /**
     * Update the hourly forecast time series channel from the last OpenWeatherMap data retrieved.
     * 
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateHourlyForecastTimeSeries(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (channelId.equals(CHANNEL_TIME_STAMP)) {
            logger.debug("Channel `{}` of group '{}' is no supported time-series channel.", channelId, channelGroupId);
            return;
        }
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (localWeatherData != null && !localWeatherData.getHourly().isEmpty()) {
            List<org.openhab.binding.openweathermap.internal.dto.onecall.Hourly> forecastData = localWeatherData
                    .getHourly();
            TimeSeries timeSeries = new TimeSeries(REPLACE);
            forecastData.forEach((h) -> {
                Instant timestamp = Instant.ofEpochSecond(h.getDt());
                State state = getHourlyForecastState(channelId, h, localWeatherData);
                timeSeries.add(timestamp, state);
            });
            logger.debug("Update channel '{}' of group '{}' with new time-series '{}'.", channelId, channelGroupId,
                    timeSeries);
            sendTimeSeries(channelUID, timeSeries);
        } else {
            logger.debug("No weather data available to update channel '{}'.", channelId);
        }
    }

    private State getHourlyForecastState(String channelId, Hourly forecastData,
            OpenWeatherMapOneCallAPIData localWeatherData) {
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
                    state = getStringTypeState(Integer.toString(forecastData.getWeather().get(0).getId()));
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
            case CHANNEL_DEW_POINT:
                state = getQuantityTypeState(forecastData.getDewPoint(), CELSIUS);
                break;
            case CHANNEL_WIND_SPEED:
                state = getQuantityTypeState(forecastData.getWindSpeed(), METRE_PER_SECOND);
                break;
            case CHANNEL_WIND_DIRECTION:
                state = getQuantityTypeState(forecastData.getWindDeg(), DEGREE_ANGLE);
                break;
            case CHANNEL_GUST_SPEED:
                state = getQuantityTypeState(forecastData.getWindGust(), METRE_PER_SECOND);
                break;
            case CHANNEL_CLOUDINESS:
                state = getQuantityTypeState(forecastData.getClouds(), PERCENT);
                break;
            case CHANNEL_VISIBILITY:
                State tempstate = new QuantityType<>(localWeatherData.getCurrent().getVisibility(), METRE)
                        .toUnit(KILO(METRE));
                state = (tempstate == null ? state : tempstate);
            case CHANNEL_PRECIP_PROBABILITY:
                state = getQuantityTypeState(forecastData.getPop() * 100.0, PERCENT);
                break;
            case CHANNEL_RAIN:
                Precipitation rain = forecastData.getRain();
                state = getQuantityTypeState(rain == null ? 0 : rain.get1h(), MILLI(METRE));
                break;
            case CHANNEL_SNOW:
                Precipitation snow = forecastData.getSnow();
                state = getQuantityTypeState(snow == null ? 0 : snow.get1h(), MILLI(METRE));
                break;
            default:
                // This should not happen
                logger.warn("Unknown channel id {} in OneCall hourly weather data", channelId);
                break;
        }
        return state;
    }

    /**
     * Update the daily forecast channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count the index of the daily data referenced by the channel (today is count 0)
     */
    private void updateDailyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (forecastDays == 0) {
            logger.warn(
                    "Can't update channel group {} because forecastDays is set to '0'. Please adjust config accordingly",
                    channelGroupId);
            return;
        }
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (localWeatherData != null && localWeatherData.getDaily().size() > count) {
            org.openhab.binding.openweathermap.internal.dto.onecall.Daily forecastData = localWeatherData.getDaily()
                    .get(count);
            State state = getDailyForecastState(channelId, forecastData, localWeatherData);
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    private void updateDailyForecastTimeSeries(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (channelId.equals(CHANNEL_TIME_STAMP)) {
            logger.debug("Channel `{}` of group '{}' is no supported time-series channel.", channelId, channelGroupId);
            return;
        }
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (localWeatherData != null && !localWeatherData.getDaily().isEmpty()) {
            List<org.openhab.binding.openweathermap.internal.dto.onecall.Daily> forecastData = localWeatherData
                    .getDaily();
            TimeSeries timeSeries = new TimeSeries(REPLACE);
            forecastData.forEach((d) -> {
                Instant timestamp = Instant.ofEpochSecond(d.getDt());
                State state = getDailyForecastState(channelId, d, localWeatherData);
                timeSeries.add(timestamp, state);
            });
            logger.debug("Update channel '{}' of group '{}' with new time-series '{}'.", channelId, channelGroupId,
                    timeSeries);
            sendTimeSeries(channelUID, timeSeries);
        } else {
            logger.debug("No weather data available to update channel '{}'.", channelId);
        }
    }

    private State getDailyForecastState(String channelId, Daily forecastData,
            OpenWeatherMapOneCallAPIData localWeatherData) {
        State state = UnDefType.UNDEF;
        FeelsLikeTemp feelsLike;
        Temp temp;
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
            case CHANNEL_MOONRISE:
                state = getDateTimeTypeState(forecastData.getMoonrise());
                break;
            case CHANNEL_MOONSET:
                state = getDateTimeTypeState(forecastData.getMoonset());
                break;
            case CHANNEL_MOON_PHASE:
                state = getDecimalTypeState(forecastData.getMoonPhase());
                break;
            case CHANNEL_CONDITION:
                if (!forecastData.getWeather().isEmpty()) {
                    state = getStringTypeState(forecastData.getWeather().get(0).getDescription());
                }
                break;
            case CHANNEL_CONDITION_ID:
                if (!forecastData.getWeather().isEmpty()) {
                    state = getStringTypeState(Integer.toString(forecastData.getWeather().get(0).getId()));
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
                temp = forecastData.getTemp();
                if (temp != null) {
                    state = getQuantityTypeState(temp.getMin(), CELSIUS);
                }
                break;
            case CHANNEL_MAX_TEMPERATURE:
                temp = forecastData.getTemp();
                if (temp != null) {
                    state = getQuantityTypeState(temp.getMax(), CELSIUS);
                }
                break;
            case CHANNEL_MORNING_TEMPERATURE:
                temp = forecastData.getTemp();
                if (temp != null) {
                    state = getQuantityTypeState(temp.getMorn(), CELSIUS);
                }
                break;
            case CHANNEL_DAY_TEMPERATURE:
                temp = forecastData.getTemp();
                if (temp != null) {
                    state = getQuantityTypeState(temp.getDay(), CELSIUS);
                }
                break;
            case CHANNEL_EVENING_TEMPERATURE:
                temp = forecastData.getTemp();
                if (temp != null) {
                    state = getQuantityTypeState(temp.getEve(), CELSIUS);
                }
                break;
            case CHANNEL_NIGHT_TEMPERATURE:
                temp = forecastData.getTemp();
                if (temp != null) {
                    state = getQuantityTypeState(temp.getNight(), CELSIUS);
                }
                break;

            case CHANNEL_APPARENT_DAY:
                feelsLike = forecastData.getFeelsLike();
                if (feelsLike != null) {
                    state = getQuantityTypeState(feelsLike.getDay(), CELSIUS);
                }
                break;
            case CHANNEL_APPARENT_MORNING:
                feelsLike = forecastData.getFeelsLike();
                if (feelsLike != null) {
                    state = getQuantityTypeState(feelsLike.getMorn(), CELSIUS);
                }
                break;
            case CHANNEL_APPARENT_EVENING:
                feelsLike = forecastData.getFeelsLike();
                if (feelsLike != null) {
                    state = getQuantityTypeState(feelsLike.getEve(), CELSIUS);
                }
                break;
            case CHANNEL_APPARENT_NIGHT:
                feelsLike = forecastData.getFeelsLike();
                if (feelsLike != null) {
                    state = getQuantityTypeState(feelsLike.getNight(), CELSIUS);
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
            case CHANNEL_GUST_SPEED:
                state = getQuantityTypeState(forecastData.getWindGust(), METRE_PER_SECOND);
                break;
            case CHANNEL_CLOUDINESS:
                state = getQuantityTypeState(forecastData.getClouds(), PERCENT);
                break;
            case CHANNEL_DEW_POINT:
                state = getQuantityTypeState(forecastData.getDewPoint(), CELSIUS);
                break;
            case CHANNEL_UVINDEX:
                state = getDecimalTypeState(forecastData.getUvi());
                break;
            case CHANNEL_VISIBILITY:
                State tempstate = new QuantityType<>(localWeatherData.getCurrent().getVisibility(), METRE)
                        .toUnit(KILO(METRE));
                state = (tempstate == null ? state : tempstate);
            case CHANNEL_PRECIP_PROBABILITY:
                state = getQuantityTypeState(forecastData.getPop() * 100.0, PERCENT);
                break;
            case CHANNEL_RAIN:
                state = getQuantityTypeState(forecastData.getRain(), MILLI(METRE));
                break;
            case CHANNEL_SNOW:
                state = getQuantityTypeState(forecastData.getSnow(), MILLI(METRE));
                break;
            default:
                // This should not happen
                logger.warn("Unknown channel id {} in OneCall daily weather data", channelId);
                break;
        }
        return state;
    }

    /**
     * Update the channel from the last OpenWeaterhMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count the index of the alert data referenced by the channel (alert 1 is count 0)
     */
    private void updateAlertsChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        List<Alert> alerts = localWeatherData != null ? localWeatherData.alerts : null;
        State state = UnDefType.UNDEF;
        if (alerts != null && alerts.size() > count) {
            Alert alert = alerts.get(count);
            switch (channelId) {
                case CHANNEL_ALERT_EVENT:
                    state = getStringTypeState(alert.getEvent());
                    break;
                case CHANNEL_ALERT_DESCRIPTION:
                    state = getStringTypeState(alert.getDescription());
                    break;
                case CHANNEL_ALERT_ONSET:
                    state = getDateTimeTypeState(alert.getStart());
                    break;
                case CHANNEL_ALERT_EXPIRES:
                    state = getDateTimeTypeState(alert.getEnd());
                    break;
                case CHANNEL_ALERT_SOURCE:
                    state = getStringTypeState(alert.getSenderName());
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        } else {
            logger.debug("No data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
        updateState(channelUID, state);
    }
}
