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
package org.openhab.binding.enocean.internal.eep.A5_3F;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_DIMMER;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_ROLLERSHUTTER;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.enocean.internal.statemachine.BlindAction;
import org.openhab.binding.enocean.internal.statemachine.BlindState;
import org.openhab.binding.enocean.internal.statemachine.STMStateMachine;
import org.openhab.binding.enocean.internal.statemachine.STMTransitionConfiguration;
import org.openhab.binding.enocean.internal.statemachine.STMWatchdog;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for the state machine handling of {@link A5_3F_7F_EltakoFSB}.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class A5_3F_7F_EltakoFSBTest {

    private static final Map<String, Object> BLINDS_CONFIG = Map.of("shutTime", 27, "swapTime", 14, "configMode",
            "blinds");

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) Channel rollershutterChannel;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduler;
    private final A5_3F_7F_EltakoFSB eep = new A5_3F_7F_EltakoFSB();

    @BeforeEach
    void setUp() {
        when(thing.getUID()).thenReturn(new ThingUID("enocean:rollershutter:gateway:fsb14"));
        when(thing.getChannel(CHANNEL_ROLLERSHUTTER)).thenReturn(rollershutterChannel);
        when(rollershutterChannel.getConfiguration()).thenReturn(new Configuration(BLINDS_CONFIG));
    }

    @Test
    void transientStatesAreReplacedByInvalidOnStartup() {
        for (BlindState persisted : BlindState.values()) {
            BlindState expected = persisted == BlindState.IDLE ? BlindState.IDLE : BlindState.INVALID;
            assertThat("persisted " + persisted, eep.getStateOnStartup(persisted), is(expected));
        }
    }

    @Test
    void missingPersistedStateIsPassedThrough() {
        assertThat(eep.getStateOnStartup(null), is(nullValue()));
    }

    @Test
    void watchdogSupervisesMovementStatesWithMarginForFeedbackDelay() {
        STMWatchdog<BlindAction, BlindState> watchdog = Objects.requireNonNull(eep.getWatchdog(thing));

        assertThat(watchdog.timeoutAction(), is(BlindAction.MOVEMENT_TIMEOUT));
        assertThat(watchdog.states(), is(EnumSet.of(BlindState.MOVEMENT_POSITION_UP, BlindState.MOVEMENT_POSITION_DOWN,
                BlindState.MOVEMENT_SLATS, BlindState.MOVEMENT_CALIBRATION_UP, BlindState.MOVEMENT_CALIBRATION_DOWN)));
        // 27 s travel + 1.4 s slats + feedback margin
        assertThat(watchdog.timeoutMs(), is(27000L + 1400L + A5_3F_7F_EltakoFSB.FEEDBACK_MARGIN_MS));
    }

    @Test
    void watchdogIgnoresSlatTimeInRollershutterMode() {
        when(rollershutterChannel.getConfiguration())
                .thenReturn(new Configuration(Map.of("shutTime", 27, "swapTime", 14, "configMode", "rollershutter")));

        STMWatchdog<BlindAction, BlindState> watchdog = Objects.requireNonNull(eep.getWatchdog(thing));

        assertThat(watchdog.timeoutMs(), is(27000L + A5_3F_7F_EltakoFSB.FEEDBACK_MARGIN_MS));
    }

    @Test
    void noWatchdogInLegacyMode() {
        when(rollershutterChannel.getConfiguration())
                .thenReturn(new Configuration(Map.of("shutTime", 27, "swapTime", 14)));

        assertThat(eep.getWatchdog(thing), is(nullValue()));
    }

    @Test
    void slatAdjustmentIsSkippedWhenDimmerHasNoValue() {
        STMStateMachine<BlindAction, BlindState> stm = STMStateMachine.build(
                STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, BlindState.POSITION_REACHED, scheduler, null);

        eep.processCmdAsStopMoveType(thing, StopMoveType.MOVE, new Configuration(BLINDS_CONFIG), id -> UnDefType.UNDEF,
                stm);

        assertThat(stm.getState(), is(BlindState.IDLE));
    }

    @Test
    void slatAdjustmentIsStartedWhenDimmerHasValue() {
        STMStateMachine<BlindAction, BlindState> stm = STMStateMachine.build(
                STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, BlindState.POSITION_REACHED, scheduler, null);

        eep.processCmdAsStopMoveType(thing, StopMoveType.MOVE, new Configuration(BLINDS_CONFIG),
                id -> dimmerValue(id, 50), stm);

        assertThat(stm.getState(), is(BlindState.MOVEMENT_SLATS));
    }

    @Test
    void stopDuringCalibrationDiscardsStoredCommand() {
        STMStateMachine<BlindAction, BlindState> stm = STMStateMachine
                .build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, BlindState.INVALID, scheduler, null);
        Configuration config = new Configuration(BLINDS_CONFIG);

        eep.processCmdAsPercentType(thing, new PercentType(50), config, id -> UnDefType.UNDEF, stm);
        assertThat(stm.getState(), is(BlindState.MOVEMENT_CALIBRATION_UP));
        assertThat(stm.getStoredCommand(), is(new PercentType(50)));

        eep.processCmdAsStopMoveType(thing, StopMoveType.STOP, config, id -> UnDefType.UNDEF, stm);

        assertThat(stm.getState(), is(BlindState.INVALID));
        assertThat(stm.getStoredCommand(), is(nullValue()));
    }

    private State dimmerValue(String channelId, int value) {
        return CHANNEL_DIMMER.equals(channelId) ? new PercentType(value) : UnDefType.UNDEF;
    }
}
