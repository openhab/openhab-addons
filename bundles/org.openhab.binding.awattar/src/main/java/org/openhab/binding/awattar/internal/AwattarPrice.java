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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class to store hourly price data.
 *
 * @author Wolfgang Klimt - initial contribution
 */
@NonNullByDefault
public class AwattarPrice implements Comparable<AwattarPrice> {
    private final Double price;
    private final long endTimestamp;
    private final long startTimestamp;

    private final int hour;

    public AwattarPrice(double price, long startTimestamp, long endTimestamp, ZoneId zoneId) {
        this.price = price;
        this.endTimestamp = endTimestamp;
        this.startTimestamp = startTimestamp;
        this.hour = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), zoneId).getHour();
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format("(%1$tF %1$tR - %2$tR: %3$.3f)", startTimestamp, endTimestamp, getPrice());
    }

    public int getHour() {
        return hour;
    }

    @Override
    public int compareTo(AwattarPrice o) {
        return price.compareTo(o.price);
    }

    public boolean isBetween(long start, long end) {
        return startTimestamp >= start && endTimestamp <= end;
    }

    public boolean contains(long timestamp) {
        return startTimestamp <= timestamp && endTimestamp > timestamp;
    }
}
