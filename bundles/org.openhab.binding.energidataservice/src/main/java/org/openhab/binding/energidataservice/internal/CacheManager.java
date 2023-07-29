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

    private Collection<DatahubPricelistRecord> netTariffRecords = new ArrayList<>();
    private Collection<DatahubPricelistRecord> systemTariffRecords = new ArrayList<>();
    private Collection<DatahubPricelistRecord> electricityTaxRecords = new ArrayList<>();
    private Collection<DatahubPricelistRecord> transmissionNetTariffRecords = new ArrayList<>();

    private Map<Instant, BigDecimal> spotPriceMap = new ConcurrentHashMap<>(SPOT_PRICE_MAX_CACHE_SIZE);
    private Map<Instant, BigDecimal> netTariffMap = new ConcurrentHashMap<>(TARIFF_MAX_CACHE_SIZE);
    private Map<Instant, BigDecimal> systemTariffMap = new ConcurrentHashMap<>(TARIFF_MAX_CACHE_SIZE);
    private Map<Instant, BigDecimal> electricityTaxMap = new ConcurrentHashMap<>(TARIFF_MAX_CACHE_SIZE);
    private Map<Instant, BigDecimal> transmissionNetTariffMap = new ConcurrentHashMap<>(TARIFF_MAX_CACHE_SIZE);

    public CacheManager() {
        this(Clock.systemDefaultZone());
    }

    public CacheManager(Clock clock) {
        this.clock = clock.withZone(NORD_POOL_TIMEZONE);
    }

    /**
     * Clear all cached data.
     */
    public void clear() {
        netTariffRecords.clear();
        systemTariffRecords.clear();
        electricityTaxRecords.clear();
        transmissionNetTariffRecords.clear();

        spotPriceMap.clear();
        netTariffMap.clear();
        systemTariffMap.clear();
        electricityTaxMap.clear();
        transmissionNetTariffMap.clear();
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
     * Replace current "raw"/unprocessed net tariff records in cache.
     * Map of hourly tariffs will be updated automatically.
     *
     * @param records to cache
     */
    public void putNetTariffs(Collection<DatahubPricelistRecord> records) {
        putDatahubRecords(netTariffRecords, records);
        updateNetTariffs();
    }

    /**
     * Replace current "raw"/unprocessed system tariff records in cache.
     * Map of hourly tariffs will be updated automatically.
     *
     * @param records to cache
     */
    public void putSystemTariffs(Collection<DatahubPricelistRecord> records) {
        putDatahubRecords(systemTariffRecords, records);
        updateSystemTariffs();
    }

    /**
     * Replace current "raw"/unprocessed electricity tax records in cache.
     * Map of hourly taxes will be updated automatically.
     *
     * @param records to cache
     */
    public void putElectricityTaxes(Collection<DatahubPricelistRecord> records) {
        putDatahubRecords(electricityTaxRecords, records);
        updateElectricityTaxes();
    }

    /**
     * Replace current "raw"/unprocessed transmission net tariff records in cache.
     * Map of hourly tariffs will be updated automatically.
     *
     * @param records to cache
     */
    public void putTransmissionNetTariffs(Collection<DatahubPricelistRecord> records) {
        putDatahubRecords(transmissionNetTariffRecords, records);
        updateTransmissionNetTariffs();
    }

    private void putDatahubRecords(Collection<DatahubPricelistRecord> destination,
            Collection<DatahubPricelistRecord> source) {
        LocalDateTime localHourStart = LocalDateTime.now(clock.withZone(DATAHUB_TIMEZONE))
                .minus(NUMBER_OF_HISTORIC_HOURS, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);

        destination.clear();
        destination.addAll(source.stream().filter(r -> !r.validTo().isBefore(localHourStart)).toList());
    }

    /**
     * Update map of hourly net tariffs from internal cache.
     */
    public void updateNetTariffs() {
        netTariffMap = priceListParser.toHourly(netTariffRecords);
        cleanup();
    }

    /**
     * Update map of system tariffs from internal cache.
     */
    public void updateSystemTariffs() {
        systemTariffMap = priceListParser.toHourly(systemTariffRecords);
        cleanup();
    }

    /**
     * Update map of electricity taxes from internal cache.
     */
    public void updateElectricityTaxes() {
        electricityTaxMap = priceListParser.toHourly(electricityTaxRecords);
        cleanup();
    }

    /**
     * Update map of hourly transmission net tariffs from internal cache.
     */
    public void updateTransmissionNetTariffs() {
        transmissionNetTariffMap = priceListParser.toHourly(transmissionNetTariffRecords);
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
     * Get current net tariff.
     *
     * @return net tariff currently valid
     */
    public @Nullable BigDecimal getNetTariff() {
        return getNetTariff(Instant.now(clock));
    }

    /**
     * Get net tariff valid at provided instant.
     *
     * @param time {@link Instant} for which to get the net tariff
     * @return net tariff at given time or null if not available
     */
    public @Nullable BigDecimal getNetTariff(Instant time) {
        return netTariffMap.get(getHourStart(time));
    }

    /**
     * Get map of all cached net tariffs.
     *
     * @return net tariffs currently available, {@link #NUMBER_OF_HISTORIC_HOURS} back
     */
    public Map<Instant, BigDecimal> getNetTariffs() {
        return new HashMap<Instant, BigDecimal>(netTariffMap);
    }

    /**
     * Get current system tariff.
     *
     * @return system tariff currently valid
     */
    public @Nullable BigDecimal getSystemTariff() {
        return getSystemTariff(Instant.now(clock));
    }

    /**
     * Get system tariff valid at provided instant.
     *
     * @param time {@link Instant} for which to get the system tariff
     * @return system tariff at given time or null if not available
     */
    public @Nullable BigDecimal getSystemTariff(Instant time) {
        return systemTariffMap.get(getHourStart(time));
    }

    /**
     * Get map of all cached system tariffs.
     *
     * @return system tariffs currently available, {@link #NUMBER_OF_HISTORIC_HOURS} back
     */
    public Map<Instant, BigDecimal> getSystemTariffs() {
        return new HashMap<Instant, BigDecimal>(systemTariffMap);
    }

    /**
     * Get current electricity tax.
     *
     * @return electricity tax currently valid
     */
    public @Nullable BigDecimal getElectricityTax() {
        return getElectricityTax(Instant.now(clock));
    }

    /**
     * Get electricity tax valid at provided instant.
     *
     * @param time {@link Instant} for which to get the electricity tax
     * @return electricity tax at given time or null if not available
     */
    public @Nullable BigDecimal getElectricityTax(Instant time) {
        return electricityTaxMap.get(getHourStart(time));
    }

    /**
     * Get map of all cached electricity taxes.
     *
     * @return electricity taxes currently available, {@link #NUMBER_OF_HISTORIC_HOURS} back
     */
    public Map<Instant, BigDecimal> getElectricityTaxes() {
        return new HashMap<Instant, BigDecimal>(electricityTaxMap);
    }

    /**
     * Get current transmission net tariff.
     *
     * @return transmission net tariff currently valid
     */
    public @Nullable BigDecimal getTransmissionNetTariff() {
        return getTransmissionNetTariff(Instant.now(clock));
    }

    /**
     * Get transmission net tariff valid at provided instant.
     *
     * @param time {@link Instant} for which to get the transmission net tariff
     * @return transmission net tariff at given time or null if not available
     */
    public @Nullable BigDecimal getTransmissionNetTariff(Instant time) {
        return transmissionNetTariffMap.get(getHourStart(time));
    }

    /**
     * Get map of all cached transmission net tariffs.
     *
     * @return transmission net tariffs currently available, {@link #NUMBER_OF_HISTORIC_HOURS} back
     */
    public Map<Instant, BigDecimal> getTransmissionNetTariffs() {
        return new HashMap<Instant, BigDecimal>(transmissionNetTariffMap);
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
     * Check if we have "raw" net tariff records cached which are valid tomorrow.
     * 
     * @return true if net tariff records for tomorrow are cached
     */
    public boolean areNetTariffsValidTomorrow() {
        return isValidNextDay(netTariffRecords);
    }

    /**
     * Check if we have "raw" system tariff records cached which are valid tomorrow.
     * 
     * @return true if system tariff records for tomorrow are cached
     */
    public boolean areSystemTariffsValidTomorrow() {
        return isValidNextDay(systemTariffRecords);
    }

    /**
     * Check if we have "raw" electricity tax records cached which are valid tomorrow.
     * 
     * @return true if electricity tax records for tomorrow are cached
     */
    public boolean areElectricityTaxesValidTomorrow() {
        return isValidNextDay(electricityTaxRecords);
    }

    /**
     * Check if we have "raw" transmission net tariff records cached which are valid tomorrow.
     * 
     * @return true if transmission net tariff records for tomorrow are cached
     */
    public boolean areTransmissionNetTariffsValidTomorrow() {
        return isValidNextDay(transmissionNetTariffRecords);
    }

    /**
     * Remove historic prices.
     */
    public void cleanup() {
        Instant firstHourStart = getFirstHourStart();

        spotPriceMap.entrySet().removeIf(entry -> entry.getKey().isBefore(firstHourStart));
        netTariffMap.entrySet().removeIf(entry -> entry.getKey().isBefore(firstHourStart));
        systemTariffMap.entrySet().removeIf(entry -> entry.getKey().isBefore(firstHourStart));
        electricityTaxMap.entrySet().removeIf(entry -> entry.getKey().isBefore(firstHourStart));
        transmissionNetTariffMap.entrySet().removeIf(entry -> entry.getKey().isBefore(firstHourStart));
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
