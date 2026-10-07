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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import org.openhab.binding.energycharts.internal.EnergyChartsConfiguration;
import org.openhab.binding.energycharts.internal.MarketPrice;
import org.openhab.core.test.java.JavaTest;

/**
 * Tests for the {@link EnergyChartsApi}.
 *
 * @author Thomas Leber - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class EnergyChartsApiTest extends JavaTest {
    private static final LocalDate TODAY = LocalDate.of(2024, 6, 15);

    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) Request request;
    private @Mock @NonNullByDefault({}) ContentResponse response;

    private EnergyChartsConfiguration config = new EnergyChartsConfiguration();

    @BeforeEach
    void setUp() throws Exception {
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(HttpMethod.GET)).thenReturn(request);
        when(request.timeout(10, TimeUnit.SECONDS)).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response.getContentAsString())
                .thenReturn("{\"unix_seconds\":[1718400000,1718400900,1718401800,1718403600],"
                        + "\"price\":[100.0,200.0,null,400.0],\"unit\":\"EUR / MWh\"}");
        config.biddingZone = "DE-LU";
        config.basePrice = 10.0;
        config.vatPercent = 20.0;
    }

    @Test
    void testPricesKeepIntervalsFromTimestamps() throws EnergyChartsApiException {
        SortedSet<MarketPrice> prices = new EnergyChartsApi(httpClient, config).getData(TODAY);

        // the interval without a price is dropped
        assertThat(prices.size(), is(3));
        MarketPrice first = prices.first();
        assertThat(first.timerange().start(), is(1718400000000L));
        assertThat(first.timerange().end(), is(1718400900000L));
        assertThat(prices.last().timerange().end(), is(1718405400000L));
        assertThat(first.netPrice(), is(10.0));
        assertThat(first.grossTotal(), is(24.0));
    }

    @Test
    void testBiddingZoneAndDateRange() throws EnergyChartsApiException {
        config.biddingZone = "AT";
        new EnergyChartsApi(httpClient, config).getData(TODAY);

        verify(httpClient).newRequest("https://api.energy-charts.info/price?bzn=AT&start=2024-06-15&end=2024-06-17");
    }

    @Test
    void testServiceFeeIsApplied() throws EnergyChartsApiException {
        config.serviceFee = 10.0;

        MarketPrice first = new EnergyChartsApi(httpClient, config).getData(TODAY).first();

        // 10 ct market + 10 ct base = 20 ct net, +10 % service fee = 22 ct, +20 % VAT
        assertThat(first.netTotal(), is(closeTo(22.0, 1e-9)));
        assertThat(first.grossTotal(), is(closeTo(26.4, 1e-9)));
    }

    @Test
    void testPricesReturnNot200() {
        when(response.getStatus()).thenReturn(HttpStatus.TOO_MANY_REQUESTS_429);

        EnergyChartsApiException thrown = assertThrows(EnergyChartsApiException.class,
                () -> new EnergyChartsApi(httpClient, config).getData(TODAY));
        assertThat(thrown.getMessage(), is("@text/error.statuscode [\"429\"]"));
    }

    @Test
    void testInvalidJson() {
        when(response.getContentAsString()).thenReturn("{\"unix_seconds\":[1718400000],\"price\":[]}");

        assertThrows(EnergyChartsApiException.class, () -> new EnergyChartsApi(httpClient, config).getData(TODAY));
    }
}
