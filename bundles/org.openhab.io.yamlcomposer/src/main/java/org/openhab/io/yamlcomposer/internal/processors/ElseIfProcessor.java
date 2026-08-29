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

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.directives.ElseIfDirective;
import org.openhab.io.yamlcomposer.internal.placeholders.ElseIfPlaceholder;

/**
 * Processor for resolving {@link ElseIfPlaceholder} in YAML models.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ElseIfProcessor extends AbstractConditionalProcessor implements PlaceholderProcessor<ElseIfPlaceholder> {

    public ElseIfProcessor(Consumer<String> envVarCallback, BufferedLogger logger) {
        super(logger, envVarCallback);
    }

    @Override
    public Class<ElseIfPlaceholder> getPlaceholderType() {
        return ElseIfPlaceholder.class;
    }

    @Override
    public @Nullable Object process(ElseIfPlaceholder elseIfPlaceholder, RecursiveTransformer recursiveTransformer) {
        Object value = elseIfPlaceholder.value();

        @Nullable
        Boolean simpleSyntaxResult = processSimpleSyntax(value, elseIfPlaceholder.sourceLocation(),
                recursiveTransformer);
        if (simpleSyntaxResult != null) {
            return new ElseIfDirective(elseIfPlaceholder.tag(), simpleSyntaxResult, elseIfPlaceholder.sourceLocation());
        }
        logger.warn("{} Invalid syntax for {} {}", elseIfPlaceholder.sourceLocation(), elseIfPlaceholder.tag(), value);
        return null;
    }
}
