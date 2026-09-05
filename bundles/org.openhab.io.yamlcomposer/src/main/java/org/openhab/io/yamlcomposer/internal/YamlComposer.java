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
package org.openhab.io.yamlcomposer.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.core.EvaluationContext;
import org.openhab.io.yamlcomposer.internal.core.PackageProcessor;
import org.openhab.io.yamlcomposer.internal.core.ProcessingPhase;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.core.Scope;
import org.openhab.io.yamlcomposer.internal.core.SourceLocator;
import org.openhab.io.yamlcomposer.internal.core.TemplateLoader;
import org.openhab.io.yamlcomposer.internal.core.VariableLoader;
import org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder;
import org.openhab.io.yamlcomposer.internal.processors.DefaultProcessor;
import org.openhab.io.yamlcomposer.internal.processors.ElseIfProcessor;
import org.openhab.io.yamlcomposer.internal.processors.ElseProcessor;
import org.openhab.io.yamlcomposer.internal.processors.ForProcessor;
import org.openhab.io.yamlcomposer.internal.processors.FreezeProcessor;
import org.openhab.io.yamlcomposer.internal.processors.IfProcessor;
import org.openhab.io.yamlcomposer.internal.processors.IncludeProcessor;
import org.openhab.io.yamlcomposer.internal.processors.InsertProcessor;
import org.openhab.io.yamlcomposer.internal.processors.SubstitutionProcessor;
import org.openhab.io.yamlcomposer.internal.processors.VarProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

/**
 * The {@link YamlComposer} is a utility class to load YAML files
 * and process them into a final YAML structure that openHAB can use.
 *
 * The following features are supported:
 *
 * <ul>
 * <li>YAML Anchors and aliases.
 * <li>YAML Merge keys (<code>&lt;&lt;</code>) to allow merging of maps with override semantics.
 * <li>Variable substitution and interpolation using <code>${var}</code> syntax.
 * <li>Conditional evaluation using <code>!if</code> tag with simple boolean logic.
 * <li><code>!include</code> tag for including other YAML files.
 * <li><code>!insert</code> tag for inserting template content with local variable substitution.
 * <li>Combining elements using packages.
 * </ul>
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class YamlComposer {
    private static final Logger RAW_LOGGER = LoggerFactory.getLogger(YamlComposer.class);

    private final BufferedLogger logger;

    public static record CacheEntry(byte[] bytes, long mtime) {
    }

    private final Path absolutePath;
    private final Path relativePath;

    private final Scope scope;
    private final Map<Object, @Nullable Object> templates;

    private final List<Path> includeStack;
    private final ConcurrentHashMap<Path, @Nullable CacheEntry> includeCache;

    private final RecursiveTransformer recursiveTransformer;

    /**
     * Constructs a YamlComposer for the given file path and context.
     *
     * @param path the file path for resolving relative includes
     * @param variables initial variable context
     * @param includeStack current include stack for circular reference detection
     * @param includeCallback callback invoked for each included file
     * @param logSession the log session for warning consolidation
     * @param includeCache the cache for included files
     * @throws YamlEngineException if the YAML model cannot be processed
     */
    public YamlComposer(Path path, Map<String, @Nullable Object> variables, List<Path> includeStack,
            Consumer<Path> includeCallback, Consumer<String> envVarCallback, LogSession logSession,
            ConcurrentHashMap<Path, @Nullable CacheEntry> includeCache) {
        this.absolutePath = Objects.requireNonNull(path.toAbsolutePath().normalize());
        this.relativePath = ComposerConfig.configRoot().relativize(absolutePath);
        this.logger = new BufferedLogger(RAW_LOGGER, logSession);
        this.scope = new Scope();
        this.scope.putAll(variables);
        this.includeCache = includeCache;
        this.templates = new HashMap<>();

        List<Path> newIncludeStack = new ArrayList<>(includeStack);
        newIncludeStack.add(absolutePath);
        this.includeStack = newIncludeStack;

        this.recursiveTransformer = new RecursiveTransformer(envVarCallback, absolutePath, logger);

        this.recursiveTransformer.register(new SubstitutionProcessor(envVarCallback, logger));
        this.recursiveTransformer.register(new ForProcessor());
        this.recursiveTransformer.register(new IfProcessor(envVarCallback, logger));
        this.recursiveTransformer.register(new ElseIfProcessor(envVarCallback, logger));
        this.recursiveTransformer.register(new ElseProcessor());
        this.recursiveTransformer.register(new VarProcessor(logger));
        this.recursiveTransformer.register(new DefaultProcessor());
        this.recursiveTransformer.register(new IncludeProcessor(absolutePath.getParent(), newIncludeStack,
                includeCallback, includeCache, envVarCallback, logger));
        this.recursiveTransformer.register(new InsertProcessor(templates, logger));
        this.recursiveTransformer.register(new FreezeProcessor());
    }

    /**
     * Loads a YAML file from the given {@link Path} and processes it through the
     * full composer pipeline.
     * <p>
     * This is the main entry point for the YAML Composer. It reads the file,
     * parses the YAML, and applies all supported composer features.
     * <p>
     * The {@code includeCallback} is invoked for each file referenced via an
     * include directive, allowing the caller to track include usage so it can
     * refresh models when included files change.
     * <p>
     * The returned value is the fully evaluated Java object representation of the
     * YAML document after all the processing steps have been applied.
     *
     * @param path the path to the YAML file to load and process; also used as
     *            the base directory for resolving relative includes
     * @param includeCallback a callback invoked for each included file
     * @return the processed Java object representation of the YAML file
     * @throws IOException if the file cannot be read or if processing fails
     */
    public static @Nullable Object load(Path path, Consumer<Path> includeCallback, Consumer<String> envVarCallback)
            throws IOException {
        // Create a LogSession autocloseable object. It consolidates warnings and duplicates.
        // Upon exit, any warnings will be logged.
        try (LogSession session = new LogSession()) {
            ConcurrentHashMap<Path, @Nullable CacheEntry> cache = new ConcurrentHashMap<>();
            return load(path, includeCallback, envVarCallback, session, cache);
        }
    }

    /**
     * Internal method to allow passing in a LogSession so we can manage it externally in tests.
     *
     * @param path the file path for resolving relative includes
     * @param includeCallback callback invoked for each included file
     * @param logSession the LogSession to use for logging warnings during loading
     * @param includeCache the cache for included files to optimize repeated loads
     * @return the processed Java object representation of the YAML file
     * @throws IOException if there is an error reading or processing the YAML
     */
    static @Nullable Object load(Path path, Consumer<Path> includeCallback, Consumer<String> envVarCallback,
            LogSession logSession, ConcurrentHashMap<Path, @Nullable CacheEntry> includeCache) throws IOException {
        Path absolutePath = path.toAbsolutePath().normalize();
        Path relativePath = ComposerConfig.configRoot().relativize(absolutePath);
        try {
            YamlComposer composer = new YamlComposer(absolutePath, Map.of(), List.of(), includeCallback, envVarCallback,
                    logSession, includeCache);
            Object result = composer.load();

            // Print a summary of warnings before the LogSession outputs all the warnings.
            int totalWarnings = logSession.getTotalWarningCount();
            if (totalWarnings > 0) {
                int unique = logSession.getTrackedWarnings().size();
                String issuesLabel = (unique == 1) ? "unique issue" : "unique issues";
                String warningLabel = (totalWarnings == 1) ? "warning" : "warnings";

                RAW_LOGGER.warn("YAML Composer {}: Preprocessing completed with {} {} ({} {}).", relativePath,
                        totalWarnings, warningLabel, unique, issuesLabel);
            }

            return result;
        } catch (MarkedYamlEngineException e) {
            String errorMsg = e.getMessage();
            Mark mark = e.getProblemMark().orElse(null);
            if (mark != null) {
                String location = "%d:%d".formatted(mark.getLine() + 1, mark.getColumn() + 1);
                String errorClass = e.getClass().getSimpleName();
                errorMsg = "\n%s:%s %s %s".formatted(relativePath, location, errorClass, e.getMessage());
            }
            throw new IOException(errorMsg, e);
        } catch (YamlEngineException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Internal load method that performs the actual loading and processing of the YAML file.
     *
     * @return the processed Java object representation of the YAML file
     * @throws IOException if there is an error reading the YAML file
     * @throws YamlEngineException if there is an error during YAML parsing or processing
     */
    public @Nullable Object load() throws IOException, YamlEngineException {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading file({}): {} with given vars {}", includeStack.size(), absolutePath, scope.flatten());
        }

        EvaluationContext standardContext = new EvaluationContext(scope, ProcessingPhase.STANDARD);

        // Phase 1: Parse YAML and initialize helper objects
        byte[] yamlBytes = readYamlBytes();
        SourceLocator locator = new SourceLocator(yamlBytes);

        // Phase 2: set up initial variables
        VariableLoader variableLoader = new VariableLoader(scope, recursiveTransformer, logger);
        variableLoader.setSpecialVariables();

        // Phase 3: load and parse YAML
        Object yamlObj = ComposerUtils.loadYaml(yamlBytes, relativePath);
        if (!(yamlObj instanceof Map<?, ?>)) {
            yamlObj = recursiveTransformer.transform(yamlObj, standardContext);

            if (!(yamlObj instanceof Map<?, ?>)) {
                return yamlObj;
            }
        }

        Map<?, ?> yamlMap = (Map<?, ?>) yamlObj;

        // Phase 4: extract variables and templates
        Object variablesSection = removeByScalarKey(yamlMap, ComposerConfig.VARIABLES_KEY);
        variableLoader.extractVariables(variablesSection, locator);

        // Extract templates because we want to defer substitutions in the templates
        // until the template is instantiated with !insert, so that the variable context
        // includes any variables passed in the !insert directive.
        Object templatesSection = removeByScalarKey(yamlMap, ComposerConfig.TEMPLATES_KEY);
        new TemplateLoader(logger, relativePath, templates, recursiveTransformer, locator, scope)
                .extractTemplates(templatesSection);

        // Phase 5: extract/remove packages from the main data because we want to
        // inject the package_id into each package context
        @Nullable
        Object packagesObj = removeByScalarKey(yamlMap, ComposerConfig.PACKAGES_KEY);

        // Phase 6: Resolve merge keys and process substitutions, conditionals, includes and inserts
        // in a single pass so that merge keys can merge data produced by includes/inserts.
        yamlMap = (Map<?, ?>) Objects.requireNonNull(recursiveTransformer.transform(yamlMap, standardContext));

        // Phase 7: process and merge packages
        new PackageProcessor(scope, recursiveTransformer, absolutePath, relativePath, logger, locator)
                .mergePackages(yamlMap, packagesObj);

        // Phase 8: process structural placeholders (!default, !replace/!freeze, !remove)
        yamlMap = (Map<?, ?>) Objects.requireNonNull(recursiveTransformer.transform(yamlMap,
                standardContext.withProcessingPhase(ProcessingPhase.FINALIZATION)));

        // Phase 9: final cleanup and optional compiled output
        ComposerUtils.removeHiddenKeys(yamlMap);

        return yamlMap;
    }

    /**
     * Reads the YAML file bytes with caching based on file modification time to optimize repeated loads of the same
     * file. The bytes are cached in {@link #includeCache}.
     *
     * @return the YAML file bytes
     * @throws IOException if there is an error reading the file
     */
    private byte[] readYamlBytes() throws IOException {
        CacheEntry cached = includeCache.get(absolutePath);
        long currentMtime = Files.getLastModifiedTime(absolutePath).toMillis();

        if (cached != null && cached.mtime == currentMtime) {
            return cached.bytes;
        }

        byte[] yamlBytes = Files.readAllBytes(absolutePath);
        includeCache.put(absolutePath, new CacheEntry(yamlBytes, currentMtime));
        return yamlBytes;
    }

    /**
     * Checks if the given file name is an include file based on its extension.
     * An include file ends with .inc.yml or .inc.yaml.
     *
     * @param fileName the name of the file to check
     * @return true if it's an include file, false otherwise
     */
    public static boolean isIncludeFile(String fileName) {
        return fileName.endsWith(".inc.yml") || fileName.endsWith(".inc.yaml");
    }

    /**
     * Checks if the given file name is a Yaml file based on its extension.
     * A Yaml file ends with .yml or .yaml.
     *
     * @param fileName the name of the file to check
     * @return true if it's a Yaml file, false otherwise
     */
    public static boolean isYamlFile(String fileName) {
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    private static @Nullable Object removeByScalarKey(Map<?, ?> map, String key) {
        @SuppressWarnings("unchecked")
        Map<@Nullable Object, @Nullable Object> mutableMap = (Map<@Nullable Object, @Nullable Object>) map;

        for (Iterator<Map.Entry<@Nullable Object, @Nullable Object>> iterator = mutableMap.entrySet()
                .iterator(); iterator.hasNext();) {
            Map.Entry<@Nullable Object, @Nullable Object> entry = iterator.next();
            @Nullable
            Object entryKey = entry.getKey();

            if (entryKey == null) {
                continue;
            }

            boolean keyMatches = switch (entryKey) {
                case String s -> key.equals(s);
                case SubstitutionPlaceholder p -> key.equals(p.value());
                default -> false;
            };

            if (keyMatches) {
                Object value = entry.getValue();
                iterator.remove();
                return value;
            }
        }
        return null;
    }

    public Path getAbsolutePath() {
        return absolutePath;
    }

    public Path getRelativePath() {
        return relativePath;
    }
}
