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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.State;

/**
 * Debounces a {@link State} by counting.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class DebounceCountingStateProfileTest {

    public static class ParameterSet {
        public final List<State> sourceStates;
        public final List<State> resultingStates;
        public final int numberOfChanges;

        public ParameterSet(List<State> sourceStates, List<State> resultingStates, int numberOfChanges) {
            this.sourceStates = sourceStates;
            this.resultingStates = resultingStates;
            this.numberOfChanges = numberOfChanges;
        }
    }

    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] { //
                { new ParameterSet(List.of(OnOffType.ON), List.of(OnOffType.ON), 0) }, //
                { new ParameterSet(List.of(OnOffType.ON), List.of(OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON), List.of(OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF), List.of(OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.OFF), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.ON, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.ON, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.OFF, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF, OnOffType.OFF), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.OFF, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF, OnOffType.OFF), 1) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.ON, OnOffType.OFF, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.ON, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.ON, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.OFF, OnOffType.ON),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.ON), 2) }, //
                { new ParameterSet(List.of(OnOffType.ON, OnOffType.OFF, OnOffType.OFF, OnOffType.OFF),
                        List.of(OnOffType.ON, OnOffType.ON, OnOffType.ON, OnOffType.OFF), 2) } //
        });
    }

    private @NonNullByDefault({}) @Mock ProfileCallback mockCallback;
    private @NonNullByDefault({}) @Mock ProfileContext mockContext;

    @Test
    public void testWrongParameterLower() {
        assertThrows(IllegalArgumentException.class, () -> initProfile(-1));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnStateUpdateFromHandler(ParameterSet parameterSet) {
        final StateProfile profile = initProfile(parameterSet.numberOfChanges);
        for (int i = 0; i < parameterSet.sourceStates.size(); i++) {
            verifySendUpdate(profile, parameterSet.sourceStates.get(i), parameterSet.resultingStates.get(i));
        }
    }

    private StateProfile initProfile(int numberOfChanges) {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("numberOfChanges", numberOfChanges)));
        return new DebounceCountingStateProfile(mockCallback, mockContext);
    }

    private void verifySendUpdate(StateProfile profile, State state, State expectedState) {
        reset(mockCallback);
        profile.onStateUpdateFromHandler(state);
        verify(mockCallback, times(1)).sendUpdate(eq(expectedState));
    }
}
