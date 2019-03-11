/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.darksky.internal.handler;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.eclipse.smarthome.core.library.unit.SIUnits.*;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.darksky.internal.DarkSkyBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.darksky.internal.config.DarkSkyChannelConfiguration;
import org.openhab.binding.darksky.internal.config.DarkSkyWeatherAndForecastConfiguration;
import org.openhab.binding.darksky.internal.connection.DarkSkyCommunicationException;
import org.openhab.binding.darksky.internal.connection.DarkSkyConfigurationException;
import org.openhab.binding.darksky.internal.connection.DarkSkyConnection;
import org.openhab.binding.darksky.internal.model.DarkSkyCurrently;
import org.openhab.binding.darksky.internal.model.DarkSkyDailyData.DailyData;
import org.openhab.binding.darksky.internal.model.DarkSkyHourlyData.HourlyData;
import org.openhab.binding.darksky.internal.model.DarkSkyJsonWeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link DarkSkyWeatherAndForecastHandler} is responsible for handling commands, which are sent to one of
 * the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DarkSkyWeatherAndForecastHandler extends AbstractDarkSkyHandler {

    private final Logger logger = LoggerFactory.getLogger(DarkSkyWeatherAndForecastHandler.class);

    private static final String PRECIP_TYPE_SNOW = "snow";
    private static final String PRECIP_TYPE_RAIN = "rain";

    private static final String CHANNEL_GROUP_HOURLY_FORECAST_PREFIX = "forecastHours";
    private static final String CHANNEL_GROUP_DAILY_FORECAST_PREFIX = "forecastDay";
    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + "([0-9]*)");

    // keeps track of the parsed counts
    private int forecastHours = 24;
    private int forecastDays = 8;

    private @Nullable DarkSkyChannelConfiguration sunriseTriggerChannelConfig;
    private @Nullable DarkSkyChannelConfiguration sunsetTriggerChannelConfig;
    private @Nullable DarkSkyJsonWeatherData weatherData;

    public DarkSkyWeatherAndForecastHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Initialize DarkSkyWeatherAndForecastHandler handler '{}'.", getThing().getUID());
        DarkSkyWeatherAndForecastConfiguration config = getConfigAs(DarkSkyWeatherAndForecastConfiguration.class);

        boolean configValid = true;
        int newForecastHours = config.getForecastHours();
        if (newForecastHours < 0 || newForecastHours > 48) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-number-of-hours");
            configValid = false;
        }
        int newForecastDays = config.getForecastDays();
        if (newForecastDays < 0 || newForecastDays > 8) {
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
                    for (int i = newForecastHours + 1; i <= forecastHours; ++i) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i)));
                    }
                } else {
                    for (int i = forecastHours + 1; i <= newForecastHours; ++i) {
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

            Channel sunriseTriggerChannel = getThing().getChannel(TRIGGER_SUNRISE);
            sunriseTriggerChannelConfig = (sunriseTriggerChannel == null) ? null
                    : sunriseTriggerChannel.getConfiguration().as(DarkSkyChannelConfiguration.class);
            Channel sunsetTriggerChannel = getThing().getChannel(TRIGGER_SUNSET);
            sunsetTriggerChannelConfig = (sunsetTriggerChannel == null) ? null
                    : sunsetTriggerChannel.getConfiguration().as(DarkSkyChannelConfiguration.class);
        }
    }

    @Override
    protected boolean requestData(DarkSkyConnection connection)
            throws DarkSkyCommunicationException, DarkSkyConfigurationException {
        logger.debug("Update weather and forecast data of thing '{}'.", getThing().getUID());
        try {
            weatherData = connection.getWeatherData(location);
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
        switch (channelGroupId) {
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
                if (hourlyForecastMatcher.find() && (i = Integer.parseInt(hourlyForecastMatcher.group(1))) >= 1
                        && i <= 48) {
                    updateHourlyForecastChannel(channelUID, i);
                    break;
                }
                Matcher dailyForecastMatcher = CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (dailyForecastMatcher.find() && (i = Integer.parseInt(dailyForecastMatcher.group(1))) > 1
                        && i <= 8) {
                    updateDailyForecastChannel(channelUID, i);
                    break;
                }
                break;
        }
    }

    /**
     * Update the channel from the last Dark Sky data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateCurrentChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (weatherData != null && weatherData.getCurrently() != null) {
            DarkSkyCurrently currentData = weatherData.getCurrently();
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(currentData.getTime());
                    break;
                case CHANNEL_CONDITION:
                    state = getStringTypeState(currentData.getSummary());
                    break;
                case CHANNEL_CONDITION_ICON:
                    state = getRawTypeState(DarkSkyConnection.getWeatherIcon(currentData.getIcon()));
                    break;
                case CHANNEL_CONDITION_ICON_ID:
                    state = getStringTypeState(currentData.getIcon());
                    break;
                case CHANNEL_TEMPERATURE:
                    state = getQuantityTypeState(currentData.getTemperature(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(currentData.getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(currentData.getHumidity() * 100, PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(currentData.getWindSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(currentData.getWindBearing(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(currentData.getWindGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(currentData.getCloudCover() * 100, PERCENT);
                    break;
                case CHANNEL_VISIBILITY:
                    state = getQuantityTypeState(currentData.getVisibility(), KILO(METRE));
                    break;
                case CHANNEL_RAIN:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_RAIN.equals(currentData.getPrecipType()) ? currentData.getPrecipIntensity() : 0,
                            MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_SNOW.equals(currentData.getPrecipType()) ? currentData.getPrecipIntensity() : 0,
                            MILLI(METRE));
                    break;
                case CHANNEL_PRECIPITATION_PROBABILITY:
                    state = getQuantityTypeState(currentData.getPrecipProbability() * 100, PERCENT);
                    break;
                case CHANNEL_UVINDEX:
                    state = getDecimalTypeState(currentData.getUvIndex());
                    break;
                case CHANNEL_OZONE:
                    state = getQuantityTypeState(currentData.getOzone(), DOBSON_UNIT);
                    break;
                case CHANNEL_SUNRISE:
                case CHANNEL_SUNSET:
                    updateDailyForecastChannel(channelUID, 0);
                    return;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    /**
     * Update the channel from the last Dark Sky data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count
     */
    private void updateHourlyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (weatherData != null && weatherData.getHourly() != null
                && weatherData.getHourly().getData().size() > count) {
            HourlyData forecastData = weatherData.getHourly().getData().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getTime());
                    break;
                case CHANNEL_CONDITION:
                    state = getStringTypeState(forecastData.getSummary());
                    break;
                case CHANNEL_CONDITION_ICON:
                    state = getRawTypeState(DarkSkyConnection.getWeatherIcon(forecastData.getIcon()));
                    break;
                case CHANNEL_CONDITION_ICON_ID:
                    state = getStringTypeState(forecastData.getIcon());
                    break;
                case CHANNEL_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getTemperature(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(forecastData.getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(forecastData.getHumidity() * 100, PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(forecastData.getWindSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(forecastData.getWindBearing(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(forecastData.getWindGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(forecastData.getCloudCover() * 100, PERCENT);
                    break;
                case CHANNEL_VISIBILITY:
                    state = getQuantityTypeState(forecastData.getVisibility(), KILO(METRE));
                    break;
                case CHANNEL_RAIN:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_RAIN.equals(forecastData.getPrecipType()) ? forecastData.getPrecipIntensity()
                                    : 0,
                            MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_SNOW.equals(forecastData.getPrecipType()) ? forecastData.getPrecipIntensity()
                                    : 0,
                            MILLI(METRE));
                    break;
                case CHANNEL_PRECIPITATION_PROBABILITY:
                    state = getQuantityTypeState(forecastData.getPrecipProbability() * 100, PERCENT);
                    break;
                case CHANNEL_UVINDEX:
                    state = getDecimalTypeState(forecastData.getUvIndex());
                    break;
                case CHANNEL_OZONE:
                    state = getQuantityTypeState(forecastData.getOzone(), DOBSON_UNIT);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    /**
     * Update the channel from the last Dark Sky data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count
     */
    private void updateDailyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (weatherData != null && weatherData.getDaily() != null && weatherData.getDaily().getData().size() > count) {
            DailyData forecastData = weatherData.getDaily().getData().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getTime());
                    break;
                case CHANNEL_CONDITION:
                    state = getStringTypeState(forecastData.getSummary());
                    break;
                case CHANNEL_CONDITION_ICON:
                    state = getRawTypeState(DarkSkyConnection.getWeatherIcon(forecastData.getIcon()));
                    break;
                case CHANNEL_CONDITION_ICON_ID:
                    state = getStringTypeState(forecastData.getIcon());
                    break;
                case CHANNEL_MIN_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getTemperatureMin(), CELSIUS);
                    break;
                case CHANNEL_MAX_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getTemperatureMax(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(forecastData.getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(forecastData.getHumidity() * 100, PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(forecastData.getWindSpeed(), METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(forecastData.getWindBearing(), DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(forecastData.getWindGust(), METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(forecastData.getCloudCover() * 100, PERCENT);
                    break;
                case CHANNEL_VISIBILITY:
                    state = getQuantityTypeState(forecastData.getVisibility(), KILO(METRE));
                    break;
                case CHANNEL_RAIN:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_RAIN.equals(forecastData.getPrecipType()) ? forecastData.getPrecipIntensity()
                                    : 0,
                            MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_SNOW.equals(forecastData.getPrecipType()) ? forecastData.getPrecipIntensity()
                                    : 0,
                            MILLI(METRE));
                    break;
                case CHANNEL_PRECIPITATION_PROBABILITY:
                    state = getQuantityTypeState(forecastData.getPrecipProbability() * 100, PERCENT);
                    break;
                case CHANNEL_UVINDEX:
                    state = getDecimalTypeState(forecastData.getUvIndex());
                    break;
                case CHANNEL_OZONE:
                    state = getQuantityTypeState(forecastData.getOzone(), DOBSON_UNIT);
                    break;
                case CHANNEL_SUNRISE:
                    state = getDateTimeTypeState(forecastData.getSunriseTime());
                    if (count == 0 && state instanceof DateTimeType) {
                        scheduleJob(TRIGGER_SUNRISE, applyChannelConfig(((DateTimeType) state).getZonedDateTime(),
                                sunriseTriggerChannelConfig));
                    }
                    break;
                case CHANNEL_SUNSET:
                    state = getDateTimeTypeState(forecastData.getSunsetTime());
                    if (count == 0 && state instanceof DateTimeType) {
                        scheduleJob(TRIGGER_SUNSET, applyChannelConfig(((DateTimeType) state).getZonedDateTime(),
                                sunsetTriggerChannelConfig));
                    }
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }
}
