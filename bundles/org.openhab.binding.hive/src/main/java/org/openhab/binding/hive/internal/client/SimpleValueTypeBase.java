/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A base class for simple value types.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
abstract class SimpleValueTypeBase<T extends @NonNull Object> {
    private final T value;

    public SimpleValueTypeBase(final T value) {
        Objects.requireNonNull(value);

        this.value = value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;
        final SimpleValueTypeBase<?> other = (SimpleValueTypeBase<?>) o;
        return this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
