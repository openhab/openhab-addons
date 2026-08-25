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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.ComposerConfig;
import org.openhab.io.yamlcomposer.internal.placeholders.IncludePlaceholder;

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

    private final RecursiveTransformer recursiveTransformer;
    private final BufferedLogger logger;
    private final Scope scope;

    public VariableLoader(Scope scope, RecursiveTransformer recursiveTransformer, BufferedLogger logger) {
        this.recursiveTransformer = recursiveTransformer;
        this.logger = logger;
        this.scope = scope;
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
        Path absolutePath = recursiveTransformer.getAbsolutePath();
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

        scope.put("OPENHAB_CONF", ComposerConfig.configRoot().toString());
        scope.put("OPENHAB_USERDATA", ComposerConfig.userDataRoot().toString());
        scope.put("__FILE__", absolutePath.toString());
        scope.put("__FILE_NAME__", fileName);
        scope.put("__FILE_EXT__", fileExtension);
        scope.put("__DIRECTORY__", directory);
        scope.put("__DIR__", directory);
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

            EvaluationContext context = new EvaluationContext(scope, ProcessingPhase.STANDARD);
            StructuralMerger structuralMerger = recursiveTransformer.getStructuralMerger();

            Map<Object, @Nullable Object> mergedMap = new LinkedHashMap<>(variablesMap.size());
            structuralMerger.composeMapPreserveValues(variablesMap, mergedMap, recursiveTransformer, context);

            mergedMap.forEach((key, value) -> {
                String keyStr = String.valueOf(key);

                if (isSpecialVariable(keyStr)) {
                    logger.warn("{} Cannot redefine special variable '{}'.", recursiveTransformer.getAbsolutePath(),
                            keyStr);
                    return;
                }

                // Scope precedence: existing/parent entries take priority
                if (!scope.containsKey(keyStr)) {
                    Object resolvedValue = recursiveTransformer.transform(value, context);
                    scope.put(keyStr, resolvedValue);
                }
            });
        } else if (variablesSection instanceof IncludePlaceholder includePlaceholder) {
            EvaluationContext includeContext = new EvaluationContext(scope, ProcessingPhase.INCLUDES);
            Object includedData = recursiveTransformer.transform(includePlaceholder, includeContext);
            extractVariables(includedData, locator);
        } else if (variablesSection != null) {
            var position = locator.findPosition(ComposerConfig.VARIABLES_KEY);
            Path absolutePath = recursiveTransformer.getAbsolutePath();
            Path relativePath = ComposerConfig.configRoot().relativize(absolutePath);
            logger.warn("{}:{} 'variables' is not a map", relativePath, position);
        }
    }
}
