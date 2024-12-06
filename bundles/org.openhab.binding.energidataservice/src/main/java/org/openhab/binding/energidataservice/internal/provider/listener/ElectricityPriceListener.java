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
package org.openhab.binding.energidataservice.internal.provider.listener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.DatahubTariff;

/**
 * {@link ElectricityPriceListener} provides an interface for receiving
 * electricity price data.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface ElectricityPriceListener extends SubscriptionListener {
    /**
     * New day-ahead spot prices are available.
     */
    void onDayAheadAvailable();

    /**
     * Current spot price has been updated (every hour).
     *
     * @param price New current price
     * @param currency Currency
     */
    void onCurrentSpotPrice(@Nullable BigDecimal price, Currency currency);

    /**
     * Spot prices have changed.
     * Can be used to update time series.
     *
     * @param spotPrices New spot prices
     * @param currency Currency
     */
    void onSpotPrices(Map<Instant, BigDecimal> spotPrices, Currency currency);

    /**
     * Current tariff has been updated.
     *
     * @param datahubTariff Tariff type that was updated
     * @param tariff New tariff
     */
    void onCurrentTariff(DatahubTariff datahubTariff, @Nullable BigDecimal tariff);

    /**
     * Tariffs have changed.
     * Can be used to update time series.
     *
     * @param datahubTariff Tariff type that was updated
     * @param tariffs New tariffs
     */
    void onTariffs(DatahubTariff datahubTariff, Map<Instant, BigDecimal> tariffs);
}
