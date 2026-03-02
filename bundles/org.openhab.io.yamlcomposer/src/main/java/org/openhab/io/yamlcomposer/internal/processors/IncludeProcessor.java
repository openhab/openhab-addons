/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.yamlcomposer.internal.processors;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.ComposerConfig;
import org.openhab.io.yamlcomposer.internal.YamlComposer;
import org.openhab.io.yamlcomposer.internal.YamlComposer.CacheEntry;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.placeholders.IncludePlaceholder;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

/**
 * Processor for resolving {@link IncludePlaceholder} in YAML models
 * into the included file content.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class IncludeProcessor implements PlaceholderProcessor<IncludePlaceholder> {

    private final BufferedLogger logger;
    private final Path basePath;
    private final Set<Path> includeStack;
    private final Consumer<Path> includeCallback;
    private final ConcurrentHashMap<Path, @Nullable CacheEntry> includeCache;

    /**
     * Creates a new IncludeProcessor.
     *
     * @param basePath the base path to resolve relative includes against
     * @param includeStack the stack of currently included files
     * @param includeCallback the callback to invoke when a file is included
     * @param includeCache the cache for storing included file contents
     * @param logger the logger to use for logging messages
     */
    public IncludeProcessor(Path basePath, Set<Path> includeStack, Consumer<Path> includeCallback,
            ConcurrentHashMap<Path, @Nullable CacheEntry> includeCache, BufferedLogger logger) {
        this.logger = logger;
        this.basePath = basePath.toAbsolutePath().normalize();
        this.includeStack = includeStack;
        this.includeCallback = includeCallback;
        this.includeCache = includeCache;
    }

    @Override
    public Class<IncludePlaceholder> getPlaceholderType() {
        return IncludePlaceholder.class;
    }

    /**
     * Resolves an {@link IncludePlaceholder}, loads the referenced file
     * (recursively following any nested includes), and returns the fully expanded
     * content.
     *
     * @param placeholder the placeholder to process
     * @param recursiveTransformer the recursive transformer to use
     * @return the expanded content
     */
    @Override
    public @Nullable Object process(IncludePlaceholder placeholder, RecursiveTransformer recursiveTransformer) {

        FragmentUtils.Parameters params = FragmentUtils.parseParameters(placeholder, "file");
        if (params == null) {
            logger.warn("{} Failed to process !include: invalid parameters", placeholder.sourceLocation());
            return null;
        }

        Object fileNameObj = params.name();
        if (fileNameObj == null || String.valueOf(fileNameObj).isBlank()) {
            logger.warn("{} Failed to process !include: missing 'file' parameter", placeholder.sourceLocation());
            return null;
        }

        Path includeFilePath = resolvePathPlaceholder(String.valueOf(fileNameObj), placeholder.sourceLocation());
        Path includePathRelative = ComposerConfig.configRoot().relativize(includeFilePath);

        // Handle parameters and variables
        Map<String, @Nullable Object> includeVariables = new HashMap<>(recursiveTransformer.getVariables());
        includeVariables.putAll(params.varsMap()); // params override current variables

        try {
            YamlComposer includeComposer = new YamlComposer(includeFilePath, includeVariables, includeStack,
                    includeCallback, logger.getLogSession(), includeCache);
            includeCallback.accept(includeFilePath);
            return includeComposer.load();

        } catch (YamlEngineException | IOException e) {
            logIncludeError(placeholder, String.valueOf(fileNameObj), includePathRelative, e);
            return null;
        }
    }

    /**
     * Resolves the file path from the !include statement, handling any placeholders
     * and providing detailed logging if resolution fails.
     *
     * Placeholders starting with '@' are resolved relative to OPENHAB_CONF,
     * while those starting with '$' are resolved relative to the top-level directory
     * of the base path. The top-level directory is the directory one level below
     * OPENHAB_CONF that contains the base path.
     * For example, if the base path is "OPENHAB_CONF/yaml/rooms/kitchen.yaml",
     * the top-level directory would be "OPENHAB_CONF/yaml".
     *
     *
     * This allows for flexible referencing of files
     * within the configuration structure.
     *
     * If resolution fails, it falls back to resolving relative to the base path's sibling directory.
     *
     * @param includeFileName the file name from the !include statement, which may contain placeholders
     * @param sourceLocation the source location for logging purposes
     * @return the resolved Path to the included file, attempting to resolve placeholders if present
     */
    private Path resolvePathPlaceholder(String includeFileName, String sourceLocation) {
        char prefix = includeFileName.charAt(0);
        Path root = switch (prefix) {
            case '@' -> ComposerConfig.configRoot();
            case '$' -> ComposerConfig.sourceRoot();
            default -> null;
        };

        if (root != null) {
            String cleanedPath = includeFileName.replaceFirst("^[@$]/*", "");
            return root.resolve(cleanedPath);
        }

        return basePath.resolve(includeFileName);
    }

    private void logIncludeError(IncludePlaceholder p, String name, Path path, Exception e) {
        Mark mark = (e instanceof MarkedYamlEngineException me) ? me.getProblemMark().orElse(null) : null;
        String location = (mark != null) ? "%d:%d".formatted(mark.getLine() + 1, mark.getColumn() + 1) : "";

        String msg = (e instanceof IOException ioe) ? getFriendlyMessage(ioe) : e.getMessage();

        logger.warn("{} Failed to process !include '{}'\n{}:{} {}", p.sourceLocation(), name, path, location, msg);
    }

    private static @Nullable String getFriendlyMessage(Exception e) {
        if (e instanceof FileSystemException fse) {
            // If the JDK provided a specific reason string, use it
            if (fse.getReason() != null && !fse.getReason().isBlank()) {
                return fse.getReason();
            }

            // Otherwise, use our "Sentence case" class name logic
            String name = e.getClass().getSimpleName().replace("Exception", "");
            String spaced = name.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase().trim();
            return (spaced.isBlank()) ? "File system error"
                    : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
        }
        return e.getMessage();
    }
}
