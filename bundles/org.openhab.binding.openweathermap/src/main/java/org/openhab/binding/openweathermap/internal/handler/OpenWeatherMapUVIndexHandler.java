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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapUVIndexConfiguration;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonUVIndexData;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link OpenWeatherMapUVIndexHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapUVIndexHandler extends AbstractOpenWeatherMapHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapUVIndexHandler.class);

    private static final String CHANNEL_GROUP_FORECAST_PREFIX = "forecastDay";
    private static final Pattern CHANNEL_GROUP_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_FORECAST_PREFIX + "([0-9]*)");

    // keeps track of the parsed count
    private int forecastDays = 6;

    private @Nullable OpenWeatherMapJsonUVIndexData uvindexData;
    private @Nullable List<OpenWeatherMapJsonUVIndexData> uvindexForecastData;

    public OpenWeatherMapUVIndexHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Initialize OpenWeatherMapUVIndexHandler handler '{}'.", getThing().getUID());
        OpenWeatherMapUVIndexConfiguration config = getConfigAs(OpenWeatherMapUVIndexConfiguration.class);

        boolean configValid = true;
        int newForecastDays = config.forecastDays;
        if (newForecastDays < 1 || newForecastDays > 8) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-uvindex-number-of-days");
            configValid = false;
        }

        if (configValid) {
            logger.debug("Rebuilding thing '{}'.", getThing().getUID());
            List<Channel> toBeAddedChannels = new ArrayList<>();
            List<Channel> toBeRemovedChannels = new ArrayList<>();
            if (forecastDays != newForecastDays) {
                logger.debug("Rebuilding UV index channel groups.");
                if (forecastDays > newForecastDays) {
                    if (newForecastDays < 2) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(CHANNEL_GROUP_FORECAST_TOMORROW));
                    }
                    for (int i = newForecastDays; i < forecastDays; ++i) {
                        toBeRemovedChannels
                                .addAll(removeChannelsOfGroup(CHANNEL_GROUP_FORECAST_PREFIX + Integer.toString(i)));
                    }
                } else {
                    if (forecastDays <= 1 && newForecastDays > 1) {
                        toBeAddedChannels.addAll(createChannelsForGroup(CHANNEL_GROUP_FORECAST_TOMORROW,
                                CHANNEL_GROUP_TYPE_UVINDEX_FORECAST));
                    }
                    for (int i = (forecastDays < 2) ? 2 : forecastDays; i < newForecastDays; ++i) {
                        toBeAddedChannels
                                .addAll(createChannelsForGroup(CHANNEL_GROUP_FORECAST_PREFIX + Integer.toString(i),
                                        CHANNEL_GROUP_TYPE_UVINDEX_FORECAST));
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
        logger.debug("Update UV Index data of thing '{}'.", getThing().getUID());
        try {
            uvindexData = connection.getUVIndexData(location);
            if (forecastDays > 0) {
                uvindexForecastData = connection.getUVIndexForecastData(location, forecastDays);
            }
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void updateChannel(ChannelUID channelUID) {
        switch (channelUID.getGroupId()) {
            case CHANNEL_GROUP_CURRENT_UVINDEX:
                updateUVIndexChannel(channelUID);
                break;
            case CHANNEL_GROUP_FORECAST_TOMORROW:
                updateUVIndexForecastChannel(channelUID, 1);
                break;
            default:
                Matcher m = CHANNEL_GROUP_FORECAST_PREFIX_PATTERN.matcher(channelUID.getGroupId());
                int i;
                if (m.find() && (i = Integer.parseInt(m.group(1))) > 1 && i <= 8) {
                    updateUVIndexForecastChannel(channelUID, i);
                }
                break;
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateUVIndexChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        OpenWeatherMapJsonUVIndexData localUVIndexData = uvindexData;
        if (localUVIndexData != null) {
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(localUVIndexData.getDate());
                    break;
                case CHANNEL_UVINDEX:
                    state = getDecimalTypeState(localUVIndexData.getValue());
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No UV Index data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count
     */
    private void updateUVIndexForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        List<OpenWeatherMapJsonUVIndexData> localUVIndexForecastData = uvindexForecastData;
        if (localUVIndexForecastData != null && localUVIndexForecastData.size() >= count) {
            OpenWeatherMapJsonUVIndexData forecastData = localUVIndexForecastData.get(count - 1);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getDate());
                    break;
                case CHANNEL_UVINDEX:
                    state = getDecimalTypeState(forecastData.getValue());
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No UV Index data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }
}
