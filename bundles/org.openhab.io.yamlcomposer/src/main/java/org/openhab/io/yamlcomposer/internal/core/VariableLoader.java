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
package org.openhab.io.yamlcomposer.internal.core;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.ComposerConfig;
import org.openhab.io.yamlcomposer.internal.placeholders.IncludePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.MergeKeyPlaceholder;

/**
 * The {@link VariableLoader} is responsible for extracting variable definitions from the YAML model and storing
 * them in the composer context for later use in substitutions.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class VariableLoader {

    public static final Set<String> SPECIAL_VARIABLES = Set.of("OPENHAB_CONF", "OPENHAB_USERDATA", "__FILE__",
            "__FILE_NAME__", "__FILE_EXT__", "__DIRECTORY__", "__DIR__", "ENV", "VARS", "ARGS");

    private final Map<String, @Nullable Object> variables;
    private final Path absolutePath;
    private final RecursiveTransformer recursiveTransformer;
    private final BufferedLogger logger;

    public VariableLoader(Map<String, @Nullable Object> variables, Path absolutePath,
            RecursiveTransformer recursiveTransformer, BufferedLogger logger) {
        this.variables = variables;
        this.absolutePath = absolutePath;
        this.recursiveTransformer = recursiveTransformer;
        this.logger = logger;
    }

    public static boolean isSpecialVariable(String name) {
        return SPECIAL_VARIABLES.contains(name);
    }

    /**
     * Add special file-related variables
     *
     * These are added early so they're available in variable definitions during the
     * first pass
     * Special variables will override any user-defined variables with the same name
     */
    public void setSpecialVariables() {
        Path fileNamePath = absolutePath.getFileName();
        String fullFileName = fileNamePath != null ? fileNamePath.toString() : "";
        int dotIndex = fullFileName.lastIndexOf(".");
        String fileName = fullFileName;
        String fileExtension = "";
        if (dotIndex > 0) {
            fileName = fullFileName.substring(0, dotIndex);
            fileExtension = fullFileName.substring(dotIndex + 1);
        }
        var parentPath = absolutePath.getParent();
        String directory = parentPath != null ? parentPath.toString() : "";

        variables.put("OPENHAB_CONF", ComposerConfig.configRoot().toString());
        variables.put("OPENHAB_USERDATA", ComposerConfig.userDataRoot().toString());
        variables.put("__FILE__", absolutePath.toString());
        variables.put("__FILE_NAME__", fileName);
        variables.put("__FILE_EXT__", fileExtension);
        variables.put("__DIRECTORY__", directory);
        variables.put("__DIR__", directory);
    }

    /**
     * Extracts variables from the given map and stores them into the context's variable map.
     *
     * Since variables can reference previously defined variables, we perform incremental resolution
     * while iterating through the variable definitions.
     *
     * @param variablesSection the section of the YAML file containing variable definitions, can be null
     * @param locator the source locator for logging purposes
     * @see ComposerConfig#VARIABLES_KEY
     */
    public void extractVariables(@Nullable Object variablesSection, SourceLocator locator) {
        if (variablesSection instanceof Map<?, ?> variablesMap) {
            Map<Object, @Nullable Object> mergeKeyEntries = new LinkedHashMap<>();
            Map<Object, @Nullable Object> explicitEntries = new LinkedHashMap<>();

            variablesMap.forEach((rawKey, rawValue) -> {
                Object key = Objects.requireNonNull(rawKey);
                @Nullable
                Object value = rawValue;
                if (key instanceof MergeKeyPlaceholder) {
                    mergeKeyEntries.put(key, value);
                } else {
                    explicitEntries.put(key, value);
                }
            });

            Set<String> keysFromMergeDefaults = new HashSet<>();

            // Process merge keys first so defaults are available in scope
            if (!mergeKeyEntries.isEmpty()) {
                Map<Object, @Nullable Object> processedMergeKeys = (Map<Object, @Nullable Object>) recursiveTransformer
                        .transform(mergeKeyEntries);
                Map<Object, @Nullable Object> mergedDefaults = new LinkedHashMap<>(processedMergeKeys);
                recursiveTransformer.resolveMergeKeys(mergedDefaults, ProcessingPhase.STANDARD);

                mergedDefaults.forEach((k, v) -> {
                    String kStr = String.valueOf(k);
                    if (!isSpecialVariable(kStr) && !variables.containsKey(kStr)) {
                        Object resolvedValue = recursiveTransformer.transform(v);
                        variables.put(kStr, resolvedValue);
                        keysFromMergeDefaults.add(kStr);
                    }
                });
            }

            // Process explicit entries sequentially
            explicitEntries.forEach((key, value) -> {
                Object keyObj = recursiveTransformer.transform(key);
                if (keyObj == null) {
                    return;
                }

                String keyStr = String.valueOf(keyObj);

                if (isSpecialVariable(keyStr)) {
                    logger.warn("{} Cannot redefine special variable '{}'.", absolutePath, keyStr);
                    return;
                }

                // Explicit entries overwrite merge key defaults from this map, but do not clobber inherited
                // parent/global scope
                if (!variables.containsKey(keyStr) || keysFromMergeDefaults.contains(keyStr)) {
                    Object resolvedValue = recursiveTransformer.transform(value);
                    variables.put(keyStr, resolvedValue);
                }
            });
        } else if (variablesSection instanceof IncludePlaceholder includePlaceholder) {
            Object includedData = recursiveTransformer.transform(includePlaceholder, ProcessingPhase.INCLUDES);
            extractVariables(includedData, locator);
        } else if (variablesSection != null) {
            var position = locator.findPosition(ComposerConfig.VARIABLES_KEY);
            Path relativePath = ComposerConfig.configRoot().relativize(absolutePath);
            logger.warn("{}:{} 'variables' is not a map", relativePath, position);
        }
    }
}
