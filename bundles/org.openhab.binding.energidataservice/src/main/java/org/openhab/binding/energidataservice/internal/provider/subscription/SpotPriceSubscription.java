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
package org.openhab.binding.energidataservice.internal.provider.subscription;

import java.util.Currency;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class for spot price subscription.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SpotPriceSubscription implements ElectricityPriceSubscription {
    private final String priceArea;
    private final Currency currency;

    private SpotPriceSubscription(String priceArea, Currency currency) {
        this.priceArea = priceArea;
        this.currency = currency;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SpotPriceSubscription other)) {
            return false;
        }

        return this.priceArea.equals(other.priceArea) && this.currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceArea, currency);
    }

    @Override
    public String toString() {
        return "SpotPriceSubscription: PriceArea=" + priceArea + ", Currency=" + currency;
    }

    public String getPriceArea() {
        return priceArea;
    }

    public Currency getCurrency() {
        return currency;
    }

    public static SpotPriceSubscription of(String priceArea, Currency currency) {
        return new SpotPriceSubscription(priceArea, currency);
    }
}
