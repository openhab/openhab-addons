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

/**
 * Parses results from {@link org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecords}
 * into map of hourly tariffs.
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
        Instant firstHourStart = Instant.now(clock).minus(CacheManager.NUMBER_OF_HISTORIC_HOURS, ChronoUnit.HOURS)
                .truncatedTo(ChronoUnit.HOURS);
        Instant lastHourStart = Instant.now(clock).truncatedTo(ChronoUnit.HOURS).plus(2, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS);

        return toHourly(records, firstHourStart, lastHourStart);
    }

    public Map<Instant, BigDecimal> toHourly(Collection<DatahubPricelistRecord> records, Instant firstHourStart,
            Instant lastHourStart) {
        Map<Instant, BigDecimal> totalMap = new ConcurrentHashMap<>(CacheManager.TARIFF_MAX_CACHE_SIZE);
        records.stream().map(record -> record.chargeTypeCode()).distinct().forEach(chargeTypeCode -> {
            Map<Instant, BigDecimal> currentMap = toHourly(records, chargeTypeCode, firstHourStart, lastHourStart);
            for (Entry<Instant, BigDecimal> current : currentMap.entrySet()) {
                BigDecimal total = totalMap.get(current.getKey());
                if (total == null) {
                    total = BigDecimal.ZERO;
                }
                totalMap.put(current.getKey(), total.add(current.getValue()));
            }
        });

        return totalMap;
    }

    private Map<Instant, BigDecimal> toHourly(Collection<DatahubPricelistRecord> records, String chargeTypeCode,
            Instant firstHourStart, Instant lastHourStart) {
        Map<Instant, BigDecimal> tariffMap = new ConcurrentHashMap<>(CacheManager.TARIFF_MAX_CACHE_SIZE);

        LocalDateTime previousValidFrom = LocalDateTime.MAX;
        LocalDateTime previousValidTo = LocalDateTime.MIN;
        Map<LocalTime, BigDecimal> tariffs = Map.of();
        for (Instant hourStart = firstHourStart; hourStart
                .isBefore(lastHourStart); hourStart = hourStart.plus(1, ChronoUnit.HOURS)) {
            LocalDateTime localDateTime = hourStart.atZone(EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE)
                    .toLocalDateTime();
            if (localDateTime.compareTo(previousValidFrom) < 0 || localDateTime.compareTo(previousValidTo) >= 0) {
                DatahubPricelistRecord priceList = getTariffs(records, localDateTime, chargeTypeCode);
                if (priceList != null) {
                    tariffs = priceList.getTariffMap();
                    previousValidFrom = priceList.validFrom();
                    previousValidTo = priceList.validTo();
                } else {
                    tariffs = Map.of();
                }
            }

            LocalTime localTime = LocalTime
                    .of(hourStart.atZone(EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE).getHour(), 0);
            BigDecimal tariff = tariffs.get(localTime);
            if (tariff != null) {
                tariffMap.put(hourStart, tariff);
            }
        }

        return tariffMap;
    }

    private @Nullable DatahubPricelistRecord getTariffs(Collection<DatahubPricelistRecord> records,
            LocalDateTime localDateTime, String chargeTypeCode) {
        return records.stream()
                .filter(record -> localDateTime.compareTo(record.validFrom()) >= 0
                        && localDateTime.compareTo(record.validTo()) < 0
                        && record.chargeTypeCode().equals(chargeTypeCode))
                .findFirst().orElse(null);
    }
}
