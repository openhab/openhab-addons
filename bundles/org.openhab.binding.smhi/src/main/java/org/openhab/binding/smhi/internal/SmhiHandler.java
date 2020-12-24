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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmhiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class SmhiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmhiHandler.class);

    private SmhiConfiguration config = new SmhiConfiguration();

    private final HttpClient httpClient;
    private @Nullable SmhiConnector connection;
    private ZonedDateTime currentHour;
    private ZonedDateTime currentDay;
    private @Nullable TimeSeries cachedTimeSeries;
    private boolean hasLatestForecast = false;
    private @Nullable Future<?> forecastUpdater;
    private @Nullable Future<?> instantUpdate;

    public SmhiHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
        this.currentHour = calculateCurrentHour();
        this.currentDay = calculateCurrentDay();
    }

    /**
     * Handles commands sent to channels. Since all values are read-only, only REFRESH commands are allowed.
     * Sending REFRESH to any item updates all items, since all values are returned in the response from Smhi.
     * Therefore there's a wait of 5 seconds before the values are fetched, in which time all other commands are
     * blocked, to prevent spamming Smhi's API.
     *
     * @param channelUID
     * @param command
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateNow();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SmhiConfiguration.class);

        connection = new SmhiConnector(httpClient);

        // Check which channel groups are selected in the config.
        List<Channel> channels = new ArrayList<>();
        channels.addAll(createChannels());
        updateThing(editThing().withChannels(channels).build());

        startPolling();
        updateNow();
    }

    /**
     * Start polling for updated weather forecast.
     */
    private synchronized void startPolling() {
        logger.debug("Start polling");
        forecastUpdater = scheduler.scheduleWithFixedDelay(this::waitForForecast, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Cancels all jobs.
     */
    private synchronized void cancelPolling() {
        logger.debug("Cancelling polling");
        Future<?> localRef = forecastUpdater;
        if (localRef != null) {
            localRef.cancel(false);
        }
        localRef = instantUpdate;
        if (localRef != null) {
            localRef.cancel(false);
        }
    }

    /**
     * Update channels with new forecast data.
     * 
     * @param timeSeries A {@link TimeSeries} object containing forecasts.
     */
    private void updateChannels(TimeSeries timeSeries) {
        // Loop through hourly forecasts and update those available
        for (int i = 0; i < 25; i++) {
            List<Channel> channels = thing.getChannelsOfGroup("hour_" + i);
            if (channels.isEmpty()) {
                continue;
            }
            Optional<Forecast> forecast = timeSeries.getForecast(i);
            if (forecast.isPresent()) {
                channels.forEach(c -> {
                    String id = c.getUID().getIdWithoutGroup();
                    Optional<BigDecimal> value = forecast.get().getParameter(id);
                    updateChannel(c, value);
                });
            }
        }
        // Loop through daily forecasts and updates those available
        for (int i = 0; i < 10; i++) {
            List<Channel> channels = thing.getChannelsOfGroup("day_" + i);
            if (channels.isEmpty()) {
                continue;
            }

            int dayOffset = i;
            int hourOffset = 24 * dayOffset + 12;
            Optional<Forecast> forecast = timeSeries.getForecast(currentDay, hourOffset);

            if (forecast.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No forecast yet for {}", currentDay.plusHours(hourOffset));
                }
                channels.forEach(c -> {
                    updateState(c.getUID(), UnDefType.UNDEF);
                });
            } else {
                channels.forEach(c -> {
                    String id = c.getUID().getIdWithoutGroup();
                    Optional<BigDecimal> value;
                    if (isAggregatedChannel(id)) {
                        value = getAggregatedValue(id, timeSeries, dayOffset);
                    } else {
                        value = forecast.get().getParameter(id);
                    }
                    updateChannel(c, value);
                });
            }
        }
    }

    private void updateChannel(Channel channel, Optional<BigDecimal> value) {
        String id = channel.getUID().getIdWithoutGroup();
        State newState = UnDefType.UNDEF;

        if (value.isPresent()) {
            switch (id) {
                case PRESSURE:
                    newState = new QuantityType<>(value.get(), MetricPrefix.HECTO(SIUnits.PASCAL));
                    break;
                case TEMPERATURE:
                case TEMPERATURE_MAX:
                case TEMPERATURE_MIN:
                    newState = new QuantityType<>(value.get(), SIUnits.CELSIUS);
                    break;
                case VISIBILITY:
                    newState = new QuantityType<>(value.get(), MetricPrefix.KILO(SIUnits.METRE));
                    break;
                case WIND_DIRECTION:
                    newState = new QuantityType<>(value.get(), Units.DEGREE_ANGLE);
                    break;
                case WIND_SPEED:
                case WIND_MAX:
                case WIND_MIN:
                case GUST:
                    newState = new QuantityType<>(value.get(), Units.METRE_PER_SECOND);
                    break;
                case RELATIVE_HUMIDITY:
                case THUNDER_PROBABILITY:
                    newState = new QuantityType<>(value.get(), Units.PERCENT);
                    break;
                case PERCENT_FROZEN:
                    // Smhi returns -9 for spp if there's no precipitation, convert to UNDEF
                    if (value.get().intValue() == -9) {
                        newState = UnDefType.UNDEF;
                    } else {
                        newState = new QuantityType<>(value.get(), Units.PERCENT);
                    }
                    break;
                case HIGH_CLOUD_COVER:
                case MEDIUM_CLOUD_COVER:
                case LOW_CLOUD_COVER:
                case TOTAL_CLOUD_COVER:
                    newState = new QuantityType<>(value.get().multiply(OCTAS_TO_PERCENT), Units.PERCENT);
                    break;
                case PRECIPITATION_MAX:
                case PRECIPITATION_MEAN:
                case PRECIPITATION_MEDIAN:
                case PRECIPITATION_MIN:
                    newState = new QuantityType<>(value.get(), Units.MILLIMETRE_PER_HOUR);
                    break;
                case PRECIPITATION_TOTAL:
                    newState = new QuantityType<>(value.get(), MetricPrefix.MILLI(SIUnits.METRE));
                    break;
                default:
                    newState = new DecimalType(value.get());
            }
        }

        updateState(channel.getUID(), newState);
    }

    /**
     * Dispose the {@link org.openhab.core.thing.binding.ThingHandler}. Cancel scheduled jobs
     */
    public void dispose() {
        cancelPolling();
    }

    /**
     * First check if the time has shifted to a new hour, then start checking if a new forecast have been
     * published, in that case, fetch it and update channels.
     */
    private void waitForForecast() {
        if (isItNewHour()) {
            currentHour = calculateCurrentHour();
            currentDay = calculateCurrentDay();
            // Update channels with cached forecasts - just shift an hour forward
            TimeSeries forecast = cachedTimeSeries;
            if (forecast != null) {
                updateChannels(forecast);
            }
            hasLatestForecast = false;
        }
        if (!hasLatestForecast && isForecastUpdated()) {
            getUpdatedForecast();
        }
    }

    /**
     * Schedules an imminent update, making it wait 5 seconds to catch any bursts of calls before executing.
     */
    private synchronized void updateNow() {
        Future<?> localRef = instantUpdate;
        if (localRef == null || localRef.isDone()) {
            instantUpdate = scheduler.schedule(this::getUpdatedForecast, 5, TimeUnit.SECONDS);
        } else {
            logger.debug("Already waiting for scheduled refresh");
        }
    }

    /**
     * Checks if it is a new hour.
     * 
     * @return true if the current time is more than one hour after currentHour, otherwise false.
     */
    private boolean isItNewHour() {
        return ZonedDateTime.now().minusHours(1).isAfter(currentHour);
    }

    /**
     * Call Smhi's endpoint to check for the time of the last forecast, to see if a new one is available.
     * 
     * @return true if the time of the latest forecast is equal to or after currentHour, otherwise false
     */
    private boolean isForecastUpdated() {
        ZonedDateTime referenceTime;
        SmhiConnector apiConnection = connection;
        if (apiConnection != null) {
            try {
                referenceTime = apiConnection.getReferenceTime();
            } catch (SmhiException e) {
                return false;
            }
            return referenceTime.isEqual(currentHour) || referenceTime.isAfter(currentHour);
        }
        return false;
    }

    /**
     * Fetches latest forecast from Smhi, update channels and check if it was published in the current hour.
     * If it is, set flag to indicate we have the latest forecast.
     */
    private void getUpdatedForecast() {
        TimeSeries forecast;
        ZonedDateTime referenceTime;
        SmhiConnector apiConnection = connection;
        if (apiConnection != null) {
            try {
                forecast = apiConnection.getForecast(config.latitude, config.longitude);
            } catch (SmhiException e) {
                String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
                logger.debug("Failed to get new forecast: {}", message);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
                return;
            } catch (PointOutOfBoundsException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Coordinates outside valid area");
                cancelPolling();
                return;
            }
            updateStatus(ThingStatus.ONLINE);
            referenceTime = forecast.getReferenceTime();
            updateChannels(forecast);
            if (referenceTime.isEqual(currentHour) || referenceTime.isAfter(currentHour)) {
                hasLatestForecast = true;
            }
            cachedTimeSeries = forecast;
        }
    }

    /**
     * Get the current time rounded down to hour
     * 
     * @return A {@link ZonedDateTime} corresponding to the last even hour
     */
    private ZonedDateTime calculateCurrentHour() {
        ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        int y = now.getYear();
        int m = now.getMonth().getValue();
        int d = now.getDayOfMonth();
        int h = now.getHour();
        return ZonedDateTime.of(y, m, d, h, 0, 0, 0, ZoneOffset.UTC);
    }

    /**
     * Get the current time rounded down to day
     * 
     * @return A {@link ZonedDateTime} corresponding to the last even day.
     */
    private ZonedDateTime calculateCurrentDay() {
        ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        int y = now.getYear();
        int m = now.getMonth().getValue();
        int d = now.getDayOfMonth();
        return ZonedDateTime.of(y, m, d, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    /**
     * Creates channels based on selections in thing configuration
     * 
     * @return
     */
    private List<Channel> createChannels() {
        List<Channel> channels = new ArrayList<>();

        @Nullable
        List<Integer> hourlyForecasts = config.hourlyForecasts;
        @Nullable
        List<Integer> dailyForecasts = config.dailyForecasts;

        if (hourlyForecasts != null) {
            for (int i : hourlyForecasts) {
                ChannelGroupUID groupUID = new ChannelGroupUID(thing.getUID(), "hour_" + i);
                HOURLY_CHANNELS.forEach(id -> {
                    channels.add(createChannel(groupUID, id));
                });
            }
        }

        if (dailyForecasts != null) {
            for (int i : dailyForecasts) {
                ChannelGroupUID groupUID = new ChannelGroupUID(thing.getUID(), "day_" + i);
                DAILY_CHANNELS.forEach(id -> {
                    channels.add(createChannel(groupUID, id));
                });
            }
        }
        return channels;
    }

    /**
     * Create a channel with the correct item type based on the channel ID
     *
     * @param channelGroupUID Channel group the channel belongs to
     * @param channelID ID of the channel (without group ID)
     * @return The created channel
     */
    private Channel createChannel(ChannelGroupUID channelGroupUID, String channelID) {
        ChannelUID channelUID = new ChannelUID(channelGroupUID, channelID);
        String itemType = "Number";
        switch (channelID) {
            case TEMPERATURE:
            case TEMPERATURE_MAX:
            case TEMPERATURE_MIN:
                itemType += ":Temperature";
                break;
            case PRESSURE:
                itemType += ":Pressure";
                break;
            case VISIBILITY:
            case PRECIPITATION_TOTAL:
                itemType += ":Length";
                break;
            case WIND_DIRECTION:
                itemType += ":Angle";
            case WIND_SPEED:
            case WIND_MAX:
            case WIND_MIN:
            case GUST:
            case PRECIPITATION_MAX:
            case PRECIPITATION_MEAN:
            case PRECIPITATION_MEDIAN:
            case PRECIPITATION_MIN:
                itemType += ":Speed";
                break;
            case RELATIVE_HUMIDITY:
            case PERCENT_FROZEN:
            case TOTAL_CLOUD_COVER:
            case HIGH_CLOUD_COVER:
            case MEDIUM_CLOUD_COVER:
            case LOW_CLOUD_COVER:
            case THUNDER_PROBABILITY:
                itemType += ":Dimensionless";
                break;

        }
        Channel channel = ChannelBuilder.create(channelUID, itemType)
                .withType(new ChannelTypeUID(BINDING_ID, channelID)).build();
        return channel;
    }

    private boolean isAggregatedChannel(String channelId) {
        switch (channelId) {
            case TEMPERATURE_MAX:
            case TEMPERATURE_MIN:
            case WIND_MAX:
            case WIND_MIN:
            case PRECIPITATION_TOTAL:
                return true;
            default:
                return false;
        }
    }

    private Optional<BigDecimal> getAggregatedValue(String channelId, TimeSeries timeSeries, int dayOffset) {
        switch (channelId) {
            case TEMPERATURE_MAX:
                return ForecastAggregator.max(timeSeries, dayOffset, TEMPERATURE);
            case TEMPERATURE_MIN:
                return ForecastAggregator.min(timeSeries, dayOffset, TEMPERATURE);
            case WIND_MAX:
                return ForecastAggregator.max(timeSeries, dayOffset, WIND_SPEED);
            case WIND_MIN:
                return ForecastAggregator.min(timeSeries, dayOffset, WIND_SPEED);
            case PRECIPITATION_TOTAL:
                return ForecastAggregator.total(timeSeries, dayOffset, PRECIPITATION_MEAN);
            default:
                return Optional.empty();
        }
    }
}
