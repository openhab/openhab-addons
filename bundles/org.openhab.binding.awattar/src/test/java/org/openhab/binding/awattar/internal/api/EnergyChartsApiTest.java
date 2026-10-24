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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.awattar.internal.AwattarBridgeConfiguration;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.dto.AwattarTimeProvider;
import org.openhab.core.test.java.JavaTest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class EnergyChartsApiTest extends JavaTest {
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) Request request;
    private @Mock @NonNullByDefault({}) ContentResponse response;
    private @Mock @NonNullByDefault({}) AwattarBridgeConfiguration config;
    private @Mock @NonNullByDefault({}) AwattarTimeProvider timeProvider;

    private @NonNullByDefault({}) EnergyChartsApi api;

    @BeforeEach
    void setUp() throws Exception {
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(HttpMethod.GET)).thenReturn(request);
        when(request.timeout(10, TimeUnit.SECONDS)).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response.getContentAsString())
                .thenReturn("{\"unix_seconds\":[1718400000,1718400900,1718401800,1718403600],"
                        + "\"price\":[100.0,200.0,300.0,400.0],\"unit\":\"EUR / MWh\"}");
        when(timeProvider.getZonedDateTimeNow())
                .thenReturn(Instant.parse("2024-06-15T12:00:00Z").atZone(ZoneId.of("GMT+2")));
        config.country = "DE";
        config.basePrice = 10.0;
        config.vatPercent = 20.0;
        api = new EnergyChartsApi(httpClient, timeProvider, config);
    }

    @Test
    void testPricesKeepIntervalsFromTimestamps() throws MarketPriceApiException {
        SortedSet<AwattarPrice> prices = api.getData();

        assertThat(prices.size(), is(4));
        AwattarPrice first = prices.first();
        assertThat(first.timerange().start(), is(1718400000000L));
        assertThat(first.timerange().end(), is(1718400900000L));
        assertThat(prices.last().timerange().end(), is(1718405400000L));
        assertThat(first.netPrice(), is(10.0));
        assertThat(first.grossTotal(), is(24.0));
    }

    @Test
    void testGermanBiddingZoneAndDateRange() throws MarketPriceApiException {
        api.getData();

        verify(httpClient).newRequest("https://api.energy-charts.info/price?bzn=DE-LU&start=2024-06-14&end=2024-06-17");
    }

    @Test
    void testAustrianBiddingZone() throws MarketPriceApiException {
        config.country = "AT";
        api = new EnergyChartsApi(httpClient, timeProvider, config);

        api.getData();

        verify(httpClient).newRequest("https://api.energy-charts.info/price?bzn=AT&start=2024-06-14&end=2024-06-17");
    }

    @Test
    void testServiceFeeIsApplied() throws MarketPriceApiException {
        config.serviceFee = 10.0;
        api = new EnergyChartsApi(httpClient, timeProvider, config);

        AwattarPrice first = api.getData().first();

        // 10 ct market + 10 ct base = 20 ct net, +10 % service fee = 22 ct, +20 % VAT
        assertThat(first.netTotal(), is(closeTo(22.0, 1e-9)));
        assertThat(first.grossTotal(), is(closeTo(26.4, 1e-9)));
    }

    @Test
    void testPricesReturnNot200() {
        when(response.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);

        MarketPriceApiException thrown = assertThrows(MarketPriceApiException.class, () -> api.getData());
        assertThat(thrown.getMessage(), is("@text/warn.energycharts.statuscode [\"400\"]"));
    }

    @Test
    void testRejectsUnsupportedCountry() {
        config.country = "CH";

        assertThrows(IllegalArgumentException.class, () -> new EnergyChartsApi(httpClient, timeProvider, config));
    }
}
