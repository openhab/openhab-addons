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
package org.openhab.binding.energycharts.internal.handler;

import static org.openhab.binding.energycharts.internal.EnergyChartsBindingConstants.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.SortedSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.energycharts.internal.EnergyChartsConfiguration;
import org.openhab.binding.energycharts.internal.MarketPrice;
import org.openhab.binding.energycharts.internal.api.EnergyChartsApi;
import org.openhab.binding.energycharts.internal.api.EnergyChartsApiException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;

/**
 * The {@link EnergyChartsHandler} retrieves day-ahead prices from Energy-Charts, sends them as time series and keeps
 * the channel states at the price of the current interval.
 *
 * Day-ahead prices for the next day are published around 13:00. The API is rate-limited, so prices are only
 * requested when the cache does not cover the current interval, or after 13:00 at most once per hour until the next
 * day is available.
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Thomas Leber - Energy-Charts binding
 */
@NonNullByDefault
public class EnergyChartsHandler extends BaseThingHandler {
    private static final Duration REFRESH_CHECK_INTERVAL = Duration.ofMinutes(15);
    private static final Duration MIN_TIME_BETWEEN_REQUESTS = Duration.ofHours(1);
    private static final int NEXT_DAY_AVAILABLE_HOUR = 13;

    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;
    private final Clock clock;
    private final Unit<?> priceUnit;
    private final Object lock = new Object();

    // counted up on initialize and dispose, so results of an earlier session are dropped
    private long session;
    private @Nullable EnergyChartsApi api;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> stateJob;

    private @Nullable SortedSet<MarketPrice> prices;
    private @Nullable MarketPrice currentPrice;
    private Instant lastRequest = Instant.EPOCH;

    public EnergyChartsHandler(Thing thing, HttpClient httpClient, TimeZoneProvider timeZoneProvider, Clock clock) {
        super(thing);
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
        this.clock = clock;

        Unit<?> unit = UnitUtils.parseUnit("EUR/kWh");
        priceUnit = unit != null ? unit : CurrencyUnits.BASE_ENERGY_PRICE;
    }

    @Override
    public void initialize() {
        EnergyChartsConfiguration config = getConfigAs(EnergyChartsConfiguration.class);
        if (!BIDDING_ZONES.contains(config.biddingZone)) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.unsupported.zone [\"" + config.biddingZone + "\"]");
            return;
        }

        synchronized (lock) {
            session++;
            api = new EnergyChartsApi(httpClient, config);
            prices = null;
            currentPrice = null;
            lastRequest = Instant.EPOCH;
        }
        updateStatus(ThingStatus.UNKNOWN);

        refreshJob = scheduler.scheduleWithFixedDelay(this::refreshIfNeeded, 0, REFRESH_CHECK_INTERVAL.toSeconds(),
                TimeUnit.SECONDS);

        // the state job is required to run exactly at minute borders, hence we can't use scheduleWithFixedDelay
        Instant now = clock.instant();
        long delay = Duration.between(now, now.truncatedTo(ChronoUnit.MINUTES).plus(Duration.ofMinutes(1))).toMillis();
        stateJob = scheduler.scheduleAtFixedRate(this::updateCurrentStates, delay, Duration.ofMinutes(1).toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localJob = refreshJob;
        if (localJob != null) {
            localJob.cancel(true);
        }
        refreshJob = null;

        localJob = stateJob;
        if (localJob != null) {
            localJob.cancel(true);
        }
        stateJob = null;

        synchronized (lock) {
            session++;
            api = null;
            prices = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType && CHANNEL_SPOT_PRICE.equals(channelUID.getId())) {
            synchronized (lock) {
                SortedSet<MarketPrice> localPrices = prices;
                if (localPrices != null) {
                    sendTimeSeries(localPrices);
                    updateState(CHANNEL_SPOT_PRICE, toState(findCurrentPrice(localPrices)));
                }
            }
        }
    }

    private void refreshIfNeeded() {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(timeZoneProvider.getTimeZone());
        EnergyChartsApi localApi;
        long localSession;
        synchronized (lock) {
            localApi = api;
            localSession = session;
            if (localApi == null
                    || (thing.getStatus() == ThingStatus.ONLINE && !needsRefresh(prices, now, lastRequest))) {
                return;
            }
            lastRequest = now.toInstant();
        }

        // the request runs without the lock, so dispose() does not wait for it
        try {
            SortedSet<MarketPrice> refreshedPrices = localApi.getData(now.toLocalDate());
            synchronized (lock) {
                if (session != localSession) {
                    return;
                }
                prices = refreshedPrices;
                sendTimeSeries(refreshedPrices);
                currentPrice = null;
                updateCurrentStates();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (EnergyChartsApiException e) {
            synchronized (lock) {
                if (session == localSession) {
                    updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        }
    }

    /**
     * Check if the prices need to be requested again.
     *
     * @param prices the cached prices
     * @param now the current time in the configured time zone
     * @param lastRequest the time of the last request
     * @return {@code true} if the cache does not cover the current interval, or if it is after 13:00, the next day is
     *         missing and the last request was more than an hour ago
     */
    static boolean needsRefresh(@Nullable SortedSet<MarketPrice> prices, ZonedDateTime now, Instant lastRequest) {
        if (prices == null || prices.isEmpty()) {
            return true;
        }

        long nowMillis = now.toInstant().toEpochMilli();
        if (prices.first().timerange().start() > nowMillis || prices.last().timerange().end() <= nowMillis) {
            return true;
        }

        long endOfNextDay = now.toLocalDate().plusDays(2).atStartOfDay(now.getZone()).toInstant().toEpochMilli();
        if (prices.last().timerange().end() >= endOfNextDay) {
            return false;
        }

        return now.getHour() >= NEXT_DAY_AVAILABLE_HOUR
                && !lastRequest.plus(MIN_TIME_BETWEEN_REQUESTS).isAfter(now.toInstant());
    }

    private void updateCurrentStates() {
        synchronized (lock) {
            SortedSet<MarketPrice> localPrices = prices;
            if (localPrices == null) {
                return;
            }

            MarketPrice price = findCurrentPrice(localPrices);
            if (price != null && price.equals(currentPrice)) {
                return;
            }
            currentPrice = price;
            updateState(CHANNEL_SPOT_PRICE, toState(price));
        }
    }

    private @Nullable MarketPrice findCurrentPrice(SortedSet<MarketPrice> localPrices) {
        long now = clock.millis();
        return localPrices.stream().filter(p -> p.timerange().contains(now)).findFirst().orElse(null);
    }

    private State toState(@Nullable MarketPrice price) {
        return price == null ? UnDefType.UNDEF : new QuantityType<>(price.price(), priceUnit);
    }

    private void sendTimeSeries(SortedSet<MarketPrice> localPrices) {
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        localPrices.forEach(p -> timeSeries.add(Instant.ofEpochMilli(p.timerange().start()), toState(p)));
        sendTimeSeries(CHANNEL_SPOT_PRICE, timeSeries);
    }
}
