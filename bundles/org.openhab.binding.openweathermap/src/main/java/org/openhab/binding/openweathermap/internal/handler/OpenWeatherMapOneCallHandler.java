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

import com.google.gson.JsonSyntaxException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapCommunicationException;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConfigurationException;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.onecall.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.eclipse.smarthome.core.library.unit.SIUnits.*;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;

/**
 * The {@link OpenWeatherMapOneCallHandler} is responsible for handling commands, which are sent to one of
 * the channels.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapOneCallHandler extends AbstractOpenWeatherMapHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapOneCallHandler.class);

    private static final String CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX = "forecastMinutes";
    private static final String CHANNEL_GROUP_HOURLY_FORECAST_PREFIX = "forecastHours";
    private static final String CHANNEL_GROUP_DAILY_FORECAST_PREFIX = "forecastDay";
    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX + "([0-9]*)");


    private @Nullable OpenWeatherMapOneCallAPIData weatherData;


    public OpenWeatherMapOneCallHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Initialize OpenWeatherMapOneCallHandler handler '{}'.", getThing().getUID());
    }

    @Override
    protected boolean requestData(OpenWeatherMapConnection connection)
            throws OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        logger.debug("Update weather and forecast data of thing '{}'.", getThing().getUID());
        try {
            weatherData = connection.getOneCallAPIData(location);
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
        logger.debug("OneCallHandler: updateChannel {}, groupID {}",channelUID,channelGroupId);
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
                Matcher minutelyForecastMatcher = CHANNEL_GROUP_MINUTELY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (minutelyForecastMatcher.find() && (i = Integer.parseInt(minutelyForecastMatcher.group(1))) >= 1
                        && i <= 60) {
                    updateMinutelyForecastChannel(channelUID, i-1);
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
                    state = getPointTypeState(localWeatherData.getLat(),
                            localWeatherData.getLon());
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
                        state = getStringTypeState(Integer.toString(localWeatherData.getCurrent().getWeather().get(0).getId()));
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
                    Rain rain = localWeatherData.getCurrent().getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain.get1h(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Snow snow = localWeatherData.getCurrent().getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow.get1h(), MILLI(METRE));
                    break;
                case CHANNEL_VISIBILITY:
                    @Nullable State tempstate = new QuantityType<>(localWeatherData.getCurrent().getVisibility(),METRE).toUnit(KILO(METRE));
                    state = (tempstate == null ? state : tempstate);
                default:
                    // This should not happen
                    logger.warn("Unknown channel id {} in onecall current weather data",channelId);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    private void updateMinutelyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (localWeatherData != null && localWeatherData.getMinutely().size() > count) {
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
        }
        else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }
    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count the number of the hour referenced by the channel
     */
    private void updateHourlyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (localWeatherData != null && localWeatherData.getHourly().size() > count) {
            org.openhab.binding.openweathermap.internal.dto.onecall.Hourly forecastData = localWeatherData
                    .getHourly().get(count);
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
                    @Nullable State tempstate = new QuantityType<>(localWeatherData.getCurrent().getVisibility(),METRE).toUnit(KILO(METRE));
                    state = (tempstate == null ? state : tempstate);
                case CHANNEL_PROBABILITY:
                    state = getDecimalTypeState(forecastData.getPop());
                    break;
                case CHANNEL_RAIN:
                    Rain rain = forecastData.getRain();
                    state = getQuantityTypeState(rain == null ? 0 : rain.get1h(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    Snow snow = forecastData.getSnow();
                    state = getQuantityTypeState(snow == null ? 0 : snow.get1h(), MILLI(METRE));
                    break;
                default:
                    // This should not happen
                    logger.warn("Unknown channel id {} in onecall hourly weather data",channelId);
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
        @Nullable OpenWeatherMapOneCallAPIData localWeatherData = weatherData;
        if (localWeatherData != null && localWeatherData.getDaily().size() > count) {
            org.openhab.binding.openweathermap.internal.dto.onecall.Daily forecastData = localWeatherData
                    .getDaily().get(count);
            State state = UnDefType.UNDEF;
            Temp temp;
            FeelsLike feelsLike;
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
                    @Nullable State tempstate = new QuantityType<>(localWeatherData.getCurrent().getVisibility(),METRE).toUnit(KILO(METRE));
                    state = (tempstate == null ? state : tempstate);
                case CHANNEL_PROBABILITY:
                    state = getDecimalTypeState(forecastData.getPop());
                    break;
                case CHANNEL_RAIN:
                    state = getQuantityTypeState(forecastData.getRain(), MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(forecastData.getSnow(), MILLI(METRE));
                    break;
                default:
                    // This should not happen
                    logger.warn("Unknown channel id {} in onecall daily weather data",channelId);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }
}
