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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;

/**
 * Processor for handling 'packages' in YAML models.
 *
 * Merges package definitions into the main data structure, injecting the package ID
 * into any included or inserted fragments.
 *
 * This is slightly different to the other processors as it operates on the overall
 * data structure rather than merely performing resolutions of a particular placeholder.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class PackageProcessor {
    private static final String PACKAGES_KEY = "packages";
    private static final String PACKAGE_ID_VAR = "package_id";

    private final BufferedLogger logger;
    private final Path absolutePath;
    private final Path relativePath;
    private final SourceLocator sourceLocator;
    private final RecursiveTransformer recursiveTransformer;
    private final Scope scope;

    public PackageProcessor(Scope scope, RecursiveTransformer recursiveTransformer, Path absolutePath,
            Path relativePath, BufferedLogger logger, SourceLocator sourceLocator) {
        this.logger = logger;
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
        this.sourceLocator = sourceLocator;
        this.recursiveTransformer = recursiveTransformer;
        this.scope = scope;
    }

    /**
     * Convenience wrapper that accepts the raw 'packages' section value and
     * applies merging if it's a map, otherwise logs the same warning used by
     * the composer.
     *
     * @param yamlMap the root YAML map to merge packages into
     * @param packagesObj the raw 'packages' section value to process and merge, or null if not present
     */
    public void mergePackages(Map<?, ?> yamlMap, @Nullable Object packagesObj) {
        if (packagesObj == null) {
            return;
        }

        // Expand root structural directives (!for, !if) while deferring !include and !insert
        EvaluationContext pkgContext = new EvaluationContext(scope, ProcessingPhase.DIRECTIVES_WITH_SUBSTITUTIONS);
        Object expandedPackages = recursiveTransformer.transform(packagesObj, pkgContext);

        if (expandedPackages instanceof Map<?, ?> packagesMap) {
            mergePackages(yamlMap, packagesMap);
            logger.debug("Merged packages into data in {}: {}", absolutePath, yamlMap);
        } else if (expandedPackages != null) {
            var position = sourceLocator.findPosition(PACKAGES_KEY);
            logger.warn("{}:{} The 'packages' section is not a map", relativePath, position);
        }
    }

    /**
     * Deep merge packages map into the main data map
     */
    private void mergePackages(Map<?, ?> mainData, Map<?, ?> packages) {
        packages.forEach((pkgKey, pkg) -> {
            EvaluationContext keyContext = new EvaluationContext(scope, ProcessingPhase.STANDARD);
            Object pkgKeyObj = recursiveTransformer.transform(pkgKey, keyContext);
            if (pkgKeyObj == null) {
                var position = sourceLocator.findPosition(PACKAGES_KEY);
                logger.warn("{}:{} package key resolved to null; skipping package entry", relativePath, position);
                return;
            }
            String packageId = String.valueOf(pkgKeyObj);

            // Inject `package_id` into the package context for use in
            // !include and !insert within the package definition
            Scope packageScope = scope.createChild();
            packageScope.put(PACKAGE_ID_VAR, packageId);
            EvaluationContext pkgContext = new EvaluationContext(packageScope, ProcessingPhase.STANDARD);
            Object resolvedPkg = recursiveTransformer.transform(pkg, pkgContext);

            resolvedPkg = stripEmptyMapsAndLists(resolvedPkg);
            if (!(resolvedPkg instanceof Map<?, ?> packageMap)) {
                var position = sourceLocator.findPosition(PACKAGES_KEY, packageId);
                logger.warn("{}:{} package '{}' resolved to {} instead of a Map", relativePath, position, packageId,
                        resolvedPkg == null ? "null" : resolvedPkg.getClass().getSimpleName());
                return;
            }

            logger.debug("Merging package '{}' {} into main data: {}", packageId, packageMap, mainData);
            recursiveTransformer.getStructuralMerger().deepMerge(packageMap, mainData);
        });
    }

    private static @Nullable Object stripEmptyMapsAndLists(@Nullable Object data) {
        if (data == null || data instanceof String s && s.isBlank()) {
            return null;
        }
        if (data instanceof Map<?, ?> map) {
            var result = new LinkedHashMap<Object, Object>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                Object key = e.getKey();
                Object value = stripEmptyMapsAndLists(e.getValue());
                if (value != null) {
                    result.put(key, value);
                }
            }
            return result.isEmpty() ? null : result;
        } else if (data instanceof List<?> list) {
            var result = new java.util.ArrayList<Object>(list.size());
            for (Object item : list) {
                Object value = stripEmptyMapsAndLists(item);
                if (value != null) {
                    result.add(value);
                }
            }
            return result.isEmpty() ? null : result;
        }
        return data;
    }
}
