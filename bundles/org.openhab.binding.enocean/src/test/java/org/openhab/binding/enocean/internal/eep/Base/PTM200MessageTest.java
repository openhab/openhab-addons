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
package org.openhab.binding.enocean.internal.eep.Base;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_DIMMER;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_ROLLERSHUTTER;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.statemachine.BlindAction;
import org.openhab.binding.enocean.internal.statemachine.BlindState;
import org.openhab.binding.enocean.internal.statemachine.STMStateMachine;
import org.openhab.binding.enocean.internal.statemachine.STMTransitionConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;

/**
 * Tests for the feedback of the FSB14 as handled by {@link PTM200Message}.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PTM200MessageTest {

    private static final String START_UP = "F601000000DE30";
    private static final String START_DOWN = "F602000000DE30";
    private static final String END_POSITION_UP = "F670000000DE30";
    private static final String END_POSITION_DOWN = "F650000000DE30";

    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduler;
    private final Map<String, State> channelUpdates = new HashMap<>();

    private STMStateMachine<BlindAction, BlindState> build(BlindState initialState) {
        return STMStateMachine
                .build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, initialState, scheduler, null)
                .registerChannelUpdater(channelUpdates::put);
    }

    private static State receive(String payloadHex, STMStateMachine<BlindAction, BlindState> stm) {
        return receive(payloadHex, stm, new Configuration());
    }

    private static State receive(String payloadHex, STMStateMachine<BlindAction, BlindState> stm,
            Configuration config) {
        byte[] payload = HexUtils.hexToBytes(payloadHex);
        return new PTM200Message(new ERP1Message(payload.length, 0, payload)).convertToStateImpl(CHANNEL_ROLLERSHUTTER,
                "", id -> PercentType.ZERO, config, stm);
    }

    @Test
    void startWhileIdleTracksManualMovement() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.IDLE);
        assertThat(receive(START_UP, stm), is(UnDefType.UNDEF));
        assertThat(stm.getState(), is(BlindState.MOVEMENT_MANUAL_UP));

        stm = build(BlindState.IDLE);
        assertThat(receive(START_DOWN, stm), is(UnDefType.UNDEF));
        assertThat(stm.getState(), is(BlindState.MOVEMENT_MANUAL_DOWN));
    }

    @Test
    void startOfMovementRequestedByOpenhabIsIgnored() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_POSITION_DOWN);

        receive(START_DOWN, stm);

        assertThat(stm.getState(), is(BlindState.MOVEMENT_POSITION_DOWN));
    }

    @Test
    void startWhileUncalibratedIsIgnored() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.INVALID);

        receive(START_UP, stm);

        assertThat(stm.getState(), is(BlindState.INVALID));
    }

    @Test
    void endPositionEndsManualMovement() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_UP);
        assertThat(receive(END_POSITION_UP, stm), is(PercentType.ZERO));
        assertThat(stm.getState(), is(BlindState.IDLE));

        stm = build(BlindState.MOVEMENT_MANUAL_DOWN);
        assertThat(receive(END_POSITION_DOWN, stm), is(PercentType.HUNDRED));
        assertThat(stm.getState(), is(BlindState.IDLE));
    }

    @Test
    void endPositionOfOtherDirectionDoesNotEndManualMovement() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.MOVEMENT_MANUAL_UP);

        receive(END_POSITION_DOWN, stm);

        assertThat(stm.getState(), is(BlindState.MOVEMENT_MANUAL_UP));
    }

    @Test
    void endPositionAfterManualMovementSetsEndAngleOfBlinds() {
        Configuration blinds = new Configuration(Map.of("shutTime", 27, "swapTime", 14, "configMode", "blinds"));

        receive(END_POSITION_UP, build(BlindState.MOVEMENT_MANUAL_UP), blinds);
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.ZERO));

        receive(END_POSITION_DOWN, build(BlindState.MOVEMENT_MANUAL_DOWN), blinds);
        assertThat(channelUpdates.get(CHANNEL_DIMMER), is(PercentType.HUNDRED));
    }

    @Test
    void endPositionDoesNotTouchTheDimmerOutsideBlindsMode() {
        Configuration rollershutter = new Configuration(
                Map.of("shutTime", 27, "swapTime", 14, "configMode", "rollershutter"));

        receive(END_POSITION_UP, build(BlindState.MOVEMENT_MANUAL_UP), rollershutter);

        assertThat(channelUpdates.isEmpty(), is(true));
    }

    @Test
    void endPositionCalibratesUncalibratedStateMachine() {
        STMStateMachine<BlindAction, BlindState> stm = build(BlindState.INVALID);

        receive(END_POSITION_UP, stm);

        assertThat(stm.getState(), is(BlindState.IDLE));
    }
}
