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
package org.openhab.binding.awattar.internal;

import static org.openhab.binding.awattar.internal.AwattarUtil.formatDate;
import static org.openhab.binding.awattar.internal.AwattarUtil.getHourFrom;

import java.time.Instant;
import java.time.ZoneId;
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

    public AwattarConsecutiveBestPriceResult(List<AwattarPrice> prices, ZoneId zoneId) {
        super();
        this.zoneId = zoneId;
        StringBuilder hours = new StringBuilder();
        boolean second = false;
        for (AwattarPrice price : prices) {
            priceSum += price.netPrice();
            length++;
            updateStart(price.timerange().start());
            updateEnd(price.timerange().end());
            if (second) {
                hours.append(',');
            }
            hours.append(getHourFrom(price.timerange().start(), zoneId));
            second = true;
        }
        this.hours = hours.toString();
    }

    @Override
    public boolean isActive() {
        return contains(Instant.now().toEpochMilli());
    }

    public boolean contains(long timestamp) {
        return timestamp >= getStart() && timestamp < getEnd();
    }

    public double getPriceSum() {
        return priceSum;
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
