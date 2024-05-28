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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.openhab.core.types.Command;

/**
 * Basic unit tests for {@link GenericCommandTriggerProfile}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
public class GenericCommandTriggerProfileTest {

    public static class ParameterSet {
        public final String events;
        public final Command command;

        public ParameterSet(String events, Command command) {
            this.events = events;
            this.command = command;
        }
    }

    public static Collection<Object[]> parameters() {
        return List.of(new Object[][] { //
                { new ParameterSet("1002", OnOffType.ON) }, //
                { new ParameterSet("1002", OnOffType.OFF) }, //
                { new ParameterSet("1002,1003", PlayPauseType.PLAY) }, //
                { new ParameterSet("1002,1003", PlayPauseType.PAUSE) }, //
                { new ParameterSet("1002,1003,3001", StopMoveType.STOP) }, //
                { new ParameterSet("1002,1003,3001", StopMoveType.MOVE) }, //
                { new ParameterSet(CommonTriggerEvents.LONG_PRESSED + "," + CommonTriggerEvents.SHORT_PRESSED,
                        UpDownType.UP) }, //
                { new ParameterSet(CommonTriggerEvents.LONG_PRESSED + "," + CommonTriggerEvents.SHORT_PRESSED,
                        UpDownType.DOWN) }, //
                { new ParameterSet("1003", StringType.valueOf("SELECT")) } //
        });
    }

    private @Mock @NonNullByDefault({}) ProfileCallback mockCallback;
    private @Mock @NonNullByDefault({}) ProfileContext mockContext;

    @ParameterizedTest
    @MethodSource("parameters")
    public void testOnOffSwitchItem(ParameterSet parameterSet) {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of(AbstractTriggerProfile.PARAM_EVENTS,
                parameterSet.events, GenericCommandTriggerProfile.PARAM_COMMAND, parameterSet.command.toFullString())));

        TriggerProfile profile = new GenericCommandTriggerProfile(mockCallback, mockContext);

        verifyNoAction(profile, CommonTriggerEvents.PRESSED, parameterSet.command);
        for (String event : parameterSet.events.split(",")) {
            verifyAction(profile, event, parameterSet.command);
        }
    }

    private void verifyAction(TriggerProfile profile, String trigger, Command expectation) {
        reset(mockCallback);
        profile.onTriggerFromHandler(trigger);
        verify(mockCallback, times(1)).sendCommand(eq(expectation));
    }

    private void verifyNoAction(TriggerProfile profile, String trigger, Command expectation) {
        reset(mockCallback);
        profile.onTriggerFromHandler(trigger);
        verify(mockCallback, times(0)).sendCommand(eq(expectation));
    }
}
