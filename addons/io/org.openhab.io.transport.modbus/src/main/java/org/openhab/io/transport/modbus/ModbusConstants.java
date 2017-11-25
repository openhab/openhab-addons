/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Constants for Modbus transport
 *
 * @author Sami Salonen
 *
 */
public class ModbusConstants {

    /**
     * Value types for different number types.
     *
     * @author Sami Salonen
     *
     */
    public static enum ValueType {
        BIT("bit", 1),
        INT8("int8", 8),
        UINT8("uint8", 8),
        INT16("int16", 16),
        UINT16("uint16", 16),
        INT32("int32", 32),
        UINT32("uint32", 32),
        FLOAT32("float32", 32),
        INT32_SWAP("int32_swap", 32),
        UINT32_SWAP("uint32_swap", 32),
        FLOAT32_SWAP("float32_swap", 32);

        private final @NonNull String configValue;
        private final int bits;

        ValueType(@NonNull String configValue, int bits) {
            this.configValue = configValue;
            this.bits = bits;
        }

        /**
         * Returns number of bits represented by this ValueType
         *
         * @return number of bits
         */
        public int getBits() {
            return bits;
        }

        /**
         * Returns config value to refer to this value type
         *
         * @return config value as string
         */
        public @NonNull String getConfigValue() {
            return configValue;
        }

        /**
         * Returns config value
         */
        @Override
        public String toString() {
            return getConfigValue();
        }

        /**
         * Constructs ValueType given the config value string.
         *
         * @param configValueType config value that will be parsed to ValueType
         * @return ValueType matching the config value
         * @throws IllegalArgumentException with unknown value types
         */
        @SuppressWarnings("null")
        public static @NonNull ValueType fromConfigValue(String configValueType) throws IllegalArgumentException {
            return Stream.of(ValueType.values()).filter(v -> v.getConfigValue().equals(configValueType)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid valueType " + configValueType));
        }
    }

}
