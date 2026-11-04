/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.energycharts.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class to store the price data of one market interval.
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Jan N. Klug - Refactored to record
 *
 * @param price the day-ahead market price in EUR/kWh, without VAT
 * @param timerange the time range of the price
 */
@NonNullByDefault
public record MarketPrice(double price, TimeRange timerange) implements Comparable<MarketPrice> {

    @Override
    public String toString() {
        return String.format("(%1$tF %1$tR - %2$tR: %3$.5f)", timerange.start(), timerange.end(), price);
    }

    @Override
    public int compareTo(MarketPrice o) {
        return timerange.compareTo(o.timerange);
    }
}
