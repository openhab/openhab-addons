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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.placeholders.InsertPlaceholder;

/**
 * Processor for resolving {@link InsertPlaceholder} in YAML models
 * into template content with local variable substitution.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class InsertProcessor implements PlaceholderProcessor<InsertPlaceholder> {

    private final Map<Object, @Nullable Object> templates;
    private final BufferedLogger logger;

    /**
     * Creates a new InsertProcessor.
     *
     * @param templates the map of available templates for insertion
     * @param logger the logger to use for logging messages
     */
    public InsertProcessor(Map<Object, @Nullable Object> templates, BufferedLogger logger) {
        this.templates = templates;
        this.logger = logger;
    }

    @Override
    public Class<InsertPlaceholder> getPlaceholderType() {
        return InsertPlaceholder.class;
    }

    /**
     * Resolves an {@link InsertPlaceholder} recursively
     * following any nested inserts, and returns the fully expanded content.
     */
    @Override
    public @Nullable Object process(InsertPlaceholder placeholder, RecursiveTransformer recursiveTransformer) {
        FragmentUtils.Parameters params = FragmentUtils.parseParameters(placeholder, "template");
        if (params == null) {
            logger.warn("{} Failed to process !insert: invalid parameters", placeholder.sourceLocation());
            return null;
        }

        String templateName = params.name();
        if (templateName == null || templateName.isBlank()) {
            logger.warn("{} Failed to process !insert: missing template name", placeholder.sourceLocation());
            return null;
        }

        Object templateObj = templates.get(templateName);
        if (templateObj == null) {
            logger.warn("{} Failed to process !insert '{}': template not found", placeholder.sourceLocation(),
                    templateName);
            return null;
        }

        // The substitution placeholders in the template are resolved using the templateVariables context
        // unlike any other processing which uses the main variables map
        Map<String, @Nullable Object> templateVariables = params.varsMap();
        RecursiveTransformer localTransformer = recursiveTransformer.withOverrideVariables(templateVariables);
        Object resolvedTemplate = localTransformer.transform(templateObj);
        return resolvedTemplate;
    }
}
