package org.openhab.binding.restify.internal.config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record ConfigContent(Optional<String> globalConfig, List<String> endpoints) {
    public static ConfigContent ofGeneralConfig(String generalConfig) {
        return new ConfigContent(Optional.ofNullable(generalConfig), List.of());
    }

    public static ConfigContent ofEndpoint(String endpoint) {
        return new ConfigContent(Optional.empty(), List.of(endpoint));
    }

    public ConfigContent merge(ConfigContent config) {
        if (globalConfig.isPresent() && config.globalConfig.isPresent()) {
            throw new IllegalStateException("Cannot merge with global config");
        }
        return new ConfigContent(globalConfig.or(config::globalConfig),
                Stream.concat(endpoints.stream(), config.endpoints().stream()).toList());
    }
}
