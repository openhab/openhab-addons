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
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.directives.ElseDirective;
import org.openhab.io.yamlcomposer.internal.placeholders.ElsePlaceholder;

/**
 * Processor for resolving {@link ElsePlaceholder} in YAML models.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ElseProcessor implements PlaceholderProcessor<ElsePlaceholder> {

    @Override
    public Class<ElsePlaceholder> getPlaceholderType() {
        return ElsePlaceholder.class;
    }

    @Override
    public @Nullable Object process(ElsePlaceholder elsePlaceholder, RecursiveTransformer recursiveTransformer) {
        return new ElseDirective(elsePlaceholder.sourceLocation());
    }
}
