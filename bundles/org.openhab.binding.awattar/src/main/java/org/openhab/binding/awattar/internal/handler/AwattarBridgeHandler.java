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
import static org.openhab.binding.awattar.internal.AwattarBindingConstants.BINDING_ID;

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.awattar.internal.AwattarBridgeConfiguration;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.dto.AwattarApiData;
import org.openhab.binding.awattar.internal.dto.Datum;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

        dataRefresher = scheduler.scheduleWithFixedDelay(this::refreshIfNeeded, 0, DATA_REFRESH_INTERVAL * 1000,
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
        updateStatus(ThingStatus.ONLINE);
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
                    for (Datum d : apiData.data) {
                        double netPrice = d.marketprice / 10.0;
                        TimeRange timerange = new TimeRange(d.startTimestamp, d.endTimestamp);
                        result.add(new AwattarPrice(netPrice, netPrice * vatFactor, netPrice + basePrice,
                                (netPrice + basePrice) * vatFactor, timerange));
                    }
                    prices = result;
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

    private boolean needRefresh() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return true;
        }
        SortedSet<AwattarPrice> localPrices = prices;
        return localPrices == null
                || localPrices.last().timerange().start() < Instant.now().toEpochMilli() + 9 * 3600 * 1000;
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
            refresh();
        } else {
            logger.debug("Binding {} only supports refresh command", BINDING_ID);
        }
    }
}
