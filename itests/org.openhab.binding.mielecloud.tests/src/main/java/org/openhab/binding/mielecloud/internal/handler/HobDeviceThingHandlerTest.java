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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.HOB_DEVICE_THING_UID;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add plate step
 * @author Benjamin Bolte - Add info state channel and map signal flags from API tests
 */
@NonNullByDefault
public class HobDeviceThingHandlerTest extends AbstractMieleThingHandlerTest {
    @Override
    protected AbstractMieleThingHandler setUpThingHandler() {
        return createThingHandler(MieleCloudBindingConstants.THING_TYPE_HOB, HOB_DEVICE_THING_UID,
                HobDeviceThingHandler.class, MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER, "0");
    }

    @Test
    public void testChannelUpdatesForNullValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(HOB_DEVICE_THING_UID.getId());
        when(deviceState.getStateType()).thenReturn(Optional.empty());
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.empty());
        when(deviceState.getStatus()).thenReturn(Optional.empty());
        when(deviceState.getStatusRaw()).thenReturn(Optional.empty());
        when(deviceState.getPlateStep(anyInt())).thenReturn(Optional.empty());
        when(deviceState.getPlateStepRaw(anyInt())).thenReturn(Optional.empty());

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_1_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_1_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_2_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_2_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_3_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_3_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_4_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_4_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_5_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_5_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_6_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_6_POWER_STEP_RAW));
        });
    }

    @Test
    public void testChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(HOB_DEVICE_THING_UID.getId());
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.of(false));
        when(deviceState.getStatus()).thenReturn(Optional.of("Running"));
        when(deviceState.getStatusRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));
        when(deviceState.hasError()).thenReturn(false);
        when(deviceState.hasInfo()).thenReturn(true);
        when(deviceState.getPlateStep(0)).thenReturn(Optional.of("1."));
        when(deviceState.getPlateStepRaw(0)).thenReturn(Optional.of(2));
        when(deviceState.getPlateStep(1)).thenReturn(Optional.empty());
        when(deviceState.getPlateStep(2)).thenReturn(Optional.empty());
        when(deviceState.getPlateStep(3)).thenReturn(Optional.empty());
        when(deviceState.getPlateStep(4)).thenReturn(Optional.empty());
        when(deviceState.getPlateStep(5)).thenReturn(Optional.empty());

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(new StringType("Running"), getChannelState(OPERATION_STATE));
            assertEquals(new DecimalType(StateType.RUNNING.getCode()), getChannelState(OPERATION_STATE_RAW));
            assertEquals(OnOffType.OFF, getChannelState(ERROR_STATE));
            assertEquals(OnOffType.ON, getChannelState(INFO_STATE));
            assertEquals(new StringType("1."), getChannelState(PLATE_1_POWER_STEP));
            assertEquals(new DecimalType(2), getChannelState(PLATE_1_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_2_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_2_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_3_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_3_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_4_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_4_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_5_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_5_POWER_STEP_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_6_POWER_STEP));
            assertEquals(NULL_VALUE_STATE, getChannelState(PLATE_6_POWER_STEP_RAW));
        });
    }
}
