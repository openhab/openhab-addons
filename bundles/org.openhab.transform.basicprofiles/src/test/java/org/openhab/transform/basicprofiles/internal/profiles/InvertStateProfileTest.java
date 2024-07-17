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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

/**
 * Basic unit tests for {@link InvertStateProfile}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
public class InvertStateProfileTest {

    private static final DateTimeType NOW = new DateTimeType();

    public static class ParameterSet {
        public Type type;
        public Type resultingType;

        public ParameterSet(Type source, Type result) {
            this.type = source;
            this.resultingType = result;
        }
    }

    public static Collection<Object[]> parameters() {
        return List.of(new Object[][] { //
                { new ParameterSet(UnDefType.NULL, UnDefType.NULL) }, //
                { new ParameterSet(UnDefType.UNDEF, UnDefType.UNDEF) }, //
                { new ParameterSet(new QuantityType<>(25, Units.LITRE), new QuantityType<>(-25, Units.LITRE)) }, //
                { new ParameterSet(PercentType.ZERO, PercentType.HUNDRED) }, //
                { new ParameterSet(PercentType.HUNDRED, PercentType.ZERO) }, //
                { new ParameterSet(new PercentType(25), new PercentType(75)) }, //
                { new ParameterSet(new DecimalType(25L), new DecimalType(-25L)) }, //
                { new ParameterSet(OnOffType.ON, OnOffType.OFF) }, //
                { new ParameterSet(OnOffType.OFF, OnOffType.ON) }, //
                { new ParameterSet(NOW, NOW) } //
        });
    }

    private @Mock @NonNullByDefault({}) ProfileCallback mockCallback;

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnCommandFromHandler(ParameterSet parameterSet) {
        final StateProfile profile = initProfile();
        if (parameterSet.type instanceof Command && parameterSet.resultingType instanceof Command) {
            verifyCommandFromItem(profile, (Command) parameterSet.type, (Command) parameterSet.resultingType);
            verifyCommandFromHandler(profile, (Command) parameterSet.type, (Command) parameterSet.resultingType);
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnStateUpdateFromHandler(ParameterSet parameterSet) {
        final StateProfile profile = initProfile();
        if (parameterSet.type instanceof State && parameterSet.resultingType instanceof State) {
            verifyStateUpdateFromHandler(profile, (State) parameterSet.type, (State) parameterSet.resultingType);
        }
    }

    private StateProfile initProfile() {
        return new InvertStateProfile(mockCallback);
    }

    private void verifyCommandFromItem(StateProfile profile, Command command, Command result) {
        reset(mockCallback);
        profile.onCommandFromItem(command);
        verify(mockCallback, times(1)).handleCommand(eq(result));
    }

    private void verifyCommandFromHandler(StateProfile profile, Command command, Command result) {
        reset(mockCallback);
        profile.onCommandFromHandler(command);
        verify(mockCallback, times(1)).sendCommand(eq(result));
    }

    private void verifyStateUpdateFromHandler(StateProfile profile, State state, State result) {
        reset(mockCallback);
        profile.onStateUpdateFromHandler(state);
        verify(mockCallback, times(1)).sendUpdate(eq(result));
    }
}
