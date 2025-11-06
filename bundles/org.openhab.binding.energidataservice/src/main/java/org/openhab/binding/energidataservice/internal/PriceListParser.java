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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecord;
import org.openhab.binding.energidataservice.internal.provider.cache.DatahubPriceSubscriptionCache;
import org.openhab.binding.energidataservice.internal.provider.cache.ElectricityPriceSubscriptionCache;

/**
 * Parses results from {@link org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecords}
 * into a map of tariffs with configurable time resolution (e.g. hourly or quarter-hourly).
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class PriceListParser {

    private final Clock clock;

    public PriceListParser() {
        this(Clock.system(EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE));
    }

    public PriceListParser(Clock clock) {
        this.clock = clock;
    }

    public Map<Instant, BigDecimal> toHourly(Collection<DatahubPricelistRecord> records) {
        return toResolution(records, Duration.ofHours(1));
    }

    public Map<Instant, BigDecimal> toHourly(Collection<DatahubPricelistRecord> records, Instant firstStart,
            Instant lastStart) {
        return toResolution(records, firstStart, lastStart, Duration.ofHours(1));
    }

    private Map<Instant, BigDecimal> toResolution(Collection<DatahubPricelistRecord> records, Duration resolution) {
        Instant firstStart = truncateToResolution(
                Instant.now(clock).minus(ElectricityPriceSubscriptionCache.NUMBER_OF_HISTORIC_HOURS, ChronoUnit.HOURS),
                resolution);
        Instant lastStart = Instant.now(clock).truncatedTo(ChronoUnit.HOURS).plus(2, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS);

        return toResolution(records, firstStart, lastStart, resolution);
    }

    private Map<Instant, BigDecimal> toResolution(Collection<DatahubPricelistRecord> records, Instant firstStart,
            Instant lastStart, Duration resolution) {
        Map<Instant, BigDecimal> totalMap = new ConcurrentHashMap<>(DatahubPriceSubscriptionCache.MAX_CACHE_SIZE);
        records.stream().map(record -> record.chargeTypeCode()).distinct().forEach(chargeTypeCode -> {
            Map<Instant, BigDecimal> currentMap = toResolution(records, chargeTypeCode, firstStart, lastStart,
                    resolution);
            for (Entry<Instant, BigDecimal> current : currentMap.entrySet()) {
                totalMap.merge(current.getKey(), current.getValue(), BigDecimal::add);
            }
        });

        return totalMap;
    }

    private Map<Instant, BigDecimal> toResolution(Collection<DatahubPricelistRecord> records, String chargeTypeCode,
            Instant firstStart, Instant lastStart, Duration resolution) {
        Map<Instant, BigDecimal> tariffMap = new ConcurrentHashMap<>(DatahubPriceSubscriptionCache.MAX_CACHE_SIZE);

        LocalDateTime previousValidFrom = LocalDateTime.MAX;
        LocalDateTime previousValidTo = LocalDateTime.MIN;
        Map<LocalTime, BigDecimal> tariffs = Map.of();
        for (Instant start = firstStart; start.isBefore(lastStart); start = start.plus(resolution)) {
            LocalDateTime localDateTime = start.atZone(EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE)
                    .toLocalDateTime();
            if (localDateTime.isBefore(previousValidFrom) || !localDateTime.isBefore(previousValidTo)) {
                DatahubPricelistRecord priceList = getTariffs(records, localDateTime, chargeTypeCode);
                if (priceList != null) {
                    tariffs = priceList.getTariffMap();
                    previousValidFrom = priceList.validFrom();
                    previousValidTo = priceList.validTo();
                } else {
                    tariffs = Map.of();
                }
            }

            LocalTime localTime = truncateToResolution(
                    start.atZone(EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE).toLocalTime(), resolution);
            BigDecimal tariff = tariffs.get(localTime);
            if (tariff != null) {
                tariffMap.put(start, tariff);
            }
        }

        return tariffMap;
    }

    private Instant truncateToResolution(Instant instant, Duration resolution) {
        long seconds = resolution.getSeconds();
        long truncated = (instant.getEpochSecond() / seconds) * seconds;
        return Instant.ofEpochSecond(truncated);
    }

    private LocalTime truncateToResolution(LocalTime time, Duration resolution) {
        long seconds = resolution.getSeconds();
        long truncated = (time.toSecondOfDay() / seconds) * seconds;
        return LocalTime.ofSecondOfDay(truncated);
    }

    private @Nullable DatahubPricelistRecord getTariffs(Collection<DatahubPricelistRecord> records,
            LocalDateTime localDateTime, String chargeTypeCode) {
        return records.stream()
                .filter(record -> !localDateTime.isBefore(record.validFrom())
                        && localDateTime.isBefore(record.validTo()) && record.chargeTypeCode().equals(chargeTypeCode))
                .findFirst().orElse(null);
    }
}
