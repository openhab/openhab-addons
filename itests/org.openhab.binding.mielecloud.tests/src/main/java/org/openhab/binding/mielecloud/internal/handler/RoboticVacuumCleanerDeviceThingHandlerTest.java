/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.library.types.StringType;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class RoboticVacuumCleanerDeviceThingHandlerTest extends AbstractMieleThingHandlerTest {
    @Override
    protected AbstractMieleThingHandler setUpThingHandler() {
        return createThingHandler(MieleCloudBindingConstants.THING_TYPE_ROBOTIC_VACUUM_CLEANER,
                MieleCloudBindingIntegrationTestConstants.ROBOTIC_VACUUM_CLEANER_THING_UID,
                RoboticVacuumCleanerDeviceThingHandler.class, MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER);
    }

    @Test
    public void testChannelUpdatesForNullValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier())
                .thenReturn(MieleCloudBindingIntegrationTestConstants.ROBOTIC_VACUUM_CLEANER_THING_UID.getId());
        when(deviceState.getSelectedProgramId()).thenReturn(Optional.empty());
        when(deviceState.getStatus()).thenReturn(Optional.empty());
        when(deviceState.getStatusRaw()).thenReturn(Optional.empty());
        when(deviceState.getStateType()).thenReturn(Optional.empty());
        when(deviceState.hasError()).thenReturn(false);
        when(deviceState.hasInfo()).thenReturn(false);
        when(deviceState.getBatteryLevel()).thenReturn(Optional.empty());

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(NULL_VALUE_STATE, getChannelState(VACUUM_CLEANER_PROGRAM_ACTIVE));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE_RAW));
            assertEquals(new StringType(ProgramStatus.PROGRAM_STOPPED.getState()),
                    getChannelState(PROGRAM_START_STOP_PAUSE));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
            assertEquals(OnOffType.OFF, getChannelState(ERROR_STATE));
            assertEquals(OnOffType.OFF, getChannelState(INFO_STATE));
            assertEquals(NULL_VALUE_STATE, getChannelState(BATTERY_LEVEL));
        });
    }

    @Test
    public void testChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.isInState(any())).thenCallRealMethod();
        when(deviceState.getDeviceIdentifier())
                .thenReturn(MieleCloudBindingIntegrationTestConstants.ROBOTIC_VACUUM_CLEANER_THING_UID.getId());
        when(deviceState.getSelectedProgramId()).thenReturn(Optional.of(1L));
        when(deviceState.getStatus()).thenReturn(Optional.of("Running"));
        when(deviceState.getStatusRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceState.hasError()).thenReturn(true);
        when(deviceState.hasInfo()).thenReturn(true);
        when(deviceState.getBatteryLevel()).thenReturn(Optional.of(25));

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(new StringType("1"), getChannelState(VACUUM_CLEANER_PROGRAM_ACTIVE));
            assertEquals(new StringType("Running"), getChannelState(OPERATION_STATE));
            assertEquals(new DecimalType(StateType.RUNNING.getCode()), getChannelState(OPERATION_STATE_RAW));
            assertEquals(new StringType(ProgramStatus.PROGRAM_STARTED.getState()),
                    getChannelState(PROGRAM_START_STOP_PAUSE));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
            assertEquals(OnOffType.ON, getChannelState(ERROR_STATE));
            assertEquals(OnOffType.ON, getChannelState(INFO_STATE));
            assertEquals(new DecimalType(25), getChannelState(BATTERY_LEVEL));
        });
    }

    @Test
    public void testFinishStateChannelIsSetToOnWhenProgramHasFinished() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceStateBefore = mock(DeviceState.class);
        when(deviceStateBefore.getDeviceIdentifier())
                .thenReturn(MieleCloudBindingIntegrationTestConstants.ROBOTIC_VACUUM_CLEANER_THING_UID.getId());
        when(deviceStateBefore.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceStateBefore.isInState(any())).thenCallRealMethod();

        getThingHandler().onDeviceStateUpdated(deviceStateBefore);

        DeviceState deviceStateAfter = mock(DeviceState.class);
        when(deviceStateAfter.getDeviceIdentifier())
                .thenReturn(MieleCloudBindingIntegrationTestConstants.ROBOTIC_VACUUM_CLEANER_THING_UID.getId());
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
    public void testActionsChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        ActionsState actionsState = mock(ActionsState.class);
        when(actionsState.getDeviceIdentifier())
                .thenReturn(MieleCloudBindingIntegrationTestConstants.ROBOTIC_VACUUM_CLEANER_THING_UID.getId());
        when(actionsState.canBeStarted()).thenReturn(true);
        when(actionsState.canBeStopped()).thenReturn(false);
        when(actionsState.canBePaused()).thenReturn(true);
        when(actionsState.canSetActiveProgramId()).thenReturn(false);

        // when:
        getBridgeHandler().onProcessActionUpdated(actionsState);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.ON, getChannelState(REMOTE_CONTROL_CAN_BE_STARTED));
            assertEquals(OnOffType.OFF, getChannelState(REMOTE_CONTROL_CAN_BE_STOPPED));
            assertEquals(OnOffType.ON, getChannelState(REMOTE_CONTROL_CAN_BE_PAUSED));
            assertEquals(OnOffType.OFF, getChannelState(REMOTE_CONTROL_CAN_SET_PROGRAM_ACTIVE));
        });
    }

    @Test
    public void testHandleCommandVacuumCleanerProgramActive() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(VACUUM_CLEANER_PROGRAM_ACTIVE), new StringType("1"));

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProgram(getThingHandler().getDeviceId(), 1);
        });
    }
}
