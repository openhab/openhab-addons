/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.windowcontact;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.vibrationsensor.dto.VibrationSensorSensitivity;
import org.openhab.binding.boschshc.internal.services.vibrationsensor.dto.VibrationSensorServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link WindowContact2PlusHandler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class WindowContact2PlusHandlerTest extends WindowContact2HandlerTest {

    private static final String NO_INITIAL_VIBRATION_SENSOR_STATE = "noInitialVibrationSensorState";

    @Override
    protected WindowContactHandler createFixture() {
        return new WindowContact2PlusHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return THING_TYPE_WINDOW_CONTACT_2_PLUS;
    }

    @Override
    protected void afterHandlerInitialization(TestInfo testInfo) {
        super.afterHandlerInitialization(testInfo);

        if (!testInfo.getTags().contains(NO_INITIAL_VIBRATION_SENSOR_STATE)) {
            String json = """
                    {
                        "@type": "vibrationSensorState",
                        "enabled": true,
                        "value": "VIBRATION_DETECTED",
                        "sensitivity": "LOW"
                    }
                    """;
            JsonElement jsonObject = JsonParser.parseString(json);

            getFixture().processUpdate("VibrationSensor", jsonObject);
        }
    }

    @Test
    void testUpdateChannelsVibrationSensorService() {
        verify(getCallback()).stateUpdated(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_ENABLED),
                OnOffType.ON);
        verify(getCallback()).stateUpdated(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_STATE),
                new StringType("VIBRATION_DETECTED"));
        verify(getCallback()).stateUpdated(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_SENSITIVITY),
                new StringType("LOW"));
    }

    @Test
    void testHandleCommandVibrationSensorOn() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_ENABLED), OnOffType.ON);
        ArgumentCaptor<BoschSHCServiceState> serviceStateCaptor = ArgumentCaptor.forClass(BoschSHCServiceState.class);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("VibrationSensor"), serviceStateCaptor.capture());

        BoschSHCServiceState state = serviceStateCaptor.getValue();
        assertTrue(state instanceof VibrationSensorServiceState);
        VibrationSensorServiceState vibrationSensorServiceState = (VibrationSensorServiceState) state;

        assertTrue(vibrationSensorServiceState.enabled);
    }

    @Test
    void testHandleCommandVibrationSensorOff() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_ENABLED),
                OnOffType.OFF);
        ArgumentCaptor<BoschSHCServiceState> serviceStateCaptor = ArgumentCaptor.forClass(BoschSHCServiceState.class);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("VibrationSensor"), serviceStateCaptor.capture());

        BoschSHCServiceState state = serviceStateCaptor.getValue();
        assertTrue(state instanceof VibrationSensorServiceState);
        VibrationSensorServiceState vibrationSensorServiceState = (VibrationSensorServiceState) state;

        assertFalse(vibrationSensorServiceState.enabled);
    }

    @Test
    void testHandleCommandVibrationSensorInvalidCommand()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_ENABLED),
                new DecimalType(1));
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("VibrationSensor"), any());
    }

    @Tag(NO_INITIAL_VIBRATION_SENSOR_STATE)
    @Test
    void testHandleCommandVibrationSensorNoStateAvailable()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_ENABLED), OnOffType.ON);
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("VibrationSensor"), any());
    }

    @Test
    void testHandleCommandVibrationSensorSensitivity()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_SENSITIVITY),
                new StringType("VERY_HIGH"));
        ArgumentCaptor<BoschSHCServiceState> serviceStateCaptor = ArgumentCaptor.forClass(BoschSHCServiceState.class);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("VibrationSensor"), serviceStateCaptor.capture());

        BoschSHCServiceState state = serviceStateCaptor.getValue();
        assertTrue(state instanceof VibrationSensorServiceState);
        VibrationSensorServiceState vibrationSensorServiceState = (VibrationSensorServiceState) state;

        assertSame(VibrationSensorSensitivity.VERY_HIGH, vibrationSensorServiceState.sensitivity);
    }

    @Test
    void testHandleCommandVibrationSensorSensitivityInvalidCommand()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_SENSITIVITY),
                new DecimalType(1));
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("VibrationSensor"), any());
    }

    @Test
    void testHandleCommandVibrationSensorSensitivityInvalidSensitivityValue()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_SENSITIVITY),
                new StringType("INVALID"));
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("VibrationSensor"), any());
    }

    @Tag(NO_INITIAL_VIBRATION_SENSOR_STATE)
    @Test
    void testHandleCommandVibrationSensorSensitivityNoStateAvailable()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_VIBRATION_SENSOR_SENSITIVITY),
                new StringType("MEDIUM"));
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("VibrationSensor"), any());
    }
}
