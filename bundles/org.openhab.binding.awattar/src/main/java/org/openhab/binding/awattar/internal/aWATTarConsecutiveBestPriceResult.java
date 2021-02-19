package org.openhab.binding.awattar.internal;

import static org.openhab.binding.awattar.internal.aWATTarUtil.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public class aWATTarConsecutiveBestPriceResult extends aWATTarBestPriceResult {

    private double priceSum = 0;
    private int length = 0;
    private String hours;
    private ZoneId zoneId;

    public aWATTarConsecutiveBestPriceResult(List<aWATTarPrice> prices, ZoneId zoneId) {
        super();
        this.zoneId = zoneId;
        StringBuilder hours = new StringBuilder();
        boolean second = false;
        for (aWATTarPrice price : prices) {
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
