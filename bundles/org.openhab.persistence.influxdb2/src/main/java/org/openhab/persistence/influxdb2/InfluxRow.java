package org.openhab.persistence.influxdb2;

import java.time.Instant;

public class InfluxRow {
    private final String itemName;
    private final Instant time;
    private final Object value;

    public InfluxRow(Instant time, String itemName, Object value) {
        this.time = time;
        this.itemName = itemName;
        this.value = value;
    }

    public Instant getTime() {
        return time;
    }

    public String getItemName() {
        return itemName;
    }

    public Object getValue() {
        return value;
    }
}
