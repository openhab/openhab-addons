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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gme.internal.api.GmeApiException;
import org.openhab.binding.gme.internal.api.GmeAuthManager;
import org.openhab.binding.gme.internal.model.GmeGranularity;
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

    /*
     * Retry interval used only when tomorrow's dataset is missing or
     * incomplete.
     *
     * This is intentionally independent from the normal bridge refresh
     * interval, which may be much longer (for example 60 minutes).
     */
    private static final int API_RETRY_INTERVAL_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(GmePunHandler.class);
    private final AtomicBoolean refreshInProgress = new AtomicBoolean();
    private final AtomicLong lifecycleGeneration = new AtomicLong();

    private @Nullable GmeApiBridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> channelRefreshJob;
    private @Nullable ScheduledFuture<?> apiRefreshJob;
    private @Nullable ScheduledFuture<?> apiRetryJob;
    private @Nullable Instant lastSuccessfulMarketUpdate;
    private String zonalCacheZone = "";

    private final GmePriceCache priceCache = new GmePriceCache();
    private final GmePriceCache zonalPriceCache = new GmePriceCache();

    public GmePunHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        lifecycleGeneration.incrementAndGet();
        updateStatus(ThingStatus.UNKNOWN);

        if (!linkBridge()) {
            return;
        }

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            scheduleChannelRefresh();
            scheduleApiRefresh();
            refreshPrices(false);
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
            lifecycleGeneration.incrementAndGet();
            if (bridgeHandler != null || linkBridge()) {
                scheduleChannelRefresh();
                scheduleApiRefresh();
                refreshPrices(false);
            }
        } else {
            lifecycleGeneration.incrementAndGet();
            cancelJobs();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "GME API bridge is not online.");
        }
    }

    private synchronized void scheduleChannelRefresh() {
        ScheduledFuture<?> currentJob = channelRefreshJob;
        if (currentJob != null && !currentJob.isCancelled()) {
            return;
        }

        long generation = lifecycleGeneration.get();
        channelRefreshJob = scheduler.scheduleWithFixedDelay(() -> {
            if (isCurrentGeneration(generation)) {
                refreshLocalState();
            }
        }, 1, 1, TimeUnit.MINUTES);
        logger.debug("Scheduled local GME PUN channel refresh every minute");
    }

    private synchronized void scheduleApiRefresh() {
        ScheduledFuture<?> currentJob = apiRefreshJob;
        if (currentJob != null && !currentJob.isCancelled()) {
            return;
        }

        GmeApiBridgeHandler currentBridgeHandler = bridgeHandler;
        if (currentBridgeHandler == null) {
            return;
        }

        int refreshInterval = currentBridgeHandler.getRefreshInterval();
        long generation = lifecycleGeneration.get();
        apiRefreshJob = scheduler.scheduleWithFixedDelay(() -> {
            if (isCurrentGeneration(generation)) {
                logger.debug("Executing scheduled GME market data refresh");
                refreshPrices(true);
            }
        }, refreshInterval, refreshInterval, TimeUnit.MINUTES);
        logger.debug("Scheduled GME market data refresh every {} minutes", refreshInterval);
    }

    /**
     * Schedule a single retry for retrieving tomorrow's dataset.
     *
     * The retry is one-shot rather than a repeating fixed-delay task.
     * Immediately before executing refreshPrices(), the apiRetryJob reference
     * is cleared. This allows refreshPrices() to schedule another retry if the
     * dataset is still unavailable.
     */
    private synchronized void scheduleApiRetry() {
        ScheduledFuture<?> currentJob = apiRetryJob;

        if (currentJob != null && !currentJob.isDone() && !currentJob.isCancelled()) {
            return;
        }

        long generation = lifecycleGeneration.get();
        apiRetryJob = scheduler.schedule(() -> {
            synchronized (GmePunHandler.this) {
                apiRetryJob = null;
            }

            if (isCurrentGeneration(generation)) {
                logger.debug("Executing scheduled GME PUN API retry");
                refreshPrices(false);
            }
        }, API_RETRY_INTERVAL_MINUTES, TimeUnit.MINUTES);

        logger.debug("Scheduled GME PUN API retry in {} minutes", API_RETRY_INTERVAL_MINUTES);
    }

    private synchronized void cancelApiRetry() {
        ScheduledFuture<?> currentJob = apiRetryJob;
        apiRetryJob = null;

        if (currentJob != null) {
            currentJob.cancel(false);
        }
    }

    private synchronized void cancelJobs() {
        ScheduledFuture<?> currentChannelJob = channelRefreshJob;
        if (currentChannelJob != null) {
            currentChannelJob.cancel(false);
            channelRefreshJob = null;
        }

        ScheduledFuture<?> currentApiRefreshJob = apiRefreshJob;
        if (currentApiRefreshJob != null) {
            currentApiRefreshJob.cancel(false);
            apiRefreshJob = null;
        }

        cancelApiRetry();
    }

    private void refreshLocalState() {
        LocalDate currentDate = LocalDate.now(GME_ZONE);
        LocalDate tomorrow = currentDate.plusDays(1);
        LocalDate loadedTodayDate = priceCache.getTodayDate();

        GmeApiBridgeHandler currentBridgeHandler = bridgeHandler;
        String marketZone = currentBridgeHandler != null ? currentBridgeHandler.getMarketZone() : "";
        prepareZonalCache(marketZone);

        if (!currentDate.equals(loadedTodayDate)) {
            boolean punPromoted = priceCache.promoteTomorrowToToday(currentDate, GME_ZONE, getGranularity());

            boolean zonalPromoted = marketZone.isBlank()
                    || zonalPriceCache.promoteTomorrowToToday(currentDate, GME_ZONE, getGranularity());

            if (punPromoted) {
                markMarketDataUpdated();
                logger.debug("Promoted cached PUN prices for {} to today", currentDate);
            }

            if (!marketZone.isBlank() && zonalPromoted) {
                markMarketDataUpdated();
                logger.debug("Promoted cached {} zonal prices for {} to today", marketZone, currentDate);
            }

            if (punPromoted && zonalPromoted) {
                updateChannels();
            }

            /*
             * A date rollover requires a new "tomorrow" dataset.
             *
             * Arm the retry before attempting the immediate refresh so that
             * a concurrent refresh cannot permanently lose the rollover
             * request.
             */
            logger.debug("GME date rollover detected for {}; requesting new tomorrow dataset {}", currentDate,
                    tomorrow);

            scheduleApiRetry();
            refreshPrices(false);
            return;
        }

        updateChannels();

        /*
         * Defensive rollover recovery.
         *
         * refreshPrices() can legitimately be skipped when another refresh is
         * already in progress. If this happens exactly at midnight, the
         * tomorrow-to-today promotion may succeed while retrieval of the new
         * tomorrow dataset is lost.
         *
         * Check the cache date on every local refresh. If tomorrow is missing
         * or belongs to a different date, ensure that the normal API retry job
         * is armed.
         */
        GmeGranularity granularity = getGranularity();

        boolean tomorrowPunMissing = !tomorrow.equals(priceCache.getTomorrowDate())
                || priceCache.getTomorrowPrices().isEmpty() || !priceCache.hasTomorrowGranularity(granularity);

        boolean tomorrowZonalMissing = !marketZone.isBlank()
                && (!tomorrow.equals(zonalPriceCache.getTomorrowDate()) || zonalPriceCache.getTomorrowPrices().isEmpty()
                        || !zonalPriceCache.hasTomorrowGranularity(granularity));

        if (tomorrowPunMissing || tomorrowZonalMissing) {
            logger.debug("Tomorrow dataset for {} is missing or stale; ensuring API retry is scheduled", tomorrow);
            scheduleApiRetry();
        }
    }

    private void refreshPrices(boolean forceRefresh) {
        if (!refreshInProgress.compareAndSet(false, true)) {
            return;
        }

        long generation = lifecycleGeneration.get();
        scheduler.execute(() -> {
            try {
                if (!isCurrentGeneration(generation)) {
                    return;
                }
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

                String marketZone = currentBridgeHandler.getMarketZone();
                prepareZonalCache(marketZone);

                LocalDate today = LocalDate.now(GME_ZONE);
                LocalDate tomorrow = today.plusDays(1);

                GmeGranularity granularity = getGranularity();

                boolean todayPunMissing = forceRefresh || !today.equals(priceCache.getTodayDate())
                        || priceCache.getTodayPrices().isEmpty() || !priceCache.hasTodayGranularity(granularity);

                boolean todayZonalMissing = !marketZone.isBlank() && (forceRefresh
                        || !today.equals(zonalPriceCache.getTodayDate()) || zonalPriceCache.getTodayPrices().isEmpty()
                        || !zonalPriceCache.hasTodayGranularity(granularity));

                if (todayPunMissing || todayZonalMissing) {
                    if (!forceRefresh && todayPunMissing
                            && priceCache.promoteTomorrowToToday(today, GME_ZONE, getGranularity())) {
                        markMarketDataUpdated();
                        logger.debug("Promoted cached PUN prices for {} to today", today);
                        todayPunMissing = false;
                    }

                    if (!forceRefresh && todayZonalMissing
                            && zonalPriceCache.promoteTomorrowToToday(today, GME_ZONE, getGranularity())) {
                        markMarketDataUpdated();
                        logger.debug("Promoted cached {} zonal prices for {} to today", marketZone, today);
                        todayZonalMissing = false;
                    }

                    if (todayPunMissing || todayZonalMissing) {
                        List<GmePriceEntry> marketPrices = authManager.requestMarketPrices(today, granularity);
                        if (!isCurrentGeneration(generation)) {
                            return;
                        }

                        if (todayPunMissing) {
                            List<GmePriceEntry> newTodayPrices = filterZone(marketPrices, "PUN");

                            if (!GmePriceTimeline.isCompleteDailySet(newTodayPrices, today, GME_ZONE, granularity)) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "GME returned an incomplete PUN price dataset for today.");
                                return;
                            }

                            priceCache.setToday(today, newTodayPrices);
                            markMarketDataUpdated();
                        }

                        if (todayZonalMissing) {
                            List<GmePriceEntry> newTodayZonalPrices = filterZone(marketPrices, marketZone);

                            if (GmePriceTimeline.isCompleteDailySet(newTodayZonalPrices, today, GME_ZONE,
                                    granularity)) {
                                zonalPriceCache.setToday(today, newTodayZonalPrices);
                                markMarketDataUpdated();
                            } else {
                                logger.warn(
                                        "GME returned an incomplete {} zonal price dataset for today: received {} entries, expected {}",
                                        marketZone, newTodayZonalPrices.size(),
                                        GmePriceTimeline.getExpectedPeriods(today, GME_ZONE, granularity));
                            }
                        }
                    }
                }

                boolean tomorrowPunMissing = !tomorrow.equals(priceCache.getTomorrowDate())
                        || priceCache.getTomorrowPrices().isEmpty() || !priceCache.hasTomorrowGranularity(granularity);

                boolean tomorrowZonalMissing = !marketZone.isBlank()
                        && (!tomorrow.equals(zonalPriceCache.getTomorrowDate())
                                || zonalPriceCache.getTomorrowPrices().isEmpty()
                                || !zonalPriceCache.hasTomorrowGranularity(granularity));

                if (tomorrowPunMissing || tomorrowZonalMissing) {
                    try {
                        List<GmePriceEntry> marketPrices = authManager.requestMarketPrices(tomorrow, granularity);
                        if (!isCurrentGeneration(generation)) {
                            return;
                        }

                        boolean retryRequired = false;

                        if (tomorrowPunMissing) {
                            List<GmePriceEntry> newTomorrowPrices = filterZone(marketPrices, "PUN");

                            if (!GmePriceTimeline.isCompleteDailySet(newTomorrowPrices, tomorrow, GME_ZONE,
                                    granularity)) {
                                priceCache.clearTomorrow();
                                retryRequired = true;

                                logger.debug(
                                        "PUN price dataset for tomorrow is incomplete: received {} entries, expected {}",
                                        newTomorrowPrices.size(),
                                        GmePriceTimeline.getExpectedPeriods(tomorrow, GME_ZONE, granularity));
                            } else {
                                priceCache.setTomorrow(tomorrow, newTomorrowPrices);
                                markMarketDataUpdated();
                            }
                        }

                        if (tomorrowZonalMissing) {
                            List<GmePriceEntry> newTomorrowZonalPrices = filterZone(marketPrices, marketZone);

                            if (!GmePriceTimeline.isCompleteDailySet(newTomorrowZonalPrices, tomorrow, GME_ZONE,
                                    granularity)) {
                                zonalPriceCache.clearTomorrow();
                                retryRequired = true;

                                logger.debug(
                                        "{} zonal price dataset for tomorrow is incomplete: received {} entries, expected {}",
                                        marketZone, newTomorrowZonalPrices.size(),
                                        GmePriceTimeline.getExpectedPeriods(tomorrow, GME_ZONE, granularity));
                            } else {
                                zonalPriceCache.setTomorrow(tomorrow, newTomorrowZonalPrices);
                                markMarketDataUpdated();
                            }
                        }

                        if (retryRequired) {
                            scheduleApiRetry();
                        } else {
                            cancelApiRetry();
                        }
                    } catch (GmeApiException e) {
                        if (!isCurrentGeneration(generation)) {
                            return;
                        }
                        if (e.isAuthenticationError()) {
                            throw e;
                        }

                        if (tomorrowPunMissing) {
                            priceCache.clearTomorrow();
                        }

                        if (tomorrowZonalMissing) {
                            zonalPriceCache.clearTomorrow();
                        }

                        scheduleApiRetry();
                        logger.debug("Unable to retrieve GME prices for tomorrow: {}", e.getMessage());
                    } catch (IOException | TimeoutException | ExecutionException | IllegalStateException e) {
                        if (!isCurrentGeneration(generation)) {
                            return;
                        }
                        if (tomorrowPunMissing) {
                            priceCache.clearTomorrow();
                        }

                        if (tomorrowZonalMissing) {
                            zonalPriceCache.clearTomorrow();
                        }

                        scheduleApiRetry();
                        logger.debug("GME prices for tomorrow are not available yet: {}", e.getMessage());
                    }
                } else {
                    cancelApiRetry();
                }

                if (!isCurrentGeneration(generation)) {
                    return;
                }

                updateChannels();

                logger.debug("Loaded {} PUN prices for today and {} for tomorrow", priceCache.getTodayPrices().size(),
                        priceCache.getTomorrowPrices().size());

                if (!marketZone.isBlank()) {
                    logger.debug("Loaded {} {} zonal prices for today and {} for tomorrow",
                            zonalPriceCache.getTodayPrices().size(), marketZone,
                            zonalPriceCache.getTomorrowPrices().size());
                }

                if (isCurrentGeneration(generation)) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (isCurrentGeneration(generation)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "GME price refresh was interrupted.");
                }
            } catch (IOException | TimeoutException | ExecutionException | IllegalStateException e) {
                if (isCurrentGeneration(generation)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to retrieve GME prices: " + e.getMessage());
                }
            } finally {
                refreshInProgress.set(false);
            }
        });
    }

    private boolean isCurrentGeneration(long generation) {
        return lifecycleGeneration.get() == generation;
    }

    private void markMarketDataUpdated() {
        lastSuccessfulMarketUpdate = Instant.now();
    }

    private void prepareZonalCache(String marketZone) {
        if (marketZone.equals(zonalCacheZone)) {
            return;
        }

        zonalPriceCache.clear();
        zonalCacheZone = marketZone;
        clearZonalChannels();
    }

    private void clearZonalChannels() {
        updateState(CHANNEL_TODAY_ZONAL_PRICES, UnDefType.UNDEF);
        updateState(CHANNEL_TOMORROW_ZONAL_PRICES, UnDefType.UNDEF);
    }

    private GmeGranularity getGranularity() {
        GmeApiBridgeHandler currentBridgeHandler = bridgeHandler;
        return currentBridgeHandler != null ? currentBridgeHandler.getGranularity() : GmeGranularity.PT60;
    }

    private List<GmePriceEntry> filterZone(List<GmePriceEntry> prices, String zone) {
        return prices.stream().filter(price -> zone.equals(price.zone())).toList();
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

        sendPriceTimeSeries(CHANNEL_TODAY_ZONAL_PRICES, zonalPriceCache.getTodayPrices());
        sendPriceTimeSeries(CHANNEL_TOMORROW_ZONAL_PRICES, zonalPriceCache.getTomorrowPrices());

        if (tomorrowPrices.isEmpty()) {
            updateState(CHANNEL_TOMORROW_AVAILABLE, OnOffType.OFF);
            clearTomorrowChannels();
        } else {
            updateState(CHANNEL_TOMORROW_AVAILABLE, OnOffType.ON);

            updateDayStatistics(tomorrowPrices, CHANNEL_TOMORROW_AVERAGE, CHANNEL_TOMORROW_MIN, CHANNEL_TOMORROW_MAX,
                    CHANNEL_TOMORROW_MIN_TIME, CHANNEL_TOMORROW_MAX_TIME);
        }

        Instant lastUpdate = lastSuccessfulMarketUpdate;
        if (lastUpdate != null) {
            updateState(CHANNEL_LAST_UPDATE, new DateTimeType(lastUpdate));
        }
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
            updateState(averageChannel, UnDefType.UNDEF);
            updateState(minChannel, UnDefType.UNDEF);
            updateState(maxChannel, UnDefType.UNDEF);
            updateState(minTimeChannel, UnDefType.UNDEF);
            updateState(maxTimeChannel, UnDefType.UNDEF);
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
        if (prices.isEmpty()) {
            updateState(channelId, UnDefType.UNDEF);
            return;
        }

        updateState(channelId, toPriceState(prices.get(0).priceKWh()));

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
        lifecycleGeneration.incrementAndGet();
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

    public List<GmePriceEntry> getTodayZonalPrices() {
        return zonalPriceCache.getTodayPrices();
    }

    public List<GmePriceEntry> getTomorrowZonalPrices() {
        return zonalPriceCache.getTomorrowPrices();
    }
}
