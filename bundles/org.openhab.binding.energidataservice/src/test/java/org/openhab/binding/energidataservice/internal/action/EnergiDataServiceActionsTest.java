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
package org.openhab.binding.energidataservice.internal.action;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants;
import org.openhab.binding.energidataservice.internal.PriceListParser;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecords;
import org.openhab.binding.energidataservice.internal.api.serialization.InstantDeserializer;
import org.openhab.binding.energidataservice.internal.api.serialization.LocalDateTimeDeserializer;
import org.openhab.binding.energidataservice.internal.handler.EnergiDataServiceHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Tests for {@link EnergiDataServiceActions}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EnergiDataServiceActionsTest {

    private @NonNullByDefault({}) @Mock EnergiDataServiceHandler handler;
    private EnergiDataServiceActions actions = new EnergiDataServiceActions();

    private Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer()).create();

    private record SpotPrice(Instant hourStart, BigDecimal spotPrice) {
    }

    private <T> T getObjectFromJson(String filename, Class<T> clazz) throws IOException {
        try (InputStream inputStream = EnergiDataServiceActionsTest.class.getResourceAsStream(filename)) {
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

    @BeforeEach
    void setUp() {
        final Logger logger = (Logger) LoggerFactory.getLogger(EnergiDataServiceActions.class);
        logger.setLevel(Level.OFF);

        actions = new EnergiDataServiceActions();
    }

    @Test
    void getPricesSpotPrice() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices("SpotPrice");
        assertThat(actual.size(), is(35));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("0.992840027"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("1.267680054"))));
    }

    @Test
    void getPricesGridTariff() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices("GridTariff");
        assertThat(actual.size(), is(60));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("0.432225"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("1.05619"))));
    }

    @Test
    void getPricesSystemTariff() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices("SystemTariff");
        assertThat(actual.size(), is(60));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("0.054"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("0.054"))));
    }

    @Test
    void getPricesElectricityTax() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices("ElectricityTax");
        assertThat(actual.size(), is(60));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("0.008"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("0.008"))));
    }

    @Test
    void getPricesTransmissionGridTariff() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices("TransmissionGridTariff");
        assertThat(actual.size(), is(60));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("0.058"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("0.058"))));
    }

    @Test
    void getPricesSpotPriceGridTariff() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices("SpotPrice,GridTariff");
        assertThat(actual.size(), is(35));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("1.425065027"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("2.323870054"))));
    }

    @Test
    void getPricesSpotPriceGridTariffElectricityTax() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices("SpotPrice,GridTariff,ElectricityTax");
        assertThat(actual.size(), is(35));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("1.433065027"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("2.331870054"))));
    }

    @Test
    void getPricesTotal() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices();
        assertThat(actual.size(), is(35));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("1.545065027"))));
        assertThat(actual.get(Instant.parse("2023-02-04T15:00:00Z")), is(equalTo(new BigDecimal("1.708765039"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("2.443870054"))));
    }

    @Test
    void getPricesTotalFullElectricityTax() throws IOException {
        mockCommonDatasets(actions, "SpotPrices20231003.json");

        Map<Instant, BigDecimal> actual = actions.getPrices();
        assertThat(actual.size(), is(4));
        assertThat(actual.get(Instant.parse("2023-10-03T20:00:00Z")), is(equalTo(new BigDecimal("0.829059999"))));
    }

    @Test
    void getPricesTotalReducedElectricityTax() throws IOException {
        mockCommonDatasets(actions, "SpotPrices20231003.json", true);

        Map<Instant, BigDecimal> actual = actions.getPrices();
        assertThat(actual.size(), is(4));
        assertThat(actual.get(Instant.parse("2023-10-03T20:00:00Z")), is(equalTo(new BigDecimal("0.140059999"))));
    }

    @Test
    void getPricesTotalAllComponents() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions
                .getPrices("spotprice,gridtariff,systemtariff,electricitytax,transmissiongridtariff");
        assertThat(actual.size(), is(35));
        assertThat(actual.get(Instant.parse("2023-02-04T12:00:00Z")), is(equalTo(new BigDecimal("1.545065027"))));
        assertThat(actual.get(Instant.parse("2023-02-04T15:00:00Z")), is(equalTo(new BigDecimal("1.708765039"))));
        assertThat(actual.get(Instant.parse("2023-02-04T16:00:00Z")), is(equalTo(new BigDecimal("2.443870054"))));
    }

    @Test
    void getPricesInvalidPriceComponent() throws IOException {
        mockCommonDatasets(actions);

        Map<Instant, BigDecimal> actual = actions.getPrices("spotprice,gridtarif");
        assertThat(actual.size(), is(0));
    }

    @Test
    void getPricesMixedCurrencies() throws IOException {
        mockCommonDatasets(actions);
        when(handler.getCurrency()).thenReturn(EnergiDataServiceBindingConstants.CURRENCY_EUR);

        Map<Instant, BigDecimal> actual = actions.getPrices("spotprice,gridtariff");
        assertThat(actual.size(), is(0));
    }

    /**
     * Calculate price in period 15:30-16:30 (UTC) with consumption 150 W and the following total prices:
     * 15:00:00: 1.708765039
     * 16:00:00: 2.443870054
     *
     * Result = (1.708765039 / 2) + (2.443870054 / 2) * 0.150
     *
     * @throws IOException
     */
    @Test
    void calculatePriceSimple() throws IOException {
        mockCommonDatasets(actions);

        @Nullable
        BigDecimal actual = actions.calculatePrice(Instant.parse("2023-02-04T15:30:00Z"),
                Instant.parse("2023-02-04T16:30:00Z"), new QuantityType<>(150, Units.WATT));
        assertThat(actual, is(equalTo(new BigDecimal("0.311447631975000000")))); // 0.3114476319750
    }

    /**
     * Calculate price in period 15:00-17:00 (UTC) with consumption 1000 W and the following total prices:
     * 15:00:00: 1.708765039
     * 16:00:00: 2.443870054
     *
     * Result = 1.708765039 + 2.443870054
     *
     * @throws IOException
     */
    @Test
    void calculatePriceFullHours() throws IOException {
        mockCommonDatasets(actions);

        @Nullable
        BigDecimal actual = actions.calculatePrice(Instant.parse("2023-02-04T15:00:00Z"),
                Instant.parse("2023-02-04T17:00:00Z"), new QuantityType<>(1, Units.KILOVAR));
        assertThat(actual, is(equalTo(new BigDecimal("4.152635093000000000")))); // 4.152635093
    }

    @Test
    void calculatePriceOutOfRangeStart() throws IOException {
        mockCommonDatasets(actions);

        @Nullable
        BigDecimal actual = actions.calculatePrice(Instant.parse("2023-02-03T23:59:00Z"),
                Instant.parse("2023-02-04T12:30:00Z"), new QuantityType<>(1000, Units.WATT));
        assertThat(actual, is(nullValue()));
    }

    @Test
    void calculatePriceOutOfRangeEnd() throws IOException {
        mockCommonDatasets(actions);

        @Nullable
        BigDecimal actual = actions.calculatePrice(Instant.parse("2023-02-05T22:00:00Z"),
                Instant.parse("2023-02-05T23:01:00Z"), new QuantityType<>(1000, Units.WATT));
        assertThat(actual, is(nullValue()));
    }

    /**
     * Miele G 6895 SCVi XXL K2O dishwasher, program ECO.
     *
     * @throws IOException
     */
    @Test
    void calculateCheapestPeriodWithPowerDishwasher() throws IOException {
        mockCommonDatasets(actions, "SpotPrices20230205.json");

        List<Duration> durations = List.of(Duration.ofMinutes(37), Duration.ofMinutes(8), Duration.ofMinutes(4),
                Duration.ofMinutes(2), Duration.ofMinutes(4), Duration.ofMinutes(36), Duration.ofMinutes(41),
                Duration.ofMinutes(104));
        List<QuantityType<Power>> consumptions = List.of(QuantityType.valueOf(162.162162, Units.WATT),
                QuantityType.valueOf(750, Units.WATT), QuantityType.valueOf(1500, Units.WATT),
                QuantityType.valueOf(3000, Units.WATT), QuantityType.valueOf(1500, Units.WATT),
                QuantityType.valueOf(166.666666, Units.WATT), QuantityType.valueOf(146.341463, Units.WATT),
                QuantityType.valueOf(0, Units.WATT));
        Map<String, Object> actual = actions.calculateCheapestPeriod(Instant.parse("2023-02-05T16:00:00Z"),
                Instant.parse("2023-02-06T06:00:00Z"), durations, consumptions);
        assertThat(actual.get("LowestPrice"), is(equalTo(new BigDecimal("1.024218147103792520"))));
        assertThat(actual.get("CheapestStart"), is(equalTo(Instant.parse("2023-02-05T19:23:00Z"))));
        assertThat(actual.get("HighestPrice"), is(equalTo(new BigDecimal("1.530671034828983196"))));
        assertThat(actual.get("MostExpensiveStart"), is(equalTo(Instant.parse("2023-02-05T16:00:00Z"))));
    }

    @Test
    void calculateCheapestPeriodWithPowerOutOfRange() throws IOException {
        mockCommonDatasets(actions);

        List<Duration> durations = List.of(Duration.ofMinutes(61));
        List<QuantityType<Power>> consumptions = List.of(QuantityType.valueOf(1000, Units.WATT));
        Map<String, Object> actual = actions.calculateCheapestPeriod(Instant.parse("2023-02-04T12:00:00Z"),
                Instant.parse("2023-02-06T00:01:00Z"), durations, consumptions);
        assertThat(actual.size(), is(equalTo(0)));
    }

    /**
     * Miele G 6895 SCVi XXL K2O dishwasher, program ECO.
     *
     * @throws IOException
     */
    @Test
    void calculateCheapestPeriodWithEnergyDishwasher() throws IOException {
        mockCommonDatasets(actions, "SpotPrices20230205.json");

        List<Duration> durations = List.of(Duration.ofMinutes(37), Duration.ofMinutes(8), Duration.ofMinutes(4),
                Duration.ofMinutes(2), Duration.ofMinutes(4), Duration.ofMinutes(36), Duration.ofMinutes(41));
        Map<String, Object> actual = actions.calculateCheapestPeriod(Instant.parse("2023-02-05T16:00:00Z"),
                Instant.parse("2023-02-06T06:00:00Z"), Duration.ofMinutes(236), durations,
                QuantityType.valueOf(0.1, Units.KILOWATT_HOUR));
        assertThat(actual.get("LowestPrice"), is(equalTo(new BigDecimal("1.024218147103792520"))));
        assertThat(actual.get("CheapestStart"), is(equalTo(Instant.parse("2023-02-05T19:23:00Z"))));
        assertThat(actual.get("HighestPrice"), is(equalTo(new BigDecimal("1.530671034828983196"))));
        assertThat(actual.get("MostExpensiveStart"), is(equalTo(Instant.parse("2023-02-05T16:00:00Z"))));
    }

    @Test
    void calculateCheapestPeriodWithEnergyTotalDurationIsExactSum() throws IOException {
        mockCommonDatasets(actions, "SpotPrices20230205.json");

        List<Duration> durations = List.of(Duration.ofMinutes(60), Duration.ofMinutes(60));
        Map<String, Object> actual = actions.calculateCheapestPeriod(Instant.parse("2023-02-05T16:00:00Z"),
                Instant.parse("2023-02-06T06:00:00Z"), Duration.ofMinutes(120), durations,
                QuantityType.valueOf(100, Units.WATT_HOUR));
        assertThat(actual.get("LowestPrice"), is(equalTo(new BigDecimal("0.293540001200000000"))));
        assertThat(actual.get("CheapestStart"), is(equalTo(Instant.parse("2023-02-05T19:00:00Z"))));
    }

    @Test
    void calculateCheapestPeriodWithEnergyTotalDurationInvalid() throws IOException {
        mockCommonDatasets(actions, "SpotPrices20230205.json");

        List<Duration> durations = List.of(Duration.ofMinutes(60), Duration.ofMinutes(60));
        Map<String, Object> actual = actions.calculateCheapestPeriod(Instant.parse("2023-02-05T16:00:00Z"),
                Instant.parse("2023-02-06T06:00:00Z"), Duration.ofMinutes(119), durations,
                QuantityType.valueOf(0.1, Units.KILOWATT_HOUR));
        assertThat(actual.size(), is(equalTo(0)));
    }

    /**
     * Like {@link #calculateCheapestPeriodWithEnergyDishwasher} but with unknown consumption/timetable map.
     *
     * @throws IOException
     */
    @Test
    void calculateCheapestPeriodAssumingLinearUnknownConsumption() throws IOException {
        mockCommonDatasets(actions, "SpotPrices20230205.json");

        Map<String, Object> actual = actions.calculateCheapestPeriod(Instant.parse("2023-02-05T16:00:00Z"),
                Instant.parse("2023-02-06T06:00:00Z"), Duration.ofMinutes(236));
        assertThat(actual.get("LowestPrice"), is(nullValue()));
        assertThat(actual.get("CheapestStart"), is(equalTo(Instant.parse("2023-02-05T19:00:00Z"))));
        assertThat(actual.get("HighestPrice"), is(nullValue()));
        assertThat(actual.get("MostExpensiveStart"), is(equalTo(Instant.parse("2023-02-05T16:00:00Z"))));
    }

    @Test
    void calculateCheapestPeriodForLinearPowerUsage() throws IOException {
        mockCommonDatasets(actions);

        Map<String, Object> actual = actions.calculateCheapestPeriod(Instant.parse("2023-02-04T12:00:00Z"),
                Instant.parse("2023-02-05T23:00:00Z"), Duration.ofMinutes(61), QuantityType.valueOf(1000, Units.WATT));
        assertThat(actual.get("LowestPrice"), is(equalTo(new BigDecimal("1.323990859575000000"))));
        assertThat(actual.get("CheapestStart"), is(equalTo(Instant.parse("2023-02-05T12:00:00Z"))));
        assertThat(actual.get("HighestPrice"), is(equalTo(new BigDecimal("2.589061780353348000"))));
        assertThat(actual.get("MostExpensiveStart"), is(equalTo(Instant.parse("2023-02-04T17:00:00Z"))));
    }

    private void mockCommonDatasets(EnergiDataServiceActions actions) throws IOException {
        mockCommonDatasets(actions, "SpotPrices20230204.json");
    }

    private void mockCommonDatasets(EnergiDataServiceActions actions, String spotPricesFilename) throws IOException {
        mockCommonDatasets(actions, spotPricesFilename, false);
    }

    private void mockCommonDatasets(EnergiDataServiceActions actions, String spotPricesFilename,
            boolean isReducedElectricityTax) throws IOException {
        SpotPrice[] spotPriceRecords = getObjectFromJson(spotPricesFilename, SpotPrice[].class);
        Map<Instant, BigDecimal> spotPrices = Arrays.stream(spotPriceRecords)
                .collect(Collectors.toMap(SpotPrice::hourStart, SpotPrice::spotPrice));

        PriceListParser priceListParser = new PriceListParser(
                Clock.fixed(spotPriceRecords[0].hourStart, EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
        DatahubPricelistRecords datahubRecords = getObjectFromJson("GridTariffs.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> gridTariffs = priceListParser
                .toHourly(Arrays.stream(datahubRecords.records()).toList());
        datahubRecords = getObjectFromJson("SystemTariffs.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> systemTariffs = priceListParser
                .toHourly(Arrays.stream(datahubRecords.records()).toList());
        datahubRecords = getObjectFromJson("ElectricityTaxes.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> electricityTaxes = priceListParser
                .toHourly(Arrays.stream(datahubRecords.records()).toList());
        datahubRecords = getObjectFromJson("ReducedElectricityTaxes.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> reducedElectricityTaxes = priceListParser
                .toHourly(Arrays.stream(datahubRecords.records()).toList());
        datahubRecords = getObjectFromJson("TransmissionGridTariffs.json", DatahubPricelistRecords.class);
        Map<Instant, BigDecimal> transmissionGridTariffs = priceListParser
                .toHourly(Arrays.stream(datahubRecords.records()).toList());

        when(handler.getSpotPrices()).thenReturn(spotPrices);
        when(handler.getTariffs(DatahubTariff.GRID_TARIFF)).thenReturn(gridTariffs);
        when(handler.getTariffs(DatahubTariff.SYSTEM_TARIFF)).thenReturn(systemTariffs);
        when(handler.getTariffs(DatahubTariff.TRANSMISSION_GRID_TARIFF)).thenReturn(transmissionGridTariffs);
        when(handler.getTariffs(DatahubTariff.ELECTRICITY_TAX)).thenReturn(electricityTaxes);
        when(handler.getTariffs(DatahubTariff.REDUCED_ELECTRICITY_TAX)).thenReturn(reducedElectricityTaxes);
        when(handler.getCurrency()).thenReturn(EnergiDataServiceBindingConstants.CURRENCY_DKK);
        when(handler.isReducedElectricityTax()).thenReturn(isReducedElectricityTax);
        actions.setThingHandler(handler);
    }
}
