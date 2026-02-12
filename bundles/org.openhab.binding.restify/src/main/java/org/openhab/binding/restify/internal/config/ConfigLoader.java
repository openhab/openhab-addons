package org.openhab.binding.restify.internal.config;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.stream.Stream.concat;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.schema.ValidationMessage;

class ConfigLoader implements Serializable {
    private static final String GENERAL_CONFIG_FILE_NAME = "general.json";
    @Serial
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private final JsonSchemaValidator validator;

    ConfigLoader(JsonSchemaValidator validator) {
        this.validator = validator;
    }

    ConfigContent load(Path configPath) throws UncheckedIOException {
        logger.debug("Using dir for config loading: {}", configPath.toAbsolutePath());
        if (!exists(configPath)) {
            throw new IllegalArgumentException("Config dir does not exist: " + configPath.toAbsolutePath());
        }
        if (!isDirectory(configPath)) {
            throw new IllegalArgumentException(format("%s is not a directory", configPath.toAbsolutePath()));
        }
        record PathAndContent(Path path, String content) {
        }
        record PathConfig(Optional<PathAndContent> globalConfig, List<PathAndContent> responses) {
            PathConfig merge(PathConfig other) {
                if (globalConfig.isPresent() && other.globalConfig.isPresent()) {
                    throw new IllegalStateException("Cannot merge with global config");
                }
                var mergedGlobalConfig = globalConfig.or(() -> other.globalConfig);
                var mergedResponses = concat(responses.stream(), other.responses.stream()).toList();
                return new PathConfig(mergedGlobalConfig, mergedResponses);
            }
        }
        var configContent = list(configPath).filter(path -> {
            var isJson = path.toString().endsWith(".json");
            if (!isJson) {
                logger.debug("Skipping non-json file: {}", path.getFileName());
            }
            return isJson;
        }).map(path -> {
            var content = readString(path);

            if (path.getFileName().toString().equals(GENERAL_CONFIG_FILE_NAME)) {
                logger.debug("Loading general config from file: {}", path.getFileName());
                return new PathConfig(Optional.of(new PathAndContent(path, content)), List.of());
            }
            logger.debug("Loading endpoint config from file: {}", path.getFileName());
            return new PathConfig(empty(), List.of(new PathAndContent(path, content)));
        }).reduce(PathConfig::merge)
                .orElseThrow(() -> new IllegalStateException("No config file found in " + configPath.toAbsolutePath()));
        if (configContent.responses().isEmpty()) {
            logger.warn("No responses found in {}", configPath.toAbsolutePath());
        }
        record ValidationResult(Path path, Collection<ValidationMessage> validationMessages) {
        }

        var globalConfigValidationErrors = configContent.globalConfig()
                .map(pac -> new ValidationResult(pac.path, validator.validateGlobalConfig(pac.content))).stream();

        var endpointConfigValidationErrors = configContent.responses.stream()
                .map(pac -> new ValidationResult(pac.path, validator.validateEndpointConfig(pac.content)));

        var errors = concat(globalConfigValidationErrors, endpointConfigValidationErrors)
                .filter(result -> !result.validationMessages.isEmpty()).toList();

        if (!errors.isEmpty()) {
            var msg = errors.stream().sorted(comparing(a -> a.path))
                    .map(er -> "\t%s: %s".formatted(er.path.getFileName(), er.validationMessages))
                    .collect(Collectors.joining("\n"));
            var errorMessage = "Found (%d) errors:\n%s".formatted(errors.size(), msg);
            logger.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        return new ConfigContent(configContent.globalConfig.map(PathAndContent::content),
                configContent.responses.stream().map(PathAndContent::content).toList());
    }

    private @NonNull Stream<Path> list(Path configPath) {
        try {
            return walk(configPath).filter(Files::isRegularFile);
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
