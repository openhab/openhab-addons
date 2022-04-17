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
package org.openhab.binding.awattar.internal.handler;

import static org.openhab.binding.awattar.internal.AwattarBindingConstants.BINDING_ID;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_ACTIVE;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_COUNTDOWN;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_END;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_HOURS;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_REMAINING;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_START;
import static org.openhab.binding.awattar.internal.AwattarUtil.getCalendarForHour;
import static org.openhab.binding.awattar.internal.AwattarUtil.getDateTimeType;
import static org.openhab.binding.awattar.internal.AwattarUtil.getDuration;
import static org.openhab.binding.awattar.internal.AwattarUtil.getMillisToNextMinute;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awattar.internal.AwattarBestPriceResult;
import org.openhab.binding.awattar.internal.AwattarBestpriceConfiguration;
import org.openhab.binding.awattar.internal.AwattarConsecutiveBestPriceResult;
import org.openhab.binding.awattar.internal.AwattarNonConsecutiveBestPriceResult;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
 * The {@link AwattarBestpriceHandler} is responsible for computing the best prices for a given configuration.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class AwattarBestpriceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AwattarBestpriceHandler.class);

    private final int thingRefreshInterval = 60;
    @Nullable
    private ScheduledFuture<?> thingRefresher;

    private final TimeZoneProvider timeZoneProvider;

    public AwattarBestpriceHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.trace("Initializing aWATTar bestprice handler {}", this);
        AwattarBestpriceConfiguration config = getConfigAs(AwattarBestpriceConfiguration.class);
        logger.trace("Got Config: {}", config.toString());

        boolean configValid = true;

        if (config.rangeStart < 0 || config.rangeStart > 23) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.start.value");
            configValid = false;
        }
        if (config.rangeDuration < 1 || config.rangeDuration > 24) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.duration.value");
            configValid = false;
        }

        if (config.length < 1 || config.length >= config.rangeDuration) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.length.value");
            configValid = false;
        }

        if (!configValid) {
            return;
        }

        synchronized (this) {
            ScheduledFuture<?> localRefresher = thingRefresher;
            if (localRefresher == null || localRefresher.isCancelled()) {
                logger.trace("Start Thing refresh job at interval {} seconds.", thingRefreshInterval);
                /*
                 * The scheduler is required to run exactly at minute borders, hence we can't use scheduleWithFixedDelay
                 * here
                 */
                thingRefresher = scheduler.scheduleAtFixedRate(this::refreshChannels,
                        getMillisToNextMinute(1, timeZoneProvider), thingRefreshInterval * 1000, TimeUnit.MILLISECONDS);
            }

        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        logger.trace("Diposing aWATTar price handler {}", this);
        ScheduledFuture<?> localRefresher = thingRefresher;
        if (localRefresher != null) {
            localRefresher.cancel(true);
            thingRefresher = null;
        }
    }

    public void refreshChannels() {
        logger.trace("Refreshing channels for {}", getThing().getUID());
        updateStatus(ThingStatus.ONLINE);
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && isLinked(channelUID)) {
                logger.trace("Refreshing linked channel {}", channelUID);
                refreshChannel(channel.getUID());
            }
        }
    }

    public void refreshChannel(ChannelUID channelUID) {
        logger.trace("refreshing channel {}", channelUID);
        State state = UnDefType.UNDEF;
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.error("No Bridge available. This should not happen!!");
            updateState(channelUID, state);
            return;
        }
        AwattarBridgeHandler bridgeHandler = (AwattarBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null || bridgeHandler.getPriceMap() == null) {
            logger.debug("No prices available, so can't refresh channel.");
            // no prices available, can't continue
            updateState(channelUID, state);
            return;
        }
        AwattarBestpriceConfiguration config = getConfigAs(AwattarBestpriceConfiguration.class);
        Timerange timerange = getRange(config.rangeStart, config.rangeDuration, bridgeHandler.getTimeZone());
        if (!(bridgeHandler.containsPriceFor(timerange.start) && bridgeHandler.containsPriceFor(timerange.end))) {
            updateState(channelUID, state);
            return;
        }

        AwattarBestPriceResult result;
        if (config.consecutive) {
            ArrayList<AwattarPrice> range = new ArrayList<AwattarPrice>(config.rangeDuration);
            range.addAll(getPriceRange(bridgeHandler, timerange,
                    (o1, o2) -> Long.compare(o1.getStartTimestamp(), o2.getStartTimestamp())));
            AwattarConsecutiveBestPriceResult res = new AwattarConsecutiveBestPriceResult(
                    range.subList(0, config.length), bridgeHandler.getTimeZone());

            for (int i = 1; i <= range.size() - config.length; i++) {
                AwattarConsecutiveBestPriceResult res2 = new AwattarConsecutiveBestPriceResult(
                        range.subList(i, i + config.length), bridgeHandler.getTimeZone());
                if (res2.getPriceSum() < res.getPriceSum()) {
                    res = res2;
                }
            }
            result = res;
        } else {
            List<AwattarPrice> range = getPriceRange(bridgeHandler, timerange,
                    (o1, o2) -> Double.compare(o1.getPrice(), o2.getPrice()));
            AwattarNonConsecutiveBestPriceResult res = new AwattarNonConsecutiveBestPriceResult(config.length,
                    bridgeHandler.getTimeZone());
            logger.trace("Bestprice Candidate range: {}", range);
            int ct = 0;
            for (AwattarPrice price : range) {
                res.addMember(price);
                if (++ct >= config.length) {
                    break;
                }
            }
            result = res;
        }
        logger.trace("refreshChannel {}: result is: {}", channelUID, result);
        String channelId = channelUID.getIdWithoutGroup();
        long diff;
        switch (channelId) {
            case CHANNEL_ACTIVE:
                state = OnOffType.from(result.isActive());
                break;
            case CHANNEL_START:
                state = getDateTimeType(result.getStart(), timeZoneProvider);
                break;
            case CHANNEL_END:
                state = getDateTimeType(result.getEnd(), timeZoneProvider);
                break;
            case CHANNEL_COUNTDOWN:
                diff = result.getStart() - Instant.now().toEpochMilli();
                if (diff >= 0) {
                    state = getDuration(diff);
                }
                break;
            case CHANNEL_REMAINING:
                diff = result.getEnd() - Instant.now().toEpochMilli();
                if (result.isActive()) {
                    state = getDuration(diff);
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
        logger.trace("Handling command {} for channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            refreshChannel(channelUID);
        } else {
            logger.debug("Binding {} only supports refresh command", BINDING_ID);
        }
    }

    private List<AwattarPrice> getPriceRange(AwattarBridgeHandler bridgeHandler, Timerange range,
            Comparator<AwattarPrice> comparator) {
        ArrayList<AwattarPrice> result = new ArrayList<>();
        SortedMap<Long, AwattarPrice> priceMap = bridgeHandler.getPriceMap();
        if (priceMap == null) {
            logger.error("No prices available, returning empty result");
            return result;
        }
        result.addAll(priceMap.values().stream().filter(x -> x.isBetween(range.start, range.end))
                .collect(Collectors.toSet()));
        result.sort(comparator);
        logger.trace("getPriceRange result: {}", result);
        return result;
    }

    private Timerange getRange(int start, int duration, ZoneId zoneId) {

        ZonedDateTime startCal = getCalendarForHour(start, zoneId);
        ZonedDateTime endCal = startCal.plusHours(duration);
        logger.trace("getRange(1): startCal: {}, endCal: {} ", startCal.toString(), endCal.toString());
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        if (now.getHour() < start) {
            // we are before the range, so we might be still within the last range
            startCal = startCal.minusDays(1);
            endCal = endCal.minusDays(1);
        }
        logger.trace("getRange(2): startCal: {}, endCal: {} ", startCal.toString(), endCal.toString());
        if (endCal.toInstant().toEpochMilli() < Instant.now().toEpochMilli()) {
            // span is in the past, add one day
            startCal = startCal.plusDays(1);
            endCal = endCal.plusDays(1);
        }
        logger.trace("getRange(3): startCal: {}, endCal: {} ", startCal.toString(), endCal.toString());
        return new Timerange(startCal.toInstant().toEpochMilli(), endCal.toInstant().toEpochMilli());
    }

    private class Timerange {
        long start;
        long end;

        Timerange(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
