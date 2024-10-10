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
package org.openhab.binding.energidataservice.internal.provider.cache;

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.energidataservice.internal.PriceListParser;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecord;

/**
 * Datahub price (tariff) specific {@link ElectricityPriceSubscriptionCache} implementation.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DatahubPriceSubscriptionCache
        extends ElectricityPriceSubscriptionCache<Collection<DatahubPricelistRecord>> {

    public static final int MAX_CACHE_SIZE = 24 * 2 + NUMBER_OF_HISTORIC_HOURS;

    private final PriceListParser priceListParser = new PriceListParser();

    private Collection<DatahubPricelistRecord> datahubRecords = new CopyOnWriteArrayList<>();

    public DatahubPriceSubscriptionCache() {
        this(Clock.systemDefaultZone());
    }

    public DatahubPriceSubscriptionCache(Clock clock) {
        super(clock, MAX_CACHE_SIZE);
    }

    /**
     * Replace current "raw"/unprocessed tariff records in cache.
     * Map of hourly tariffs will be updated automatically.
     * 
     * @param records The records as received from Energi Data Service.
     */
    @Override
    public boolean put(Collection<DatahubPricelistRecord> records) {
        LocalDateTime localHourStart = LocalDateTime.now(clock.withZone(DATAHUB_TIMEZONE))
                .minus(NUMBER_OF_HISTORIC_HOURS, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);

        List<DatahubPricelistRecord> newRecords = records.stream().filter(r -> !r.validTo().isBefore(localHourStart))
                .toList();
        boolean recordsAreEqual = datahubRecords.containsAll(newRecords) && newRecords.containsAll(datahubRecords);
        datahubRecords = new CopyOnWriteArrayList<>(newRecords);
        update();

        return !recordsAreEqual;
    }

    /**
     * Update map of hourly tariffs from internal cache.
     */
    public void update() {
        priceMap.putAll(priceListParser.toHourly(datahubRecords));
        flush();
    }

    /**
     * Check if we have "raw" tariff records cached which are valid tomorrow.
     * 
     * @return true if tariff records for tomorrow are cached
     */
    public boolean areTariffsValidTomorrow() {
        LocalDateTime localHourStart = LocalDateTime.now(clock.withZone(DATAHUB_TIMEZONE))
                .truncatedTo(ChronoUnit.HOURS);
        LocalDateTime localMidnight = localHourStart.plusDays(1).truncatedTo(ChronoUnit.DAYS);

        return datahubRecords.stream().anyMatch(r -> r.validTo().isAfter(localMidnight));
    }
}
