/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.darksky.internal.DarkSkyBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.darksky.internal.config.DarkSkyChannelConfiguration;
import org.openhab.binding.darksky.internal.config.DarkSkyWeatherAndForecastConfiguration;
import org.openhab.binding.darksky.internal.connection.DarkSkyCommunicationException;
import org.openhab.binding.darksky.internal.connection.DarkSkyConfigurationException;
import org.openhab.binding.darksky.internal.connection.DarkSkyConnection;
import org.openhab.binding.darksky.internal.model.DarkSkyCurrentlyData;
import org.openhab.binding.darksky.internal.model.DarkSkyDailyData.DailyData;
import org.openhab.binding.darksky.internal.model.DarkSkyHourlyData.HourlyData;
import org.openhab.binding.darksky.internal.model.DarkSkyJsonWeatherData;
import org.openhab.binding.darksky.internal.model.DarkSkyJsonWeatherData.AlertsData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link DarkSkyWeatherAndForecastHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DarkSkyWeatherAndForecastHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DarkSkyWeatherAndForecastHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(THING_TYPE_WEATHER_AND_FORECAST);

    private static final String PRECIP_TYPE_SNOW = "snow";
    private static final String PRECIP_TYPE_RAIN = "rain";

    private static final String CHANNEL_GROUP_HOURLY_FORECAST_PREFIX = "forecastHours";
    private static final String CHANNEL_GROUP_DAILY_FORECAST_PREFIX = "forecastDay";
    private static final String CHANNEL_GROUP_ALERTS_PREFIX = "alerts";
    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_ALERTS_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_ALERTS_PREFIX + "([0-9]*)");

    // keeps track of all jobs
    private static final Map<String, Job> JOBS = new ConcurrentHashMap<>();

    // keeps track of the parsed location
    protected @Nullable PointType location;
    // keeps track of the parsed counts
    private int forecastHours = 24;
    private int forecastDays = 8;
    private int numberOfAlerts = 0;

    private @Nullable DarkSkyChannelConfiguration sunriseTriggerChannelConfig;
    private @Nullable DarkSkyChannelConfiguration sunsetTriggerChannelConfig;
    private @Nullable DarkSkyJsonWeatherData weatherData;

    public DarkSkyWeatherAndForecastHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize DarkSkyWeatherAndForecastHandler handler '{}'.", getThing().getUID());
        DarkSkyWeatherAndForecastConfiguration config = getConfigAs(DarkSkyWeatherAndForecastConfiguration.class);

        boolean configValid = true;
        if (config.location == null || config.location.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-location");
            configValid = false;
        }

        try {
            location = new PointType(config.location);
        } catch (IllegalArgumentException e) {
            logger.warn("Error parsing 'location' parameter: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-parsing-location");
            location = null;
            configValid = false;
        }

        int newForecastHours = config.forecastHours;
        if (newForecastHours < 0 || newForecastHours > 48) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-number-of-hours");
            configValid = false;
        }
        int newForecastDays = config.forecastDays;
        if (newForecastDays < 0 || newForecastDays > 8) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-number-of-days");
            configValid = false;
        }
        int newNumberOfAlerts = config.numberOfAlerts;
        if (newNumberOfAlerts < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-number-of-alerts");
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
            if (numberOfAlerts != newNumberOfAlerts) {
                logger.debug("Rebuilding alerts channel groups.");
                if (numberOfAlerts > newNumberOfAlerts) {
                    for (int i = newNumberOfAlerts + 1; i <= numberOfAlerts; ++i) {
                        toBeRemovedChannels
                                .addAll(removeChannelsOfGroup(CHANNEL_GROUP_ALERTS_PREFIX + Integer.toString(i)));
                    }
                } else {
                    for (int i = numberOfAlerts + 1; i <= newNumberOfAlerts; ++i) {
                        toBeAddedChannels.addAll(createChannelsForGroup(
                                CHANNEL_GROUP_ALERTS_PREFIX + Integer.toString(i), CHANNEL_GROUP_TYPE_ALERTS));
                    }
                }
                numberOfAlerts = newNumberOfAlerts;
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

            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void dispose() {
        cancelAllJobs();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID);
        } else {
            logger.debug("The Dark Sky binding is a read-only binding and cannot handle command '{}'.", command);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())
                && ThingStatusDetail.BRIDGE_OFFLINE.equals(getThing().getStatusInfo().getStatusDetail())) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else if (ThingStatus.OFFLINE.equals(bridgeStatusInfo.getStatus())
                && !ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Creates all {@link Channel}s for the given {@link ChannelGroupTypeUID}.
     *
     * @param channelGroupId the channel group id
     * @param channelGroupTypeUID the {@link ChannelGroupTypeUID}
     * @return a list of all {@link Channel}s for the channel group
     */
    private List<Channel> createChannelsForGroup(String channelGroupId, ChannelGroupTypeUID channelGroupTypeUID) {
        logger.debug("Building channel group '{}' for thing '{}'.", channelGroupId, getThing().getUID());
        List<Channel> channels = new ArrayList<>();
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            for (ChannelBuilder channelBuilder : callback.createChannelBuilders(
                    new ChannelGroupUID(getThing().getUID(), channelGroupId), channelGroupTypeUID)) {
                Channel newChannel = channelBuilder.build(),
                        existingChannel = getThing().getChannel(newChannel.getUID().getId());
                if (existingChannel != null) {
                    logger.trace("Thing '{}' already has an existing channel '{}'. Omit adding new channel '{}'.",
                            getThing().getUID(), existingChannel.getUID(), newChannel.getUID());
                    continue;
                }
                channels.add(newChannel);
            }
        }
        return channels;
    }

    /**
     * Removes all {@link Channel}s of the given channel group.
     *
     * @param channelGroupId the channel group id
     * @return a list of all {@link Channel}s in the given channel group
     */
    private List<Channel> removeChannelsOfGroup(String channelGroupId) {
        logger.debug("Removing channel group '{}' from thing '{}'.", channelGroupId, getThing().getUID());
        return getThing().getChannelsOfGroup(channelGroupId);
    }

    /**
     * Updates Dark Sky data for this location.
     *
     * @param connection {@link DarkSkyConnection} instance
     */
    public void updateData(DarkSkyConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (DarkSkyCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } catch (DarkSkyConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * Requests the data from Dark Sky API.
     *
     * @param connection {@link DarkSkyConnection} instance
     * @return true, if the request for the Dark Sky data was successful
     * @throws DarkSkyCommunicationException
     * @throws DarkSkyConfigurationException
     */
    private boolean requestData(DarkSkyConnection connection)
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

    /**
     * Updates all channels of this handler from the latest Dark Sky data retrieved.
     */
    private void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && channelUID.isInGroup() && channelUID.getGroupId() != null
                    && isLinked(channelUID)) {
                updateChannel(channelUID);
            }
        }
    }

    /**
     * Updates the channel with the given UID from the latest Dark Sky data retrieved.
     *
     * @param channelUID UID of the channel
     */
    private void updateChannel(ChannelUID channelUID) {
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
                Matcher alertsMatcher = CHANNEL_GROUP_ALERTS_PREFIX_PATTERN.matcher(channelGroupId);
                if (alertsMatcher.find() && (i = Integer.parseInt(alertsMatcher.group(1))) >= 1) {
                    updateAlertsChannel(channelUID, i);
                    break;
                }
                logger.warn("Unknown channel group '{}'. Cannot update channel '{}'.", channelGroupId, channelUID);
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
            DarkSkyCurrentlyData currentData = weatherData.getCurrently();
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
                case CHANNEL_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(currentData.getApparentTemperature(), CELSIUS);
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
                            MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_SNOW.equals(currentData.getPrecipType()) ? currentData.getPrecipIntensity() : 0,
                            MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_PRECIPITATION_INTENSITY:
                    state = getQuantityTypeState(currentData.getPrecipIntensity(), MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_PRECIPITATION_PROBABILITY:
                    state = getQuantityTypeState(currentData.getPrecipProbability() * 100, PERCENT);
                    break;
                case CHANNEL_PRECIPITATION_TYPE:
                    state = getStringTypeState(currentData.getPrecipType());
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
                case CHANNEL_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getApparentTemperature(), CELSIUS);
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
                            MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_SNOW.equals(forecastData.getPrecipType()) ? forecastData.getPrecipIntensity()
                                    : 0,
                            MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_PRECIPITATION_INTENSITY:
                    state = getQuantityTypeState(forecastData.getPrecipIntensity(), MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_PRECIPITATION_PROBABILITY:
                    state = getQuantityTypeState(forecastData.getPrecipProbability() * 100, PERCENT);
                    break;
                case CHANNEL_PRECIPITATION_TYPE:
                    state = getStringTypeState(forecastData.getPrecipType());
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
                case CHANNEL_MIN_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getApparentTemperatureMin(), CELSIUS);
                    break;
                case CHANNEL_MAX_APPARENT_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getApparentTemperatureMax(), CELSIUS);
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
                            MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(
                            PRECIP_TYPE_SNOW.equals(forecastData.getPrecipType()) ? forecastData.getPrecipIntensity()
                                    : 0,
                            MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_PRECIPITATION_INTENSITY:
                    state = getQuantityTypeState(forecastData.getPrecipIntensity(), MILLIMETRE_PER_HOUR);
                    break;
                case CHANNEL_PRECIPITATION_PROBABILITY:
                    state = getQuantityTypeState(forecastData.getPrecipProbability() * 100, PERCENT);
                    break;
                case CHANNEL_PRECIPITATION_TYPE:
                    state = getStringTypeState(forecastData.getPrecipType());
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

    /**
     * Update the channel from the last Dark Sky data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count
     */
    private void updateAlertsChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        List<AlertsData> alerts = weatherData != null ? weatherData.getAlerts() : null;
        State state = UnDefType.UNDEF;
        if (alerts != null && alerts.size() > count) {
            AlertsData alertsData = alerts.get(count - 1);
            switch (channelId) {
                case CHANNEL_ALERT_TITLE:
                    state = getStringTypeState(alertsData.title);
                    break;
                case CHANNEL_ALERT_DESCRIPTION:
                    state = getStringTypeState(alertsData.description);
                    break;
                case CHANNEL_ALERT_SEVERITY:
                    state = getStringTypeState(alertsData.severity);
                    break;
                case CHANNEL_ALERT_ISSUED:
                    state = getDateTimeTypeState(alertsData.time);
                    break;
                case CHANNEL_ALERT_EXPIRES:
                    state = getDateTimeTypeState(alertsData.expires);
                    break;
                case CHANNEL_ALERT_URI:
                    state = getStringTypeState(alertsData.uri);
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        } else {
            logger.debug("No data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
        updateState(channelUID, state);
    }

    private State getDateTimeTypeState(int value) {
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(value), ZoneId.systemDefault()));
    }

    private State getDecimalTypeState(int value) {
        return new DecimalType(value);
    }

    private State getRawTypeState(@Nullable RawType image) {
        return (image == null) ? UnDefType.UNDEF : image;
    }

    private State getStringTypeState(@Nullable String value) {
        return (value == null) ? UnDefType.UNDEF : new StringType(value);
    }

    private State getQuantityTypeState(double value, Unit<?> unit) {
        return new QuantityType<>(value, unit);
    }

    /**
     * Applies the given configuration to the given timestamp.
     *
     * @param dateTime timestamp represented as {@link ZonedDateTime}
     * @param config {@link DarkSkyChannelConfiguration} instance
     * @return the modified timestamp
     */
    private ZonedDateTime applyChannelConfig(ZonedDateTime dateTime, @Nullable DarkSkyChannelConfiguration config) {
        ZonedDateTime modifiedDateTime = dateTime;
        if (config != null) {
            if (config.getOffset() != 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Apply offset of {} min to timestamp '{}'.", config.getOffset(),
                            modifiedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                modifiedDateTime = modifiedDateTime.plusMinutes(config.getOffset());
            }
            long earliestInMinutes = config.getEarliestInMinutes();
            if (earliestInMinutes > 0) {
                ZonedDateTime earliestDateTime = modifiedDateTime.truncatedTo(ChronoUnit.DAYS)
                        .plusMinutes(earliestInMinutes);
                if (modifiedDateTime.isBefore(earliestDateTime)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Use earliest timestamp '{}' instead of '{}'.",
                                earliestDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                modifiedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                    return earliestDateTime;
                }
            }
            long latestInMinutes = config.getLatestInMinutes();
            if (latestInMinutes > 0) {
                ZonedDateTime latestDateTime = modifiedDateTime.truncatedTo(ChronoUnit.DAYS)
                        .plusMinutes(latestInMinutes);
                if (modifiedDateTime.isAfter(latestDateTime)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Use latest timestamp '{}' instead of '{}'.",
                                latestDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                modifiedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                    return latestDateTime;
                }
            }
        }
        return modifiedDateTime;
    }

    /**
     * Schedules or reschedules a job for the channel with the given id if the given timestamp is in the future.
     *
     * @param channelId id of the channel
     * @param dateTime timestamp of the job represented as {@link ZonedDateTime}
     */
    @SuppressWarnings("null")
    private synchronized void scheduleJob(String channelId, ZonedDateTime dateTime) {
        long delay = dateTime.toEpochSecond() - ZonedDateTime.now().toEpochSecond();
        if (delay > 0) {
            Job job = JOBS.get(channelId);
            if (job == null || job.getFuture().isCancelled()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Schedule job for '{}' in {} s (at '{}').", channelId, delay,
                            dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                JOBS.put(channelId, new Job(channelId, delay));
            } else {
                if (delay != job.getDelay()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Reschedule job for '{}' in {} s (at '{}').", channelId, delay,
                                dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                    job.getFuture().cancel(true);
                    JOBS.put(channelId, new Job(channelId, delay));
                }
            }
        }
    }

    /**
     * Cancels all jobs.
     */
    private void cancelAllJobs() {
        logger.debug("Cancel all jobs.");
        JOBS.keySet().forEach(this::cancelJob);
    }

    /**
     * Cancels the job for the channel with the given id.
     *
     * @param channelId id of the channel
     */
    @SuppressWarnings("null")
    private synchronized void cancelJob(String channelId) {
        Job job = JOBS.remove(channelId);
        if (job != null && !job.getFuture().isCancelled()) {
            logger.debug("Cancel job for '{}'.", channelId);
            job.getFuture().cancel(true);
        }
    }

    /**
     * Executes the job for the channel with the given id.
     *
     * @param channelId id of the channel
     */
    private void executeJob(String channelId) {
        logger.debug("Trigger channel '{}' with event '{}'.", channelId, EVENT_START);
        triggerChannel(channelId, EVENT_START);
    }

    private final class Job {
        private final long delay;
        private final ScheduledFuture<?> future;

        public Job(String event, long delay) {
            this.delay = delay;
            this.future = scheduler.schedule(() -> {
                executeJob(event);
            }, delay, TimeUnit.SECONDS);
        }

        public long getDelay() {
            return delay;
        }

        public ScheduledFuture<?> getFuture() {
            return future;
        }
    }
}
