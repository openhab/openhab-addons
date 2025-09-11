/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Util} contains helper methods
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Util {

    private Util() {
        // prevent instantiation
    }

    public static <T, U> Optional<T> findIn(Collection<T> collection, Function<T, U> keyExtractor,
            @Nullable U searchKey) {
        return collection.stream().filter(e -> Objects.equals(searchKey, keyExtractor.apply(e))).findAny();
    }

    public static <T, U> List<T> filterList(List<T> list, Function<T, U> keyExtractor, U searchKey) {
        return list.stream().filter(e -> Objects.equals(searchKey, keyExtractor.apply(e))).toList();
    }
}
