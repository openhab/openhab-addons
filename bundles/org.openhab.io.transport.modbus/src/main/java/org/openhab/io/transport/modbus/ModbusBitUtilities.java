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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Command;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;

/**
 * Utilities for working with binary data.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusBitUtilities {

    /**
     * Read data from registers and convert the result to DecimalType
     * Interpretation of <tt>index</tt> goes as follows depending on type
     *
     * BIT:
     * - a single bit is read from the registers
     * - indices between 0...15 (inclusive) represent bits of the first register
     * - indices between 16...31 (inclusive) represent bits of the second register, etc.
     * - index 0 refers to the least significant bit of the first register
     * - index 1 refers to the second least significant bit of the first register, etc.
     * INT8:
     * - a byte (8 bits) from the registers is interpreted as signed integer
     * - index 0 refers to low byte of the first register, 1 high byte of first register
     * - index 2 refers to low byte of the second register, 3 high byte of second register, etc.
     * - it is assumed that each high and low byte is encoded in most significant bit first order
     * UINT8:
     * - same as INT8 except value is interpreted as unsigned integer
     * INT16:
     * - register with index (counting from zero) is interpreted as 16 bit signed integer.
     * - it is assumed that each register is encoded in most significant bit first order
     * UINT16:
     * - same as INT16 except value is interpreted as unsigned integer
     * INT32:
     * - registers (index) and (index + 1) are interpreted as signed 32bit integer.
     * - it assumed that the first register contains the most significant 16 bits
     * - it is assumed that each register is encoded in most significant bit first order
     * INT32_SWAP:
     * - Same as INT32 but registers swapped
     * UINT32:
     * - same as INT32 except value is interpreted as unsigned integer
     * UINT32_SWAP:
     * - same as INT32_SWAP except value is interpreted as unsigned integer
     * FLOAT32:
     * - registers (index) and (index + 1) are interpreted as signed 32bit floating point number.
     * - it assumed that the first register contains the most significant 16 bits
     * - it is assumed that each register is encoded in most significant bit first order
     * - floating point NaN and infinity will return as empty optional
     * FLOAT32_SWAP:
     * - Same as FLOAT32 but registers swapped
     * INT64:
     * - registers (index), (index + 1), (index + 2), (index + 3) are interpreted as signed 64bit integer.
     * - it assumed that the first register contains the most significant 16 bits
     * - it is assumed that each register is encoded in most significant bit first order
     * INT64_SWAP:
     * - same as INT64 but registers swapped, that is, registers (index + 3), (index + 2), (index + 1), (index + 1) are
     * interpreted as signed 64bit integer
     * UINT64:
     * - same as INT64 except value is interpreted as unsigned integer
     * UINT64_SWAP:
     * - same as INT64_SWAP except value is interpreted as unsigned integer
     *
     * @param registers list of registers, each register represent 16bit of data
     * @param index zero based item index. Interpretation of this depends on type, see examples above.
     *            With type larger or equal to 16 bits, the index tells the register index to start reading
     *            from.
     *            With type less than 16 bits, the index tells the N'th item to read from the registers.
     * @param type item type, e.g. unsigned 16bit integer (<tt>ModbusBindingProvider.ValueType.UINT16</tt>)
     * @return number representation queried value, <tt>DecimalType</tt>. Empty optional is returned
     *         with NaN and infinity floating point values
     * @throws NotImplementedException in cases where implementation is lacking for the type. This can be considered a
     *             bug
     * @throws IllegalArgumentException when <tt>index</tt> is out of bounds of registers
     *
     */
    public static Optional<DecimalType> extractStateFromRegisters(ModbusRegisterArray registers, int index,
            ModbusConstants.ValueType type) {
        byte[] bytes = registers.getBytes();
        switch (type) {
            case BIT:
                return Optional.of(new DecimalType(extractBit(bytes, index)));
            case INT8: {
                int registerIndex = index / 2;
                boolean hiByte = index % 2 == 1;
                return Optional.of(new DecimalType(extractSInt8(bytes, registerIndex, hiByte)));
            }
            case UINT8: {
                int registerIndex = index / 2;
                boolean hiByte = index % 2 == 1;
                return Optional.of(new DecimalType(extractUInt8(bytes, registerIndex, hiByte)));
            }
            case INT16:
                return Optional.of(new DecimalType(extractSInt16(bytes, index * 2)));
            case UINT16:
                return Optional.of(new DecimalType(extractUInt16(bytes, index * 2)));
            case INT32:
                return Optional.of(new DecimalType(extractSInt32(bytes, index * 2)));
            case UINT32:
                return Optional.of(new DecimalType(extractUInt32(bytes, index * 2)));
            case FLOAT32:
                try {
                    return Optional.of(new DecimalType(extractFloat32(bytes, index * 2)));
                } catch (NumberFormatException e) {
                    // floating point NaN or infinity encountered
                    return Optional.empty();
                }
            case INT64:
                return Optional.of(new DecimalType(extractSInt64(bytes, index * 2)));
            case UINT64:
                return Optional.of(new DecimalType(new BigDecimal(extractUInt64(bytes, index * 2))));
            case INT32_SWAP:
                return Optional.of(new DecimalType(extractSInt32Swap(bytes, index * 2)));
            case UINT32_SWAP:
                return Optional.of(new DecimalType(extractUInt32Swap(bytes, index * 2)));
            case FLOAT32_SWAP:
                try {
                    return Optional.of(new DecimalType(extractFloat32Swap(bytes, index * 2)));
                } catch (NumberFormatException e) {
                    // floating point NaN or infinity encountered
                    return Optional.empty();
                }
            case INT64_SWAP:
                return Optional.of(new DecimalType(extractSInt64Swap(bytes, index * 2)));
            case UINT64_SWAP:
                return Optional.of(new DecimalType(new BigDecimal(extractUInt64Swap(bytes, index * 2))));
            default:
                throw new IllegalArgumentException(type.getConfigValue());
        }
    }

    private static void assertIndexAndType(byte[] bytes, int index, ValueType type) {
        int typeBits = type.getBits();
        // for 8-bit types and larger, index specifies the index of the byte. For bits, index specifies the index of the
        // bit (of the whole data)
        int indexPositionAsBitIndex = Math.min(type.getBits(), 8) * index;
        int endBitIndex = indexPositionAsBitIndex + typeBits - 1;
        int lastValidIndex = bytes.length * 8 - 1;
        if (endBitIndex > lastValidIndex || index < 0) {
            throw new IllegalArgumentException(
                    String.format("Index=%d with type=%s is out-of-bounds given registers of size %d ", index, type,
                            bytes.length / 2));
        }
    }

    /**
     * Extract single bit from registers represented by sequence of bytes
     *
     * - indices between 0...15 (inclusive) represent bits of the first register
     * - indices between 16...31 (inclusive) represent bits of the second register, etc.
     * - index 0 refers to the least significant bit of the first register
     * - index 1 refers to the second least significant bit of the first register, etc.
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of bit
     * @return 0 when bit is set, 1 otherwise
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static int extractBit(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.BIT);
        int registerIndex = index / 16;
        int bitIndexWithinRegister = index % 16;
        return extractBit(bytes, registerIndex, bitIndexWithinRegister);
    }

    /**
     * Extract single bit from registers represented by sequence of bytes
     *
     * bitIndexWithinRegister between 0...15 (inclusive) represent bits of the first register, where 0 refers to the
     * least significant bit of the register, index 1 refers to the second least significant bit of the register, etc.
     *
     * @param bytes registers represented by sequence of bytes
     * @param registerIndex index of register. First register has index of 0.
     * @param bitIndexWithinRegister bit index within the register
     * @return 0 when bit is set, 1 otherwise
     * @throws IllegalArgumentException when registerIndex and/or bitIndexWithinRegister is out of bounds
     */
    public static int extractBit(byte[] bytes, int registerIndex, int bitIndexWithinRegister) {
        if (bitIndexWithinRegister < 0 || bitIndexWithinRegister > 15) {
            throw new IllegalArgumentException(
                    String.format("bitIndexWithinRegister=%d is out-of-bounds (max 15)", bitIndexWithinRegister));
        } else if (registerIndex < 0) {
            throw new IllegalArgumentException(
                    String.format("registerIndex=%d is out-of-bounds", bitIndexWithinRegister));
        }
        boolean hiByte = bitIndexWithinRegister >= 8;
        int indexWithinByte = bitIndexWithinRegister % 8;
        int byteIndex = 2 * registerIndex + (hiByte ? 0 : 1);
        if (byteIndex >= bytes.length) {
            throw new IllegalArgumentException(String.format(
                    "registerIndex=%d, bitIndexWithinRegister=%d is out-of-bounds with registers of size %d",
                    registerIndex, bitIndexWithinRegister, bytes.length / 2));
        }
        return ((bytes[byteIndex] >>> indexWithinByte) & 1);
    }

    /**
     * Extract signed 8-bit integer (byte) from registers represented by sequence of bytes
     *
     * @param bytes registers represented by sequence of bytes
     * @param registerIndex index of register. First register has index of 0.
     * @param hiByte whether to extract hi byte or lo byte
     * @return 0 when bit is set, 1 otherwise
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static byte extractSInt8(byte[] bytes, int registerIndex, boolean hiByte) {
        int byteIndex = 2 * registerIndex + (hiByte ? 0 : 1);
        byte signed = extractSInt8(bytes, byteIndex);
        return signed;
    }

    /**
     * Extract signed 8-bit integer (byte) from registers represented by sequence of bytes
     *
     * - index 0 refers to low byte of the first register, 1 high byte of first register
     * - index 2 refers to low byte of the second register, 3 high byte of second register, etc.
     * - it is assumed that each high and low byte is encoded in most significant bit first order
     *
     * @param bytes registers represented by sequence of bytes
     * @param registerIndex index of register. First register has index of 0.
     * @param index index of the byte in registers
     * @return 0 when bit is set, 1 otherwise
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static byte extractSInt8(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.INT8);
        byte signed = bytes[index];
        return signed;
    }

    /**
     * Extract unsigned 8-bit integer (byte) from registers represented by sequence of bytes
     *
     * @param bytes registers represented by sequence of bytes
     * @param registerIndex index of register. First register has index of 0.
     * @param hiByte whether to extract hi byte or lo byte
     * @return 0 when bit is set, 1 otherwise
     * @throws IllegalArgumentException when registerIndex is out of bounds
     */
    public static short extractUInt8(byte[] bytes, int registerIndex, boolean hiByte) {
        int byteIndex = 2 * registerIndex + (hiByte ? 0 : 1);
        short unsigned = extractUInt8(bytes, byteIndex);
        return unsigned;
    }

    /**
     * Extract unsigned 8-bit integer (byte) from registers represented by sequence of bytes
     *
     * - index 0 refers to low byte of the first register, 1 high byte of first register
     * - index 2 refers to low byte of the second register, 3 high byte of second register, etc.
     * - it is assumed that each high and low byte is encoded in most significant bit first order
     *
     * @param bytes registers represented by sequence of bytes
     * @param registerIndex index of register. First register has index of 0.
     * @param index index of the byte in registers
     * @return 0 when bit is set, 1 otherwise
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static short extractUInt8(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.UINT8);
        int signed = extractSInt8(bytes, index);
        short unsigned = (short) (signed & 0xff);
        assert unsigned >= 0;
        return unsigned;
    }

    /**
     * Extract signed 16-bit integer (short) from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of register. First register has index of 0.
     * @return register with index interpreted as 16 bit signed integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static short extractSInt16(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.INT16);
        int hi = (bytes[index] & 0xff);
        int lo = (bytes[index + 1] & 0xff);
        short signed = (short) ((hi << 8) | lo);
        return signed;
    }

    /**
     * Extract unsigned 16-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of register. First register has index of 0.
     * @return register with index interpreted as 16 bit unsigned integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static int extractUInt16(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.UINT16);
        int signed = extractSInt16(bytes, index);
        int unsigned = signed & 0xffff;
        assert unsigned >= 0;
        return unsigned;
    }

    /**
     * Extract signed 32-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index) and (index+1) interpreted as 32 bit signed integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static int extractSInt32(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.INT32);
        int hi1 = bytes[index + 0] & 0xff;
        int lo1 = bytes[index + 1] & 0xff;
        int hi2 = bytes[index + 2] & 0xff;
        int lo2 = bytes[index + 3] & 0xff;
        int signed = (hi1 << 24) | (lo1 << 16) | (hi2 << 8) | lo2;
        return signed;
    }

    /**
     * Extract unsigned 32-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index) and (index+1) interpreted as 32 bit unsigned integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static long extractUInt32(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.UINT32);
        long signed = extractSInt32(bytes, index);
        long unsigned = signed & 0xffff_ffffL;
        assert unsigned >= 0;
        return unsigned;
    }

    /**
     * Extract signed 32-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order.
     *
     * This is identical with extractSInt32, but with registers swapped.
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index+1), (index) interpreted as 32 bit signed integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static int extractSInt32Swap(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.INT32_SWAP);
        // swapped order of registers, high 16 bits *follow* low 16 bits
        int hi1 = bytes[index + 2] & 0xff;
        int lo1 = bytes[index + 3] & 0xff;
        int hi2 = bytes[index + 0] & 0xff;
        int lo2 = bytes[index + 1] & 0xff;
        int signed = (hi1 << 24) | (lo1 << 16) | (hi2 << 8) | lo2;
        return signed;
    }

    /**
     * Extract unsigned 32-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order.
     *
     * This is identical with extractUInt32, but with registers swapped.
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index+1), (index) interpreted as 32 bit unsigned integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static long extractUInt32Swap(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.UINT32_SWAP);
        long signed = extractSInt32Swap(bytes, index);
        long unsigned = signed & 0xffff_ffffL;
        assert unsigned >= 0;
        return unsigned;
    }

    /**
     * Extract signed 64-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order.
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index), (index+1), (index+2), (index+3) interpreted as 64 bit signed integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static long extractSInt64(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.INT64);
        byte hi1 = (byte) (bytes[index + 0] & 0xff);
        byte lo1 = (byte) (bytes[index + 1] & 0xff);
        byte hi2 = (byte) (bytes[index + 2] & 0xff);
        byte lo2 = (byte) (bytes[index + 3] & 0xff);
        byte hi3 = (byte) (bytes[index + 4] & 0xff);
        byte lo3 = (byte) (bytes[index + 5] & 0xff);
        byte hi4 = (byte) (bytes[index + 6] & 0xff);
        byte lo4 = (byte) (bytes[index + 7] & 0xff);
        return new BigInteger(new byte[] { hi1, lo1, hi2, lo2, hi3, lo3, hi4, lo4 }).longValue();
    }

    /**
     * Extract unsigned 64-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order.
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index), (index+1), (index+2), (index+3) interpreted as 64 bit unsigned integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static BigInteger extractUInt64(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.UINT64);
        byte hi1 = (byte) (bytes[index + 0] & 0xff);
        byte lo1 = (byte) (bytes[index + 1] & 0xff);
        byte hi2 = (byte) (bytes[index + 2] & 0xff);
        byte lo2 = (byte) (bytes[index + 3] & 0xff);
        byte hi3 = (byte) (bytes[index + 4] & 0xff);
        byte lo3 = (byte) (bytes[index + 5] & 0xff);
        byte hi4 = (byte) (bytes[index + 6] & 0xff);
        byte lo4 = (byte) (bytes[index + 7] & 0xff);
        return new BigInteger(1, new byte[] { hi1, lo1, hi2, lo2, hi3, lo3, hi4, lo4 });
    }

    /**
     * Extract signed 64-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order.
     *
     * This is identical with extractInt64, but with registers swapped (registers with higher index before lower index).
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index+3), (index+2), (index+1), (index) interpreted as 64 bit signed integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static long extractSInt64Swap(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.INT64_SWAP);
        // Swapped order of registers
        byte hi1 = (byte) (bytes[index + 6] & 0xff);
        byte lo1 = (byte) (bytes[index + 7] & 0xff);
        byte hi2 = (byte) (bytes[index + 4] & 0xff);
        byte lo2 = (byte) (bytes[index + 5] & 0xff);
        byte hi3 = (byte) (bytes[index + 2] & 0xff);
        byte lo3 = (byte) (bytes[index + 3] & 0xff);
        byte hi4 = (byte) (bytes[index + 0] & 0xff);
        byte lo4 = (byte) (bytes[index + 1] & 0xff);
        return new BigInteger(new byte[] { hi1, lo1, hi2, lo2, hi3, lo3, hi4, lo4 }).longValue();
    }

    /**
     * Extract unsigned 64-bit integer from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order.
     *
     * This is identical with extractUInt64, but with registers swapped (registers with higher index before lower
     * index).
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index+3), (index+2), (index+1), (index) interpreted as 64 bit unsigned integer
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static BigInteger extractUInt64Swap(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.UINT64_SWAP);
        // Swapped order of registers
        byte hi1 = (byte) (bytes[index + 6] & 0xff);
        byte lo1 = (byte) (bytes[index + 7] & 0xff);
        byte hi2 = (byte) (bytes[index + 4] & 0xff);
        byte lo2 = (byte) (bytes[index + 5] & 0xff);
        byte hi3 = (byte) (bytes[index + 2] & 0xff);
        byte lo3 = (byte) (bytes[index + 3] & 0xff);
        byte hi4 = (byte) (bytes[index + 0] & 0xff);
        byte lo4 = (byte) (bytes[index + 1] & 0xff);
        return new BigInteger(1, new byte[] { hi1, lo1, hi2, lo2, hi3, lo3, hi4, lo4 });
    }

    /**
     * Extract single-precision 32-bit IEEE 754 floating point from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order.
     *
     * Note that this method can return floating point NaN and floating point infinity.
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index), (index+1), (index+2), (index+3) interpreted as single-precision 32-bit IEEE 754
     *         floating point
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static float extractFloat32(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.FLOAT32);
        int hi1 = bytes[index + 0] & 0xff;
        int lo1 = bytes[index + 1] & 0xff;
        int hi2 = bytes[index + 2] & 0xff;
        int lo2 = bytes[index + 3] & 0xff;
        int bits32 = (hi1 << 24) | (lo1 << 16) | (hi2 << 8) | lo2;
        return Float.intBitsToFloat(bits32);
    }

    /**
     * Extract single-precision 32-bit IEEE 754 floating point from registers represented by sequence of bytes
     *
     * It is assumed that each register is encoded in most significant bit first order.
     *
     * This is identical with extractFloat32, but with registers swapped (registers with higher index before lower
     * index).
     *
     * Note that this method can return floating point NaN and floating point infinity.
     *
     * @param bytes registers represented by sequence of bytes
     * @param index index of first register. First register has index of 0.
     * @return registers (index+3), (index+2), (index+1), (index) interpreted as single-precision 32-bit IEEE 754
     *         floating point
     * @throws IllegalArgumentException when index is out of bounds
     */
    public static float extractFloat32Swap(byte[] bytes, int index) {
        assertIndexAndType(bytes, index, ValueType.FLOAT32_SWAP);
        // swapped order of registers, high 16 bits *follow* low 16 bits
        int hi1 = bytes[index + 2] & 0xff;
        int lo1 = bytes[index + 3] & 0xff;
        int hi2 = bytes[index + 0] & 0xff;
        int lo2 = bytes[index + 1] & 0xff;
        int bits32 = (hi1 << 24) | (lo1 << 16) | (hi2 << 8) | lo2;
        return Float.intBitsToFloat(bits32);
    }

    /**
     * Read data from registers and convert the result to String
     * Strings should start the the first byte of a register, but could
     * have an odd number of characters.
     * Raw byte array values are converted using the charset parameter
     * and a maximum of length bytes are read. However reading stops at the first
     * NUL byte encountered.
     *
     * Registers are read in big-endian order, i.e. two registers consisting 4 bytes (ab, cd) are parsed as sequence of
     * bytes (a,b,c,d).
     *
     * @param registers list of registers, each register represent 16bit of data
     * @param registerIndex zero based register index. Registers are handled as 16bit registers,
     *            this parameter defines the starting register.
     * @param length maximum length of string in 8bit characters (number of bytes considered)
     * @param charset the character set used to construct the string.
     * @return string representation queried value
     * @throws IllegalArgumentException when <tt>index</tt> is out of bounds of registers
     */
    public static String extractStringFromRegisters(ModbusRegisterArray registers, int registerIndex, int length,
            Charset charset) {
        return extractStringFromBytes(registers.getBytes(), registerIndex * 2, length, charset);
    }

    /**
     * Read data from bytes and convert the result to String
     *
     * Raw byte array values are converted using the charset parameter
     * and a maximum of length bytes are read. However reading stops at the first
     * NUL byte encountered.
     *
     * @param bytes bytes representing the registers
     * @param byteIndex zero based byte index
     * @param length maximum length of string in 8bit characters (number of bytes considered)
     * @param charset the character set used to construct the string.
     * @return string representation queried value
     * @throws IllegalArgumentException when <tt>index</tt> is out of bounds of registers
     */
    public static String extractStringFromBytes(byte[] bytes, int byteIndex, int length, Charset charset) {
        if (byteIndex + length > bytes.length) {
            throw new IllegalArgumentException(
                    String.format("byteIndex=%d with length=%d is out-of-bounds given registers of size %d", byteIndex,
                            length, bytes.length));
        }
        if (byteIndex < 0) {
            throw new IllegalArgumentException("Negative index values are not supported");
        }
        if (length < 0) {
            throw new IllegalArgumentException("Negative string length is not supported");
        }

        int effectiveLength = length;

        // Find first zero byte in registers and call reduce length such that we stop before it
        for (int i = 0; i < length; i++) {
            if (bytes[byteIndex + i] == '\0') {
                effectiveLength = i;
                break;
            }
        }

        return new String(bytes, byteIndex, effectiveLength, charset);
    }

    /**
     * Convert command to array of registers using a specific value type
     *
     * @param command command to be converted
     * @param type value type to use in conversion
     * @return array of registers
     * @throws NotImplementedException in cases where implementation is lacking for the type. This is thrown with 1-bit
     *             and 8-bit value types
     */
    public static ModbusRegisterArray commandToRegisters(Command command, ModbusConstants.ValueType type) {
        DecimalType numericCommand;
        if (command instanceof OnOffType || command instanceof OpenClosedType) {
            numericCommand = translateCommand2Boolean(command).get() ? new DecimalType(BigDecimal.ONE)
                    : DecimalType.ZERO;
        } else if (command instanceof DecimalType) {
            numericCommand = (DecimalType) command;
        } else {
            throw new NotImplementedException(String.format(
                    "Command '%s' of class '%s' cannot be converted to registers. Please use OnOffType, OpenClosedType, or DecimalType commands.",
                    command, command.getClass().getName()));
        }
        if (type.getBits() != 16 && type.getBits() != 32 && type.getBits() != 64) {
            throw new IllegalArgumentException(String.format(
                    "Illegal type=%s (bits=%d). Only 16bit and 32bit types are supported", type, type.getBits()));
        }
        switch (type) {
            case INT16:
            case UINT16: {
                short shortValue = numericCommand.shortValue();
                // big endian byte ordering
                byte hi = (byte) (shortValue >> 8);
                byte lo = (byte) shortValue;
                return new ModbusRegisterArray(new byte[] { hi, lo });
            }
            case INT32:
            case UINT32: {
                int intValue = numericCommand.intValue();
                // big endian byte ordering
                byte hi1 = (byte) (intValue >> 24);
                byte lo1 = (byte) (intValue >> 16);
                byte hi2 = (byte) (intValue >> 8);
                byte lo2 = (byte) intValue;
                return new ModbusRegisterArray(new byte[] { hi1, lo1, hi2, lo2 });
            }
            case INT32_SWAP:
            case UINT32_SWAP: {
                int intValue = numericCommand.intValue();
                // big endian byte ordering
                byte hi1 = (byte) (intValue >> 24);
                byte lo1 = (byte) (intValue >> 16);
                byte hi2 = (byte) (intValue >> 8);
                byte lo2 = (byte) intValue;
                // Swapped order of registers
                return new ModbusRegisterArray(new byte[] { hi2, lo2, hi1, lo1 });
            }
            case FLOAT32: {
                float floatValue = numericCommand.floatValue();
                int intBits = Float.floatToIntBits(floatValue);
                // big endian byte ordering
                byte hi1 = (byte) (intBits >> 24);
                byte lo1 = (byte) (intBits >> 16);
                byte hi2 = (byte) (intBits >> 8);
                byte lo2 = (byte) intBits;
                return new ModbusRegisterArray(new byte[] { hi1, lo1, hi2, lo2 });
            }
            case FLOAT32_SWAP: {
                float floatValue = numericCommand.floatValue();
                int intBits = Float.floatToIntBits(floatValue);
                // big endian byte ordering
                byte hi1 = (byte) (intBits >> 24);
                byte lo1 = (byte) (intBits >> 16);
                byte hi2 = (byte) (intBits >> 8);
                byte lo2 = (byte) intBits;
                // Swapped order of registers
                return new ModbusRegisterArray(new byte[] { hi2, lo2, hi1, lo1 });
            }
            case INT64:
            case UINT64: {
                long longValue = numericCommand.longValue();
                // big endian byte ordering
                byte hi1 = (byte) (longValue >> 56);
                byte lo1 = (byte) (longValue >> 48);
                byte hi2 = (byte) (longValue >> 40);
                byte lo2 = (byte) (longValue >> 32);
                byte hi3 = (byte) (longValue >> 24);
                byte lo3 = (byte) (longValue >> 16);
                byte hi4 = (byte) (longValue >> 8);
                byte lo4 = (byte) longValue;
                return new ModbusRegisterArray(new byte[] { hi1, lo1, hi2, lo2, hi3, lo3, hi4, lo4 });
            }
            case INT64_SWAP:
            case UINT64_SWAP: {
                long longValue = numericCommand.longValue();
                // big endian byte ordering
                byte hi1 = (byte) (longValue >> 56);
                byte lo1 = (byte) (longValue >> 48);
                byte hi2 = (byte) (longValue >> 40);
                byte lo2 = (byte) (longValue >> 32);
                byte hi3 = (byte) (longValue >> 24);
                byte lo3 = (byte) (longValue >> 16);
                byte hi4 = (byte) (longValue >> 8);
                byte lo4 = (byte) longValue;
                // Swapped order of registers
                return new ModbusRegisterArray(new byte[] { hi4, lo4, hi3, lo3, hi2, lo2, hi1, lo1 });
            }
            default:
                throw new NotImplementedException(
                        String.format("Illegal type=%s. Missing implementation for this type", type));
        }
    }

    /**
     * Converts command to a boolean
     *
     * true value is represented by {@link OnOffType.ON}, {@link OpenClosedType.OPEN}.
     * false value is represented by {@link OnOffType.OFF}, {@link OpenClosedType.CLOSED}.
     * Furthermore, {@link DecimalType} are converted to boolean true if they are unequal to zero.
     *
     * @param command to convert to boolean
     * @return Boolean value matching the command. Empty if command cannot be converted
     */
    public static Optional<Boolean> translateCommand2Boolean(Command command) {
        if (command.equals(OnOffType.ON)) {
            return Optional.of(Boolean.TRUE);
        }
        if (command.equals(OnOffType.OFF)) {
            return Optional.of(Boolean.FALSE);
        }
        if (command.equals(OpenClosedType.OPEN)) {
            return Optional.of(Boolean.TRUE);
        }
        if (command.equals(OpenClosedType.CLOSED)) {
            return Optional.of(Boolean.FALSE);
        }
        if (command instanceof DecimalType) {
            return Optional.of(!command.equals(DecimalType.ZERO));
        }
        return Optional.empty();
    }
}
