/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.transport.message;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines the data types that can be used in the fields of a message.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public enum DataType {
    BYTE("byte", 1),
    ADDRESS("address", 3);

    private static final Map<String, DataType> NAME_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(type -> type.name, Function.identity()));

    private final String name;
    private final int size;

    private DataType(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    /**
     * Factory method for getting a DataType from the data type name
     *
     * @param name the data type name
     * @return the data type if defined, otherwise null
     */
    public static @Nullable DataType get(String name) {
        return NAME_MAP.get(name);
    }
}
