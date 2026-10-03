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
package org.openhab.binding.awattar.internal.api;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.awattar.internal.AwattarBridgeConfiguration;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.dto.AwattarApiData;
import org.openhab.binding.awattar.internal.dto.AwattarTimeProvider;
import org.openhab.binding.awattar.internal.handler.TimeRange;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link AwattarApi} class is responsible for encapsulating the aWATTar API
 * and providing the data to the bridge.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class AwattarApi extends AbstractMarketPriceApi {
    private static final String URL_DE = "https://api.awattar.de/v1/marketdata";
    private static final String URL_AT = "https://api.awattar.at/v1/marketdata";

    private final String url;

    /**
     * Constructor for the aWATTar API.
     *
     * @param httpClient the HTTP client to use
     * @param timeProvider the time provider to use
     * @param config the bridge configuration
     */
    public AwattarApi(HttpClient httpClient, AwattarTimeProvider timeProvider, AwattarBridgeConfiguration config) {
        super(httpClient, timeProvider, config);

        url = switch (config.country) {
            case "DE" -> URL_DE;
            case "AT" -> URL_AT;
            default -> throw new IllegalArgumentException("Country code must be 'DE' or 'AT'");
        };
    }

    /**
     * Get the data from the aWATTar API.
     * The data is requested from midnight yesterday to midnight the day after tomorrow.
     *
     * @return the data as a sorted set of {@link AwattarPrice} objects
     *
     * @throws MarketPriceApiException if the request fails or the response cannot be parsed
     */
    @Override
    public SortedSet<AwattarPrice> getData() throws MarketPriceApiException {
        // we start one day in the past to cover ranges that already started yesterday
        ZonedDateTime zdt = timeProvider.getZonedDateTimeNow().truncatedTo(ChronoUnit.DAYS).minusDays(1);
        long start = zdt.toInstant().toEpochMilli();

        // Starting from midnight yesterday we add three days so that the range covers the whole next day.
        long end = zdt.plusDays(3).toInstant().toEpochMilli();

        String content = fetch(url + "?start=" + start + "&end=" + end, "warn.awattar.statuscode");

        try {
            @Nullable
            AwattarApiData apiData = gson.fromJson(content, AwattarApiData.class);

            if (apiData == null) {
                throw new MarketPriceApiException("@text/error.empty.data");
            }

            SortedSet<AwattarPrice> result = new TreeSet<>(Comparator.comparing(AwattarPrice::timerange));

            apiData.data.forEach(d -> {
                result.add(toPrice(d.marketprice, new TimeRange(d.startTimestamp, d.endTimestamp)));
            });

            return result;
        } catch (JsonSyntaxException e) {
            throw new MarketPriceApiException("@text/error.json");
        }
    }
}
