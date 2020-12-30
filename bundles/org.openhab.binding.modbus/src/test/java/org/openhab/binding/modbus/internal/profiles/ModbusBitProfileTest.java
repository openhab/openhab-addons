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
package org.openhab.binding.modbus.internal.profiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusBitProfileTest {

    private static Stream<Arguments> provideArgsForSingleUpdateFromHandler() {
        return Stream.of(
                //
                // 0b0011_0100_0000_1111 = 13327 sint16
                //
                // low byte
                Arguments.of("1", "0", (short) 0b0011_0100_0000_1111),
                Arguments.of("1", "1", (short) 0b0011_0100_0000_1111),
                Arguments.of("1", "2", (short) 0b0011_0100_0000_1111),
                Arguments.of("1", "3", (short) 0b0011_0100_0000_1111),
                // 2nd low byte
                Arguments.of("0", "4", (short) 0b0011_0100_0000_1111),
                Arguments.of("0", "5", (short) 0b0011_0100_0000_1111),
                Arguments.of("0", "6", (short) 0b0011_0100_0000_1111),
                Arguments.of("0", "7", (short) 0b0011_0100_0000_1111),
                // 3rd low byte
                Arguments.of("0", "8", (short) 0b0011_0100_0000_1111),
                Arguments.of("0", "9", (short) 0b0011_0100_0000_1111),
                Arguments.of("1", "10", (short) 0b0011_0100_0000_1111),
                Arguments.of("0", "11", (short) 0b0011_0100_0000_1111),
                // hi byte
                Arguments.of("1", "12", (short) 0b0011_0100_0000_1111),
                Arguments.of("1", "13", (short) 0b0011_0100_0000_1111),
                Arguments.of("0", "14", (short) 0b0011_0100_0000_1111),
                Arguments.of("0", "15", (short) 0b0011_0100_0000_1111),

                //
                // 0b1011_0100_0000_1111 = -19441 sint16 (or 46095 uint16)
                //
                // low byte
                Arguments.of("1", "0", (short) 0b1011_0100_0000_1111),
                Arguments.of("1", "1", (short) 0b1011_0100_0000_1111),
                Arguments.of("1", "2", (short) 0b1011_0100_0000_1111),
                Arguments.of("1", "3", (short) 0b1011_0100_0000_1111),
                // 2nd low byte
                Arguments.of("0", "4", (short) 0b1011_0100_0000_1111),
                Arguments.of("0", "5", (short) 0b1011_0100_0000_1111),
                Arguments.of("0", "6", (short) 0b1011_0100_0000_1111),
                Arguments.of("0", "7", (short) 0b1011_0100_0000_1111),
                // 3rd low byte
                Arguments.of("0", "8", (short) 0b1011_0100_0000_1111),
                Arguments.of("0", "9", (short) 0b1011_0100_0000_1111),
                Arguments.of("1", "10", (short) 0b1011_0100_0000_1111),
                Arguments.of("0", "11", (short) 0b1011_0100_0000_1111),
                // hi byte
                Arguments.of("1", "12", (short) 0b1011_0100_0000_1111),
                Arguments.of("1", "13", (short) 0b1011_0100_0000_1111),
                Arguments.of("0", "14", (short) 0b1011_0100_0000_1111),
                Arguments.of("1", "15", (short) 0b1011_0100_0000_1111),

                // value = uint16 max
                Arguments.of("1", "0", "65535"),
                // value > uint16 max. Value is truncated and only low 16 bits (all zero with 65536!) are considered
                Arguments.of("0", "0", "65536"), Arguments.of("1", "0", "65537"),

                // value = sint16 min -32768
                Arguments.of("0", "0", "-32768"),
                // value < sint16 min. . Value is truncated and only low 16 bits (all one with -32769!) are considered
                Arguments.of("1", "0", "-32769"), Arguments.of("0", "0", "-32770"),

                // UNDEF passes the profile unchanged
                Arguments.of(UnDefType.UNDEF, "0", UnDefType.UNDEF)
        //
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForSingleUpdateFromHandler")
    public void testDecimalTypeOnStateUpdateFromHandler(Object expectedStateUpdateTowardsItem, String profileBitIndex,
            Object updateFromHandler) {
        testDecimalTypeOnUpdateFromHandlerGeneric(expectedStateUpdateTowardsItem, profileBitIndex, updateFromHandler,
                true);
    }

    @ParameterizedTest
    @MethodSource("provideArgsForSingleUpdateFromHandler")
    public void testDecimalTypeOnCommandFromHandler(Object expectedStateUpdateTowardsItem, String profileBitIndex,
            Object updateFromHandler) {
        // UNDEF is not a command, cannot be sent by handler
        assumeTrue(updateFromHandler != UnDefType.UNDEF);
        testDecimalTypeOnUpdateFromHandlerGeneric(expectedStateUpdateTowardsItem, profileBitIndex, updateFromHandler,
                false);
    }

    /**
     *
     * @param expectedStateUpdateTowardsItem expected update from the profile towards the item. If String it
     *            provided, it is treated as DecimalType. Otherwise, state is used as-is
     * @param profileBitIndex bitIndex of the profile
     * @param updateFromHandler update from handler. If String it provided, it is treated as DecimalType.
     *            Otherwise, state is used as-is
     * @param stateUpdateFromHandler whether there is state update from handler. Otherwise command
     */
    private void testDecimalTypeOnUpdateFromHandlerGeneric(Object expectedUpdateTowardsItemObj, String profileBitIndex,
            Object updateFromHandler, boolean stateUpdateFromHandler) {
        ProfileCallback callback = mock(ProfileCallback.class);
        ModbusBitProfile profile = createProfile(callback, profileBitIndex);

        Type actualUpdateTowardsItem;
        if (stateUpdateFromHandler) {
            final State state;
            if (updateFromHandler instanceof String) {
                state = new DecimalType((String) updateFromHandler);
            } else if (updateFromHandler instanceof Short) {
                state = new DecimalType((short) updateFromHandler);
            } else {
                state = (State) updateFromHandler;
            }
            profile.onStateUpdateFromHandler(state);

            ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
            verify(callback, times(1)).sendUpdate(capture.capture());
            actualUpdateTowardsItem = capture.getValue();
        } else {
            final Command command;
            if (updateFromHandler instanceof String) {
                command = new DecimalType((String) updateFromHandler);
            } else if (updateFromHandler instanceof Short) {
                command = new DecimalType((short) updateFromHandler);
            } else {
                command = (Command) updateFromHandler;
            }
            profile.onCommandFromHandler(command);

            ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
            verify(callback, times(1)).sendCommand(capture.capture());
            actualUpdateTowardsItem = capture.getValue();
        }

        State expectedUpdateTowardsItems = (expectedUpdateTowardsItemObj instanceof String)
                ? new DecimalType((String) expectedUpdateTowardsItemObj)
                : (State) expectedUpdateTowardsItemObj;
        assertEquals(expectedUpdateTowardsItems, actualUpdateTowardsItem);
    }

    private static Stream<Arguments> provideArgsForUpdateFromHandlerThenCommandFromItem()

    {
        return Stream.of(//
                // ON/OFF commands
                Arguments.of((short) 0b1011_0100_0000_1111, "1", (short) 0b1011_0100_0000_1101, OnOffType.OFF),
                Arguments.of((short) 0b1011_0100_0000_1111, "4", (short) 0b1011_0100_0001_1111, OnOffType.ON),
                //
                // DecimalType commands
                Arguments.of((short) 0b1011_0100_0000_1111, "1", (short) 0b1011_0100_0000_1101, new DecimalType(0)),
                Arguments.of((short) 0b1011_0100_0000_1111, "4", (short) 0b1011_0100_0001_1111, new DecimalType(5))

        //
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForUpdateFromHandlerThenCommandFromItem")
    public void testUpdateFromHandlerThenCommandFromItem(short stateUpdateFromHandler, String profileBitIndex,
            short expectedStateAfterCommand, Command commandFromItem) {
        DecimalType stateUpdateFromHandlerDecimal = new DecimalType(stateUpdateFromHandler);
        ProfileCallback callback = mock(ProfileCallback.class);
        ModbusBitProfile profile = createProfile(callback, profileBitIndex);

        profile.onStateUpdateFromHandler(stateUpdateFromHandlerDecimal);

        assertByteArrayEquals(intToBytes(stateUpdateFromHandler), profile.getLastState().get());

        profile.onCommandFromItem(commandFromItem);

        @SuppressWarnings("null")
        ArgumentCaptor<Command> capturedCommand = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capturedCommand.capture());

        assertByteArrayEquals(intToBytes(expectedStateAfterCommand), profile.getLastState().get());
        Command capturedCommandToBinding = capturedCommand.getValue();
        assertEquals(new DecimalType(new BigDecimal(BigInteger.valueOf(expectedStateAfterCommand))),
                capturedCommandToBinding);
    }

    private ModbusBitProfile createProfile(ProfileCallback callback, String bitIndex) {
        ProfileContext context = mock(ProfileContext.class);
        Configuration config = new Configuration();
        config.put("bit-index", bitIndex);
        when(context.getConfiguration()).thenReturn(config);

        return new ModbusBitProfile(callback, context);
    }

    private static byte[] intToBytes(int value) {
        return ModbusBitUtilities.commandToRegisters(new DecimalType(value), ValueType.INT16).getBytes();
    }

    private static void assertByteArrayEquals(byte[] expected, byte[] actual) {
        // assertion method designed only with length-2 arrays (16 bit numbers)
        assert expected.length == 2;
        String actualBinary = Integer.toBinaryString(new BigInteger(actual).intValue()).substring(15);
        assertArrayEquals(expected, actual, String.format("Expected %s, got %s (16bits: %s)", Arrays.toString(expected),
                Arrays.toString(actual), actualBinary));
    }
}
