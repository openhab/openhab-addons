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
package org.openhab.binding.modbus.hager.internal.modbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.BitFlag;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RegisterDecoder} turns the registers of one read block into channel states.
 * Registers are encoded most significant word first, which is the default order of the Modbus transport.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class RegisterDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterDecoder.class);

    private static final int BITS_PER_REGISTER = 16;
    private static final int BYTES_PER_REGISTER = 2;
    private static final int F64_BYTES = 8;

    private RegisterDecoder() {
        // static helper, not instantiable
    }

    /**
     * Decodes one register of the given read block.
     *
     * @param context identifies the Thing the register belongs to in the log messages
     * @param register the register to decode
     * @param registers the registers answered for the read block
     * @param blockStart address of the first register of the read block
     * @return one state per channel the register publishes to; empty for registers which are deliberately not decoded
     */
    public static List<ChannelUpdate> decode(String context, Register register, ModbusRegisterArray registers,
            int blockStart) {
        int index = register.address() - blockStart;
        if (index < 0 || index + register.length() > registers.size()) {
            LOGGER.debug("[{}] Register {} at {} is not part of the block starting at {}", context,
                    register.channelId(), register.address(), blockStart);
            return List.of();
        }
        if (!register.bitFlags().isEmpty()) {
            return decodeBits(context, register, registers, index);
        }
        Optional<State> state = switch (register.dataType()) {
            case U16 -> Optional.of(toState(context, registers.getRegister(index), register));
            case I16 -> Optional.of(toState(context, (short) registers.getRegister(index), register));
            case U32 -> Optional.of(toState(context, toUInt32(registers, index), register));
            case I32 -> Optional.of(toState(context, (int) toUInt32(registers, index), register));
            case F32 -> Optional.of(toState(context, Float.intBitsToFloat((int) toUInt32(registers, index)), register));
            case F64 -> Optional.of(toState(context, toDouble(registers, index), register));
            case STRING -> Optional.of(new StringType(ModbusBitUtilities.extractStringFromRegisters(registers, index,
                    register.length() * BYTES_PER_REGISTER, StandardCharsets.US_ASCII).trim()));
            case BOOL -> Optional.of(OnOffType.from(registers.getRegister(index) != 0));
            // BITFIELD mixes sub fields which are not decoded; BITSTRING publishes its bits instead of a value
            case BITFIELD, BITSTRING -> Optional.<State> empty();
        };
        return state.map(value -> List.of(new ChannelUpdate(register.groupId(), register.channelId(), value)))
                .orElseGet(List::of);
    }

    private static List<ChannelUpdate> decodeBits(String context, Register register, ModbusRegisterArray registers,
            int index) {
        List<ChannelUpdate> updates = new ArrayList<>(register.bitFlags().size());
        for (BitFlag flag : register.bitFlags()) {
            int registerIndex = index + flag.bit() / BITS_PER_REGISTER;
            if (registerIndex >= registers.size()) {
                LOGGER.debug("[{}] Bit {} of register {} is outside the read block", context, flag.bit(),
                        register.channelId());
                continue;
            }
            boolean set = ((registers.getRegister(registerIndex) >> (flag.bit() % BITS_PER_REGISTER)) & 1) == 1;
            updates.add(new ChannelUpdate(register.groupId(), flag.channelId(), OnOffType.from(set)));
        }
        return updates;
    }

    private static State toState(String context, double value, Register register) {
        Map<Integer, String> enumValues = register.enumValues();
        if (!enumValues.isEmpty()) {
            String label = enumValues.get((int) value);
            if (label == null) {
                LOGGER.debug("[{}] Value {} of register {} is not part of the enumeration", context, value,
                        register.channelId());
                return UnDefType.UNDEF;
            }
            return new StringType(label);
        }
        Unit<?> unit = register.unit();
        if (unit != null) {
            return QuantityType.valueOf(value, unit);
        }
        return isInteger(register.dataType()) ? new DecimalType((long) value) : new DecimalType(value);
    }

    private static boolean isInteger(DataType dataType) {
        return switch (dataType) {
            case U16, I16, U32, I32 -> true;
            case F32, F64, STRING, BOOL, BITSTRING, BITFIELD -> false;
        };
    }

    private static long toUInt32(ModbusRegisterArray registers, int index) {
        return ((long) registers.getRegister(index) << BITS_PER_REGISTER) | registers.getRegister(index + 1);
    }

    private static double toDouble(ModbusRegisterArray registers, int index) {
        // F64 registers span four words and are encoded most significant word first
        return ByteBuffer.wrap(registers.getBytes(), index * BYTES_PER_REGISTER, F64_BYTES).order(ByteOrder.BIG_ENDIAN)
                .getDouble();
    }
}
