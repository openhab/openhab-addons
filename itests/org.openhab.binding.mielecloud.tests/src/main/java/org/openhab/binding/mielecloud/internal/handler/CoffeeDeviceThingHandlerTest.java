/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.COFFEE_SYSTEM_THING_UID;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.PowerStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add info state channel and map signal flags from API tests
 * @author Björn Lange - Add elapsed time channel
 */
@NonNullByDefault
public class CoffeeDeviceThingHandlerTest extends AbstractMieleThingHandlerTest {
    @Override
    protected AbstractMieleThingHandler setUpThingHandler() {
        return createThingHandler(MieleCloudBindingConstants.THING_TYPE_COFFEE_SYSTEM, COFFEE_SYSTEM_THING_UID,
                CoffeeSystemThingHandler.class, MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER);
    }

    @Test
    public void testChannelUpdatesForNullValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.empty());
        when(deviceState.getSelectedProgram()).thenReturn(Optional.empty());
        when(deviceState.getSelectedProgramId()).thenReturn(Optional.empty());
        when(deviceState.getProgramPhase()).thenReturn(Optional.empty());
        when(deviceState.getProgramPhaseRaw()).thenReturn(Optional.empty());
        when(deviceState.getStateType()).thenReturn(Optional.empty());
        when(deviceState.getStatus()).thenReturn(Optional.empty());
        when(deviceState.getStatusRaw()).thenReturn(Optional.empty());
        when(deviceState.getElapsedTime()).thenReturn(Optional.empty());
        when(deviceState.getLightState()).thenReturn(Optional.empty());

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_ACTIVE));
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_ACTIVE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_PHASE));
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_PHASE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_ELAPSED_TIME));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
            assertEquals(NULL_VALUE_STATE, getChannelState(LIGHT_SWITCH));
        });
    }

    @Test
    public void testChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.of(true));
        when(deviceState.getSelectedProgram()).thenReturn(Optional.of("Latte Macchiato"));
        when(deviceState.getSelectedProgramId()).thenReturn(Optional.of(5L));
        when(deviceState.getProgramPhase()).thenReturn(Optional.of("Spühlen"));
        when(deviceState.getProgramPhaseRaw()).thenReturn(Optional.of(1));
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceState.getStatus()).thenReturn(Optional.of("Running"));
        when(deviceState.getStatusRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));
        when(deviceState.hasError()).thenReturn(false);
        when(deviceState.hasInfo()).thenReturn(true);
        when(deviceState.getLightState()).thenReturn(Optional.of(false));
        when(deviceState.getElapsedTime()).thenReturn(Optional.of(3));

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(new StringType("Latte Macchiato"), getChannelState(PROGRAM_ACTIVE));
            assertEquals(new DecimalType(5), getChannelState(PROGRAM_ACTIVE_RAW));
            assertEquals(new StringType("Spühlen"), getChannelState(PROGRAM_PHASE));
            assertEquals(new DecimalType(1), getChannelState(PROGRAM_PHASE_RAW));
            assertEquals(new StringType("Running"), getChannelState(OPERATION_STATE));
            assertEquals(new DecimalType(StateType.RUNNING.getCode()), getChannelState(OPERATION_STATE_RAW));
            assertEquals(new DecimalType(3), getChannelState(PROGRAM_ELAPSED_TIME));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
            assertEquals(OnOffType.OFF, getChannelState(ERROR_STATE));
            assertEquals(OnOffType.ON, getChannelState(INFO_STATE));
            assertEquals(OnOffType.OFF, getChannelState(LIGHT_SWITCH));
        });
    }

    @Test
    public void testFinishStateChannelIsSetToOnWhenProgramHasFinished() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceStateBefore = mock(DeviceState.class);
        when(deviceStateBefore.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(deviceStateBefore.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateBefore.isInState(any())).thenCallRealMethod();

        getBridgeHandler().onDeviceStateUpdated(deviceStateBefore);

        DeviceState deviceStateAfter = mock(DeviceState.class);
        when(deviceStateAfter.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(deviceStateAfter.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));
        when(deviceStateAfter.isInState(any())).thenCallRealMethod();

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceStateAfter);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.ON, getChannelState(FINISH_STATE));
        });
    }

    @Test
    public void testTransitionChannelUpdatesForNullValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceStateBefore = mock(DeviceState.class);
        when(deviceStateBefore.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(deviceStateBefore.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateBefore.isInState(any())).thenCallRealMethod();
        when(deviceStateBefore.getRemainingTime()).thenReturn(Optional.empty());

        getThingHandler().onDeviceStateUpdated(deviceStateBefore);

        DeviceState deviceStateAfter = mock(DeviceState.class);
        when(deviceStateAfter.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(deviceStateAfter.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateAfter.isInState(any())).thenCallRealMethod();
        when(deviceStateAfter.getRemainingTime()).thenReturn(Optional.empty());

        // when:
        getThingHandler().onDeviceStateUpdated(deviceStateAfter);

        waitForAssert(() -> {
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_REMAINING_TIME));
        });
    }

    @Test
    public void testTransitionChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceStateBefore = mock(DeviceState.class);
        when(deviceStateBefore.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(deviceStateBefore.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateBefore.isInState(any())).thenCallRealMethod();
        when(deviceStateBefore.getRemainingTime()).thenReturn(Optional.of(10));

        getThingHandler().onDeviceStateUpdated(deviceStateBefore);

        DeviceState deviceStateAfter = mock(DeviceState.class);
        when(deviceStateAfter.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(deviceStateAfter.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateAfter.isInState(any())).thenCallRealMethod();
        when(deviceStateAfter.getRemainingTime()).thenReturn(Optional.of(10));

        // when:
        getThingHandler().onDeviceStateUpdated(deviceStateAfter);

        waitForAssert(() -> {
            assertEquals(new DecimalType(10), getChannelState(PROGRAM_REMAINING_TIME));
        });
    }

    @Test
    public void testActionsChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        ActionsState actionsState = mock(ActionsState.class);
        when(actionsState.getDeviceIdentifier()).thenReturn(COFFEE_SYSTEM_THING_UID.getId());
        when(actionsState.canBeSwitchedOn()).thenReturn(true);
        when(actionsState.canBeSwitchedOff()).thenReturn(false);
        when(actionsState.canControlLight()).thenReturn(true);

        // when:
        getBridgeHandler().onProcessActionUpdated(actionsState);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.ON, getChannelState(REMOTE_CONTROL_CAN_BE_SWITCHED_ON));
            assertEquals(OnOffType.OFF, getChannelState(REMOTE_CONTROL_CAN_BE_SWITCHED_OFF));
            assertEquals(OnOffType.ON, getChannelState(LIGHT_CAN_BE_CONTROLLED));
        });
    }
}
