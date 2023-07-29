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
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.OVEN_DEVICE_THING_UID;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.PowerStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.ProgramStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;

/**
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add pre-heat finished channel
 * @author Benjamin Bolte - Add info state channel and map signal flags from API tests
 * @author Björn Lange - Add elapsed time channel
 */
@NonNullByDefault
public class OvenDeviceThingHandlerTest extends AbstractMieleThingHandlerTest {
    @Override
    protected AbstractMieleThingHandler setUpThingHandler() {
        return createThingHandler(MieleCloudBindingConstants.THING_TYPE_OVEN, OVEN_DEVICE_THING_UID,
                OvenDeviceThingHandler.class, MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER);
    }

    @Test
    public void testChannelUpdatesForNullValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
        when(deviceState.getStateType()).thenReturn(Optional.empty());
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.empty());
        when(deviceState.getSelectedProgram()).thenReturn(Optional.empty());
        when(deviceState.getSelectedProgramId()).thenReturn(Optional.empty());
        when(deviceState.getProgramPhase()).thenReturn(Optional.empty());
        when(deviceState.getProgramPhaseRaw()).thenReturn(Optional.empty());
        when(deviceState.getStatus()).thenReturn(Optional.empty());
        when(deviceState.getStatusRaw()).thenReturn(Optional.empty());
        when(deviceState.getStartTime()).thenReturn(Optional.empty());
        when(deviceState.getElapsedTime()).thenReturn(Optional.empty());
        when(deviceState.hasPreHeatFinished()).thenReturn(Optional.empty());
        when(deviceState.getTargetTemperature(0)).thenReturn(Optional.empty());
        when(deviceState.getTemperature(0)).thenReturn(Optional.empty());
        when(deviceState.getLightState()).thenReturn(Optional.empty());
        when(deviceState.getDoorState()).thenReturn(Optional.empty());

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
            assertEquals(new StringType(ProgramStatus.PROGRAM_STOPPED.getState()), getChannelState(PROGRAM_START_STOP));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
            assertEquals(NULL_VALUE_STATE, getChannelState(DELAYED_START_TIME));
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_ELAPSED_TIME));
            assertEquals(NULL_VALUE_STATE, getChannelState(PRE_HEAT_FINISHED));
            assertEquals(NULL_VALUE_STATE, getChannelState(TEMPERATURE_TARGET));
            assertEquals(NULL_VALUE_STATE, getChannelState(TEMPERATURE_CURRENT));
            assertEquals(NULL_VALUE_STATE, getChannelState(LIGHT_SWITCH));
            assertEquals(NULL_VALUE_STATE, getChannelState(DOOR_STATE));
        });
    }

    @Test
    public void testChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.isInState(any())).thenCallRealMethod();
        when(deviceState.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.of(false));
        when(deviceState.getSelectedProgram()).thenReturn(Optional.of("Grill"));
        when(deviceState.getSelectedProgramId()).thenReturn(Optional.of(6L));
        when(deviceState.getProgramPhase()).thenReturn(Optional.of("Heat"));
        when(deviceState.getProgramPhaseRaw()).thenReturn(Optional.of(6));
        when(deviceState.getStatus()).thenReturn(Optional.of("Running"));
        when(deviceState.getStatusRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));
        when(deviceState.getStartTime()).thenReturn(Optional.of(3600));
        when(deviceState.getElapsedTime()).thenReturn(Optional.of(62));
        when(deviceState.hasPreHeatFinished()).thenReturn(Optional.of(true));
        when(deviceState.getTargetTemperature(0)).thenReturn(Optional.of(180));
        when(deviceState.getTemperature(0)).thenReturn(Optional.of(181));
        when(deviceState.hasError()).thenReturn(false);
        when(deviceState.hasInfo()).thenReturn(true);
        when(deviceState.getLightState()).thenReturn(Optional.of(false));
        when(deviceState.getDoorState()).thenReturn(Optional.of(false));

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(new StringType("Grill"), getChannelState(PROGRAM_ACTIVE));
            assertEquals(new DecimalType(6), getChannelState(PROGRAM_ACTIVE_RAW));
            assertEquals(new StringType("Heat"), getChannelState(PROGRAM_PHASE));
            assertEquals(new DecimalType(6), getChannelState(PROGRAM_PHASE_RAW));
            assertEquals(new StringType("Running"), getChannelState(OPERATION_STATE));
            assertEquals(new DecimalType(StateType.RUNNING.getCode()), getChannelState(OPERATION_STATE_RAW));
            assertEquals(new StringType(ProgramStatus.PROGRAM_STARTED.getState()), getChannelState(PROGRAM_START_STOP));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
            assertEquals(new DecimalType(3600), getChannelState(DELAYED_START_TIME));
            assertEquals(new DecimalType(62), getChannelState(PROGRAM_ELAPSED_TIME));
            assertEquals(OnOffType.ON, getChannelState(PRE_HEAT_FINISHED));
            assertEquals(new QuantityType<>(180, SIUnits.CELSIUS), getChannelState(TEMPERATURE_TARGET));
            assertEquals(new QuantityType<>(181, SIUnits.CELSIUS), getChannelState(TEMPERATURE_CURRENT));
            assertEquals(OnOffType.OFF, getChannelState(ERROR_STATE));
            assertEquals(OnOffType.ON, getChannelState(INFO_STATE));
            assertEquals(OnOffType.OFF, getChannelState(LIGHT_SWITCH));
            assertEquals(OnOffType.OFF, getChannelState(DOOR_STATE));
        });
    }

    @Test
    public void testFinishStateChannelIsSetToOnWhenProgramHasFinished() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceStateBefore = mock(DeviceState.class);
        when(deviceStateBefore.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
        when(deviceStateBefore.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateBefore.isInState(any())).thenCallRealMethod();

        getBridgeHandler().onDeviceStateUpdated(deviceStateBefore);

        DeviceState deviceStateAfter = mock(DeviceState.class);
        when(deviceStateAfter.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
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
        when(deviceStateBefore.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
        when(deviceStateBefore.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateBefore.isInState(any())).thenCallRealMethod();
        when(deviceStateBefore.getRemainingTime()).thenReturn(Optional.empty());
        when(deviceStateBefore.getProgress()).thenReturn(Optional.empty());

        getThingHandler().onDeviceStateUpdated(deviceStateBefore);

        DeviceState deviceStateAfter = mock(DeviceState.class);
        when(deviceStateAfter.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
        when(deviceStateAfter.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateAfter.isInState(any())).thenCallRealMethod();
        when(deviceStateAfter.getRemainingTime()).thenReturn(Optional.empty());
        when(deviceStateAfter.getProgress()).thenReturn(Optional.empty());

        // when:
        getThingHandler().onDeviceStateUpdated(deviceStateAfter);

        waitForAssert(() -> {
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_REMAINING_TIME));
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_PROGRESS));
        });
    }

    @Test
    public void testTransitionChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceStateBefore = mock(DeviceState.class);
        when(deviceStateBefore.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
        when(deviceStateBefore.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateBefore.isInState(any())).thenCallRealMethod();
        when(deviceStateBefore.getRemainingTime()).thenReturn(Optional.of(10));
        when(deviceStateBefore.getProgress()).thenReturn(Optional.of(80));

        getThingHandler().onDeviceStateUpdated(deviceStateBefore);

        DeviceState deviceStateAfter = mock(DeviceState.class);
        when(deviceStateAfter.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
        when(deviceStateAfter.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateAfter.isInState(any())).thenCallRealMethod();
        when(deviceStateAfter.getRemainingTime()).thenReturn(Optional.of(10));
        when(deviceStateAfter.getProgress()).thenReturn(Optional.of(80));

        // when:
        getThingHandler().onDeviceStateUpdated(deviceStateAfter);

        waitForAssert(() -> {
            assertEquals(new DecimalType(10), getChannelState(PROGRAM_REMAINING_TIME));
            assertEquals(new DecimalType(80), getChannelState(PROGRAM_PROGRESS));
        });
    }

    @Test
    public void testActionsChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        ActionsState actionsState = mock(ActionsState.class);
        when(actionsState.getDeviceIdentifier()).thenReturn(OVEN_DEVICE_THING_UID.getId());
        when(actionsState.canBeStarted()).thenReturn(true);
        when(actionsState.canBeStopped()).thenReturn(false);
        when(actionsState.canBeSwitchedOn()).thenReturn(true);
        when(actionsState.canBeSwitchedOff()).thenReturn(false);
        when(actionsState.canControlLight()).thenReturn(true);

        // when:
        getBridgeHandler().onProcessActionUpdated(actionsState);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.ON, getChannelState(REMOTE_CONTROL_CAN_BE_STARTED));
            assertEquals(OnOffType.OFF, getChannelState(REMOTE_CONTROL_CAN_BE_STOPPED));
            assertEquals(OnOffType.ON, getChannelState(REMOTE_CONTROL_CAN_BE_SWITCHED_ON));
            assertEquals(OnOffType.OFF, getChannelState(REMOTE_CONTROL_CAN_BE_SWITCHED_OFF));
            assertEquals(OnOffType.ON, getChannelState(LIGHT_CAN_BE_CONTROLLED));
        });
    }
}
