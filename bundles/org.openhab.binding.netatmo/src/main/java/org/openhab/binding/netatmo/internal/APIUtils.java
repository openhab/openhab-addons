/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link APIUtils} provides util methods for the usage of the generated API classes.
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public final class APIUtils {

    private APIUtils() {
    }

    public static <T> Stream<T> nonNullStream(Collection<T> collection) {
        return Optional.ofNullable(collection).stream().flatMap(Collection::stream);
    }

    public static <T> List<T> nonNullList(List<T> list) {
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }
}
