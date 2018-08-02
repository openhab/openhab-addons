/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;

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
     * - same as INT8 except values are interpreted as unsigned integers
     * INT16:
     * - register with index (counting from zero) is interpreted as 16 bit signed integer.
     * - it is assumed that each register is encoded in most significant bit first order
     * UINT16:
     * - same as INT16 except values are interpreted as unsigned integers
     * INT32:
     * - registers (index) and (index + 1) are interpreted as signed 32bit integer.
     * - it assumed that the first register contains the most significant 16 bits
     * - it is assumed that each register is encoded in most significant bit first order
     * INT32_SWAP:
     * - Same as INT32 but registers swapped
     * UINT32:
     * - same as UINT32 except values are interpreted as unsigned integers
     * UINT32_SWAP:
     * - Same as UINT32 but registers swapped
     * FLOAT32:
     * - registers (index) and (index + 1) are interpreted as signed 32bit floating point number.
     * - it assumed that the first register contains the most significant 16 bits
     * - it is assumed that each register is encoded in most significant bit first order
     * FLOAT32_SWAP:
     * - Same as FLOAT32 but registers swapped
     *
     * @param registers list of registers, each register represent 16bit of data
     * @param index zero based item index. Interpretation of this depends on type, see examples above.
     *            With type larger or equal to 16 bits, the index tells the register index to start reading from.
     *            With type less than 16 bits, the index tells the N'th item to read from the registers.
     * @param type item type, e.g. unsigned 16bit integer (<tt>ModbusBindingProvider.ValueType.UINT16</tt>)
     * @return number representation queried value
     * @throws NotImplementedException in cases where implementation is lacking for the type. This can be considered a
     *             bug
     * @throws IllegalArgumentException when <tt>index</tt> is out of bounds of registers
     *
     */
    public static DecimalType extractStateFromRegisters(ModbusRegisterArray registers, int index,
            ModbusConstants.ValueType type) {
        int endBitIndex = (type.getBits() >= 16 ? 16 * index : type.getBits() * index) + type.getBits() - 1;
        // each register has 16 bits
        int lastValidIndex = registers.size() * 16 - 1;
        if (endBitIndex > lastValidIndex || index < 0) {
            throw new IllegalArgumentException(
                    String.format("Index=%d with type=%s is out-of-bounds given registers of size %d", index, type,
                            registers.size()));
        }
        switch (type) {
            case BIT:
                return new DecimalType((registers.getRegister(index / 16).toUnsignedShort() >> (index % 16)) & 1);
            case INT8:
                return new DecimalType(registers.getRegister(index / 2).getBytes()[1 - (index % 2)]);
            case UINT8:
                return new DecimalType(
                        (registers.getRegister(index / 2).toUnsignedShort() >> (8 * (index % 2))) & 0xff);
            case INT16: {
                ByteBuffer buff = ByteBuffer.allocate(2);
                buff.put(registers.getRegister(index).getBytes());
                return new DecimalType(buff.order(ByteOrder.BIG_ENDIAN).getShort(0));
            }
            case UINT16:
                return new DecimalType(registers.getRegister(index).toUnsignedShort());
            case INT32: {
                ByteBuffer buff = ByteBuffer.allocate(4);
                buff.put(registers.getRegister(index).getBytes());
                buff.put(registers.getRegister(index + 1).getBytes());
                return new DecimalType(buff.order(ByteOrder.BIG_ENDIAN).getInt(0));
            }
            case UINT32: {
                ByteBuffer buff = ByteBuffer.allocate(8);
                buff.position(4);
                buff.put(registers.getRegister(index).getBytes());
                buff.put(registers.getRegister(index + 1).getBytes());
                return new DecimalType(buff.order(ByteOrder.BIG_ENDIAN).getLong(0));
            }
            case FLOAT32: {
                ByteBuffer buff = ByteBuffer.allocate(4);
                buff.put(registers.getRegister(index).getBytes());
                buff.put(registers.getRegister(index + 1).getBytes());
                return new DecimalType(buff.order(ByteOrder.BIG_ENDIAN).getFloat(0));
            }
            case INT32_SWAP: {
                ByteBuffer buff = ByteBuffer.allocate(4);
                buff.put(registers.getRegister(index + 1).getBytes());
                buff.put(registers.getRegister(index).getBytes());
                return new DecimalType(buff.order(ByteOrder.BIG_ENDIAN).getInt(0));
            }
            case UINT32_SWAP: {
                ByteBuffer buff = ByteBuffer.allocate(8);
                buff.position(4);
                buff.put(registers.getRegister(index + 1).getBytes());
                buff.put(registers.getRegister(index).getBytes());
                return new DecimalType(buff.order(ByteOrder.BIG_ENDIAN).getLong(0));
            }
            case FLOAT32_SWAP: {
                ByteBuffer buff = ByteBuffer.allocate(4);
                buff.put(registers.getRegister(index + 1).getBytes());
                buff.put(registers.getRegister(index).getBytes());
                return new DecimalType(buff.order(ByteOrder.BIG_ENDIAN).getFloat(0));
            }
            default:
                throw new IllegalArgumentException(type.getConfigValue());
        }
    }

    /**
     * Read data from registers and convert the result to StringType
     * Strings should start the the first byte of a register, but could
     * have an odd number of characters.
     * Raw byte array values are converted using the charset parameter
     * and a maximum of length bytes are read. However reading stops at the first
     * NUL byte encountered.
     *
     * @param registers list of registers, each register represent 16bit of data
     * @param index zero based register index. Registers are handled as 16bit registers,
     *            this parameter defines the starting register.
     * @param length maximum length of string in 8bit characters.
     * @param charset the character set used to construct the string.
     * @return string representation queried value
     * @throws IllegalArgumentException when <tt>index</tt> is out of bounds of registers
     */
    public static StringType extractStringFromRegisters(ModbusRegisterArray registers, int index, int length,
            Charset charset) {
        if (index * 2 + length > registers.size() * 2) {
            throw new IllegalArgumentException(
                    String.format("Index=%d with length=%d is out-of-bounds given registers of size %d", index, length,
                            registers.size()));
        }
        if (index < 0) {
            throw new IllegalArgumentException("Negative index values are not supported");
        }
        if (length < 0) {
            throw new IllegalArgumentException("Negative string length is not supported");
        }
        byte[] buff = new byte[length];

        int src = index;
        int dest;
        for (dest = 0; dest < length; dest++) {

            byte chr;
            if (dest % 2 == 0) {
                chr = (byte) ((registers.getRegister(src).getValue() >> 8));
            } else {
                chr = (byte) (registers.getRegister(src).getValue() & 0xff);
                src++;
            }
            if (chr == 0) {
                break;
            }
            buff[dest] = chr;
        }
        return new StringType(new String(buff, 0, dest, charset));
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
        if (type.getBits() != 16 && type.getBits() != 32) {
            throw new IllegalArgumentException(String.format(
                    "Illegal type=%s (bits=%d). Only 16bit and 32bit types are supported", type, type.getBits()));
        }
        switch (type) {
            case INT16:
            case UINT16: {
                short shortValue = numericCommand.shortValue();
                // big endian byte ordering
                byte b1 = (byte) (shortValue >> 8);
                byte b2 = (byte) shortValue;

                ModbusRegister register = new BasicModbusRegister(b1, b2);
                return new BasicModbusRegisterArray(new ModbusRegister[] { register });
            }
            case INT32:
            case UINT32: {
                int intValue = numericCommand.intValue();
                // big endian byte ordering
                byte b1 = (byte) (intValue >> 24);
                byte b2 = (byte) (intValue >> 16);
                byte b3 = (byte) (intValue >> 8);
                byte b4 = (byte) intValue;
                ModbusRegister register = new BasicModbusRegister(b1, b2);
                ModbusRegister register2 = new BasicModbusRegister(b3, b4);
                return new BasicModbusRegisterArray(new ModbusRegister[] { register, register2 });
            }
            case INT32_SWAP:
            case UINT32_SWAP: {
                int intValue = numericCommand.intValue();
                // big endian byte ordering
                byte b1 = (byte) (intValue >> 24);
                byte b2 = (byte) (intValue >> 16);
                byte b3 = (byte) (intValue >> 8);
                byte b4 = (byte) intValue;
                ModbusRegister register = new BasicModbusRegister(b3, b4);
                ModbusRegister register2 = new BasicModbusRegister(b1, b2);
                return new BasicModbusRegisterArray(new ModbusRegister[] { register, register2 });
            }
            case FLOAT32: {
                float floatValue = numericCommand.floatValue();
                int intBits = Float.floatToIntBits(floatValue);
                // big endian byte ordering
                byte b1 = (byte) (intBits >> 24);
                byte b2 = (byte) (intBits >> 16);
                byte b3 = (byte) (intBits >> 8);
                byte b4 = (byte) intBits;
                ModbusRegister register = new BasicModbusRegister(b1, b2);
                ModbusRegister register2 = new BasicModbusRegister(b3, b4);
                return new BasicModbusRegisterArray(new ModbusRegister[] { register, register2 });
            }
            case FLOAT32_SWAP: {
                float floatValue = numericCommand.floatValue();
                int intBits = Float.floatToIntBits(floatValue);
                // big endian byte ordering
                byte b1 = (byte) (intBits >> 24);
                byte b2 = (byte) (intBits >> 16);
                byte b3 = (byte) (intBits >> 8);
                byte b4 = (byte) intBits;
                ModbusRegister register = new BasicModbusRegister(b3, b4);
                ModbusRegister register2 = new BasicModbusRegister(b1, b2);
                return new BasicModbusRegisterArray(new ModbusRegister[] { register, register2 });
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
