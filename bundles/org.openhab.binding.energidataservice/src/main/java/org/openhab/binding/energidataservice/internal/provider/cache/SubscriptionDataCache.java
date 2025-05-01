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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Generic interface for caching prices related to subscription.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface SubscriptionDataCache<R> {

    /**
     * Add records to cache.
     *
     * @param records Records to add to cache
     * @return true if the provided records resulted in any cache changes
     */
    boolean put(R records);

    /**
     * Get cached prices.
     *
     * @return Map of cached key/value pairs
     */
    Map<Instant, BigDecimal> get();

    /**
     * Get cached price for specific {@link Instant}.
     *
     * @param time Get cached value at this time
     * @return Price at given time
     */
    @Nullable
    BigDecimal get(Instant time);

    /**
     * Flush expired cached values.
     */
    void flush();

    /**
     * Check if all required historic values are cached, considering
     * {@link ElectricityPriceSubscriptionCache#NUMBER_OF_HISTORIC_HOURS}.
     *
     * @return true if historic values are fully cached
     */
    boolean areHistoricPricesCached();
}
