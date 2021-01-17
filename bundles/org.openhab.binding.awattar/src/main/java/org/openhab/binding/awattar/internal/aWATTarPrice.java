package org.openhab.binding.awattar.internal;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jetbrains.annotations.NotNull;

/**
 * Class to store hourly price data.
 */
public class aWATTarPrice implements Comparable<aWATTarPrice> {
    private final Double price;
    private final long endTimestamp;
    private final long startTimestamp;

    private final int hour;

    public aWATTarPrice(double price, long startTimestamp, long endTimestamp) {
        this.price = price;
        this.endTimestamp = endTimestamp;
        this.startTimestamp = startTimestamp;
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(startTimestamp);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
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
    public int compareTo(@NotNull aWATTarPrice o) {
        return price.compareTo(o.price);
    }

    public boolean isBetween(long start, long end) {
        return startTimestamp >= start && endTimestamp <= end;
    }

    public boolean contains(long timestamp) {
        return startTimestamp <= timestamp && endTimestamp > timestamp;
    }
}
