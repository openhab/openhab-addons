package org.openhab.persistence.influxdb2;

import org.openhab.persistence.influxdb2.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb2.internal.InfluxDB2Repository;
import org.openhab.persistence.influxdb2.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb2.internal.influx2.Influx2FilterCriteriaQueryCreatorImpl;
import org.openhab.persistence.influxdb2.internal.influx2.InfluxDB2RepositoryImpl;

public class RepositoryFactory {

    public static InfluxDB2Repository createRepository(InfluxDBConfiguration influxDBConfiguration) {
        switch (influxDBConfiguration.getVersion()) {
            case V1:
            case V2:
                return new InfluxDB2RepositoryImpl(influxDBConfiguration);
            default:
                throw new RuntimeException("Not expected version " + influxDBConfiguration.getVersion());
        }
    }

    public static FilterCriteriaQueryCreator createQueryCreator(InfluxDBConfiguration influxDBConfiguration) {
        switch (influxDBConfiguration.getVersion()) {
            case V1:
            case V2:
                return new Influx2FilterCriteriaQueryCreatorImpl();
            default:
                throw new RuntimeException("Not expected version " + influxDBConfiguration.getVersion());
        }
    }
}
