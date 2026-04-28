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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.smhi.provider.ParameterMetadata;
import org.openhab.binding.smhi.provider.SmhiChannelTypeProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
    private final SmhiChannelTypeProvider channelTypeProvider;
    private final ChannelTypeRegistry channelTypeRegistry;
    private @Nullable SmhiConnector connection;
    private ZonedDateTime currentHour;
    private @Nullable SmhiTimeSeries cachedTimeSeries;
    private @Nullable Future<?> parameterUpdater;
    private @Nullable Future<?> forecastUpdater;
    private @Nullable Future<?> instantUpdate;
    private boolean parametersInitialized = false;

    public SmhiHandler(Thing thing, HttpClient httpClient, SmhiChannelTypeProvider channelTypeProvider,
            ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.httpClient = httpClient;
        this.channelTypeProvider = channelTypeProvider;
        this.channelTypeRegistry = channelTypeRegistry;
        this.currentHour = calculateCurrentHour();
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
        scheduler.execute(() -> {
            updateParameters();
            if (parametersInitialized) {
                recreateChannels();
                updateNow();
            }
        });

        startPolling();
    }

    /**
     * Start polling for updated weather forecast.
     */
    private synchronized void startPolling() {
        logger.debug("Start polling");
        parameterUpdater = scheduler.scheduleWithFixedDelay(this::updateParameters, 1, 1, TimeUnit.HOURS);
        forecastUpdater = scheduler.scheduleWithFixedDelay(this::waitForForecast, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Cancels all jobs.
     */
    private synchronized void cancelPolling() {
        logger.debug("Cancelling polling");
        Future<?> localRef = parameterUpdater;
        if (localRef != null) {
            localRef.cancel(false);
        }
        localRef = forecastUpdater;
        if (localRef != null) {
            localRef.cancel(false);
        }
        localRef = instantUpdate;
        if (localRef != null) {
            localRef.cancel(false);
        }
    }

    /**
     * Downloads the parameter description and updates channel types.
     * If any parameter has been updated, recreate the channels
     */
    private void updateParameters() {
        SmhiConnector apiConnection = connection;
        if (apiConnection != null) {
            try {
                List<ParameterMetadata> metadata = apiConnection.getParameterMetadata();
                metadata.forEach(channelTypeProvider::putParameterMetadata);
                AGGREGATE_CHANNELS_METADATA.forEach(channelTypeProvider::putParameterMetadata);
                parametersInitialized = true;
            } catch (SmhiException e) {
                logger.warn("Unable to get parameters from API");
                return;
            }
        }
        if (channelTypeProvider.channelsUpdatedSince(Duration.ofHours(1))) {
            logger.debug("New channel type(s) created from SMHI parameters, recreating channels");
            recreateChannels();
        }
    }

    /**
     * Recreate channels
     */
    private void recreateChannels() {
        Map<ChannelUID, Channel> channels = new LinkedHashMap<>();
        // Check which channel groups are selected in the config.
        createChannels().forEach(c -> {
            channels.put(c.getUID(), c);
        });

        // Keep connected channels
        getThing().getChannels().stream().filter(c -> isLinked(c.getUID())).forEach(c -> {
            channels.putIfAbsent(c.getUID(), c);
        });

        // TODO: Remove after last 2026 release
        List<String> deprecatedChannels = channels.values().stream().map(Channel::getUID)
                .filter(channelId -> PMP3G_BACKWARD_COMP.containsKey(channelId.getIdWithoutGroup()))
                .map(ChannelUID::getId).toList();
        if (!deprecatedChannels.isEmpty()) {
            logger.warn(
                    "{}: The following deprecated channels have linked Items. Please relink them to the new channels: {}",
                    thing.getUID(), deprecatedChannels);
        }
        // TODO: end

        updateThing(editThing().withChannels(new ArrayList<>(channels.values())).build());
    }

    /**
     * Update channels with new forecast data.
     *
     * @param timeSeries A {@link SmhiTimeSeries} object containing forecasts.
     */
    private void updateChannels(SmhiTimeSeries timeSeries) {
        // Update timeSeries channels
        List<Channel> tsChannels = thing.getChannelsOfGroup("timeseries");
        tsChannels.forEach(c -> {
            String id = c.getUID().getIdWithoutGroup();
            ParameterMetadata metadata = channelTypeProvider.getParameterMetadata(id);
            if (metadata == null)
                return;

            sendTimeSeries(c.getUID(), timeSeries.getTimeSeries(metadata));
        });

        // Loop through hourly forecasts and update those available
        for (int i = 0; i < 25; i++) {
            List<Channel> channels = thing.getChannelsOfGroup("hour_" + i);
            if (channels.isEmpty()) {
                continue;
            }
            Optional<Forecast> forecast = timeSeries.getForecast(currentHour, i);
            forecast.ifPresent(f -> channels.forEach(c -> {
                String id = c.getUID().getIdWithoutGroup();
                // TODO: Remove after last 2026 release
                if (id.equals(PMP3G_PRECIPITATION_CATEGORY)) {
                    updateState(c.getUID(), new DecimalType(PMP3G_PCAT_BACKWARD_COMP
                            .getOrDefault(f.getParameter(id).intValue(), f.getParameter(id).intValue())));
                    return;
                }
                id = PMP3G_BACKWARD_COMP.getOrDefault(id, id);
                // TODO: end
                ParameterMetadata metadata = channelTypeProvider.getParameterMetadata(id);
                if (metadata == null)
                    return;

                updateState(c.getUID(), f.getParameterAsState(metadata));
            }));
        }
        // Loop through daily forecasts and updates those available
        for (int i = 0; i < 10; i++) {
            List<Channel> channels = thing.getChannelsOfGroup("day_" + i);
            if (channels.isEmpty()) {
                continue;
            }

            int dayOffset = i;
            channels.forEach(c -> {
                String id = c.getUID().getIdWithoutGroup();
                // TODO: Remove after last 2026 release
                if (id.equals(PMP3G_PRECIPITATION_CATEGORY)) {
                    State value = getDayValue(id, timeSeries, dayOffset);
                    updateState(c.getUID(), new DecimalType(PMP3G_PCAT_BACKWARD_COMP
                            .getOrDefault(((DecimalType) value).intValue(), ((DecimalType) value).intValue())));
                    return;
                }
                id = PMP3G_BACKWARD_COMP.getOrDefault(id, id);
                // TODO: end
                updateState(c.getUID(), getDayValue(id, timeSeries, dayOffset));
            });
        }
    }

    /**
     * Dispose the {@link org.openhab.core.thing.binding.ThingHandler}. Cancel scheduled jobs
     */
    @Override
    public void dispose() {
        cancelPolling();
    }

    /**
     * First check if the time has shifted to a new hour, then start checking if a new forecast have been
     * published, in that case, fetch it and update channels.
     */
    private void waitForForecast() {
        if (!parametersInitialized) {
            logger.debug("Parameters not yet initialized, deferring update");
            return;
        }
        try {
            if (isItNewHour()) {
                currentHour = calculateCurrentHour();
                // Update channels with cached forecasts - just shift an hour forward
                SmhiTimeSeries forecast = cachedTimeSeries;
                if (forecast != null) {
                    updateChannels(forecast);
                }
            }
            if (isForecastUpdated()) {
                getUpdatedForecast();
            }
        } catch (RuntimeException e) {
            logger.warn("Unexpected exception occurred, please report to the developers: {}: {}", e.getClass(),
                    e.getMessage());
            logger.debug("Details: ", e);
        }
    }

    /**
     * Schedules an imminent update, making it wait 5 seconds to catch any bursts of calls before executing.
     */
    private synchronized void updateNow() {
        if (!parametersInitialized) {
            logger.debug("Parameters not yet initialized, deferring update");
            return;
        }
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
        ZonedDateTime createdTime;
        SmhiConnector apiConnection = connection;
        if (apiConnection != null) {
            try {
                createdTime = apiConnection.getCreatedTime();
            } catch (SmhiException e) {
                return false;
            }
            SmhiTimeSeries localRef = cachedTimeSeries;
            if (localRef != null) {
                return createdTime.isAfter(localRef.getCreatedTime());
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetches latest forecast from Smhi, update channels and check if it was published in the current hour.
     * If it is, set flag to indicate we have the latest forecast.
     */
    private void getUpdatedForecast() {
        SmhiTimeSeries forecast;
        SmhiConnector apiConnection = connection;
        if (apiConnection != null) {
            try {
                forecast = apiConnection.getForecast(config.getLatitude(), config.getLongitude());
            } catch (SmhiException e) {
                // False positive null warning, e cannot be null here and Optional.orElse()
                // has @Contract(value = "!null -> !null", pure = true)
                @SuppressWarnings("null")
                String message = Optional.ofNullable(e.getCause()).orElse(e).getMessage();
                logger.debug("Failed to get new forecast: {}", message);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
                return;
            } catch (PointOutOfBoundsException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.invalidCoordinates");
                cancelPolling();
                return;
            }
            updateStatus(ThingStatus.ONLINE);
            updateChannels(forecast);
            cachedTimeSeries = forecast;
        }
    }

    /**
     * Get the current time rounded down to hour
     *
     * @return A {@link ZonedDateTime} corresponding to the last even hour
     */
    private ZonedDateTime calculateCurrentHour() {
        return ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
    }

    /**
     * Creates channels based on selections in thing configuration
     *
     * @return A {@link java.util.List} of Channels to add to the Thing
     */
    private List<Channel> createChannels() {
        List<Channel> channels = new ArrayList<>();

        List<Integer> hourlyForecasts = config.getHourlyForecasts();
        List<Integer> dailyForecasts = config.getDailyForecasts();

        ChannelGroupUID tsGroup = new ChannelGroupUID(thing.getUID(), TIMESERIES_GROUP_ID);
        channelTypeProvider.getAllParameterMetadata().forEach(metadata -> {
            ChannelType channelType = channelTypeRegistry
                    .getChannelType(new ChannelTypeUID(BINDING_ID, metadata.name()));
            if (channelType == null)
                return;

            if (!AGGREGATE_CHANNELS.contains(metadata.name())) {
                channels.add(createChannel(tsGroup, channelType));
                for (int i : hourlyForecasts) {
                    ChannelGroupUID groupUID = new ChannelGroupUID(thing.getUID(), "hour_" + i);
                    channels.add(createChannel(groupUID, channelType));
                }
            }
            for (int i : dailyForecasts) {
                ChannelGroupUID groupUID = new ChannelGroupUID(thing.getUID(), "day_" + i);
                channels.add(createChannel(groupUID, channelType));
            }
        });
        return channels;
    }

    /**
     * Create a channel with the correct item type based on the channel ID
     *
     * @param channelGroupUID Channel group the channel belongs to
     * @param channelType {@link ChannelType} the channel type
     * @return The created channel
     */
    private Channel createChannel(ChannelGroupUID channelGroupUID, ChannelType channelType) {
        String channelId = channelType.getUID().getId();
        ChannelUID channelUID = new ChannelUID(channelGroupUID, channelId);
        return ChannelBuilder.create(channelUID, channelType.getItemType()).withType(channelType.getUID()).build();
    }

    /**
     * Gets the value that represents the day forecast for a specified day and channel
     *
     * @param parameter The parameter to retrieve or calculate
     * @param timeSeries A {@link SmhiTimeSeries} object containing forecasts
     * @param dayOffset The number of days from the start of the TimeSeries
     * @return A {@link State} representing the retrieved or calculated value
     */
    private State getDayValue(String parameter, SmhiTimeSeries timeSeries, int dayOffset) {
        // TODO: Remove after last 2026 release
        parameter = PMP3G_BACKWARD_COMP.getOrDefault(parameter, parameter);
        // TODO: end

        return switch (parameter) {
            case TEMPERATURE_MAX ->
                ForecastAggregator.max(timeSeries, dayOffset, channelTypeProvider.getParameterMetadata(TEMPERATURE));
            case TEMPERATURE_MIN ->
                ForecastAggregator.min(timeSeries, dayOffset, channelTypeProvider.getParameterMetadata(TEMPERATURE));
            case WIND_MAX ->
                ForecastAggregator.max(timeSeries, dayOffset, channelTypeProvider.getParameterMetadata(WIND_SPEED));
            case WIND_MIN ->
                ForecastAggregator.min(timeSeries, dayOffset, channelTypeProvider.getParameterMetadata(WIND_SPEED));
            case PRECIPITATION_TOTAL -> ForecastAggregator.total(timeSeries, dayOffset,
                    channelTypeProvider.getParameterMetadata(PRECIPITATION_MEAN),
                    channelTypeProvider.getParameterMetadata(PRECIPITATION_TOTAL));
            default -> ForecastAggregator.noonOrFirst(timeSeries, dayOffset,
                    channelTypeProvider.getParameterMetadata(parameter));
        };
    }
}
