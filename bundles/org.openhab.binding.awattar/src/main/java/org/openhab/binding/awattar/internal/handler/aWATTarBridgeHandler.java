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

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.awattar.internal.aWATTarBindingConstants.BINDING_ID;
import static org.openhab.binding.awattar.internal.aWATTarUtil.*;

import java.time.*;
import java.time.zone.ZoneRulesException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.awattar.internal.aWATTarBridgeConfiguration;
import org.openhab.binding.awattar.internal.aWATTarPrice;
import org.openhab.binding.awattar.internal.connection.aWATTarConnectionException;
import org.openhab.binding.awattar.internal.dto.AwattarApiData;
import org.openhab.binding.awattar.internal.dto.Datum;
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

/**
 * The {@link aWATTarBridgeHandler} is responsible for retrieving data from the aWATTar API.
 *
 * The API provides hourly prices for the current day and, starting from 14:00, hourly prices for the next day.
 * Check the documentation at https://www.awattar.de/services/api
 *
 *
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class aWATTarBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(aWATTarBridgeHandler.class);
    private final HttpClient httpClient;
    @Nullable
    private ScheduledFuture<?> dataRefresher;

    private final String URL_DE = "https://api.awattar.de/v1/marketdata";
    private final String URL_AT = "https://api.awattar.at/v1/marketdata";
    private String URL;

    // This cache stores price data for up to two days
    @Nullable
    private SortedMap<Long, aWATTarPrice> priceMap;
    private final int dataRefreshInterval = 60;
    @Nullable
    private aWATTarBridgeConfiguration config;
    private double vatFactor = 0;

    private long lastUpdated = 0;

    private double basePrice = 0;
    private long minTimestamp = 0;
    private long maxTimestamp = 0;
    private ZoneId zone;

    public aWATTarBridgeHandler(Bridge thing, HttpClient httpClient) {
        super(thing);
        logger.trace("Creating aWATTarBridgeHandler instance {}", this);
        this.httpClient = httpClient;
        URL = URL_DE;
        zone = ZoneId.systemDefault();
    }

    @Override
    public void initialize() {
        logger.trace("Initializing aWATTar bridge {}", this);
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(aWATTarBridgeConfiguration.class);
        if (config == null) {
            logger.error("No config provided!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.config.missing");
            return;
        }
        vatFactor = 1 + (config.vatPercent / 100);
        basePrice = config.basePrice;
        try {
            zone = ZoneId.of(config.timeZone);
            logger.trace("Using time zone {}", zone);
        } catch (ZoneRulesException ex) {
            logger.error("Zone ID not found: {}, {}", zone, ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.timezone");
            return;
        } catch (DateTimeException ex) {
            logger.error("Invalid Timezone format: {}, {}", config.timeZone, ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.timezone.format");
            return;
        }
        switch (config.country) {
            case "DE":
                URL = URL_DE;
                break;
            case "AT":
                URL = URL_AT;
                break;
            default:
                logger.error("Invalid country {}, only DE and AT are supported", config.country);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.unsupported.country");
                return;
        }

        logger.trace("Start Data refresh job at interval {} seconds.", dataRefreshInterval);
        dataRefresher = scheduler.scheduleAtFixedRate(this::refreshIfNeeded,
                getMillisToNextMinute(dataRefreshInterval / 60), dataRefreshInterval * 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        logger.trace("Disposing aWATTar bridge {}", this);
        dataRefresher.cancel(true);
        dataRefresher = null;
        priceMap = null;
        lastUpdated = 0;
    }

    public void refreshIfNeeded() {
        logger.trace("Refresh if needed called");
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

            StringBuilder request = new StringBuilder(URL);
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
                    SortedMap<Long, aWATTarPrice> result = new TreeMap<>();
                    minTimestamp = 0;
                    maxTimestamp = 0;
                    AwattarApiData apiData = gson.fromJson(content, AwattarApiData.class);
                    for (Datum d : apiData.data) {
                        result.put(d.startTimestamp,
                                new aWATTarPrice(d.marketprice / 10.0, d.startTimestamp, d.endTimestamp, zone));
                        updateMin(d.startTimestamp);
                        updateMax(d.endTimestamp);
                    }
                    priceMap = result;
                    updateStatus(ThingStatus.ONLINE);
                    lastUpdated = Instant.now().toEpochMilli();
                    break;

                default:
                    logger.warn("aWATTar server responded with status code {}: {}", httpStatus, content);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/warn.awattar.statuscode");
            }
        } catch (Exception e) {
            String errorMessage = e.getLocalizedMessage();
            logger.warn("Exception occurred during execution: {}", errorMessage);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.receiving.prices");
            throw new aWATTarConnectionException(errorMessage, e.getCause());
        }
    }

    private boolean needRefresh() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return true;
        }
        if (priceMap == null) {
            return true;
        }
        return priceMap.lastKey() < Instant.now().toEpochMilli() + 9 * 3600 * 1000;
    }

    private void refresh() {
        logger.trace("Refreshing aWATTar data ...");
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
    public synchronized SortedMap<Long, aWATTarPrice> getPriceMap() {
        if (priceMap == null) {
            refresh();
        }
        return priceMap;
    }

    @Nullable
    public aWATTarPrice getPriceFor(long timestamp) {
        SortedMap<Long, aWATTarPrice> priceMap = getPriceMap();
        if (priceMap == null) {
            return null;
        }
        if (!containsPriceFor(timestamp)) {
            return null;
        }
        for (aWATTarPrice price : priceMap.values()) {
            if (timestamp >= price.getStartTimestamp() && timestamp < price.getEndTimestamp()) {
                return price;
            }
        }
        return null;
    }

    public boolean containsPriceFor(long timestamp) {
        logger.trace("containsPriceFor: ts: {}, min: {}, max: {}", timestamp, minTimestamp, maxTimestamp);
        return minTimestamp <= timestamp && maxTimestamp >= timestamp;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command {} for channel {}", command, channelUID);
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
