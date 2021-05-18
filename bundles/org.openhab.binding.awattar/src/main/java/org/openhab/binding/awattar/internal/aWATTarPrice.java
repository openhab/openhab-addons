package org.openhab.binding.awattar.internal;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Class to store hourly price data.
 */
public class aWATTarPrice implements Comparable<aWATTarPrice> {
    private final Double price;
    private final long endTimestamp;
    private final long startTimestamp;

    private final int hour;

    public aWATTarPrice(double price, long startTimestamp, long endTimestamp, ZoneId zoneId) {
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

    public String toString() {
        return String.format("(%1$tF %1$tR - %2$tR: %3$.3f)", startTimestamp, endTimestamp, getPrice());
    }

    public int getHour() {
        return hour;
    }

    @Override
    public int compareTo(aWATTarPrice o) {
        return price.compareTo(o.price);
    }

    public boolean isBetween(long start, long end) {
        return startTimestamp >= start && endTimestamp <= end;
    }

    public boolean contains(long timestamp) {
        return startTimestamp <= timestamp && endTimestamp > timestamp;
    }
}
