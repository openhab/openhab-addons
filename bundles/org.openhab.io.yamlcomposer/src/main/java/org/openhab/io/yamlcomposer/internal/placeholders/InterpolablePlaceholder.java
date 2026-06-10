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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Common abstraction for placeholders to allow generic access to their
 * argument data while preserving typed, record-based implementations.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public interface InterpolablePlaceholder<R extends InterpolablePlaceholder<R>> extends Placeholder {

    @Nullable
    Object value();

    /**
     * The factory method each record must implement to allow the generic withValue()
     * method to create new instances of the correct type.
     */
    R recreate(@Nullable Object newValue, String location);

    /**
     * Convenience method to create a new instance of the placeholder with a new value but the same source location.
     *
     * @param newValue the new value for the placeholder
     * @return a new instance of the placeholder with the given value and the same source location
     */
    @SuppressWarnings("unchecked")
    default R withValue(@Nullable Object newValue) {
        if (Objects.equals(this.value(), newValue)) {
            return (R) this;
        }
        // Direct method call - no reflection needed
        return recreate(newValue, this.sourceLocation());
    }

    /**
     * By default, we want to eagerly process the arguments of placeholders before processing the placeholder itself
     * since most of them are just simple wrappers.
     *
     * However, some placeholders like !if need to resolve their conditions first before processing their arguments
     * to avoid processing arguments that are not relevant (e.g. !include within unmet conditions).
     *
     * Implementations can override this method to change this behavior.
     *
     * @return true if the arguments should be processed before processing the placeholder, false otherwise
     */
    default boolean eagerArgumentProcessing() {
        return true;
    }
}
