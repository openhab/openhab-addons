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

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapLocationConfiguration;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
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
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractOpenWeatherMapHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractOpenWeatherMapHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractOpenWeatherMapHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_WEATHER_AND_FORECAST,
            THING_TYPE_AIR_POLLUTION, THING_TYPE_ONECALL_WEATHER_AND_FORECAST, THING_TYPE_ONECALL_HISTORY);

    private final TimeZoneProvider timeZoneProvider;

    // keeps track of the parsed location
    protected @Nullable PointType location;

    public AbstractOpenWeatherMapHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        OpenWeatherMapLocationConfiguration config = getConfigAs(OpenWeatherMapLocationConfiguration.class);

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

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID);
        } else {
            logger.debug("The OpenWeatherMap binding is a read-only binding and cannot handle command '{}'.", command);
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
     * Updates OpenWeatherMap data for this location.
     *
     * @param connection {@link OpenWeatherMapConnection} instance
     */
    public void updateData(OpenWeatherMapConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
        } catch (ConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getRawMessage());
        }
    }

    /**
     * Requests the data from OpenWeatherMap API.
     *
     * @param connection {@link OpenWeatherMapConnection} instance
     * @return true, if the request for the OpenWeatherMap data was successful
     * @throws CommunicationException if there is a problem retrieving the data
     * @throws ConfigurationException if there is a configuration error
     */
    protected abstract boolean requestData(OpenWeatherMapConnection connection)
            throws CommunicationException, ConfigurationException;

    /**
     * Updates all channels of this handler from the latest OpenWeatherMap data retrieved.
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
     * Updates the channel with the given UID from the latest OpenWeatherMap data retrieved.
     *
     * @param channelUID UID of the channel
     */
    protected abstract void updateChannel(ChannelUID channelUID);

    protected State getDateTimeTypeState(@Nullable Integer value) {
        return (value == null) ? UnDefType.UNDEF
                : new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(value.longValue()),
                        timeZoneProvider.getTimeZone()));
    }

    protected State getDecimalTypeState(@Nullable Double value) {
        return (value == null) ? UnDefType.UNDEF : new DecimalType(value);
    }

    protected State getPointTypeState(@Nullable Double latitude, @Nullable Double longitude) {
        return ((latitude == null) || (longitude == null)) ? UnDefType.UNDEF
                : new PointType(new DecimalType(latitude), new DecimalType(longitude));
    }

    protected State getRawTypeState(@Nullable RawType image) {
        return (image == null) ? UnDefType.UNDEF : image;
    }

    protected State getStringTypeState(@Nullable String value) {
        return (value == null) ? UnDefType.UNDEF : new StringType(value);
    }

    protected State getQuantityTypeState(@Nullable Number value, Unit<?> unit) {
        return (value == null) ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    protected List<Channel> createChannelsForGroup(String channelGroupId, ChannelGroupTypeUID channelGroupTypeUID) {
        logger.debug("Building channel group '{}' for thing '{}' and GroupType '{}'.", channelGroupId,
                getThing().getUID(), channelGroupTypeUID);
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
        logger.debug("Built channels: {}", channels);
        return channels;
    }

    protected List<Channel> removeChannelsOfGroup(String channelGroupId) {
        logger.debug("Removing channel group '{}' from thing '{}'.", channelGroupId, getThing().getUID());
        return getThing().getChannelsOfGroup(channelGroupId);
    }
}
