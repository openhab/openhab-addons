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
package org.openhab.binding.energycharts.internal.api;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

import java.time.LocalDate;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.energycharts.internal.EnergyChartsConfiguration;
import org.openhab.binding.energycharts.internal.MarketPrice;
import org.openhab.binding.energycharts.internal.TimeRange;
import org.openhab.binding.energycharts.internal.dto.EnergyChartsApiData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Retrieves day-ahead prices from the Energy-Charts API and converts them into {@link MarketPrice} objects using the
 * thing configuration.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class EnergyChartsApi {
    private static final String URL = "https://api.energy-charts.info/price";
    private static final int REQUEST_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(EnergyChartsApi.class);
    private final Gson gson = new Gson();
    private final HttpClient httpClient;

    private final String biddingZone;
    private final double vatFactor;
    private final double basePrice;
    private final double serviceFee;

    public EnergyChartsApi(HttpClient httpClient, EnergyChartsConfiguration config) {
        this.httpClient = httpClient;

        biddingZone = config.biddingZone;
        vatFactor = 1 + (config.vatPercent / 100);
        basePrice = config.basePrice;
        serviceFee = config.serviceFee;
    }

    /**
     * Get the prices from the start of the given day until the end of the following day.
     *
     * @param today the first day to request
     *
     * @return the prices, sorted by time
     *
     * @throws EnergyChartsApiException if the request fails or the response cannot be parsed
     */
    public SortedSet<MarketPrice> getData(LocalDate today) throws EnergyChartsApiException {
        String requestUrl = URL + "?bzn=" + biddingZone + "&start=" + today + "&end=" + today.plusDays(2);

        String content = fetch(requestUrl);
        try {
            return parseData(content);
        } catch (JsonSyntaxException e) {
            throw new EnergyChartsApiException("@text/error.json");
        }
    }

    /**
     * Convert a market price into a {@link MarketPrice}, applying base price, service fee and VAT.
     *
     * @param marketPrice the net market price in €/MWh, as returned by the API
     * @param timeRange the time range the price is valid for
     *
     * @return the price in €ct/kWh
     */
    MarketPrice toPrice(double marketPrice, TimeRange timeRange) {
        // €/MWh -> €ct/kWh: divide by 10 (100/1000)
        double netMarket = marketPrice / 10.0;
        double grossMarket = netMarket * vatFactor;
        double netTotal = netMarket + basePrice;

        // add service fee
        if (serviceFee > 0) {
            netTotal += Math.abs(netTotal) * (serviceFee / 100);
        }

        double grossTotal = netTotal * vatFactor;

        return new MarketPrice(netMarket, grossMarket, netTotal, grossTotal, timeRange);
    }

    private String fetch(String url) throws EnergyChartsApiException {
        logger.trace("API request: '{}'", url);

        try {
            ContentResponse response = httpClient.newRequest(url).method(GET)
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int httpStatus = response.getStatus();
            String content = response.getContentAsString();
            logger.trace("API response: status = {}, content = '{}'", httpStatus, content);

            if (httpStatus != OK_200) {
                throw new EnergyChartsApiException("@text/error.statuscode [\"" + httpStatus + "\"]");
            }
            if (content == null || content.isBlank()) {
                throw new EnergyChartsApiException("@text/error.empty.data");
            }
            return content;
        } catch (ExecutionException e) {
            throw new EnergyChartsApiException("@text/error.execution");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EnergyChartsApiException("@text/error.interrupted");
        } catch (TimeoutException e) {
            throw new EnergyChartsApiException("@text/error.timeout");
        }
    }

    private SortedSet<MarketPrice> parseData(String content) throws EnergyChartsApiException {
        @Nullable
        EnergyChartsApiData apiData = gson.fromJson(content, EnergyChartsApiData.class);
        if (apiData == null || apiData.unixSeconds.size() != apiData.prices.size() || apiData.unixSeconds.size() < 2) {
            throw new EnergyChartsApiException("@text/error.json");
        }

        SortedSet<MarketPrice> result = new TreeSet<>();
        for (int index = 0; index < apiData.unixSeconds.size(); index++) {
            Double marketPrice = apiData.prices.get(index);
            if (marketPrice == null) {
                continue;
            }
            long start = apiData.unixSeconds.get(index) * 1000L;
            long end = getIntervalEnd(apiData, index, start);
            result.add(toPrice(marketPrice, new TimeRange(start, end)));
        }
        return result;
    }

    private long getIntervalEnd(EnergyChartsApiData apiData, int index, long start) throws EnergyChartsApiException {
        long end;
        if (index + 1 < apiData.unixSeconds.size()) {
            end = apiData.unixSeconds.get(index + 1) * 1000L;
        } else {
            long previousStart = apiData.unixSeconds.get(index - 1) * 1000L;
            end = start + (start - previousStart);
        }
        if (end <= start) {
            throw new EnergyChartsApiException("@text/error.json");
        }
        return end;
    }
}
