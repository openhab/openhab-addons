/**
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
package org.openhab.binding.awattar.internal.api;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.awattar.internal.AwattarBridgeConfiguration;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.dto.AwattarTimeProvider;
import org.openhab.binding.awattar.internal.dto.EnergyChartsApiData;
import org.openhab.binding.awattar.internal.handler.TimeRange;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Retrieves day-ahead prices from the Energy-Charts API.
 */
@NonNullByDefault
public class EnergyChartsApi implements MarketPriceApi {
    private static final String URL = "https://api.energy-charts.info/price";

    private final HttpClient httpClient;
    private final AwattarTimeProvider timeProvider;
    private final Gson gson = new Gson();
    private final String biddingZone;
    private final double vatFactor;
    private final double basePrice;

    public class EnergyChartsApiException extends MarketPriceApiException {
        private static final long serialVersionUID = 1L;

        public EnergyChartsApiException(String message) {
            super(message);
        }
    }

    public EnergyChartsApi(HttpClient httpClient, AwattarTimeProvider timeProvider, AwattarBridgeConfiguration config) {
        this.httpClient = httpClient;
        this.timeProvider = timeProvider;
        vatFactor = 1 + (config.vatPercent / 100);
        basePrice = config.basePrice;

        biddingZone = switch (config.country) {
            case "AT" -> "AT";
            case "DE" -> "DE-LU";
            default -> throw new IllegalArgumentException("Country code must be 'DE' or 'AT'");
        };
    }

    public SortedSet<AwattarPrice> getData() throws EnergyChartsApiException {
        LocalDate today = timeProvider.getZonedDateTimeNow().toLocalDate();
        String requestUrl = URL + "?bzn=" + biddingZone + "&start=" + today.minusDays(1) + "&end=" + today.plusDays(2);

        try {
            ContentResponse response = httpClient.newRequest(requestUrl).method(GET).timeout(10, TimeUnit.SECONDS)
                    .send();
            String content = response.getContentAsString();

            if (content == null) {
                throw new EnergyChartsApiException("@text/error.empty.data");
            }
            if (response.getStatus() != OK_200) {
                throw new EnergyChartsApiException("@text/warn.energycharts.statuscode " + response.getStatus());
            }

            return parseData(content);
        } catch (ExecutionException e) {
            throw new EnergyChartsApiException("@text/error.execution");
        } catch (JsonSyntaxException e) {
            throw new EnergyChartsApiException("@text/error.json");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EnergyChartsApiException("@text/error.interrupted");
        } catch (TimeoutException e) {
            throw new EnergyChartsApiException("@text/error.timeout");
        }
    }

    private SortedSet<AwattarPrice> parseData(String content) throws EnergyChartsApiException {
        EnergyChartsApiData apiData = gson.fromJson(content, EnergyChartsApiData.class);
        if (apiData == null || apiData.unixSeconds.size() != apiData.prices.size() || apiData.unixSeconds.size() < 2) {
            throw new EnergyChartsApiException("@text/error.json");
        }

        SortedSet<AwattarPrice> result = new TreeSet<>(Comparator.comparingLong(price -> price.timerange().start()));
        for (int index = 0; index < apiData.unixSeconds.size(); index++) {
            AwattarPrice price = toPrice(apiData, index);
            if (price != null) {
                result.add(price);
            }
        }
        return result;
    }

    private @Nullable AwattarPrice toPrice(EnergyChartsApiData apiData, int index) throws EnergyChartsApiException {
        long start = apiData.unixSeconds.get(index) * 1000L;
        long end = getIntervalEnd(apiData, index, start);
        Double marketPrice = apiData.prices.get(index);
        if (marketPrice == null) {
            return null;
        }

        double netMarket = marketPrice / 10.0;
        double grossMarket = netMarket * vatFactor;
        double netTotal = netMarket + basePrice;
        double grossTotal = netTotal * vatFactor;
        return new AwattarPrice(netMarket, grossMarket, netTotal, grossTotal, new TimeRange(start, end));
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
