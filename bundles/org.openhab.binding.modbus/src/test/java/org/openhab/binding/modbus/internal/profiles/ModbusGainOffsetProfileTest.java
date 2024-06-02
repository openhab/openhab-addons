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
package org.openhab.binding.modbus.internal.profiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusGainOffsetProfileTest {

    static Stream<Arguments> provideArgsForBoth() {
        return Stream.of(
                // dimensionless
                Arguments.of("100", "0.5", "250", "175.0"), Arguments.of("0", "1 %", "250", "250 %"),
                //
                // gain with same unit
                //
                // e.g. (handler) 3 <---> (item) 106K with pre-gain-offset=50, gain=2K
                // e.g. (handler) 3 K <---> (item) 106K^2 with pre-gain-offset=50K, gain=2K
                //
                Arguments.of("50", "2 K", "3", "106 K"),
                //
                // gain with different unit
                //
                Arguments.of("50", "2 m/s", "3", "106 m/s"),
                //
                // gain without unit
                //
                Arguments.of("50", "2", "3", "106"),
                //
                // temperature tests
                //
                // celsius gain
                Arguments.of("0", "0.1 °C", "25", "2.5 °C"),
                // kelvin gain
                Arguments.of("0", "0.1 K", "25", "2.5 K"),
                // fahrenheit gain
                Arguments.of("0", "10 °F", "0.18", "1.80 °F"),
                //
                // unsupported types are passed with error
                Arguments.of("0", "0", OnOffType.ON, OnOffType.ON)

        );
    }

    static Stream<Arguments> provideAdditionalArgsForStateUpdateFromHandler() {
        return Stream.of(
                // Dimensionless conversion 2.5/1% = 250%/1% = 250
                Arguments.of("0", "1 %", "250", "250 %"), Arguments.of("2 %", "1 %", "249.9800", "250.0000 %"),
                Arguments.of("50", "2 m/s", new DecimalType("3"), "106 m/s"),
                // UNDEF passes the profile unchanged
                Arguments.of("0", "0", UnDefType.UNDEF, UnDefType.UNDEF));
    }

    /**
     *
     * Test profile behaviour when handler updates the state
     *
     */
    @ParameterizedTest
    @MethodSource({ "provideArgsForBoth", "provideAdditionalArgsForStateUpdateFromHandler" })
    public void testOnStateUpdateFromHandler(String preGainOffset, String gain, Object updateFromHandlerObj,
            Object expectedUpdateTowardsItemObj) {
        testOnUpdateFromHandlerGeneric(preGainOffset, gain, updateFromHandlerObj, expectedUpdateTowardsItemObj, true);
    }

    /**
     *
     * Test profile behaviour when handler sends command
     *
     */
    @ParameterizedTest
    @MethodSource({ "provideArgsForBoth", "provideAdditionalArgsForStateUpdateFromHandler" })
    public void testOnCommandFromHandler(String preGainOffset, String gain, Object updateFromHandlerObj,
            Object expectedUpdateTowardsItemObj) {
        // UNDEF is not a command, cannot be sent by handler
        assumeTrue(updateFromHandlerObj != UnDefType.UNDEF);
        testOnUpdateFromHandlerGeneric(preGainOffset, gain, updateFromHandlerObj, expectedUpdateTowardsItemObj, false);
    }

    /**
     *
     * Test profile behaviour when handler updates the state
     *
     * @param preGainOffset profile pre-gain-offset offset
     * @param gain profile gain
     * @param updateFromHandlerObj state update from handler. String representing QuantityType or State/Command
     * @param expectedUpdateTowardsItemObj expected state/command update towards item. String representing QuantityType
     *            or
     *            State
     * @param stateUpdateFromHandler whether there is state update from handler. Otherwise command
     */
    @SuppressWarnings("rawtypes")
    private void testOnUpdateFromHandlerGeneric(String preGainOffset, String gain, Object updateFromHandlerObj,
            Object expectedUpdateTowardsItemObj, boolean stateUpdateFromHandler) {
        ProfileCallback callback = mock(ProfileCallback.class);
        ModbusGainOffsetProfile profile = createProfile(callback, gain, preGainOffset);

        final Type actualStateUpdateTowardsItem;
        if (stateUpdateFromHandler) {
            final State updateFromHandler;
            if (updateFromHandlerObj instanceof String str) {
                updateFromHandler = new QuantityType(str);
            } else {
                assertTrue(updateFromHandlerObj instanceof State);
                updateFromHandler = (State) updateFromHandlerObj;
            }

            profile.onStateUpdateFromHandler(updateFromHandler);

            ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
            verify(callback, times(1)).sendUpdate(capture.capture());
            actualStateUpdateTowardsItem = capture.getValue();
        } else {
            final Command updateFromHandler;
            if (updateFromHandlerObj instanceof String str) {
                updateFromHandler = new QuantityType(str);
            } else {
                assertTrue(updateFromHandlerObj instanceof State);
                updateFromHandler = (Command) updateFromHandlerObj;
            }

            profile.onCommandFromHandler(updateFromHandler);

            ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
            verify(callback, times(1)).sendCommand(capture.capture());
            actualStateUpdateTowardsItem = capture.getValue();
        }

        Type expectedStateUpdateTowardsItem = (expectedUpdateTowardsItemObj instanceof String s) ? new QuantityType(s)
                : (Type) expectedUpdateTowardsItemObj;
        assertEquals(expectedStateUpdateTowardsItem, actualStateUpdateTowardsItem);
        verifyNoMoreInteractions(callback);
    }

    static Stream<Arguments> provideAdditionalArgsForCommandFromItem() {
        return Stream.of(
                // Dimensionless conversion 2.5/1% = 250%/1% = 250
                // gain in %, command as bare ratio and the other way around
                Arguments.of("0", "1 %", "250", "2.5"), Arguments.of("2%", "1 %", "249.9800", "2.5"),

                // celsius gain, kelvin command
                Arguments.of("0", "0.1 °C", "-2706.5", "2.5 K"),

                // incompatible command unit, should be convertible with gain
                Arguments.of("0", "0.1 °C", null, "2.5 m/s"),
                //
                // incompatible offset unit
                //
                Arguments.of("50 K", "21", null, "30 m/s"), Arguments.of("50 m/s", "21", null, "30 K"),
                //
                // UNDEF command is not processed
                //
                Arguments.of("0", "0", null, UnDefType.UNDEF),
                //
                // REFRESH command is forwarded
                //
                Arguments.of("0", "0", RefreshType.REFRESH, RefreshType.REFRESH)

        );
    }

    /**
     *
     * Test profile behavior when item receives command
     *
     * @param preGainOffset profile pre-gain-offset
     * @param gain profile gain
     * @param expectedCommandTowardsHandlerObj expected command towards handler. String representing QuantityType or
     *            Command. Use null to verify that no commands are sent to handler.
     * @param commandFromItemObj command that item receives. String representing QuantityType or Command.
     */
    @SuppressWarnings({ "rawtypes" })
    @ParameterizedTest
    @MethodSource({ "provideArgsForBoth", "provideAdditionalArgsForCommandFromItem" })
    public void testOnCommandFromItem(String preGainOffset, String gain,
            @Nullable Object expectedCommandTowardsHandlerObj, Object commandFromItemObj) {
        assumeFalse(commandFromItemObj.equals(UnDefType.UNDEF));
        ProfileCallback callback = mock(ProfileCallback.class);
        ModbusGainOffsetProfile profile = createProfile(callback, gain, preGainOffset);

        Command commandFromItem = (commandFromItemObj instanceof String str) ? new QuantityType(str)
                : (Command) commandFromItemObj;
        profile.onCommandFromItem(commandFromItem);

        boolean callsExpected = expectedCommandTowardsHandlerObj != null;
        if (callsExpected) {
            ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
            verify(callback, times(1)).handleCommand(capture.capture());
            Command actualCommandTowardsHandler = capture.getValue();
            Command expectedCommandTowardsHandler = (expectedCommandTowardsHandlerObj instanceof String str)
                    ? new QuantityType(str)
                    : (Command) expectedCommandTowardsHandlerObj;
            assertEquals(expectedCommandTowardsHandler, actualCommandTowardsHandler);
            verifyNoMoreInteractions(callback);
        } else {
            verifyNoInteractions(callback);
        }
    }

    /**
     *
     * Test behaviour when item receives state update from item (no-op)
     *
     **/
    @Test
    public void testOnCommandFromItem() {
        ProfileCallback callback = mock(ProfileCallback.class);
        ModbusGainOffsetProfile<?> profile = createProfile(callback, "1.0", "0.0");

        profile.onStateUpdateFromItem(new DecimalType(3.78));
        // should be no-op
        verifyNoInteractions(callback);
    }

    @Test
    public void testInvalidInit() {
        // preGainOffset must be dimensionless
        ProfileCallback callback = mock(ProfileCallback.class);
        ModbusGainOffsetProfile<?> profile = createProfile(callback, "1.0", "0.0 K");
        assertFalse(profile.isValid());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void testInitGainDefault(String gain) {
        ProfileCallback callback = mock(ProfileCallback.class);
        ModbusGainOffsetProfile<?> p = createProfile(callback, gain, "0.0");
        assertTrue(p.isValid());
        assertEquals(p.getGain(), Optional.of(QuantityType.ONE));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void testInitOffsetDefault(String preGainOffset) {
        ProfileCallback callback = mock(ProfileCallback.class);
        ModbusGainOffsetProfile<?> p = createProfile(callback, "1", preGainOffset);
        assertTrue(p.isValid());
        assertEquals(p.getPregainOffset(), Optional.of(QuantityType.ZERO));
    }

    private ModbusGainOffsetProfile<?> createProfile(ProfileCallback callback, @Nullable String gain,
            @Nullable String preGainOffset) {
        ProfileContext context = mock(ProfileContext.class);
        Configuration config = new Configuration();
        if (gain != null) {
            config.put("gain", gain);
        }
        if (preGainOffset != null) {
            config.put("pre-gain-offset", preGainOffset);
        }
        when(context.getConfiguration()).thenReturn(config);

        return new ModbusGainOffsetProfile<>(callback, context);
    }
}
