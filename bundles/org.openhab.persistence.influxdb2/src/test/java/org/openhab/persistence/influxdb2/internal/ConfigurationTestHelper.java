package org.openhab.persistence.influxdb2.internal;

import static org.openhab.persistence.influxdb2.internal.InfluxDBConfiguration.*;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationTestHelper {

    public static Map<String, Object> createValidConfigurationParameters() {
        Map<String, Object> config = new HashMap<>();
        config.put(URL_PARAM, "http://localhost:9999");
        config.put(TOKEN_PARAM, "sampletoken");
        config.put(ORGANIZATION_PARAM, "openhab");
        config.put(BUCKET_PARAM, "default");
        return config;
    }

    public static Map<String, Object> createInvalidConfigurationParameters() {
        Map<String, Object> config = createValidConfigurationParameters();
        config.remove(TOKEN_PARAM);
        return config;
    }

}
