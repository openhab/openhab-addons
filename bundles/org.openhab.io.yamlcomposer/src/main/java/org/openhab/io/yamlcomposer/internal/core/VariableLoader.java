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
import java.util.LinkedHashMap;
import java.util.Map;

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
        Map<String, @Nullable Object> vars = variables;

        vars.put("OPENHAB_CONF", ComposerConfig.configRoot().toString());
        vars.put("OPENHAB_USERDATA", ComposerConfig.userDataRoot().toString());
        vars.put("__FILE__", absolutePath.toString());
        vars.put("__FILE_NAME__", fileName);
        vars.put("__FILE_EXT__", fileExtension);
        vars.put("__DIRECTORY__", directory);
        vars.put("__DIR__", directory);
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
        Map<String, @Nullable Object> existingVariables = variables;

        if (variablesSection instanceof Map<?, ?> variablesMap) {
            Map<Object, @Nullable Object> mergeKeys = new LinkedHashMap<>();

            variablesMap.forEach((key, value) -> {
                if (key instanceof MergeKeyPlaceholder) {
                    mergeKeys.put(key, value);
                    return;
                }

                Object keyObj = recursiveTransformer.transform(key);
                if (keyObj == null) {
                    return;
                }

                String keyStr = String.valueOf(keyObj);
                if (!existingVariables.containsKey(keyStr)) {
                    Object resolvedValue = recursiveTransformer.transform(value);
                    existingVariables.put(keyStr, resolvedValue);
                }
            });

            if (!mergeKeys.isEmpty()) {
                Map<Object, @Nullable Object> mergedVars = new LinkedHashMap<>(existingVariables);
                Map<Object, @Nullable Object> processedMergeKeys = recursiveTransformer.transform(mergeKeys);
                mergedVars.putAll(processedMergeKeys);
                recursiveTransformer.resolveMergeKeys(mergedVars);
                existingVariables.clear();
                mergedVars.forEach((k, v) -> existingVariables.put(String.valueOf(k), v));
            }
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
