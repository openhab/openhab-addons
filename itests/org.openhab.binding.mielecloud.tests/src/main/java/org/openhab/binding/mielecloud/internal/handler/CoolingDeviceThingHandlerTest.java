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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.FRIDGE_FREEZER_DEVICE_THING_UID;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;

/**
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add door state and door alarm
 * @author Benjamin Bolte - Add info state channel and map signal flags from API tests
 */
@NonNullByDefault
public class CoolingDeviceThingHandlerTest extends AbstractMieleThingHandlerTest {
    @Override
    protected AbstractMieleThingHandler setUpThingHandler() {
        return createThingHandler(MieleCloudBindingConstants.THING_TYPE_FRIDGE_FREEZER, FRIDGE_FREEZER_DEVICE_THING_UID,
                CoolingDeviceThingHandler.class, MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER, "0");
    }

    @Test
    public void testChannelUpdatesForNullValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(FRIDGE_FREEZER_DEVICE_THING_UID.getId());
        when(deviceState.getRawType()).thenReturn(DeviceType.FRIDGE_FREEZER_COMBINATION);
        when(deviceState.getStateType()).thenReturn(Optional.empty());
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.empty());
        when(deviceState.getStatus()).thenReturn(Optional.empty());
        when(deviceState.getStatusRaw()).thenReturn(Optional.empty());
        when(deviceState.getTargetTemperature(0)).thenReturn(Optional.empty());
        when(deviceState.getTargetTemperature(1)).thenReturn(Optional.empty());
        when(deviceState.getTemperature(0)).thenReturn(Optional.empty());
        when(deviceState.getTemperature(1)).thenReturn(Optional.empty());
        when(deviceState.getDoorState()).thenReturn(Optional.empty());
        when(deviceState.getDoorAlarm()).thenReturn(Optional.empty());

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(FRIDGE_SUPER_COOL));
            assertEquals(NULL_VALUE_STATE, getChannelState(FREEZER_SUPER_FREEZE));
            assertEquals(NULL_VALUE_STATE, getChannelState(FRIDGE_TEMPERATURE_TARGET));
            assertEquals(NULL_VALUE_STATE, getChannelState(FREEZER_TEMPERATURE_TARGET));
            assertEquals(NULL_VALUE_STATE, getChannelState(FRIDGE_TEMPERATURE_CURRENT));
            assertEquals(NULL_VALUE_STATE, getChannelState(FREEZER_TEMPERATURE_CURRENT));
            assertEquals(NULL_VALUE_STATE, getChannelState(DOOR_STATE));
            assertEquals(NULL_VALUE_STATE, getChannelState(DOOR_ALARM));
        });
    }

    @Test
    public void testChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(FRIDGE_FREEZER_DEVICE_THING_UID.getId());
        when(deviceState.getRawType()).thenReturn(DeviceType.FRIDGE_FREEZER_COMBINATION);
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.SUPERCOOLING));
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.of(true));
        when(deviceState.getStatus()).thenReturn(Optional.of("Super Cooling"));
        when(deviceState.getStatusRaw()).thenReturn(Optional.of(StateType.SUPERCOOLING.getCode()));
        when(deviceState.getTargetTemperature(0)).thenReturn(Optional.of(6));
        when(deviceState.getTargetTemperature(1)).thenReturn(Optional.of(-18));
        when(deviceState.getTemperature(0)).thenReturn(Optional.of(8));
        when(deviceState.getTemperature(1)).thenReturn(Optional.of(-10));
        when(deviceState.hasError()).thenReturn(false);
        when(deviceState.getDoorState()).thenReturn(Optional.of(true));
        when(deviceState.getDoorAlarm()).thenReturn(Optional.of(false));

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(new StringType("Super Cooling"), getChannelState(OPERATION_STATE));
            assertEquals(new DecimalType(StateType.SUPERCOOLING.getCode()), getChannelState(OPERATION_STATE_RAW));
            assertEquals(OnOffType.ON, getChannelState(FRIDGE_SUPER_COOL));
            assertEquals(OnOffType.OFF, getChannelState(FREEZER_SUPER_FREEZE));
            assertEquals(new QuantityType<>(6, SIUnits.CELSIUS), getChannelState(FRIDGE_TEMPERATURE_TARGET));
            assertEquals(new QuantityType<>(-18, SIUnits.CELSIUS), getChannelState(FREEZER_TEMPERATURE_TARGET));
            assertEquals(new QuantityType<>(8, SIUnits.CELSIUS), getChannelState(FRIDGE_TEMPERATURE_CURRENT));
            assertEquals(new QuantityType<>(-10, SIUnits.CELSIUS), getChannelState(FREEZER_TEMPERATURE_CURRENT));
            assertEquals(OnOffType.OFF, getChannelState(ERROR_STATE));
            assertEquals(OnOffType.ON, getChannelState(DOOR_STATE));
            assertEquals(OnOffType.OFF, getChannelState(DOOR_ALARM));
        });
    }

    @Test
    public void testChannelUpdatesForSuperCooling() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(FRIDGE_FREEZER_DEVICE_THING_UID.getId());
        when(deviceState.getRawType()).thenReturn(DeviceType.FRIDGE_FREEZER_COMBINATION);
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.SUPERCOOLING));

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.ON, getChannelState(FRIDGE_SUPER_COOL));
            assertEquals(OnOffType.OFF, getChannelState(FREEZER_SUPER_FREEZE));
        });
    }

    @Test
    public void testChannelUpdatesForSuperFreezing() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(FRIDGE_FREEZER_DEVICE_THING_UID.getId());
        when(deviceState.getRawType()).thenReturn(DeviceType.FRIDGE_FREEZER_COMBINATION);
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.SUPERFREEZING));

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.OFF, getChannelState(FRIDGE_SUPER_COOL));
            assertEquals(OnOffType.ON, getChannelState(FREEZER_SUPER_FREEZE));
        });
    }

    @Test
    public void testChannelUpdatesForSuperCollingSuperFreezing() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(FRIDGE_FREEZER_DEVICE_THING_UID.getId());
        when(deviceState.getRawType()).thenReturn(DeviceType.FRIDGE_FREEZER_COMBINATION);
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.SUPERCOOLING_SUPERFREEZING));

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.ON, getChannelState(FRIDGE_SUPER_COOL));
            assertEquals(OnOffType.ON, getChannelState(FREEZER_SUPER_FREEZE));
        });
    }

    @Test
    public void testActionsChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        ActionsState actionsState = mock(ActionsState.class);
        when(actionsState.getDeviceIdentifier()).thenReturn(FRIDGE_FREEZER_DEVICE_THING_UID.getId());
        when(actionsState.canContolSupercooling()).thenReturn(true);
        when(actionsState.canControlSuperfreezing()).thenReturn(false);

        // when:
        getBridgeHandler().onProcessActionUpdated(actionsState);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.ON, getChannelState(SUPER_COOL_CAN_BE_CONTROLLED));
            assertEquals(OnOffType.OFF, getChannelState(SUPER_FREEZE_CAN_BE_CONTROLLED));
        });
    }

    @Override
    @Test
    public void testHandleCommandDoesNothingWhenCommandIsNotOfOnOffType() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(FRIDGE_SUPER_COOL), new DecimalType(50));

        // then:
        verify(getWebserviceMock(), never()).putProcessAction(anyString(), any());
    }

    @Test
    public void testHandleCommandStartsSupercoolingWhenRequested() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(FRIDGE_SUPER_COOL), OnOffType.ON);

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(),
                    ProcessAction.START_SUPERCOOLING);
        });
    }

    @Test
    public void testHandleCommandStopsSupercoolingWhenRequested() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(FRIDGE_SUPER_COOL), OnOffType.OFF);

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(),
                    ProcessAction.STOP_SUPERCOOLING);
        });
    }

    @Test
    public void testHandleCommandStartsSuperfreezingWhenRequested() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(FREEZER_SUPER_FREEZE), OnOffType.ON);

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(),
                    ProcessAction.START_SUPERFREEZING);
        });
    }

    @Test
    public void testHandleCommandStopsSuperfreezingWhenRequested() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(FREEZER_SUPER_FREEZE), OnOffType.OFF);

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(),
                    ProcessAction.STOP_SUPERFREEZING);
        });
    }
}
