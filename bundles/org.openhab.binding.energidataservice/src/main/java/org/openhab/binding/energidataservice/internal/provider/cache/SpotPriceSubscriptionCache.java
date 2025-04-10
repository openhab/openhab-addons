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
package org.openhab.binding.energidataservice.internal.provider.cache;

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.energidataservice.internal.api.dto.ElspotpriceRecord;
import org.openhab.binding.energidataservice.internal.provider.subscription.SpotPriceSubscription;

/**
 * Spot price specific {@link ElectricityPriceSubscriptionCache} implementation.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SpotPriceSubscriptionCache extends ElectricityPriceSubscriptionCache<ElspotpriceRecord[]> {

    private final SpotPriceSubscription subscription;

    public SpotPriceSubscriptionCache(SpotPriceSubscription subscription) {
        this(subscription, Clock.systemDefaultZone(), Duration.ofHours(1));
    }

    public SpotPriceSubscriptionCache(SpotPriceSubscription subscription, Clock clock, Duration priceDuration) {
        super(clock, priceDuration);
        this.subscription = subscription;
    }

    /**
     * Convert and cache the supplied {@link ElspotpriceRecord}s.
     * 
     * @param records The records as received from Energi Data Service.
     * @return true if the provided records resulted in any cache changes
     */
    @Override
    public boolean put(ElspotpriceRecord[] records) {
        boolean isDKK = CURRENCY_DKK.equals(subscription.getCurrency());
        boolean anyChanges = false;
        int oldSize = priceMap.size();
        for (ElspotpriceRecord record : records) {
            BigDecimal newValue = (isDKK ? record.spotPriceDKK() : record.spotPriceEUR())
                    .divide(BigDecimal.valueOf(1000));
            BigDecimal oldValue = priceMap.put(record.hour(), newValue);
            if (oldValue == null || newValue.compareTo(oldValue) != 0) {
                anyChanges = true;
            }
        }
        anyChanges |= oldSize != priceMap.size();
        flush();

        return anyChanges;
    }

    /**
     * Check if all current spot prices are cached taking into consideration that next day's spot prices
     * should be available at 13:00 CET.
     *
     * @return true if spot prices are fully cached
     */
    public boolean arePricesFullyCached() {
        LocalDate date = LocalDate.now(clock);

        if (LocalTime.now(clock).isAfter(DAILY_REFRESH_TIME_CET)) {
            date = date.plusDays(1);
        }

        Instant end = ZonedDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT, NORD_POOL_TIMEZONE).minus(priceDuration)
                .toInstant();

        return arePricesCached(end);
    }
}
