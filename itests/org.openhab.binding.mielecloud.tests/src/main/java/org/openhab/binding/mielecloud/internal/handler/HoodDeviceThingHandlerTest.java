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
package org.openhab.binding.mielecloud.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.HOOD_DEVICE_THING_UID;

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
 */
@NonNullByDefault
public class HoodDeviceThingHandlerTest extends AbstractMieleThingHandlerTest {
    @Override
    protected AbstractMieleThingHandler setUpThingHandler() {
        return createThingHandler(MieleCloudBindingConstants.THING_TYPE_HOOD, HOOD_DEVICE_THING_UID,
                HoodDeviceThingHandler.class, MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER, "0");
    }

    @Test
    public void testChannelUpdatesForNullValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(HOOD_DEVICE_THING_UID.getId());
        when(deviceState.getStateType()).thenReturn(Optional.empty());
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.empty());
        when(deviceState.getProgramPhase()).thenReturn(Optional.empty());
        when(deviceState.getProgramPhaseRaw()).thenReturn(Optional.empty());
        when(deviceState.getStatus()).thenReturn(Optional.empty());
        when(deviceState.getStatusRaw()).thenReturn(Optional.empty());
        when(deviceState.getVentilationStep()).thenReturn(Optional.empty());
        when(deviceState.getVentilationStepRaw()).thenReturn(Optional.empty());
        when(deviceState.getLightState()).thenReturn(Optional.empty());

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_PHASE));
            assertEquals(NULL_VALUE_STATE, getChannelState(PROGRAM_PHASE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(VENTILATION_POWER));
            assertEquals(NULL_VALUE_STATE, getChannelState(VENTILATION_POWER_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(LIGHT_SWITCH));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
        });
    }

    @Test
    public void testChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(HOOD_DEVICE_THING_UID.getId());
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.of(false));
        when(deviceState.getProgramPhase()).thenReturn(Optional.of("Kochen"));
        when(deviceState.getProgramPhaseRaw()).thenReturn(Optional.of(5));
        when(deviceState.getStatus()).thenReturn(Optional.of("Running"));
        when(deviceState.getStatusRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));
        when(deviceState.getVentilationStep()).thenReturn(Optional.of("2"));
        when(deviceState.getVentilationStepRaw()).thenReturn(Optional.of(2));
        when(deviceState.hasError()).thenReturn(false);
        when(deviceState.hasInfo()).thenReturn(true);
        when(deviceState.getLightState()).thenReturn(Optional.of(false));

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(new StringType("Kochen"), getChannelState(PROGRAM_PHASE));
            assertEquals(new DecimalType(5), getChannelState(PROGRAM_PHASE_RAW));
            assertEquals(new StringType("Running"), getChannelState(OPERATION_STATE));
            assertEquals(new DecimalType(StateType.RUNNING.getCode()), getChannelState(OPERATION_STATE_RAW));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
            assertEquals(new StringType("2"), getChannelState(VENTILATION_POWER));
            assertEquals(new DecimalType(2), getChannelState(VENTILATION_POWER_RAW));
            assertEquals(OnOffType.OFF, getChannelState(ERROR_STATE));
            assertEquals(OnOffType.ON, getChannelState(INFO_STATE));
            assertEquals(OnOffType.OFF, getChannelState(LIGHT_SWITCH));
        });
    }

    @Test
    public void testActionsChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        ActionsState actionsState = mock(ActionsState.class);
        when(actionsState.getDeviceIdentifier()).thenReturn(HOOD_DEVICE_THING_UID.getId());
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
