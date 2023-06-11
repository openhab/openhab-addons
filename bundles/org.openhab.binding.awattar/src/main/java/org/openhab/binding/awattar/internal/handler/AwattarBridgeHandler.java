/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.SortedMap;
import java.util.TreeMap;
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
 * Check the documentation at https://www.awattar.de/services/api
 *
 *
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class AwattarBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(AwattarBridgeHandler.class);
    private final HttpClient httpClient;
    @Nullable
    private ScheduledFuture<?> dataRefresher;

    private static final String URLDE = "https://api.awattar.de/v1/marketdata";
    private static final String URLAT = "https://api.awattar.at/v1/marketdata";
    private String url;

    // This cache stores price data for up to two days
    @Nullable
    private SortedMap<Long, AwattarPrice> priceMap;
    private final int dataRefreshInterval = 60;
    private double vatFactor = 0;
    private long lastUpdated = 0;
    private double basePrice = 0;
    private long minTimestamp = 0;
    private long maxTimestamp = 0;
    private ZoneId zone;
    private TimeZoneProvider timeZoneProvider;

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

        dataRefresher = scheduler.scheduleWithFixedDelay(this::refreshIfNeeded, 0, dataRefreshInterval * 1000,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefresher = dataRefresher;
        if (localRefresher != null) {
            localRefresher.cancel(true);
        }
        dataRefresher = null;
        priceMap = null;
        lastUpdated = 0;
    }

    public void refreshIfNeeded() {
        if (needRefresh()) {
            refresh();
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private void getPrices() {
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

            switch (httpStatus) {
                case OK_200:
                    Gson gson = new Gson();
                    SortedMap<Long, AwattarPrice> result = new TreeMap<>();
                    minTimestamp = 0;
                    maxTimestamp = 0;
                    AwattarApiData apiData = gson.fromJson(content, AwattarApiData.class);
                    if (apiData != null) {
                        for (Datum d : apiData.data) {
                            result.put(d.startTimestamp,
                                    new AwattarPrice(d.marketprice / 10.0, d.startTimestamp, d.endTimestamp, zone));
                            updateMin(d.startTimestamp);
                            updateMax(d.endTimestamp);
                        }
                        priceMap = result;
                        updateStatus(ThingStatus.ONLINE);
                        lastUpdated = Instant.now().toEpochMilli();
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/error.invalid.data");
                    }
                    break;

                default:
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
        SortedMap<Long, AwattarPrice> localMap = priceMap;
        if (localMap == null) {
            return true;
        }
        return localMap.lastKey() < Instant.now().toEpochMilli() + 9 * 3600 * 1000;
    }

    private void refresh() {
        getPrices();
    }

    public double getVatFactor() {
        return vatFactor;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public ZoneId getTimeZone() {
        return zone;
    }

    @Nullable
    public synchronized SortedMap<Long, AwattarPrice> getPriceMap() {
        if (priceMap == null) {
            refresh();
        }
        return priceMap;
    }

    @Nullable
    public AwattarPrice getPriceFor(long timestamp) {
        SortedMap<Long, AwattarPrice> priceMap = getPriceMap();
        if (priceMap == null) {
            return null;
        }
        if (!containsPriceFor(timestamp)) {
            return null;
        }
        for (AwattarPrice price : priceMap.values()) {
            if (timestamp >= price.getStartTimestamp() && timestamp < price.getEndTimestamp()) {
                return price;
            }
        }
        return null;
    }

    public boolean containsPriceFor(long timestamp) {
        return minTimestamp <= timestamp && maxTimestamp >= timestamp;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        } else {
            logger.debug("Binding {} only supports refresh command", BINDING_ID);
        }
    }

    private void updateMin(long ts) {
        minTimestamp = (minTimestamp == 0) ? ts : Math.min(minTimestamp, ts);
    }

    private void updateMax(long ts) {
        maxTimestamp = (maxTimestamp == 0) ? ts : Math.max(ts, maxTimestamp);
    }
}
