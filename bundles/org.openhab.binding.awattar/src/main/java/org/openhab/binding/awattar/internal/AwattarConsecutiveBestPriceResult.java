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
package org.openhab.binding.awattar.internal;

import static org.openhab.binding.awattar.internal.AwattarUtil.formatDate;
import static org.openhab.binding.awattar.internal.AwattarUtil.getHourFrom;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Stores a consecutive bestprice result
 *
 * @author Wolfgang Klimt - initial contribution
 */
@NonNullByDefault
public class AwattarConsecutiveBestPriceResult extends AwattarBestPriceResult {
    private double priceSum = 0;
    private int length = 0;
    private final String hours;
    private final ZoneId zoneId;

    public AwattarConsecutiveBestPriceResult(List<AwattarPrice> prices, int length, ZoneId zoneId) {
        this.zoneId = zoneId;

        // sort the prices by timerange
        prices.sort(Comparator.comparing(AwattarPrice::timerange));

        // calculate the range with the lowest accumulated price of length hours from the given prices
        double minPrice = Double.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i <= prices.size() - length; i++) {
            double sum = 0;
            for (int j = 0; j < length; j++) {
                sum += prices.get(i + j).netPrice();
            }
            if (sum < minPrice) {
                minPrice = sum;
                minIndex = i;
            }
        }

        // calculate the accumulated price and the range of the best price
        for (int i = 0; i < length; i++) {
            AwattarPrice price = prices.get(minIndex + i);
            priceSum += price.netPrice();
            updateStart(price.timerange().start());
            updateEnd(price.timerange().end());
        }

        // create a list of hours for the best price range
        StringBuilder locHours = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                locHours.append(",");
            }
            locHours.append(getHourFrom(prices.get(minIndex + i).timerange().start(), zoneId));
        }

        this.hours = locHours.toString();
    }

    @Override
    public boolean isActive(Instant pointInTime) {
        return contains(pointInTime.toEpochMilli());
    }

    public boolean contains(long timestamp) {
        return timestamp >= getStart() && timestamp < getEnd();
    }

    @Override
    public String toString() {
        return String.format("{%s, %s, %.2f}", formatDate(getStart(), zoneId), formatDate(getEnd(), zoneId),
                priceSum / length);
    }

    @Override
    public String getHours() {
        return hours;
    }
}
