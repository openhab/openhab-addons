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
package org.openhab.binding.enturno.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.enturno.internal.connection.EnturCommunicationException;
import org.openhab.binding.enturno.internal.connection.EnturConfigurationException;
import org.openhab.binding.enturno.internal.connection.EnturNoConnection;
import org.openhab.binding.enturno.internal.model.simplified.DisplayData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link EnturNoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michal Kloc - Initial contribution
 */
@NonNullByDefault
public class EnturNoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EnturNoHandler.class);

    private final HttpClient httpClient;

    private @NonNullByDefault({}) EnturNoConfiguration config;

    private @NonNullByDefault({}) EnturNoConnection connection;

    private static final long INITIAL_DELAY_IN_SECONDS = 15;

    private static final long REFRESH_INTERVAL_IN_SECONDS = 30;

    private @Nullable ScheduledFuture<?> refreshJob;

    private @Nullable String stopId;

    private List<DisplayData> processedData = new ArrayList<>();

    public EnturNoHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID);
        } else {
            logger.debug("Entur binding is a read-only binding and cannot handle command '{}'.", command);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Entur EnturTimeTable API handler '{}'.", getThing().getUID());
        config = getConfigAs(EnturNoConfiguration.class);
        stopId = config.getStopPlaceId();

        logger.debug("Stop place id: {}", stopId);
        boolean configValid = true;
        if (stopId == null || stopId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-stopId");
            configValid = false;
        }

        String lineCode = config.getLineCode();
        logger.debug("Line code: {}", lineCode);
        if (lineCode == null || lineCode.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-lineCode");
            configValid = false;
        }

        if (configValid) {
            connection = new EnturNoConnection(this, httpClient);

            updateStatus(ThingStatus.UNKNOWN);

            if (refreshJob == null || refreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} sec.", REFRESH_INTERVAL_IN_SECONDS);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateThing, INITIAL_DELAY_IN_SECONDS,
                        REFRESH_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Entur real-time timetable API handler '{}'.", getThing().getUID());
        if (refreshJob != null && !refreshJob.isCancelled()) {
            logger.debug("Stop refresh job.");
            if (refreshJob.cancel(true)) {
                refreshJob = null;
            }
        }
    }

    public EnturNoConfiguration getEnturNoConfiguration() {
        return config;
    }

    private void updateThing() {
        ThingStatus status = ThingStatus.OFFLINE;
        if (connection != null) {
            logger.trace("Updating data");
            updateData(connection);
            status = thing.getStatus();
        } else {
            logger.debug("Cannot update real-time data of thing '{}' as connection is null.", thing.getUID());
            status = ThingStatus.OFFLINE;
        }

        updateStatus(status);
    }

    public void updateData(EnturNoConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (EnturCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } catch (EnturConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
        }
    }

    private boolean requestData(EnturNoConnection connection)
            throws EnturConfigurationException, EnturCommunicationException {
        logger.debug("Update real-time data of thing '{}'.", getThing().getUID());
        try {
            processedData = connection.getEnturTimeTable(stopId, config.getLineCode());

            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getLocalizedMessage(), e);
            return false;
        }
    }

    private void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && channelUID.isInGroup() && channelUID.getGroupId() != null
                    && isLinked(channelUID)) {
                updateChannel(channelUID);
            }
        }
    }

    private void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
        logger.trace("Channel group id: {}", channelGroupId);
        if (channelGroupId != null) {
            switch (channelGroupId) {
                case EnturNoBindingConstants.CHANNEL_GROUP_STOP_PLACE:
                    updateStopPlaceChannel(channelUID);
                    break;
                case EnturNoBindingConstants.CHANNEL_GROUP_DIRECTION_1:
                    updateDirectionChannel(channelUID, 0);
                    break;
                case EnturNoBindingConstants.CHANNEL_GROUP_DIRECTION_2:
                    updateDirectionChannel(channelUID, 1);
                    break;
                default:
                    break;
            }
        }
    }

    private void updateDirectionChannel(ChannelUID channelUID, int i) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        logger.trace("Channel id: {}, Channel group id: {}", channelId, channelGroupId);
        if (processedData.size() > i) {
            State state = UnDefType.UNDEF;
            List<String> departures = processedData.get(i).departures;
            int departuresCount = departures.size();
            List<String> estimatedFlags = processedData.get(i).estimatedFlags;
            int esitmatedFlagsCount = estimatedFlags.size();
            switch (channelId) {
                case EnturNoBindingConstants.CHANNEL_DEPARTURE_01:
                    state = departuresCount > 0 ? getDateTimeTypeState(departures.get(0)) : state;
                    break;
                case EnturNoBindingConstants.CHANNEL_DEPARTURE_02:
                    state = departuresCount > 1 ? getDateTimeTypeState(departures.get(1)) : state;
                    break;
                case EnturNoBindingConstants.CHANNEL_DEPARTURE_03:
                    state = departuresCount > 2 ? getDateTimeTypeState(departures.get(2)) : state;
                    break;
                case EnturNoBindingConstants.CHANNEL_DEPARTURE_04:
                    state = departuresCount > 3 ? getDateTimeTypeState(departures.get(3)) : state;
                    break;
                case EnturNoBindingConstants.CHANNEL_DEPARTURE_05:
                    state = departuresCount > 4 ? getDateTimeTypeState(departures.get(4)) : state;
                    break;
                case EnturNoBindingConstants.ESTIMATED_FLAG_01:
                    state = esitmatedFlagsCount > 0 ? getStringTypeState(estimatedFlags.get(0)) : state;
                    break;
                case EnturNoBindingConstants.ESTIMATED_FLAG_02:
                    state = esitmatedFlagsCount > 1 ? getStringTypeState(estimatedFlags.get(1)) : state;
                    break;
                case EnturNoBindingConstants.ESTIMATED_FLAG_03:
                    state = esitmatedFlagsCount > 2 ? getStringTypeState(estimatedFlags.get(2)) : state;
                    break;
                case EnturNoBindingConstants.ESTIMATED_FLAG_04:
                    state = esitmatedFlagsCount > 3 ? getStringTypeState(estimatedFlags.get(3)) : state;
                    break;
                case EnturNoBindingConstants.ESTIMATED_FLAG_05:
                    state = esitmatedFlagsCount > 4 ? getStringTypeState(estimatedFlags.get(4)) : state;
                    break;
                case EnturNoBindingConstants.CHANNEL_LINE_CODE:
                    state = getStringTypeState(processedData.get(i).lineCode);
                    break;
                case EnturNoBindingConstants.CHANNEL_FRONT_DISPLAY:
                    state = getStringTypeState(processedData.get(i).frontText);
                    break;
                default:
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No real-time data available to update channel '{}' of group '{}'.", channelId,
                    channelGroupId);
        }
    }

    /**
     * Update the channel from the last Entur data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateStopPlaceChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (!processedData.isEmpty()) {
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case EnturNoBindingConstants.CHANNEL_STOP_ID:
                    state = getStringTypeState(processedData.get(0).stopPlaceId);
                    break;
                case EnturNoBindingConstants.CHANNEL_STOP_NAME:
                    state = getStringTypeState(processedData.get(0).stopName);
                    break;
                case EnturNoBindingConstants.CHANNEL_STOP_TRANSPORT_MODE:
                    state = getStringTypeState(processedData.get(0).transportMode);
                    break;
                default:
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No real-time data available to update channel '{}' of group '{}'.", channelId,
                    channelGroupId);
        }
    }

    private State getDateTimeTypeState(@Nullable String value) {
        return (value == null) ? UnDefType.UNDEF
                : new DateTimeType(ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                        .withZoneSameInstant(ZoneId.of(EnturNoBindingConstants.TIME_ZONE)));
    }

    private State getStringTypeState(@Nullable String value) {
        return (value == null) ? UnDefType.UNDEF : new StringType(value);
    }
}
