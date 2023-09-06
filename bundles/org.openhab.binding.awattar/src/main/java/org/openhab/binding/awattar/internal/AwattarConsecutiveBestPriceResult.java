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
    private String hours;
    private ZoneId zoneId;

    public AwattarConsecutiveBestPriceResult(List<AwattarPrice> prices, ZoneId zoneId) {
        super();
        this.zoneId = zoneId;
        StringBuilder hours = new StringBuilder();
        boolean second = false;
        for (AwattarPrice price : prices) {
            priceSum += price.getPrice();
            length++;
            updateStart(price.getStartTimestamp());
            updateEnd(price.getEndTimestamp());
            if (second) {
                hours.append(',');
            }
            hours.append(getHourFrom(price.getStartTimestamp(), zoneId));
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

    public String toString() {
        return String.format("{%s, %s, %.2f}", formatDate(getStart(), zoneId), formatDate(getEnd(), zoneId),
                priceSum / length);
    }

    public String getHours() {
        return hours;
    }
}
