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
 * The {@link MergeKeyPlaceholder} replaces the merge key (<<) in a mapping node
 * as a unique object, so that multiple merge keys can be processed later.
 *
 * @param value The value associated with the merge key placeholder ('<<' - not used)
 * @param sourceLocation Description of the source location for logging purposes
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public record MergeKeyPlaceholder(@Nullable Object value,
        @NonNull String sourceLocation) implements InterpolablePlaceholder<MergeKeyPlaceholder> {

    @Override
    public MergeKeyPlaceholder recreate(@Nullable Object newValue, String location) {
        return new MergeKeyPlaceholder(newValue, location);
    }
}
