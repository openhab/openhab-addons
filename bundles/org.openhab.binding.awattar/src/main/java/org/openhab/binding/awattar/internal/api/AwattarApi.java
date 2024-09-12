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
package org.openhab.binding.awattar.internal.api;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.awattar.internal.AwattarBridgeConfiguration;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.dto.AwattarApiData;
import org.openhab.binding.awattar.internal.dto.Datum;
import org.openhab.binding.awattar.internal.handler.TimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AwattarApi} class is responsible for encapsulating the aWATTar API
 * and providing the data to the bridge.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class AwattarApi {
    private final Logger logger = LoggerFactory.getLogger(AwattarApi.class);

    private static final String URL_DE = "https://api.awattar.de/v1/marketdata";
    private static final String URL_AT = "https://api.awattar.at/v1/marketdata";
    private String url = URL_DE;

    private final HttpClient httpClient;

    private double vatFactor;
    private double basePrice;

    private ZoneId zone;

    private Gson gson;

    /**
     * Generic exception for the aWATTar API.
     */
    public class AwattarApiException extends Exception {
        private static final long serialVersionUID = 1L;

        public AwattarApiException(String message) {
            super(message);
        }
    }

    /**
     * Constructor for the aWATTar API.
     *
     * @param httpClient the HTTP client to use
     * @param zone the time zone to use
     */
    public AwattarApi(HttpClient httpClient, ZoneId zone, AwattarBridgeConfiguration config) {
        this.zone = zone;
        this.httpClient = httpClient;

        this.gson = new Gson();

        vatFactor = 1 + (config.vatPercent / 100);
        basePrice = config.basePrice;

        if (config.country.equals("DE")) {
            this.url = URL_DE;
        } else if (config.country.equals("AT")) {
            this.url = URL_AT;
        } else {
            throw new IllegalArgumentException("Country code must be 'DE' or 'AT'");
        }
    }

    /**
     * Get the data from the aWATTar API.
     * The data is returned as a sorted set of {@link AwattarPrice} objects.
     * The data is requested from now minus one day to now plus three days.
     *
     * @return the data as a sorted set of {@link AwattarPrice} objects
     * @throws AwattarApiException
     * @throws InterruptedException if the thread is interrupted
     * @throws TimeoutException if the request times out
     * @throws ExecutionException if the request fails
     * @throws EmptyDataResponseException if the response is empty
     */
    public SortedSet<AwattarPrice> getData() throws AwattarApiException {
        try {
            // we start one day in the past to cover ranges that already started yesterday
            ZonedDateTime zdt = LocalDate.now(zone).atStartOfDay(zone).minusDays(1);
            long start = zdt.toInstant().toEpochMilli();
            // Starting from midnight yesterday we add three days so that the range covers
            // the whole next day.
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

            if (content == null) {
                throw new AwattarApiException("@text/error.empty.data");
            } else if (httpStatus == OK_200) {
                SortedSet<AwattarPrice> result = new TreeSet<>(Comparator.comparing(AwattarPrice::timerange));

                AwattarApiData apiData = gson.fromJson(content, AwattarApiData.class);

                for (Datum d : apiData.data) {
                    // the API returns prices in €/MWh, we need €ct/kWh -> divide by 10 (100/1000)
                    double netMarket = d.marketprice / 10.0;
                    double grossMarket = netMarket * vatFactor;
                    double netTotal = netMarket + basePrice;
                    double grossTotal = netTotal * vatFactor;

                    result.add(new AwattarPrice(netMarket, grossMarket, netTotal, grossTotal,
                            new TimeRange(d.startTimestamp, d.endTimestamp)));
                }

                return result;
            } else {
                throw new AwattarApiException("@text/warn.awattar.statuscode" + httpStatus);
            }
        } catch (ExecutionException e) {
            throw new AwattarApiException("@text/error.execution");
        } catch (JsonSyntaxException e) {
            throw new AwattarApiException("@text/error.json");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AwattarApiException("@text/error.interrupted");
        } catch (TimeoutException e) {
            throw new AwattarApiException("@text/error.timeout");
        }
    }
}
