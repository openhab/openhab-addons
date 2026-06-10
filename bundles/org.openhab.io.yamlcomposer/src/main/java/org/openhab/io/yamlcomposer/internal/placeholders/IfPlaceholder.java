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
 * The {@link IfPlaceholder} represents an object constructed from an <code>!if</code> node
 * to be processed by the {@link org.openhab.io.yamlcomposer.internal.YamlComposer}.
 *
 * @param value The constructed object of the node containing the raw argument for the if placeholder
 * @param sourceLocation Description of the source location for logging purposes
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public record IfPlaceholder(@Nullable Object value,
        @NonNull String sourceLocation) implements InterpolablePlaceholder<IfPlaceholder> {

    @Override
    public IfPlaceholder recreate(@Nullable Object newValue, String location) {
        return new IfPlaceholder(newValue, location);
    }

    @Override
    public boolean eagerArgumentProcessing() {
        // Do not process the arguments before resolving the conditions
        // so that !include within unmet conditions are not processed
        return false;
    }
}
