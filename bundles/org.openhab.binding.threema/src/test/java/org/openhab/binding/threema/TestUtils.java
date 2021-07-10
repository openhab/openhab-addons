package org.openhab.binding.threema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;

import org.openhab.core.config.core.Configuration;

public class TestUtils {
    public static Configuration readThingConfiguration() throws IOException {
        Properties properties = new Properties();
        properties.load(TestUtils.class.getClassLoader().getResourceAsStream("thing.properties"));
        return new Configuration(properties.entrySet().stream().collect(Collectors
                .toMap(e -> String.valueOf(e.getKey()), e -> e.getValue(), (prev, next) -> next, HashMap::new)));
    }
}
