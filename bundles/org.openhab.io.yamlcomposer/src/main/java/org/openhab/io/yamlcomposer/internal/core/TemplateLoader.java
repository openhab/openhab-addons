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
    private final Scope scope;

    public TemplateLoader(BufferedLogger logger, Path relativePath, Map<Object, @Nullable Object> templates,
            RecursiveTransformer recursiveTransformer, SourceLocator locator, Scope scope) {
        this.logger = logger;
        this.relativePath = relativePath;
        this.templates = templates;
        this.recursiveTransformer = recursiveTransformer;
        this.locator = locator;
        this.scope = scope;
    }

    /**
     * Extracts templates from the given map and stores them into the templates map.
     *
     * @param templatesSection the section of the YAML model containing templates
     */
    public void extractTemplates(@Nullable Object templatesSection) {
        if (templatesSection instanceof java.util.Map<?, ?> templatesMap) {
            StructuralMerger structuralMerger = recursiveTransformer.getStructuralMerger();
            EvaluationContext context = new EvaluationContext(scope, ProcessingPhase.STANDARD);
            Map<Object, @Nullable Object> resolvedTemplates = new LinkedHashMap<>(templatesMap.size());

            // Use deferred value composition so templates are evaluated dynamically with their call-site arguments.
            structuralMerger.composeMapPreserveValues(templatesMap, resolvedTemplates, recursiveTransformer, context);
            templates.putAll(resolvedTemplates);
        } else if (templatesSection != null) {
            var position = locator.findPosition(ComposerConfig.TEMPLATES_KEY);
            logger.warn("{}:{} 'templates' is not a map", relativePath, position);
        }
    }
}
