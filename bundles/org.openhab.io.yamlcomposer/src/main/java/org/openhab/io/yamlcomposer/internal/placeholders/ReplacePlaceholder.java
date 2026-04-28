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
package org.openhab.io.yamlcomposer.internal.placeholders;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ReplacePlaceholder} represents an object constructed from a <code>!replace</code> node
 * to be processed by the {@link org.openhab.io.yamlcomposer.internal.YamlComposer}.
 *
 * @param value The value associated with the replace placeholder
 * @param sourceLocation Description of the source location for logging purposes
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public record ReplacePlaceholder(@Nullable Object value,
        @NonNull String sourceLocation) implements InterpolablePlaceholder<ReplacePlaceholder> {

    @Override
    public ReplacePlaceholder recreate(@Nullable Object newValue, String location) {
        return new ReplacePlaceholder(newValue, location);
    }
}
