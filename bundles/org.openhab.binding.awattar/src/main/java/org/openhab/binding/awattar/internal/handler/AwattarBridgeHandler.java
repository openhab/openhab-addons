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

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.awattar.internal.AwattarBridgeConfiguration;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.dto.AwattarApiData;
import org.openhab.binding.awattar.internal.dto.Datum;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AwattarBridgeHandler} is responsible for retrieving data from the aWATTar API.
 *
 * The API provides hourly prices for the current day and, starting from 14:00, hourly prices for the next day.
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

    private static final String URLDE = "https://api.awattar.de/v1/marketdata";
    private static final String URLAT = "https://api.awattar.at/v1/marketdata";
    private String url;

    // This cache stores price data for up to two days
    private @Nullable SortedSet<AwattarPrice> prices;
    private double vatFactor = 0;
    private double basePrice = 0;
    private ZoneId zone;
    private final TimeZoneProvider timeZoneProvider;

    public AwattarBridgeHandler(Bridge thing, HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.httpClient = httpClient;
        url = URLDE;
        this.timeZoneProvider = timeZoneProvider;
        zone = timeZoneProvider.getTimeZone();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        AwattarBridgeConfiguration config = getConfigAs(AwattarBridgeConfiguration.class);
        vatFactor = 1 + (config.vatPercent / 100);
        basePrice = config.basePrice;
        zone = timeZoneProvider.getTimeZone();
        switch (config.country) {
            case "DE":
                url = URLDE;
                break;
            case "AT":
                url = URLAT;
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.unsupported.country");
                return;
        }

        dataRefresher = scheduler.scheduleWithFixedDelay(this::refreshIfNeeded, 0, DATA_REFRESH_INTERVAL * 1000L,
                TimeUnit.MILLISECONDS);
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

    private void refresh() {
        try {
            // we start one day in the past to cover ranges that already started yesterday
            ZonedDateTime zdt = LocalDate.now(zone).atStartOfDay(zone).minusDays(1);
            long start = zdt.toInstant().toEpochMilli();
            // Starting from midnight yesterday we add three days so that the range covers the whole next day.
            zdt = zdt.plusDays(3);
            long end = zdt.toInstant().toEpochMilli();

            StringBuilder request = new StringBuilder(url);
            request.append("?start=").append(start).append("&end=").append(end);

            logger.trace("aWATTar API request: = '{}'", request);
            ContentResponse contentResponse = httpClient.newRequest(request.toString()).method(GET)
                    .timeout(10, TimeUnit.SECONDS).send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.trace("aWATTar API response: status = {}, content = '{}'", httpStatus, content);

            if (httpStatus == OK_200) {
                Gson gson = new Gson();
                SortedSet<AwattarPrice> result = new TreeSet<>(Comparator.comparing(AwattarPrice::timerange));
                AwattarApiData apiData = gson.fromJson(content, AwattarApiData.class);
                if (apiData != null) {
                    TimeSeries netMarketSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
                    TimeSeries netTotalSeries = new TimeSeries(TimeSeries.Policy.REPLACE);

                    Unit<?> priceUnit = getPriceUnit();

                    for (Datum d : apiData.data) {
                        double netMarket = d.marketprice / 10.0;
                        double grossMarket = netMarket * vatFactor;
                        double netTotal = netMarket + basePrice;
                        double grossTotal = netTotal * vatFactor;
                        Instant timestamp = Instant.ofEpochMilli(d.startTimestamp);

                        netMarketSeries.add(timestamp, new QuantityType<>(netMarket / 100.0, priceUnit));
                        netTotalSeries.add(timestamp, new QuantityType<>(netTotal / 100.0, priceUnit));

                        result.add(new AwattarPrice(netMarket, grossMarket, netTotal, grossTotal,
                                new TimeRange(d.startTimestamp, d.endTimestamp)));
                    }
                    prices = result;

                    // update channels
                    sendTimeSeries(CHANNEL_MARKET_NET, netMarketSeries);
                    sendTimeSeries(CHANNEL_TOTAL_NET, netTotalSeries);

                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/error.invalid.data");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/warn.awattar.statuscode");
            }
        } catch (JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.json");
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.interrupted");
        } catch (ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.execution");
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.timeout");
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
        SortedSet<AwattarPrice> prices = getPrices();
        Unit<?> priceUnit = getPriceUnit();
        if (prices == null) {
            return;
        }
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        prices.forEach(p -> {
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
     * - the current time is after 15:00 and the last refresh was more than an hour ago
     * - the current time is after 18:00 and the last refresh was more than an hour ago
     * - the current time is after 21:00 and the last refresh was more than an hour ago
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

        // Note: all this magic is made to avoid refreshing the data too often, since the API is rate-limited
        // to 100 requests per day.

        // do not refresh before 15:00, since the prices for the next day are available only after 14:00
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
        return localPrices != null && localPrices.first().timerange().start() <= timestamp
                && localPrices.last().timerange().end() > timestamp;
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
