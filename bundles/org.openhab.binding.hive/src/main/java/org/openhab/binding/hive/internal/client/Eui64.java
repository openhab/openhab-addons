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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class Eui64 {
    private final String value;

    public Eui64(final String eui64) {
        Objects.requireNonNull(eui64);

        if (eui64.length() != 16) {
            throw new IllegalArgumentException("Provided string is the wrong length.");
        }

        this.value = eui64;
    }


    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Eui64 eui64 = (Eui64) o;
        return this.value.equals(eui64.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
