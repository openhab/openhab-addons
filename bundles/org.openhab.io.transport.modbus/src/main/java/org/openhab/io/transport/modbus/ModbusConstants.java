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
package org.openhab.io.transport.modbus;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Constants for Modbus transport
 *
 * == Regarding maximum read and write limits ==
 *
 * Maximum number of registers that are allowed to be read.
 *
 * The Modbus protocol has many intepretation on maximum data size of messages. Good reference is here:
 * https://wingpath.co.uk/manpage.php?product=modtest&page=message_limits.html
 *
 * We try to follow modern specification here (V1.1B3):
 * https://modbus.org/docs/Modbus_Application_Protocol_V1_1b3.pdf. See section 4.1 Protocol Specification in the
 * specification.
 *
 * According to V1.1B3, maximum size for PDU is 253 bytes, making maximum ADU size 256 (RTU) or 260 (TCP).
 *
 * In the spec section 6, one can see maximum values for read and write counts.
 *
 * Note that this is not the only interpretation -- some sources limit the ADU to 256 also with TCP.
 * In some cases, slaves cannot take in so much data.
 *
 *
 * Reads are limited by response PDU size.
 * Writes (FC15 & FC16) are limited by write request ADU size.
 *
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusConstants {

    /**
     * Value types for different number types.
     *
     * @author Sami Salonen - Initial contribution
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
        INT64("int64", 64),
        UINT64("uint64", 64),

        INT32_SWAP("int32_swap", 32),
        UINT32_SWAP("uint32_swap", 32),
        FLOAT32_SWAP("float32_swap", 32),
        INT64_SWAP("int64_swap", 64),
        UINT64_SWAP("uint64_swap", 64);

        private final String configValue;
        private final int bits;

        ValueType(String configValue, int bits) {
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
        public String getConfigValue() {
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
        public static @NonNull ValueType fromConfigValue(@Nullable String configValueType)
                throws IllegalArgumentException {
            return Stream.of(ValueType.values()).filter(v -> v.getConfigValue().equals(configValueType)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid valueType " + configValueType));
        }
    }

    /**
     * Maximum number of coils or discrete inputs that are allowed to be read.
     * Limitation by Modbus protocol V1.1B3, 6.1 definition of Read Holding registers.
     */
    public static final int MAX_BITS_READ_COUNT = 2000;
    /**
     * Maximum number of registers that are allowed to be read.
     * Limitation by Modbus protocol V1.1B3, 6.3 definition of Read Coils.
     */
    public static final int MAX_REGISTERS_READ_COUNT = 125;
    /**
     * Maximum number of coils or discrete inputs that are allowed to be written.
     * Limitation by Modbus protocol V1.1B3, 6.11 definition of Write Multiple coils.
     */
    public static final int MAX_BITS_WRITE_COUNT = 1968;
    /**
     * Maximum number of registers that are allowed to be written.
     * Limitation by Modbus protocol V1.1B3, 6.12 definition of Write Multiple registers.
     */
    public static final int MAX_REGISTERS_WRITE_COUNT = 123;
}
