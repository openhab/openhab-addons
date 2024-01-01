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
package org.openhab.binding.energidataservice.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecords;
import org.openhab.binding.energidataservice.internal.api.serialization.LocalDateTimeDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests for {@link PriceListParser}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class PriceListParserTest {

    private Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();

    private <T> T getObjectFromJson(String filename, Class<T> clazz) throws IOException {
        try (InputStream inputStream = PriceListParserTest.class.getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("Input stream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            return Objects.requireNonNull(gson.fromJson(json, clazz));
        }
    }

    @Test
    void toHourlyNoChanges() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2023-01-23T12:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistN1.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList());

        assertThat(tariffMap.size(), is(60));
        assertThat(tariffMap.get(Instant.parse("2023-01-23T15:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-23T16:00:00Z")), is(equalTo(new BigDecimal("1.05619"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-24T15:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-24T16:00:00Z")), is(equalTo(new BigDecimal("1.05619"))));
    }

    @Test
    void toHourlyNewTariffTomorrowWhenSummertime() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2023-03-31T12:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistN1.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList());

        assertThat(tariffMap.size(), is(60));
        assertThat(tariffMap.get(Instant.parse("2023-03-31T14:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
        assertThat(tariffMap.get(Instant.parse("2023-03-31T15:00:00Z")), is(equalTo(new BigDecimal("1.05619"))));
        assertThat(tariffMap.get(Instant.parse("2023-04-01T14:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
        assertThat(tariffMap.get(Instant.parse("2023-04-01T15:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
    }

    @Test
    void toHourlyNewTariffAtMidnight() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2022-12-31T12:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistN1.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList(), "CD");

        assertThat(tariffMap.size(), is(60));
        assertThat(tariffMap.get(Instant.parse("2022-12-31T22:00:00Z")), is(equalTo(new BigDecimal("0.407717"))));
        assertThat(tariffMap.get(Instant.parse("2022-12-31T23:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-01T00:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
    }

    @Test
    void toHourlyDiscount() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2022-12-31T12:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistN1.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList(),
                "CD R");

        assertThat(tariffMap.size(), is(60));
        assertThat(tariffMap.get(Instant.parse("2022-12-31T22:00:00Z")), is(equalTo(new BigDecimal("-0.407717"))));
        assertThat(tariffMap.get(Instant.parse("2022-12-31T23:00:00Z")), is(equalTo(new BigDecimal("0.0"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-01T00:00:00Z")), is(equalTo(new BigDecimal("0.0"))));
    }

    @Test
    void toHourlyTariffAndDiscountIsSum() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2022-11-30T15:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistN1.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList());

        assertThat(tariffMap.size(), is(57));
        assertThat(tariffMap.get(Instant.parse("2022-11-30T15:00:00Z")), is(equalTo(new BigDecimal("0.387517"))));
        assertThat(tariffMap.get(Instant.parse("2022-11-30T16:00:00Z")), is(equalTo(new BigDecimal("0.973404"))));
    }

    @Test
    void toHourlyTariffAndDiscountIsFree() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2022-12-31T12:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistN1.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList());

        assertThat(tariffMap.size(), is(60));
        assertThat(tariffMap.get(Instant.parse("2022-12-31T16:00:00Z")), is(equalTo(new BigDecimal("0.000000"))));
        assertThat(tariffMap.get(Instant.parse("2022-12-31T22:00:00Z")), is(equalTo(new BigDecimal("0.000000"))));
        assertThat(tariffMap.get(Instant.parse("2022-12-31T23:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-01T00:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
    }

    @Test
    void toHourlyFixedTariff() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2022-12-31T23:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistNordEnergi.json",
                DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList());

        assertThat(tariffMap.size(), is(25)); // No records in dataset before 2023-01-01
        for (Instant i = Instant.parse("2022-12-31T23:00:00Z"); i
                .isBefore(Instant.parse("2023-01-02T00:00:00Z")); i = i.plus(1, ChronoUnit.HOURS)) {
            assertThat(tariffMap.get(i), is(equalTo(new BigDecimal("0.245"))));
        }
    }

    @Test
    void toHourlyDailyTariffs() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2023-01-28T04:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistTrefor.json",
                DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList());

        assertThat(tariffMap.size(), is(68));
        assertThat(tariffMap.get(Instant.parse("2023-01-28T04:00:00Z")), is(equalTo(new BigDecimal("0.2581"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-28T05:00:00Z")), is(equalTo(new BigDecimal("0.7742"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-28T16:00:00Z")), is(equalTo(new BigDecimal("2.3227"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-28T20:00:00Z")), is(equalTo(new BigDecimal("0.7742"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-28T23:00:00Z")), is(equalTo(new BigDecimal("0.2581"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-29T05:00:00Z")), is(equalTo(new BigDecimal("0.7742"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-29T16:00:00Z")), is(equalTo(new BigDecimal("2.3227"))));
        assertThat(tariffMap.get(Instant.parse("2023-01-29T20:00:00Z")), is(equalTo(new BigDecimal("0.7742"))));
    }

    @Test
    void toHourlySystemTariff() throws IOException {
        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(Instant.parse("2023-06-30T21:00:00Z"), EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords records = getObjectFromJson("DatahubPricelistElectricityTax.json",
                DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> tariffMap = priceListParser.toHourly(Arrays.stream(records.records()).toList());

        assertThat(tariffMap.size(), is(51));
        assertThat(tariffMap.get(Instant.parse("2023-06-30T21:00:00Z")), is(equalTo(new BigDecimal("0.008"))));
        assertThat(tariffMap.get(Instant.parse("2023-06-30T22:00:00Z")), is(equalTo(new BigDecimal("0.697"))));
    }
}
