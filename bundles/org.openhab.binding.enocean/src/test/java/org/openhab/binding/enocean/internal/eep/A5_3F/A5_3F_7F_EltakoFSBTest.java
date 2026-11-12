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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.enocean.internal.handler.EnOceanBaseActuatorHandler;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.statemachine.BlindAction;
import org.openhab.binding.enocean.internal.statemachine.BlindState;
import org.openhab.binding.enocean.internal.statemachine.STMStateMachine;
import org.openhab.binding.enocean.internal.statemachine.STMTransitionConfiguration;
import org.openhab.binding.enocean.internal.statemachine.STMWatchdog;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;

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
    private final Map<String, State> channelUpdates = new HashMap<>();
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
    void watchdogsSuperviseMovementStatesWithMarginForFeedbackDelay() {
        List<STMWatchdog<BlindAction, BlindState>> watchdogs = eep.getWatchdogs(thing);

        assertThat(watchdogs, hasSize(2));
        STMWatchdog<BlindAction, BlindState> requested = watchdogs.get(0);
        assertThat(requested.timeoutAction(), is(BlindAction.MOVEMENT_TIMEOUT));
        assertThat(requested.states(), is(EnumSet.of(BlindState.MOVEMENT_POSITION_UP, BlindState.MOVEMENT_POSITION_DOWN,
                BlindState.MOVEMENT_SLATS, BlindState.MOVEMENT_CALIBRATION_UP, BlindState.MOVEMENT_CALIBRATION_DOWN)));
        // 27 s travel + 1.4 s slats + feedback margin
        assertThat(requested.timeoutMs(), is(27000L + 1400L + A5_3F_7F_EltakoFSB.FEEDBACK_MARGIN_MS));
    }

    @Test
    void manualMovementsAreSupervisedWithLongTimeout() {
        STMWatchdog<BlindAction, BlindState> manual = eep.getWatchdogs(thing).get(1);

        assertThat(manual.timeoutAction(), is(BlindAction.MOVEMENT_TIMEOUT));
        assertThat(manual.states(), is(EnumSet.of(BlindState.MOVEMENT_MANUAL_UP, BlindState.MOVEMENT_MANUAL_DOWN)));
        assertThat(manual.timeoutMs(), is(A5_3F_7F_EltakoFSB.MANUAL_MOVEMENT_TIMEOUT_MS));
    }

    @Test
    void watchdogIgnoresSlatTimeInRollershutterMode() {
        when(rollershutterChannel.getConfiguration())
                .thenReturn(new Configuration(Map.of("shutTime", 27, "swapTime", 14, "configMode", "rollershutter")));

        STMWatchdog<BlindAction, BlindState> watchdog = eep.getWatchdogs(thing).get(0);

        assertThat(watchdog.timeoutMs(), is(27000L + A5_3F_7F_EltakoFSB.FEEDBACK_MARGIN_MS));
    }

    @Test
    void noWatchdogInLegacyMode() {
        when(rollershutterChannel.getConfiguration())
                .thenReturn(new Configuration(Map.of("shutTime", 27, "swapTime", 14)));

        assertThat(eep.getWatchdogs(thing), is(empty()));
    }

    @Test
    void requestedMovementRemembersExpectedRuntime() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.IDLE);

        eep.processCmdAsUpDownType(thing, UpDownType.DOWN, new Configuration(BLINDS_CONFIG), id -> UnDefType.UNDEF,
                stm);

        assertThat(stm.getState(), is(BlindState.MOVEMENT_POSITION_DOWN));
        assertThat(stm.getExpectedRuntime(), is(270));
        assertThat(stm.isStopRequested(), is(false));
    }

    @Test
    void stopCommandIsRemembered() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_POSITION_DOWN);

        eep.processCmdAsStopMoveType(thing, StopMoveType.STOP, new Configuration(BLINDS_CONFIG), id -> UnDefType.UNDEF,
                stm);

        assertThat(stm.isStopRequested(), is(true));
    }

    @Test
    void manualMovementEndsWithPartialFeedbackAndTracksPosition() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_DOWN);

        // 11.0 s down from 20 % with closed slats: 20 + 110 * 100 / 270 = 61 %
        State result = feedback("A5006E020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "",
                id -> state(id, 20, 100), new Configuration(BLINDS_CONFIG), stm);

        assertThat(stm.getState(), is(BlindState.IDLE));
        assertThat(result, is(new PercentType(61)));
        assertThat(stm.getStoredCommand(), is(nullValue()));
    }

    @Test
    void positionUsesRuntimeInTenthsOfSeconds() {
        // 8.1 s of 27 s are 30 %, 6.5 s are 24 %
        assertThat(positionAfter("A50051020A000000DE20", 0), is(new PercentType(30)));
        assertThat(positionAfter("A50041020A000000DE20", 0), is(new PercentType(24)));
    }

    @Test
    void positionIsLimitedToValidRange() {
        // 8.1 s up from 20 % would be below 0 %
        assertThat(positionAfter("A50051010A000000DE20", 20), is(PercentType.ZERO));
        assertThat(positionAfter("A50051020A000000DE20", 90), is(PercentType.HUNDRED));
    }

    private State positionAfter(String feedbackHex, int startPosition) {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_DOWN);
        return feedback(feedbackHex).convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> state(id, startPosition, 100),
                new Configuration(BLINDS_CONFIG), stm);
    }

    @Test
    void manualMovementTurnsSlatsBeforeChangingPosition() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_DOWN);

        // 5.0 s down, slats at 45: closing them takes 55 % of 1.4 s = 0.8 s, 4.2 s remain for the blind
        State result = feedback("A50032020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "",
                id -> state(id, 0, 45), new Configuration(BLINDS_CONFIG), stm);

        assertThat(result, is(new PercentType(16)));
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.HUNDRED));
    }

    @Test
    void shortManualMovementOnlyTurnsSlats() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_DOWN);

        // 0.5 s of the 0.8 s needed to close the slats from 45
        State result = feedback("A50005020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "",
                id -> state(id, 0, 45), new Configuration(BLINDS_CONFIG), stm);

        assertThat(result, is(PercentType.ZERO));
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(new PercentType(81)));
    }

    @Test
    void manualMovementUpOpensSlatsFirst() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_UP);

        // 4.0 s up from 50 %, slats at 30: opening them takes 0.4 s, 3.6 s remain for the blind
        State result = feedback("A50028010A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "",
                id -> state(id, 50, 30), new Configuration(BLINDS_CONFIG), stm);

        assertThat(result, is(new PercentType(37)));
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.ZERO));
    }

    @Test
    void unknownAngleIsOnlySetWhenSlatsHaveTurnedFully() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_DOWN);
        feedback("A50032020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> state(id, 0, null),
                new Configuration(BLINDS_CONFIG), stm);
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.HUNDRED));

        channelUpdates.clear();
        stm = build(BlindState.MOVEMENT_MANUAL_DOWN);
        feedback("A50005020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> state(id, 0, null),
                new Configuration(BLINDS_CONFIG), stm);
        assertThat(channelUpdates.isEmpty(), is(true));
    }

    @Test
    void rollershutterModeDoesNotTouchTheDimmer() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_DOWN);

        State result = feedback("A50032020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "",
                id -> state(id, 0, 45),
                new Configuration(Map.of("shutTime", 27, "swapTime", 14, "configMode", "rollershutter")), stm);

        assertThat(result, is(new PercentType(19)));
        assertThat(channelUpdates.isEmpty(), is(true));
    }

    @Test
    void manualMovementToTheEndSetsEndAngle() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_UP);

        feedback("A5010E010A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> state(id, 50, 45),
                new Configuration(BLINDS_CONFIG), stm);

        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.ZERO));
    }

    @Test
    void calibrationRunSetsEndAngle() {
        feedback("A5010E020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> state(id, 0, 45),
                new Configuration(BLINDS_CONFIG), build(BlindState.MOVEMENT_CALIBRATION_DOWN));
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.HUNDRED));

        feedback("A5010E010A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> state(id, 100, 45),
                new Configuration(BLINDS_CONFIG), build(BlindState.MOVEMENT_CALIBRATION_UP));
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.ZERO));
    }

    @Test
    void calibrationRunKeepsAngleWhenRequestIsWaiting() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_CALIBRATION_DOWN);
        stm.storeCommand(CHANNEL_ROLLERSHUTTER, new PercentType(50));

        feedback("A5010E020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> state(id, 0, 45),
                new Configuration(BLINDS_CONFIG), stm);

        assertThat(channelUpdates.isEmpty(), is(true));
    }

    @Test
    void manualMovementEndsWithFullRuntimeFeedback() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_UP);

        State result = feedback("A5010E010A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "",
                id -> new PercentType(50), new Configuration(BLINDS_CONFIG), stm);

        assertThat(stm.getState(), is(BlindState.IDLE));
        assertThat(result, is(PercentType.ZERO));
    }

    @Test
    void movementInterruptedByUserSkipsSlatAdjustment() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_POSITION_DOWN);
        stm.startMovement(81);

        // reports 2.7 s instead of the requested 8.1 s
        State result = feedback("A5001B020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "",
                id -> state(id, 20, 45), new Configuration(BLINDS_CONFIG), stm);

        assertThat(stm.getState(), is(BlindState.IDLE));
        assertThat(stm.getStoredCommand(), is(nullValue()));
        // 0.8 s close the slats, 1.9 s remain for the blind: 20 + 7 %
        assertThat(result, is(new PercentType(27)));
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.HUNDRED));
    }

    @Test
    void movementStoppedByOpenhabKeepsSlatAdjustment() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_POSITION_DOWN);
        stm.startMovement(270);
        stm.requestStop();

        feedback("A5006E020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> new PercentType(20),
                new Configuration(BLINDS_CONFIG), stm);

        assertThat(stm.getState(), is(BlindState.POSITION_REACHED));
        assertThat(stm.getStoredCommand(), is(StopMoveType.MOVE));
    }

    @Test
    void regularEndOfRequestedMovementKeepsSlatAdjustment() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_POSITION_DOWN);
        stm.startMovement(81);

        feedback("A50051020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> new PercentType(20),
                new Configuration(BLINDS_CONFIG), stm);

        assertThat(stm.getState(), is(BlindState.POSITION_REACHED));
    }

    @Test
    void endSlightlyBeforeRequestedRuntimeIsRegular() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_POSITION_DOWN);
        stm.startMovement(81);

        // 7.5 s instead of 8.1 s is within the tolerance
        feedback("A5004B020A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> new PercentType(20),
                new Configuration(BLINDS_CONFIG), stm);

        assertThat(stm.getState(), is(BlindState.POSITION_REACHED));
    }

    private STMStateMachine<BlindAction, BlindState> build(BlindState initialState) {
        return STMStateMachine
                .build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, initialState, scheduler, null)
                .registerChannelUpdater(channelUpdates::put);
    }

    private static State state(String channelId, int position, @Nullable Integer angle) {
        if (CHANNEL_DIMMER.equals(channelId)) {
            return angle == null ? UnDefType.UNDEF : new PercentType(angle);
        }
        return new PercentType(position);
    }

    private static A5_3F_7F_EltakoFSB feedback(String payloadHex) {
        byte[] payload = HexUtils.hexToBytes(payloadHex);
        return new A5_3F_7F_EltakoFSB(new ERP1Message(payload.length, 0, payload));
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
    void slatRequestWhileUncalibratedCalibratesFirst() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.INVALID);

        eep.processChannelDimmer(thing, new PercentType(60), new Configuration(BLINDS_CONFIG), id -> UnDefType.UNDEF,
                stm);

        assertThat(stm.getState(), is(BlindState.MOVEMENT_CALIBRATION_UP));
        assertThat(stm.getStoredChannel(), is(CHANNEL_DIMMER));
        assertThat(stm.getStoredCommand(), is(new PercentType(60)));
    }

    @Test
    void lastSlatRequestDuringCalibrationWins() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.INVALID);
        Configuration config = new Configuration(BLINDS_CONFIG);
        eep.processChannelDimmer(thing, new PercentType(60), config, id -> UnDefType.UNDEF, stm);

        eep.processChannelDimmer(thing, new PercentType(30), config, id -> UnDefType.UNDEF, stm);

        assertThat(stm.getStoredCommand(), is(new PercentType(30)));
    }

    @Test
    void positionRequestDuringCalibrationIsExecutedBeforeWaitingSlatRequest() {
        EnOceanBaseActuatorHandler handler = mock(EnOceanBaseActuatorHandler.class);
        Channel dimmerChannel = mock(Channel.class);
        ChannelUID dimmerUid = new ChannelUID("enocean:rollershutter:gateway:fsb14:dimmer");
        when(thing.getHandler()).thenReturn(handler);
        when(thing.getChannel(CHANNEL_DIMMER)).thenReturn(dimmerChannel);
        when(dimmerChannel.getUID()).thenReturn(dimmerUid);
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.INVALID);
        Configuration config = new Configuration(BLINDS_CONFIG);
        eep.processChannelDimmer(thing, new PercentType(60), config, id -> UnDefType.UNDEF, stm);

        eep.processCmdAsPercentType(thing, new PercentType(30), config, id -> UnDefType.UNDEF, stm);

        verify(handler).setState(dimmerUid, new PercentType(60));
        assertThat(stm.getStoredChannel(), is(CHANNEL_ROLLERSHUTTER));
        assertThat(stm.getStoredCommand(), is(new PercentType(30)));
    }

    @Test
    void calibrationRunSetsEndAngleBeforeWaitingSlatRequest() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_CALIBRATION_UP);
        stm.storeCommand(CHANNEL_DIMMER, new PercentType(60));

        feedback("A5010E010A000000DE20").convertToStateImpl(CHANNEL_ROLLERSHUTTER, "", id -> state(id, 100, 45),
                new Configuration(BLINDS_CONFIG), stm);

        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.ZERO));
    }

    @Test
    void positionRequestDuringSlatAdjustmentIsStoredForLater() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.IDLE);
        Configuration config = new Configuration(BLINDS_CONFIG);
        eep.processChannelDimmer(thing, new PercentType(60), config, id -> state(id, 0, 0), stm);
        assertThat(stm.getState(), is(BlindState.MOVEMENT_SLATS));

        eep.processCmdAsPercentType(thing, new PercentType(30), config, id -> state(id, 0, 0), stm);

        assertThat(stm.getState(), is(BlindState.MOVEMENT_SLATS));
        assertThat(stm.getStoredChannel(), is(CHANNEL_ROLLERSHUTTER));
        assertThat(stm.getStoredCommand(), is(new PercentType(30)));
    }

    @Test
    void finishedSlatAdjustmentTriggersStoredCommandInBlindsMode() {
        assertThat(eep.getRequiredCallbackActions(thing), hasItem(BlindAction.SLATS_POS_DONE));
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
