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
import org.openhab.io.yamlcomposer.internal.placeholders.FreezePlaceholder;

/**
 * The {@link FreezeProcessor} processes {@link FreezePlaceholder} instances in YAML models.
 * It simply returns the value contained within the placeholder.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class FreezeProcessor implements PlaceholderProcessor<FreezePlaceholder> {

    /** Returns the replacement payload shared by !replace and !freeze processing. */
    public static @Nullable Object replacementValue(@Nullable Object value) {
        return value;
    }

    @Override
    public Class<FreezePlaceholder> getPlaceholderType() {
        return FreezePlaceholder.class;
    }

    @Override
    public @Nullable Object process(FreezePlaceholder placeholder, RecursiveTransformer recursiveTransformer,
            EvaluationContext context) {
        return replacementValue(placeholder.value());
    }
}
