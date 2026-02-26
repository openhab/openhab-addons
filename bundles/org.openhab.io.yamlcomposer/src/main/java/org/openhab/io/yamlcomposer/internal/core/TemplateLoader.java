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
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.ComposerConfig;

/**
 * The {@link TemplateLoader} is responsible for extracting templates from the YAML model and storing them in the
 * composer context for later use.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class TemplateLoader {
    private final BufferedLogger logger;
    private final Path relativePath;
    private final Map<Object, @Nullable Object> templates;
    private final RecursiveTransformer recursiveTransformer;
    private final SourceLocator locator;

    public TemplateLoader(BufferedLogger logger, Path relativePath, Map<Object, @Nullable Object> templates,
            RecursiveTransformer recursiveTransformer, SourceLocator locator) {
        this.logger = logger;
        this.relativePath = relativePath;
        this.templates = templates;
        this.recursiveTransformer = recursiveTransformer;
        this.locator = locator;
    }

    /**
     * Extracts templates from the given map and stores them into the templates map.
     *
     * @param templatesSection the section of the YAML model containing templates
     */
    public void extractTemplates(@Nullable Object templatesSection) {
        if (templatesSection instanceof java.util.Map<?, ?> templatesMap) {
            templatesMap.keySet().removeIf(Objects::isNull);
            recursiveTransformer.resolveMergeKeys(templatesMap);
            templatesMap.forEach((key, value) -> {
                // Only resolve the key so we can look up the template name in the templates map.
                // The value substitutions must NOT be resolved here!
                // They will be resolved at insertion-time using insertion context instead of the main variables.
                Object resolvedKey = recursiveTransformer.transform(key, ProcessingPhase.SUBSTITUTION);
                if (resolvedKey != null) {
                    templates.put(resolvedKey, value);
                }
            });
        } else if (templatesSection != null) {
            var position = locator.findPosition(ComposerConfig.TEMPLATES_KEY);
            logger.warn("{}:{} 'templates' is not a map", relativePath, position);
        }
    }
}
