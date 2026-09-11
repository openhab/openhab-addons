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
package org.openhab.binding.gme.internal.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Cache for current and next-day GME PUN prices.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmePriceCache {

    private List<GmePriceEntry> todayPrices = List.of();
    private List<GmePriceEntry> tomorrowPrices = List.of();

    private @Nullable LocalDate todayDate;
    private @Nullable LocalDate tomorrowDate;

    public synchronized List<GmePriceEntry> getTodayPrices() {
        return todayPrices;
    }

    public synchronized List<GmePriceEntry> getTomorrowPrices() {
        return tomorrowPrices;
    }

    public synchronized @Nullable LocalDate getTodayDate() {
        return todayDate;
    }

    public synchronized @Nullable LocalDate getTomorrowDate() {
        return tomorrowDate;
    }

    public synchronized void setToday(LocalDate date, List<GmePriceEntry> prices) {
        todayDate = date;
        todayPrices = List.copyOf(prices);
    }

    public synchronized void setTomorrow(LocalDate date, List<GmePriceEntry> prices) {
        tomorrowDate = date;
        tomorrowPrices = List.copyOf(prices);
    }

    public synchronized void clearTomorrow() {
        tomorrowDate = null;
        tomorrowPrices = List.of();
    }

    public synchronized boolean promoteTomorrowToToday(LocalDate currentDate, ZoneId zoneId) {
        LocalDate cachedTomorrowDate = tomorrowDate;

        if (!currentDate.equals(cachedTomorrowDate)
                || !GmePriceTimeline.isCompleteDailySet(tomorrowPrices, currentDate, zoneId)) {
            return false;
        }

        todayDate = cachedTomorrowDate;
        todayPrices = tomorrowPrices;
        clearTomorrow();

        return true;
    }
}
