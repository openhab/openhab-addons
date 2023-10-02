/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecord;
import org.openhab.binding.energidataservice.internal.api.dto.ElspotpriceRecord;

/**
 * The {@link CacheManager} is responsible for maintaining a cache of received
 * data from Energi Data Service.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class CacheManager {

    public static final int NUMBER_OF_HISTORIC_HOURS = 24;
    public static final int SPOT_PRICE_MAX_CACHE_SIZE = 24 + 11 + NUMBER_OF_HISTORIC_HOURS;
    public static final int TARIFF_MAX_CACHE_SIZE = 24 * 2 + NUMBER_OF_HISTORIC_HOURS;

    private final Clock clock;
    private final PriceListParser priceListParser = new PriceListParser();

    private Map<DatahubTariff, Collection<DatahubPricelistRecord>> datahubRecordsMap = new HashMap<>();

    private Map<Instant, BigDecimal> spotPriceMap = new ConcurrentHashMap<>(SPOT_PRICE_MAX_CACHE_SIZE);

    private Map<DatahubTariff, Map<Instant, BigDecimal>> tariffsMap = new ConcurrentHashMap<>();

    public CacheManager() {
        this(Clock.systemDefaultZone());
    }

    public CacheManager(Clock clock) {
        this.clock = clock.withZone(NORD_POOL_TIMEZONE);

        for (DatahubTariff tariff : DatahubTariff.values()) {
            datahubRecordsMap.put(tariff, new ArrayList<>());
            tariffsMap.put(tariff, new ConcurrentHashMap<>(TARIFF_MAX_CACHE_SIZE));
        }
    }

    /**
     * Clear all cached data.
     */
    public void clear() {
        datahubRecordsMap.clear();
        spotPriceMap.clear();
        tariffsMap.clear();
    }

    /**
     * Convert and cache the supplied {@link ElspotpriceRecord}s.
     * 
     * @param records The records as received from Energi Data Service.
     * @param currency The currency in which the records were requested.
     */
    public void putSpotPrices(ElspotpriceRecord[] records, Currency currency) {
        boolean isDKK = EnergiDataServiceBindingConstants.CURRENCY_DKK.equals(currency);
        for (ElspotpriceRecord record : records) {
            spotPriceMap.put(record.hour(),
                    (isDKK ? record.spotPriceDKK() : record.spotPriceEUR()).divide(BigDecimal.valueOf(1000)));
        }
        cleanup();
    }

    /**
     * Replace current "raw"/unprocessed tariff records in cache.
     * Map of hourly tariffs will be updated automatically.
     *
     * @param records to cache
     */
    public void putTariffs(DatahubTariff datahubTariff, Collection<DatahubPricelistRecord> records) {
        Collection<DatahubPricelistRecord> datahubRecords = datahubRecordsMap.get(datahubTariff);
        if (datahubRecords == null) {
            throw new IllegalStateException("Datahub records not initialized");
        }
        putDatahubRecords(datahubRecords, records);
        updateTariffs(datahubTariff);
    }

    private void putDatahubRecords(Collection<DatahubPricelistRecord> destination,
            Collection<DatahubPricelistRecord> source) {
        LocalDateTime localHourStart = LocalDateTime.now(clock.withZone(DATAHUB_TIMEZONE))
                .minus(NUMBER_OF_HISTORIC_HOURS, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);

        destination.clear();
        destination.addAll(source.stream().filter(r -> !r.validTo().isBefore(localHourStart)).toList());
    }

    /**
     * Update map of hourly tariffs from internal cache.
     */
    public void updateTariffs(DatahubTariff datahubTariff) {
        Collection<DatahubPricelistRecord> datahubRecords = datahubRecordsMap.get(datahubTariff);
        if (datahubRecords == null) {
            throw new IllegalStateException("Datahub records not initialized");
        }
        tariffsMap.put(datahubTariff, priceListParser.toHourly(datahubRecords));
        cleanup();
    }

    /**
     * Get current spot price.
     *
     * @return spot price currently valid
     */
    public @Nullable BigDecimal getSpotPrice() {
        return getSpotPrice(Instant.now(clock));
    }

    /**
     * Get spot price valid at provided instant.
     *
     * @param time {@link Instant} for which to get the spot price
     * @return spot price at given time or null if not available
     */
    public @Nullable BigDecimal getSpotPrice(Instant time) {
        return spotPriceMap.get(getHourStart(time));
    }

    /**
     * Get map of all cached spot prices.
     *
     * @return spot prices currently available, {@link #NUMBER_OF_HISTORIC_HOURS} back
     */
    public Map<Instant, BigDecimal> getSpotPrices() {
        return new HashMap<Instant, BigDecimal>(spotPriceMap);
    }

    /**
     * Get current tariff.
     *
     * @return tariff currently valid
     */
    public @Nullable BigDecimal getTariff(DatahubTariff datahubTariff) {
        return getTariff(datahubTariff, Instant.now(clock));
    }

    /**
     * Get tariff valid at provided instant.
     *
     * @param time {@link Instant} for which to get the tariff
     * @return tariff at given time or null if not available
     */
    public @Nullable BigDecimal getTariff(DatahubTariff datahubTariff, Instant time) {
        Map<Instant, BigDecimal> tariffs = tariffsMap.get(datahubTariff);
        if (tariffs == null) {
            throw new IllegalStateException("Tariffs not initialized");
        }
        return tariffs.get(getHourStart(time));
    }

    /**
     * Get map of all cached tariffs.
     *
     * @return tariffs currently available, {@link #NUMBER_OF_HISTORIC_HOURS} back
     */
    public Map<Instant, BigDecimal> getTariffs(DatahubTariff datahubTariff) {
        Map<Instant, BigDecimal> tariffs = tariffsMap.get(datahubTariff);
        if (tariffs == null) {
            throw new IllegalStateException("Tariffs not initialized");
        }
        return new HashMap<Instant, BigDecimal>(tariffs);
    }

    /**
     * Get number of future spot prices including current hour.
     * 
     * @return number of future spot prices
     */
    public long getNumberOfFutureSpotPrices() {
        Instant currentHourStart = getCurrentHourStart();

        return spotPriceMap.entrySet().stream().filter(p -> !p.getKey().isBefore(currentHourStart)).count();
    }

    /**
     * Check if historic spot prices ({@link #NUMBER_OF_HISTORIC_HOURS}) are cached.
     * 
     * @return true if historic spot prices are cached
     */
    public boolean areHistoricSpotPricesCached() {
        return arePricesCached(spotPriceMap, getCurrentHourStart().minus(1, ChronoUnit.HOURS));
    }

    /**
     * Check if all current spot prices are cached taking into consideration that next day's spot prices
     * should be available at 13:00 CET.
     *
     * @return true if spot prices are fully cached
     */
    public boolean areSpotPricesFullyCached() {
        Instant end = ZonedDateTime.of(LocalDate.now(clock), LocalTime.of(23, 0), NORD_POOL_TIMEZONE).toInstant();
        LocalTime now = LocalTime.now(clock);
        if (now.isAfter(DAILY_REFRESH_TIME_CET)) {
            end = end.plus(24, ChronoUnit.HOURS);
        }

        return arePricesCached(spotPriceMap, end);
    }

    private boolean arePricesCached(Map<Instant, BigDecimal> priceMap, Instant end) {
        for (Instant hourStart = getFirstHourStart(); hourStart.compareTo(end) <= 0; hourStart = hourStart.plus(1,
                ChronoUnit.HOURS)) {
            if (priceMap.get(hourStart) == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if we have "raw" tariff records cached which are valid tomorrow.
     * 
     * @return true if tariff records for tomorrow are cached
     */
    public boolean areTariffsValidTomorrow(DatahubTariff datahubTariff) {
        Collection<DatahubPricelistRecord> datahubRecords = datahubRecordsMap.get(datahubTariff);
        if (datahubRecords == null) {
            throw new IllegalStateException("Datahub records not initialized");
        }
        return isValidNextDay(datahubRecords);
    }

    /**
     * Remove historic prices.
     */
    public void cleanup() {
        Instant firstHourStart = getFirstHourStart();

        spotPriceMap.entrySet().removeIf(entry -> entry.getKey().isBefore(firstHourStart));

        for (Map<Instant, BigDecimal> tariffs : tariffsMap.values()) {
            tariffs.entrySet().removeIf(entry -> entry.getKey().isBefore(firstHourStart));
        }
    }

    private boolean isValidNextDay(Collection<DatahubPricelistRecord> records) {
        LocalDateTime localHourStart = LocalDateTime.now(EnergiDataServiceBindingConstants.DATAHUB_TIMEZONE)
                .truncatedTo(ChronoUnit.HOURS);
        LocalDateTime localMidnight = localHourStart.plusDays(1).truncatedTo(ChronoUnit.DAYS);

        return records.stream().anyMatch(r -> r.validTo().isAfter(localMidnight));
    }

    private Instant getCurrentHourStart() {
        return getHourStart(Instant.now(clock));
    }

    private Instant getFirstHourStart() {
        return getHourStart(Instant.now(clock).minus(NUMBER_OF_HISTORIC_HOURS, ChronoUnit.HOURS));
    }

    private Instant getHourStart(Instant instant) {
        return instant.truncatedTo(ChronoUnit.HOURS);
    }
}
