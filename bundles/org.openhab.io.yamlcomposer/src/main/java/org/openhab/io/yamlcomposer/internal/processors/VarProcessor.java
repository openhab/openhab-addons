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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.directives.VarDirective;
import org.openhab.io.yamlcomposer.internal.placeholders.VarPlaceholder;

/**
 * Parses {@link VarPlaceholder} directives into {@link VarDirective} instances.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class VarProcessor implements PlaceholderProcessor<VarPlaceholder> {

    private final BufferedLogger logger;

    public VarProcessor(BufferedLogger logger) {
        this.logger = logger;
    }

    @Override
    public Class<VarPlaceholder> getPlaceholderType() {
        return VarPlaceholder.class;
    }

    @Override
    public @Nullable Object process(VarPlaceholder placeholder, RecursiveTransformer transformer) {
        String sourceLocation = placeholder.sourceLocation();

        if (placeholder.value() instanceof String strVal) {
            String variableName = strVal.trim();
            if (!variableName.isEmpty() && !"null".equalsIgnoreCase(variableName)) {
                return new VarDirective(variableName, sourceLocation);
            }
        }

        logger.warn("{} Invalid !var directive. Expected a variable name scalar (e.g., '!var name: value').",
                sourceLocation);
        return null;
    }
}
