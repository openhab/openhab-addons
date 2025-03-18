/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.openhab.binding.awattar.internal.api.AwattarApi.AwattarApiException;
import org.openhab.binding.awattar.internal.dto.AwattarTimeProvider;
import org.openhab.core.test.java.JavaTest;

/**
 * The {@link AwattarBridgeHandlerTest} contains tests for the
 * {@link AwattarBridgeHandler}
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class AwattarApiTest extends JavaTest {
    // API Mocks
    private @Mock @NonNullByDefault({}) HttpClient httpClientMock;
    private @Mock @NonNullByDefault({}) Request requestMock;
    private @Mock @NonNullByDefault({}) ContentResponse contentResponseMock;
    private @Mock @NonNullByDefault({}) AwattarBridgeConfiguration config;
    private @Mock @NonNullByDefault({}) AwattarTimeProvider timeProviderMock;

    // sut
    private @NonNullByDefault({}) AwattarApi api;

    @BeforeEach
    public void setUp() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        try (InputStream inputStream = AwattarApiTest.class.getResourceAsStream("api_response.json")) {
            if (inputStream == null) {
                throw new IOException("inputstream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            when(contentResponseMock.getContentAsString()).thenReturn(new String(bytes, StandardCharsets.UTF_8));
        }
        when(contentResponseMock.getStatus()).thenReturn(HttpStatus.OK_200);
        when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(10, TimeUnit.SECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(contentResponseMock);

        ZonedDateTime zdt = Instant.parse("2024-06-15T12:00:00Z").atZone(ZoneId.of("GMT+2"));
        when(timeProviderMock.getZonedDateTimeNow()).thenReturn(zdt);

        config.basePrice = 0.0;
        config.vatPercent = 0.0;
        config.serviceFee = 0.0;
        config.country = "DE";

        api = new AwattarApi(httpClientMock, timeProviderMock, config);
    }

    @Test
    void testDeUrl() throws AwattarApiException {
        api.getData();

        verify(httpClientMock, times(1))
                .newRequest("https://api.awattar.de/v1/marketdata?start=1718316000000&end=1718575200000");
    }

    @Test
    void testAtUrl() throws AwattarApiException {
        config.country = "AT";
        api = new AwattarApi(httpClientMock, timeProviderMock, config);

        api.getData();

        verify(httpClientMock, times(1))
                .newRequest("https://api.awattar.at/v1/marketdata?start=1718316000000&end=1718575200000");
    }

    @Test
    void testInvalidCountry() {
        config.country = "CH";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new AwattarApi(httpClientMock, timeProviderMock, config));
        assertThat(thrown.getMessage(), is("Country code must be 'DE' or 'AT'"));
    }

    @Test
    void testPricesRetrieval() throws AwattarApiException {
        SortedSet<AwattarPrice> prices = api.getData();

        assertThat(prices, hasSize(72));

        Objects.requireNonNull(prices);

        // check if first and last element are correct
        assertThat(prices.first().timerange().start(), is(1718316000000L));
        assertThat(prices.last().timerange().end(), is(1718575200000L));
    }

    @Test
    void testPricesRetrievalEmptyResponse() {
        when(contentResponseMock.getContentAsString()).thenReturn(null);
        when(contentResponseMock.getStatus()).thenReturn(HttpStatus.OK_200);

        AwattarApiException thrown = assertThrows(AwattarApiException.class, () -> api.getData());
        assertThat(thrown.getMessage(), is("@text/error.empty.data"));
    }

    @Test
    void testPricesReturnNot200() {
        when(contentResponseMock.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);

        AwattarApiException thrown = assertThrows(AwattarApiException.class, () -> api.getData());
        assertThat(thrown.getMessage(), is("@text/warn.awattar.statuscode400"));
    }
}
