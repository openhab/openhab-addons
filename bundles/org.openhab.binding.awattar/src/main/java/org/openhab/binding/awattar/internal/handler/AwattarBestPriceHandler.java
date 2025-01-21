/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal.handler;

import static org.openhab.binding.awattar.internal.AwattarBindingConstants.BINDING_ID;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_ACTIVE;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_COUNTDOWN;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_END;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_HOURS;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_REMAINING;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_START;
import static org.openhab.binding.awattar.internal.AwattarUtil.getCalendarForHour;
import static org.openhab.binding.awattar.internal.AwattarUtil.getDuration;
import static org.openhab.binding.awattar.internal.AwattarUtil.getMillisToNextMinute;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awattar.internal.AwattarBestPriceConfiguration;
import org.openhab.binding.awattar.internal.AwattarBestPriceResult;
import org.openhab.binding.awattar.internal.AwattarConsecutiveBestPriceResult;
import org.openhab.binding.awattar.internal.AwattarNonConsecutiveBestPriceResult;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.dto.AwattarTimeProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
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

/**
 * The {@link AwattarBestPriceHandler} is responsible for computing the best prices for a given configuration.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class AwattarBestPriceHandler extends BaseThingHandler {
    private static final int THING_REFRESH_INTERVAL = 60;

    private final Logger logger = LoggerFactory.getLogger(AwattarBestPriceHandler.class);
    private final AwattarTimeProvider timeProvider;

    private @Nullable ScheduledFuture<?> thingRefresher;

    public AwattarBestPriceHandler(Thing thing, AwattarTimeProvider timeProvider) {
        super(thing);
        this.timeProvider = timeProvider;
    }

    @Override
    public void initialize() {
        AwattarBestPriceConfiguration config = getConfigAs(AwattarBestPriceConfiguration.class);

        if (config.length >= config.rangeDuration) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.length.value");
            return;
        }

        synchronized (this) {
            ScheduledFuture<?> localRefresher = thingRefresher;
            if (localRefresher == null || localRefresher.isCancelled()) {
                /*
                 * The scheduler is required to run exactly at minute borders, hence we can't use scheduleWithFixedDelay
                 * here
                 */
                thingRefresher = scheduler.scheduleAtFixedRate(this::refreshChannels,
                        getMillisToNextMinute(1, timeProvider.getZoneId()), THING_REFRESH_INTERVAL * 1000L,
                        TimeUnit.MILLISECONDS);
            }
        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefresher = thingRefresher;
        if (localRefresher != null) {
            localRefresher.cancel(true);
            thingRefresher = null;
        }
    }

    public void refreshChannels() {
        updateStatus(ThingStatus.ONLINE);
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && isLinked(channelUID)) {
                refreshChannel(channel.getUID());
            }
        }
    }

    public void refreshChannel(ChannelUID channelUID) {
        State state = UnDefType.UNDEF;
        Bridge bridge = getBridge();

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.bridge.missing");
            updateState(channelUID, state);
            return;
        }

        AwattarBridgeHandler bridgeHandler = (AwattarBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null || bridgeHandler.getPrices() == null) {
            logger.debug("No prices available, so can't refresh channel.");
            // no prices available, can't continue
            updateState(channelUID, state);
            return;
        }

        ZoneId zoneId = timeProvider.getZoneId();

        AwattarBestPriceConfiguration config = getConfigAs(AwattarBestPriceConfiguration.class);
        TimeRange timerange = getRange(config.rangeStart, config.rangeDuration, zoneId);
        if (!(bridgeHandler.containsPriceFor(timerange))) {
            updateState(channelUID, state);
            return;
        }

        AwattarBestPriceResult result;
        List<AwattarPrice> range = getPriceRange(bridgeHandler, timerange);

        if (config.consecutive) {
            result = new AwattarConsecutiveBestPriceResult(range, config.length, zoneId);
        } else {
            result = new AwattarNonConsecutiveBestPriceResult(range, config.length, config.inverted, zoneId);
        }

        String channelId = channelUID.getIdWithoutGroup();
        long diff;
        switch (channelId) {
            case CHANNEL_ACTIVE:
                state = OnOffType.from(result.isActive(timeProvider.getInstantNow()));
                break;
            case CHANNEL_START:
                state = new DateTimeType(Instant.ofEpochMilli(result.getStart()));
                break;
            case CHANNEL_END:
                state = new DateTimeType(Instant.ofEpochMilli(result.getEnd()));
                break;
            case CHANNEL_COUNTDOWN:
                diff = result.getStart() - timeProvider.getInstantNow().toEpochMilli();
                if (diff >= 0) {
                    state = getDuration(diff);
                } else {
                    state = QuantityType.valueOf(0, Units.MINUTE);
                }
                break;
            case CHANNEL_REMAINING:
                if (result.isActive(timeProvider.getInstantNow())) {
                    diff = result.getEnd() - timeProvider.getInstantNow().toEpochMilli();
                    state = getDuration(diff);
                } else {
                    state = QuantityType.valueOf(0, Units.MINUTE);
                }
                break;
            case CHANNEL_HOURS:
                state = new StringType(result.getHours());
                break;
            default:
                logger.warn("Unknown channel id {} for Thing type {}", channelUID, getThing().getThingTypeUID());
        }
        updateState(channelUID, state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshChannel(channelUID);
        } else {
            logger.debug("Binding {} only supports refresh command", BINDING_ID);
        }
    }

    private List<AwattarPrice> getPriceRange(AwattarBridgeHandler bridgeHandler, TimeRange range) {
        List<AwattarPrice> result = new ArrayList<>();
        SortedSet<AwattarPrice> prices = bridgeHandler.getPrices();
        if (prices == null) {
            logger.debug("No prices available, can't compute ranges");
            return result;
        }
        result.addAll(prices.stream().filter(x -> range.contains(x.timerange())).toList());
        return result;
    }

    /**
     * Returns the time range for the given start hour and duration.
     *
     * @param start the start hour (0-23)
     * @param duration the duration in hours
     * @param zoneId the time zone to use
     * @return the range
     */
    protected TimeRange getRange(int start, int duration, ZoneId zoneId) {
        ZonedDateTime startTime = getStartTime(start, zoneId);
        ZonedDateTime endTime = startTime.plusHours(duration);
        ZonedDateTime now = timeProvider.getZonedDateTimeNow();
        if (now.getHour() < start) {
            // we are before the range, so we might be still within the last range
            startTime = startTime.minusDays(1);
            endTime = endTime.minusDays(1);
        }
        if (endTime.isBefore(now)) {
            // span is in the past, add one day
            startTime = startTime.plusDays(1);
            endTime = endTime.plusDays(1);
        }
        return new TimeRange(startTime.toInstant().toEpochMilli(), endTime.toInstant().toEpochMilli());
    }

    /**
     * Returns the start time for the given hour.
     *
     * @param start the hour. Must be between 0 and 23.
     * @param zoneId the time zone
     * @return the start time
     */
    protected ZonedDateTime getStartTime(int start, ZoneId zoneId) {
        return getCalendarForHour(start, zoneId);
    }
}
