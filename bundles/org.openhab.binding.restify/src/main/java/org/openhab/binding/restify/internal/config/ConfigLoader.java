package org.openhab.binding.restify.internal.config;

import static java.lang.String.format;
import static java.nio.file.Files.*;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jspecify.annotations.NonNull;
import org.openhab.binding.restify.internal.JsonSchemaValidator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component
public class ConfigLoader implements Serializable {
    public static final String GENERAL_CONFIG_FILE_NAME = "config.json";
    @Serial
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private final JsonSchemaValidator validator;

    @Activate
    public ConfigLoader(@Reference JsonSchemaValidator validator) {
        this.validator = validator;
    }

    public Optional<String> load(Path configDir) throws ConfigParseException {
        logger.debug("Using dir for config loading: {}", configDir.toAbsolutePath());
        chackInvariants(configDir);
        var file = list(configDir).filter(path -> {
            var isConfigFile = path.getFileName().toString().equals(GENERAL_CONFIG_FILE_NAME);
            if (!isConfigFile) {
                logger.debug("Skipping non-json file: {}", path.getFileName());
            }
            return isConfigFile;
        }).findAny();
        if (file.isEmpty()) {
            return Optional.empty();
        }

        logger.debug("Loading general config from file: {}", file.get().getFileName());
        var content = readString(file.get());

        var errors = validator.validateGlobalConfig(content);
        if (!errors.isEmpty()) {
            var msg = errors.stream().map(
                    er -> "\t%s -  %s: %s".formatted(file.get().getFileName(), er.getMessageKey(), er.getMessage()))
                    .collect(Collectors.joining("\n"));
            var errorMessage = "Found (%d) errors:\n%s".formatted(errors.size(), msg);
            logger.error(errorMessage);
            throw new ConfigParseException(errorMessage);
        }

        return Optional.of(content);
    }

    private static void chackInvariants(Path configDir) {
        if (!exists(configDir)) {
            throw new IllegalArgumentException("Config dir does not exist: " + configDir.toAbsolutePath());
        }
        if (!isDirectory(configDir)) {
            throw new IllegalArgumentException(format("%s is not a directory", configDir.toAbsolutePath()));
        }
    }

    private @NonNull Stream<Path> list(Path configPath) throws ConfigParseException {
        try {
            return Files.list(configPath).filter(Files::isRegularFile);
        } catch (IOException e) {
            throw new ConfigParseException("Cannot list config dir: " + configPath.toAbsolutePath(), e);
        }
    }

    private static @NonNull String readString(Path path) throws ConfigParseException {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new ConfigParseException("Cannot read config file: " + path.toAbsolutePath(), e);
        }
    }
}
