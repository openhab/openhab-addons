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
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.WINE_STORAGE_DEVICE_THING_UID;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.PowerStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;

/**
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add info state channel and map signal flags from API tests
 */
@NonNullByDefault
public class WineStorageDeviceThingHandlerTest extends AbstractMieleThingHandlerTest {
    @Override
    protected AbstractMieleThingHandler setUpThingHandler() {
        return createThingHandler(MieleCloudBindingConstants.THING_TYPE_WINE_STORAGE, WINE_STORAGE_DEVICE_THING_UID,
                WineStorageDeviceThingHandler.class, MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER, "0");
    }

    @Test
    public void testChannelUpdatesForNullValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(WINE_STORAGE_DEVICE_THING_UID.getId());
        when(deviceState.getRawType()).thenReturn(DeviceType.WINE_CONDITIONING_UNIT);
        when(deviceState.getStateType()).thenReturn(Optional.empty());
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.empty());
        when(deviceState.getStatus()).thenReturn(Optional.empty());
        when(deviceState.getStatusRaw()).thenReturn(Optional.empty());
        when(deviceState.getTargetTemperature(0)).thenReturn(Optional.empty());
        when(deviceState.getTemperature(0)).thenReturn(Optional.empty());
        when(deviceState.getTargetTemperature(1)).thenReturn(Optional.empty());
        when(deviceState.getTemperature(1)).thenReturn(Optional.empty());
        when(deviceState.getTargetTemperature(2)).thenReturn(Optional.empty());
        when(deviceState.getTemperature(2)).thenReturn(Optional.empty());

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE));
            assertEquals(NULL_VALUE_STATE, getChannelState(OPERATION_STATE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(TEMPERATURE_TARGET));
            assertEquals(NULL_VALUE_STATE, getChannelState(TEMPERATURE_CURRENT));
            assertEquals(NULL_VALUE_STATE, getChannelState(TOP_TEMPERATURE_TARGET));
            assertEquals(NULL_VALUE_STATE, getChannelState(TOP_TEMPERATURE_CURRENT));
            assertEquals(NULL_VALUE_STATE, getChannelState(MIDDLE_TEMPERATURE_TARGET));
            assertEquals(NULL_VALUE_STATE, getChannelState(MIDDLE_TEMPERATURE_CURRENT));
            assertEquals(NULL_VALUE_STATE, getChannelState(BOTTOM_TEMPERATURE_TARGET));
            assertEquals(NULL_VALUE_STATE, getChannelState(BOTTOM_TEMPERATURE_CURRENT));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
        });
    }

    @Test
    public void testChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(WINE_STORAGE_DEVICE_THING_UID.getId());
        when(deviceState.getRawType()).thenReturn(DeviceType.WINE_CONDITIONING_UNIT);
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(deviceState.isRemoteControlEnabled()).thenReturn(Optional.empty());
        when(deviceState.getStatus()).thenReturn(Optional.of("Im Betrieb"));
        when(deviceState.getStatusRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));
        when(deviceState.getTargetTemperature(0)).thenReturn(Optional.of(8));
        when(deviceState.getTemperature(0)).thenReturn(Optional.of(9));
        when(deviceState.getTargetTemperature(1)).thenReturn(Optional.of(10));
        when(deviceState.getTemperature(1)).thenReturn(Optional.of(11));
        when(deviceState.getTargetTemperature(2)).thenReturn(Optional.of(12));
        when(deviceState.getTemperature(2)).thenReturn(Optional.of(14));
        when(deviceState.hasError()).thenReturn(true);
        when(deviceState.hasInfo()).thenReturn(true);

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            assertEquals(new StringType("Im Betrieb"), getChannelState(OPERATION_STATE));
            assertEquals(new DecimalType(StateType.RUNNING.getCode()), getChannelState(OPERATION_STATE_RAW));
            assertEquals(NULL_VALUE_STATE, getChannelState(TEMPERATURE_TARGET));
            assertEquals(NULL_VALUE_STATE, getChannelState(TEMPERATURE_CURRENT));
            assertEquals(new QuantityType<>(8, SIUnits.CELSIUS), getChannelState(TOP_TEMPERATURE_TARGET));
            assertEquals(new QuantityType<>(9, SIUnits.CELSIUS), getChannelState(TOP_TEMPERATURE_CURRENT));
            assertEquals(new QuantityType<>(10, SIUnits.CELSIUS), getChannelState(MIDDLE_TEMPERATURE_TARGET));
            assertEquals(new QuantityType<>(11, SIUnits.CELSIUS), getChannelState(MIDDLE_TEMPERATURE_CURRENT));
            assertEquals(new QuantityType<>(12, SIUnits.CELSIUS), getChannelState(BOTTOM_TEMPERATURE_TARGET));
            assertEquals(new QuantityType<>(14, SIUnits.CELSIUS), getChannelState(BOTTOM_TEMPERATURE_CURRENT));
            assertEquals(new StringType(PowerStatus.POWER_ON.getState()), getChannelState(POWER_ON_OFF));
            assertEquals(OnOffType.ON, getChannelState(ERROR_STATE));
            assertEquals(OnOffType.ON, getChannelState(INFO_STATE));
        });
    }

    @Test
    public void testActionsChannelUpdatesForValidValues() throws Exception {
        // given:
        setUpBridgeAndThing();

        ActionsState actionsState = mock(ActionsState.class);
        when(actionsState.getDeviceIdentifier()).thenReturn(WINE_STORAGE_DEVICE_THING_UID.getId());
        when(actionsState.canBeSwitchedOn()).thenReturn(true);
        when(actionsState.canBeSwitchedOff()).thenReturn(false);

        // when:
        getBridgeHandler().onProcessActionUpdated(actionsState);

        // then:
        waitForAssert(() -> {
            assertEquals(OnOffType.ON, getChannelState(REMOTE_CONTROL_CAN_BE_SWITCHED_ON));
            assertEquals(OnOffType.OFF, getChannelState(REMOTE_CONTROL_CAN_BE_SWITCHED_OFF));
        });
    }
}
