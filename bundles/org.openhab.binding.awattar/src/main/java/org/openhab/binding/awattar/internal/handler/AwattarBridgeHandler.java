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
package org.openhab.binding.awattar.internal.handler;

import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_MARKET_NET;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.CHANNEL_TOTAL_NET;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.SortedSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.awattar.internal.AwattarBridgeConfiguration;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.api.AwattarApi;
import org.openhab.binding.awattar.internal.api.AwattarApi.AwattarApiException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwattarBridgeHandler} is responsible for retrieving data from the
 * aWATTar API via the {@link AwattarApi}.
 *
 * The API provides hourly prices for the current day and, starting from 14:00,
 * hourly prices for the next day.
 * Check the documentation at <a href="https://www.awattar.de/services/api" />
 *
 *
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class AwattarBridgeHandler extends BaseBridgeHandler {
    private static final int DATA_REFRESH_INTERVAL = 60;

    private final Logger logger = LoggerFactory.getLogger(AwattarBridgeHandler.class);
    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> dataRefresher;
    private Instant lastRefresh = Instant.EPOCH;

    // This cache stores price data for up to two days
    private @Nullable SortedSet<AwattarPrice> prices;
    private ZoneId zone;

    private @Nullable AwattarApi awattarApi;

    public AwattarBridgeHandler(Bridge thing, HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.httpClient = httpClient;
        zone = timeZoneProvider.getTimeZone();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        AwattarBridgeConfiguration config = getConfigAs(AwattarBridgeConfiguration.class);

        try {
            awattarApi = new AwattarApi(httpClient, zone, config);

            dataRefresher = scheduler.scheduleWithFixedDelay(this::refreshIfNeeded, 0, DATA_REFRESH_INTERVAL * 1000L,
                    TimeUnit.MILLISECONDS);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.unsupported.country");
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefresher = dataRefresher;
        if (localRefresher != null) {
            localRefresher.cancel(true);
        }
        dataRefresher = null;
        prices = null;
    }

    void refreshIfNeeded() {
        if (needRefresh()) {
            refresh();
        }
    }

    /**
     * Refresh the data from the API.
     *
     *
     */
    private void refresh() {
        try {
            // Method is private and only called when dataRefresher is initialized.
            // DataRefresher is initialized after successful creation of AwattarApi.
            prices = awattarApi.getData();

            TimeSeries netMarketSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
            TimeSeries netTotalSeries = new TimeSeries(TimeSeries.Policy.REPLACE);

            Unit<?> priceUnit = getPriceUnit();

            for (AwattarPrice price : prices) {
                Instant timestamp = Instant.ofEpochMilli(price.timerange().start());

                netMarketSeries.add(timestamp, new QuantityType<>(price.netPrice() / 100.0, priceUnit));
                netTotalSeries.add(timestamp, new QuantityType<>(price.netTotal() / 100.0, priceUnit));
            }

            // update channels
            sendTimeSeries(CHANNEL_MARKET_NET, netMarketSeries);
            sendTimeSeries(CHANNEL_TOTAL_NET, netTotalSeries);

            updateStatus(ThingStatus.ONLINE);
        } catch (AwattarApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private Unit<?> getPriceUnit() {
        Unit<?> priceUnit = UnitUtils.parseUnit("EUR/kWh");
        if (priceUnit == null) {
            priceUnit = CurrencyUnits.BASE_ENERGY_PRICE;
            logger.info("Using {} instead of EUR/kWh, because it is not available", priceUnit);
        }
        return priceUnit;
    }

    private void createAndSendTimeSeries(String channelId, Function<AwattarPrice, Double> valueFunction) {
        SortedSet<AwattarPrice> locPrices = getPrices();
        Unit<?> priceUnit = getPriceUnit();
        if (locPrices == null) {
            return;
        }
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        locPrices.forEach(p -> {
            timeSeries.add(Instant.ofEpochMilli(p.timerange().start()),
                    new QuantityType<>(valueFunction.apply(p) / 100.0, priceUnit));
        });
        sendTimeSeries(channelId, timeSeries);
    }

    /**
     * Check if the data needs to be refreshed.
     *
     * The data is refreshed if:
     * - the thing is offline
     * - the local cache is empty
     * - the current time is after 15:00 and the last refresh was more than an hour
     * ago
     * - the current time is after 18:00 and the last refresh was more than an hour
     * ago
     * - the current time is after 21:00 and the last refresh was more than an hour
     * ago
     *
     * @return true if the data needs to be refreshed
     */
    private boolean needRefresh() {
        // if the thing is offline, we need to refresh
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return true;
        }

        // if the local cache is empty, we need to refresh
        if (prices == null) {
            return true;
        }

        // Note: all this magic is made to avoid refreshing the data too often, since
        // the API is rate-limited
        // to 100 requests per day.

        // do not refresh before 15:00, since the prices for the next day are available
        // only after 14:00
        ZonedDateTime now = ZonedDateTime.now(zone);
        if (now.getHour() < 15) {
            return false;
        }

        // refresh then every 3 hours, if the last refresh was more than an hour ago
        if (now.getHour() % 3 == 0 && lastRefresh.getEpochSecond() < now.minusHours(1).toEpochSecond()) {
            // update the last refresh time
            lastRefresh = Instant.now();

            // return true to indicate an update is needed
            return true;
        }

        return false;
    }

    public ZoneId getTimeZone() {
        return zone;
    }

    @Nullable
    public synchronized SortedSet<AwattarPrice> getPrices() {
        if (prices == null) {
            refresh();
        }
        return prices;
    }

    public @Nullable AwattarPrice getPriceFor(long timestamp) {
        SortedSet<AwattarPrice> localPrices = getPrices();
        if (localPrices == null || !containsPriceFor(timestamp)) {
            return null;
        }
        return localPrices.stream().filter(e -> e.timerange().contains(timestamp)).findAny().orElse(null);
    }

    public boolean containsPriceFor(long timestamp) {
        SortedSet<AwattarPrice> localPrices = getPrices();
        if (localPrices == null) {
            return false;
        }
        return new TimeRange(localPrices.first().timerange().start(), localPrices.last().timerange().end())
                .contains(timestamp);
    }

    public boolean containsPriceFor(TimeRange timeRange) {
        SortedSet<AwattarPrice> localPrices = getPrices();
        if (localPrices == null) {
            return false;
        }
        return new TimeRange(localPrices.first().timerange().start(), localPrices.last().timerange().end())
                .contains(timeRange);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_MARKET_NET -> createAndSendTimeSeries(CHANNEL_MARKET_NET, AwattarPrice::netPrice);
                case CHANNEL_TOTAL_NET -> createAndSendTimeSeries(CHANNEL_TOTAL_NET, AwattarPrice::netTotal);
            }
        }
    }
}
