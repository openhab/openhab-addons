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
package org.openhab.binding.insteon.internal.message;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Defines the data types that can be used in the fields of a message.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public enum DataType {
    BYTE("byte", 1),
    INT("int", 4),
    FLOAT("float", 4),
    ADDRESS("address", 3),
    INVALID("INVALID", -1);

    private static Map<String, DataType> typeMap = new HashMap<>();

    private int size;
    private String name;

    static {
        typeMap.put(BYTE.getName(), BYTE);
        typeMap.put(INT.getName(), INT);
        typeMap.put(FLOAT.getName(), FLOAT);
        typeMap.put(ADDRESS.getName(), ADDRESS);
    }

    /**
     * Constructor
     *
     * @param name the name of the data type
     * @param size the size (in bytes) of this data type
     */
    DataType(String name, int size) {
        this.size = size;
        this.name = name;
    }

    /**
     * @return the size (in bytes) of this data type
     */
    public int getSize() {
        return size;
    }

    /**
     * @return clear text string with the name
     */
    public String getName() {
        return name;
    }

    /**
     * Turns a string into the corresponding data type
     *
     * @param name the string to translate to a type
     * @return the data type corresponding to the name string, or null if not found
     */
    public static DataType getDataType(String name) {
        DataType dataType = typeMap.get(name);
        if (dataType != null) {
            return dataType;
        } else {
            throw new IllegalArgumentException("Unable to find data type for " + name);
        }
    }
}
