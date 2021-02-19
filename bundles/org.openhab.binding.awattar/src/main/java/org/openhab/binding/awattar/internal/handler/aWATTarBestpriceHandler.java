package org.openhab.binding.awattar.internal.handler;

import static org.openhab.binding.awattar.internal.aWATTarBindingConstants.*;
import static org.openhab.binding.awattar.internal.aWATTarUtil.*;
import static org.openhab.binding.awattar.internal.aWATTarUtil.getMillisToNextMinute;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.awattar.internal.*;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class aWATTarBestpriceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(aWATTarBestpriceHandler.class);

    private final int thingRefreshInterval = 60;
    private @Nullable ScheduledFuture<?> thingRefresher;
    private @Nullable aWATTarBestpriceConfiguration config = null;
    private final TimeZoneProvider timeZoneProvider;

    public aWATTarBestpriceHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.trace("Initializing aWATTar bestprice handler {}", this);
        config = getConfigAs(aWATTarBestpriceConfiguration.class);
        if (config == null) {
            logger.warn("Could not get Thing config");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        logger.trace("Got Config: {}", config.toString());

        boolean configValid = true;

        if (config.rangeStart < 0 || config.rangeStart > 23) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    MessageFormat.format("Invalid start value {}", config.rangeStart));
            configValid = false;
        }
        if (config.rangeDuration < 1 || config.rangeDuration > 24) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    MessageFormat.format("Invalid duration value {}", config.rangeDuration));
            configValid = false;
        }

        if (config.length < 1 || config.length >= config.rangeDuration) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, MessageFormat
                    .format("length {} needs to be > 0 and < {} (duration).", config.length, config.rangeDuration));
            configValid = false;
        }

        if (!configValid) {
            return;
        }

        synchronized (this) {
            if (thingRefresher == null || thingRefresher.isCancelled()) {
                logger.trace("Start Thing refresh job at interval {} seconds.", thingRefreshInterval);
                thingRefresher = scheduler.scheduleAtFixedRate(this::refreshChannels, getMillisToNextMinute(1),
                        thingRefreshInterval * 1000, TimeUnit.MILLISECONDS);
            }

        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        logger.trace("Diposing aWATTar price handler {}", this);
        if (thingRefresher != null) {
            thingRefresher.cancel(true);
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
        aWATTarBridgeHandler bridgeHandler = (aWATTarBridgeHandler) getBridge().getHandler();
        if (bridgeHandler.getPriceMap() == null) {
            logger.debug("No prices available, so can't refresh channel.");
            // no prices available, can't continue
            updateState(channelUID, state);
            return;
        }
        assert config != null;
        Timerange timerange = getRange(config.rangeStart, config.rangeDuration, bridgeHandler.getTimeZone());
        if (!(bridgeHandler.containsPriceFor(timerange.start) && bridgeHandler.containsPriceFor(timerange.end))) {
            updateState(channelUID, state);
            return;
        }

        aWATTarBestPriceResult result;
        if (config.consecutive) {
            ArrayList<aWATTarPrice> range = new ArrayList<aWATTarPrice>(config.rangeDuration);
            range.addAll(
                    getPriceRange(timerange, (o1, o2) -> Long.compare(o1.getStartTimestamp(), o2.getStartTimestamp())));
            aWATTarConsecutiveBestPriceResult res = new aWATTarConsecutiveBestPriceResult(
                    range.subList(0, config.length), bridgeHandler.getTimeZone());

            for (int i = 1; i <= range.size() - config.length; i++) {
                aWATTarConsecutiveBestPriceResult res2 = new aWATTarConsecutiveBestPriceResult(
                        range.subList(i, i + config.length), bridgeHandler.getTimeZone());
                if (res2.getPriceSum() < res.getPriceSum()) {
                    res = res2;
                }
            }
            result = res;
        } else {
            SortedSet<aWATTarPrice> range = getPriceRange(timerange,
                    (o1, o2) -> Double.compare(o1.getPrice(), o2.getPrice()));
            aWATTarNonConsecutiveBestPriceResult res = new aWATTarNonConsecutiveBestPriceResult(config.length,
                    bridgeHandler.getTimeZone());
            logger.trace("Bestprice Candidate range: {}", range);
            int ct = 0;
            for (aWATTarPrice price : range) {
                res.addMember(price);
                if (++ct >= config.length) {
                    break;
                }
            }
            result = res;
        }
        logger.trace("refreshChannel: result is: {}", result);
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
                    state = new StringType(getDuration(diff));
                }
                break;
            case CHANNEL_REMAINING:
                diff = result.getEnd() - Instant.now().toEpochMilli();
                if (result.isActive()) {
                    state = new StringType(getDuration(diff));
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
    public void handleCommand(@NotNull ChannelUID channelUID, @NotNull Command command) {
        logger.trace("Handling command {} for channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            refreshChannel(channelUID);
        } else {
            logger.debug("Binding {} only supports refresh command", BINDING_ID);
        }
    }

    private SortedSet<aWATTarPrice> getPriceRange(Timerange range, Comparator<aWATTarPrice> comparator) {

        aWATTarBridgeHandler bridgeHandler = (aWATTarBridgeHandler) getBridge().getHandler();
        TreeSet<aWATTarPrice> result = new TreeSet<>(comparator);
        result.addAll(bridgeHandler.getPriceMap().values().stream().filter(x -> x.isBetween(range.start, range.end))
                .collect(Collectors.toSet()));
        logger.trace("getPriceRange result: {}", result);
        return result;
    }

    private Timerange getRange(int start, int duration, ZoneId zoneId) {
        ZonedDateTime startCal = getCalendarForHour(start, zoneId);
        ZonedDateTime endCal = getCalendarForHour(start, zoneId).plusHours(duration);

        logger.trace("getRange: startCal: {}, endCal: {} ", startCal.toString(), endCal.toString());
        if (endCal.toInstant().toEpochMilli() < Instant.now().toEpochMilli()) {
            // span is in the past, add one day
            startCal = startCal.plusDays(1);
            endCal = endCal.plusDays(1);
        }
        logger.trace("getRange - 2: startCal: {}, endCal: {} ", startCal.toString(), endCal.toString());
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
