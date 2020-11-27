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
package org.openhab.binding.mielecloud.internal.util;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class to prevent problems with null annotations with {@link Optional}s.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class OptionalUtils {
    private OptionalUtils() {
        throw new IllegalStateException();
    }

    public static <T> Optional<T> ofNullable(@Nullable T value) {
        return value == null ? Optional.empty() : Optional.of(value);
    }
}
