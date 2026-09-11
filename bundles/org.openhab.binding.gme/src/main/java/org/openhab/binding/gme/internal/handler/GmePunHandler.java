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
package org.openhab.binding.gme.internal.handler;

import static org.openhab.binding.gme.internal.GmeBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gme.internal.api.GmeApiException;
import org.openhab.binding.gme.internal.api.GmeAuthManager;
import org.openhab.binding.gme.internal.model.GmePriceCache;
import org.openhab.binding.gme.internal.model.GmePriceEntry;
import org.openhab.binding.gme.internal.model.GmePriceTimeSeries;
import org.openhab.binding.gme.internal.model.GmePriceTimeline;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for PUN electricity market prices.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmePunHandler extends BaseThingHandler {

    private static final ZoneId GME_ZONE = ZoneId.of("Europe/Rome");

    private final Logger logger = LoggerFactory.getLogger(GmePunHandler.class);
    private final AtomicBoolean refreshInProgress = new AtomicBoolean();

    private @Nullable GmeApiBridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> channelRefreshJob;
    private @Nullable ScheduledFuture<?> apiRetryJob;

    private final GmePriceCache priceCache = new GmePriceCache();

    public GmePunHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        if (!linkBridge()) {
            return;
        }

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            scheduleChannelRefresh();
            refreshPrices();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "GME API bridge is not online.");
        }
    }

    private boolean linkBridge() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "GME API bridge is missing.");
            return false;
        }

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof GmeApiBridgeHandler gmeBridgeHandler) {
            bridgeHandler = gmeBridgeHandler;
            return true;
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Configured bridge is not a GME API bridge.");
        return false;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (bridgeHandler != null || linkBridge()) {
                scheduleChannelRefresh();
                refreshPrices();
            }
        } else {
            cancelJobs();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "GME API bridge is not online.");
        }
    }

    private synchronized void scheduleChannelRefresh() {
        ScheduledFuture<?> currentJob = channelRefreshJob;
        if (currentJob != null && !currentJob.isCancelled()) {
            return;
        }

        channelRefreshJob = scheduler.scheduleWithFixedDelay(this::refreshLocalState, 1, 1, TimeUnit.MINUTES);
        logger.debug("Scheduled local GME PUN channel refresh every minute");
    }

    private synchronized void scheduleApiRetry() {
        ScheduledFuture<?> currentJob = apiRetryJob;
        if (currentJob != null && !currentJob.isCancelled()) {
            return;
        }

        GmeApiBridgeHandler currentBridgeHandler = bridgeHandler;
        if (currentBridgeHandler == null) {
            return;
        }

        int refreshInterval = currentBridgeHandler.getRefreshInterval();
        apiRetryJob = scheduler.scheduleWithFixedDelay(this::refreshPrices, refreshInterval, refreshInterval,
                TimeUnit.MINUTES);

        logger.debug("Scheduled GME PUN API retry every {} minutes", refreshInterval);
    }

    private synchronized void cancelApiRetry() {
        ScheduledFuture<?> currentJob = apiRetryJob;
        if (currentJob != null) {
            currentJob.cancel(false);
            apiRetryJob = null;
        }
    }

    private synchronized void cancelJobs() {
        ScheduledFuture<?> currentChannelJob = channelRefreshJob;
        if (currentChannelJob != null) {
            currentChannelJob.cancel(false);
            channelRefreshJob = null;
        }

        cancelApiRetry();
    }

    private void refreshLocalState() {
        LocalDate currentDate = LocalDate.now(GME_ZONE);
        LocalDate loadedTodayDate = priceCache.getTodayDate();

        if (!currentDate.equals(loadedTodayDate)) {
            if (priceCache.promoteTomorrowToToday(currentDate, GME_ZONE)) {
                logger.debug("Promoted cached PUN prices for {} to today", currentDate);

                updateChannels();
                refreshPrices();
            } else {
                refreshPrices();
            }
        } else {
            updateChannels();
        }
    }

    private void refreshPrices() {
        if (!refreshInProgress.compareAndSet(false, true)) {
            return;
        }

        scheduler.execute(() -> {
            try {
                GmeApiBridgeHandler currentBridgeHandler = bridgeHandler;
                if (currentBridgeHandler == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                            "GME API bridge handler is unavailable.");
                    return;
                }

                GmeAuthManager authManager = currentBridgeHandler.getAuthManager();
                if (authManager == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                            "GME authentication manager is unavailable.");
                    return;
                }

                LocalDate today = LocalDate.now(GME_ZONE);
                LocalDate tomorrow = today.plusDays(1);

                if (!today.equals(priceCache.getTodayDate()) || priceCache.getTodayPrices().isEmpty()) {
                    if (priceCache.promoteTomorrowToToday(today, GME_ZONE)) {
                        logger.debug("Promoted cached PUN prices for {} to today", today);
                    } else {
                        List<GmePriceEntry> newTodayPrices = authManager.requestPun(today);
                        if (!GmePriceTimeline.isCompleteDailySet(newTodayPrices, today, GME_ZONE)) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "GME returned an incomplete PUN price dataset for today.");
                            return;
                        }

                        priceCache.setToday(today, newTodayPrices);
                    }
                }

                if (!tomorrow.equals(priceCache.getTomorrowDate()) || priceCache.getTomorrowPrices().isEmpty()) {
                    try {
                        List<GmePriceEntry> newTomorrowPrices = authManager.requestPun(tomorrow);

                        if (!GmePriceTimeline.isCompleteDailySet(newTomorrowPrices, tomorrow, GME_ZONE)) {
                            priceCache.clearTomorrow();
                            scheduleApiRetry();
                            logger.debug(
                                    "PUN price dataset for tomorrow is incomplete: received {} entries, expected {}",
                                    newTomorrowPrices.size(), GmePriceTimeline.getExpectedHours(tomorrow, GME_ZONE));
                        } else {
                            priceCache.setTomorrow(tomorrow, newTomorrowPrices);
                            cancelApiRetry();
                        }
                    } catch (GmeApiException e) {
                        if (e.isAuthenticationError()) {
                            throw e;
                        }
                        priceCache.clearTomorrow();
                        scheduleApiRetry();
                        logger.debug("Unable to retrieve PUN prices for tomorrow: {}", e.getMessage());
                    } catch (IOException | TimeoutException | ExecutionException | IllegalStateException e) {
                        priceCache.clearTomorrow();
                        scheduleApiRetry();
                        logger.debug("PUN prices for tomorrow are not available yet: {}", e.getMessage());
                    }
                } else {
                    cancelApiRetry();
                }

                updateChannels();

                logger.debug("Loaded {} PUN prices for today and {} for tomorrow", priceCache.getTodayPrices().size(),
                        priceCache.getTomorrowPrices().size());

                updateStatus(ThingStatus.ONLINE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "GME price refresh was interrupted.");
            } catch (IOException | TimeoutException | ExecutionException | IllegalStateException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to retrieve GME PUN prices: " + e.getMessage());
            } finally {
                refreshInProgress.set(false);
            }
        });
    }

    private void updateChannels() {
        Instant now = Instant.now();

        List<GmePriceEntry> todayPrices = priceCache.getTodayPrices();
        List<GmePriceEntry> tomorrowPrices = priceCache.getTomorrowPrices();

        List<GmePriceEntry> timeline = new ArrayList<>(todayPrices.size() + tomorrowPrices.size());
        timeline.addAll(todayPrices);
        timeline.addAll(tomorrowPrices);

        var current = GmePriceTimeline.findCurrentPrice(timeline, now, GME_ZONE);
        var next = GmePriceTimeline.findNextPrice(timeline, now, GME_ZONE);

        if (current.isPresent()) {
            updateState(CHANNEL_CURRENT_PRICE, toPriceState(current.get().priceKWh()));
        } else {
            updateState(CHANNEL_CURRENT_PRICE, UnDefType.UNDEF);
        }

        if (next.isPresent()) {
            updateState(CHANNEL_NEXT_PRICE, toPriceState(next.get().priceKWh()));
        } else {
            updateState(CHANNEL_NEXT_PRICE, UnDefType.UNDEF);
        }

        updateDayStatistics(todayPrices, CHANNEL_TODAY_AVERAGE, CHANNEL_TODAY_MIN, CHANNEL_TODAY_MAX,
                CHANNEL_TODAY_MIN_TIME, CHANNEL_TODAY_MAX_TIME);

        sendPriceTimeSeries(CHANNEL_TODAY_PRICES, todayPrices);
        sendPriceTimeSeries(CHANNEL_TOMORROW_PRICES, tomorrowPrices);

        if (tomorrowPrices.isEmpty()) {
            updateState(CHANNEL_TOMORROW_AVAILABLE, OnOffType.OFF);
            clearTomorrowChannels();
        } else {
            updateState(CHANNEL_TOMORROW_AVAILABLE, OnOffType.ON);

            updateDayStatistics(tomorrowPrices, CHANNEL_TOMORROW_AVERAGE, CHANNEL_TOMORROW_MIN, CHANNEL_TOMORROW_MAX,
                    CHANNEL_TOMORROW_MIN_TIME, CHANNEL_TOMORROW_MAX_TIME);
        }

        updateState(CHANNEL_LAST_UPDATE, new DateTimeType(ZonedDateTime.now(GME_ZONE)));
    }

    private void clearTomorrowChannels() {
        updateState(CHANNEL_TOMORROW_AVERAGE, UnDefType.UNDEF);
        updateState(CHANNEL_TOMORROW_MIN, UnDefType.UNDEF);
        updateState(CHANNEL_TOMORROW_MAX, UnDefType.UNDEF);
        updateState(CHANNEL_TOMORROW_MIN_TIME, UnDefType.UNDEF);
        updateState(CHANNEL_TOMORROW_MAX_TIME, UnDefType.UNDEF);
    }

    private void updateDayStatistics(List<GmePriceEntry> prices, String averageChannel, String minChannel,
            String maxChannel, String minTimeChannel, String maxTimeChannel) {
        if (prices.isEmpty()) {
            return;
        }

        BigDecimal total = prices.stream().map(GmePriceEntry::priceKWh).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = total.divide(BigDecimal.valueOf(prices.size()), MathContext.DECIMAL64);

        GmePriceEntry minimum = prices.stream().min(Comparator.comparing(GmePriceEntry::priceKWh)).orElseThrow();
        GmePriceEntry maximum = prices.stream().max(Comparator.comparing(GmePriceEntry::priceKWh)).orElseThrow();

        updateState(averageChannel, toPriceState(average));
        updateState(minChannel, toPriceState(minimum.priceKWh()));
        updateState(maxChannel, toPriceState(maximum.priceKWh()));

        updateState(minTimeChannel, new DateTimeType(GmePriceTimeline.getStartTime(minimum, GME_ZONE)));
        updateState(maxTimeChannel, new DateTimeType(GmePriceTimeline.getStartTime(maximum, GME_ZONE)));
    }

    private void sendPriceTimeSeries(String channelId, List<GmePriceEntry> prices) {
        sendTimeSeries(channelId, GmePriceTimeSeries.build(prices, GME_ZONE, price -> toPriceState(price.priceKWh())));
    }

    private QuantityType<?> toPriceState(BigDecimal priceKWh) {
        Unit<?> priceUnit = UnitUtils.parseUnit("EUR/kWh");
        if (priceUnit == null) {
            priceUnit = CurrencyUnits.BASE_ENERGY_PRICE;
        }
        return new QuantityType<>(priceKWh, priceUnit);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        cancelJobs();
        bridgeHandler = null;
        super.dispose();
    }

    public List<GmePriceEntry> getTodayPrices() {
        return priceCache.getTodayPrices();
    }

    public List<GmePriceEntry> getTomorrowPrices() {
        return priceCache.getTomorrowPrices();
    }
}
