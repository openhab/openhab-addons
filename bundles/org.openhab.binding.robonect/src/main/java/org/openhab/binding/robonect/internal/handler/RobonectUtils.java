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
package org.openhab.binding.robonect.internal.handler;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helpers for accessing optional response values.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
final class RobonectUtils {

    private RobonectUtils() {
    }

    static <T, V> Optional<V> read(@Nullable T root, Function<T, @Nullable V> getter) {
        return Optional.ofNullable(root).flatMap(value -> Optional.ofNullable(getter.apply(value)));
    }
}
