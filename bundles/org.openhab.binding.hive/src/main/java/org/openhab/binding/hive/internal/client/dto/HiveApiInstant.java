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
package org.openhab.binding.hive.internal.client.dto;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class representing absolute times returned by the Hive API.
 *
 * <p>
 *     N.B. This should only be used by DTOs.
 * </p>
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HiveApiInstant {
    private final Instant instant;

    public HiveApiInstant(final Instant instant) {
        Objects.requireNonNull(instant);

        this.instant = instant;
    }

    public Instant asInstant() {
        return this.instant;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final HiveApiInstant that = (HiveApiInstant) o;

        return this.instant.equals(that.instant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.instant);
    }
}
