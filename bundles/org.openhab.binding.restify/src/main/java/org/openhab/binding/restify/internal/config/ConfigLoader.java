package org.openhab.binding.restify.internal.config;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static org.openhab.binding.restify.internal.RestifyBindingConstants.BINDING_ID;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {
    private static final String GENERAL_CONFIG_FILE_NAME = "general.json";
    private final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private final Path configPath;

    public ConfigLoader() {
        this(Path.of(OpenHAB.getConfigFolder()).resolve(BINDING_ID));
    }

    ConfigLoader(Path configPath) {
        logger.debug("Using config path: {}", configPath.toAbsolutePath());
        this.configPath = configPath;
        if (!exists(configPath)) {
            throw new IllegalArgumentException("Config dir does not exist: " + configPath.toAbsolutePath());
        }
        if (!isDirectory(configPath)) {
            throw new IllegalArgumentException(format("%s is not a directory", configPath.toAbsolutePath()));
        }
    }

    public ConfigContent load() {
        return list().parallelStream().filter(path -> {
            var isJson = path.toString().endsWith(".json");
            if (!isJson) {
                logger.debug("Skipping non-json file: {}", path.getFileName());
            }
            return isJson;
        }).map(path -> {
            var content = readString(path);

            if (path.getFileName().toString().equals(GENERAL_CONFIG_FILE_NAME)) {
                logger.debug("Loading general config from file: {}", path.getFileName());
                return ConfigContent.ofGeneralConfig(content);
            }
            logger.debug("Loading endpoint config from file: {}", path.getFileName());
            return ConfigContent.ofEndpoint(content);
        }).reduce(ConfigContent::merge)
                .orElseThrow(() -> new IllegalStateException("No config file found in " + configPath.toAbsolutePath()));
    }

    private @NonNull List<Path> list() {
        try {
            return Files.list(configPath).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot list config dir: " + configPath.toAbsolutePath(), e);
        }
    }

    private static @NonNull String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read config file: " + path.toAbsolutePath(), e);
        }
    }
}
