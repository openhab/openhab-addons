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
import org.openhab.io.yamlcomposer.internal.core.EvaluationContext;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.placeholders.DefaultPlaceholder;

/**
 * Resolves {@link DefaultPlaceholder} values during final cleanup.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class DefaultProcessor implements PlaceholderProcessor<DefaultPlaceholder> {

    @Override
    public Class<DefaultPlaceholder> getPlaceholderType() {
        return DefaultPlaceholder.class;
    }

    @Override
    public @Nullable Object process(DefaultPlaceholder placeholder, RecursiveTransformer transformer,
            EvaluationContext context) {
        return placeholder.value();
    }
}
