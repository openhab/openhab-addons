package org.openhab.persistence.influxdb2.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.persistence.influxdb2.InfluxRow;

@NonNullByDefault
public interface InfluxDB2Repository {
    boolean isConnected();

    boolean connect();

    void disconnect();

    boolean checkConnectionStatus();

    Map<String, Integer> getStoredItemsCount();

    List<InfluxRow> query(String query);

    void write(InfluxPoint influxPoint);
}
