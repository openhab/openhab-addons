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
package org.openhab.binding.boschshc.internal.services.intrusion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.IntrusionDetectionControlState;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link IntrusionDetectionControlStateService}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
class IntrusionDetectionControlStateServiceTest {

    private IntrusionDetectionControlStateService fixture;

    @Mock
    private BridgeHandler bridgeHandler;

    @Mock
    private Consumer<IntrusionDetectionControlState> consumer;

    @Mock
    private IntrusionDetectionControlState testState;

    @BeforeEach
    void beforeEach() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        fixture = new IntrusionDetectionControlStateService();
        fixture.initialize(bridgeHandler, BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION, consumer);
    }

    @Test
    void getServiceName() {
        assertEquals("IntrusionDetectionControl", fixture.getServiceName());
    }

    @Test
    void getStateClass() {
        assertSame(IntrusionDetectionControlState.class, fixture.getStateClass());
    }

    @Test
    void getState() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(bridgeHandler.getState(anyString(), anyString(), any())).thenReturn(testState);
        IntrusionDetectionControlState state = fixture.getState();
        verify(bridgeHandler).getState(BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION,
                "IntrusionDetectionControl", IntrusionDetectionControlState.class);
        assertSame(testState, state);
    }

    @Test
    void setState() throws InterruptedException, TimeoutException, ExecutionException {
        fixture.setState(testState);
        verify(bridgeHandler).putState(BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION,
                "IntrusionDetectionControl", testState);
    }

    @Test
    void onStateUpdate() {
        final String json = "{\n" + "\"@type\": \"intrusionDetectionControlState\",\n" + "\"activeProfile\": \"0\",\n"
                + "\"alarmActivationDelayTime\": 30,\n" + "\"actuators\": [\n" + "{\n" + "\"readonly\": false,\n"
                + "\"active\": true,\n" + "\"id\": \"intrusion:video\"\n" + "},\n" + "{\n" + "\"readonly\": false,\n"
                + "\"active\": false,\n" + "\"id\": \"intrusion:siren\"\n" + "}\n" + "],\n"
                + "\"remainingTimeUntilArmed\": 28959,\n" + "\"armActivationDelayTime\": 30,\n" + "\"triggers\": [\n"
                + "{\n" + "\"readonly\": false,\n" + "\"active\": true,\n" + "\"id\": \"hdm:ZigBee:000d6f0422f42378\"\n"
                + "}\n" + "],\n" + "\"value\": \"SYSTEM_ARMING\"\n" + "}";
        JsonElement jsonElement = JsonParser.parseString(json);
        fixture.onStateUpdate(jsonElement);
        verify(consumer).accept(any());
    }

    @Test
    void refreshState() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(bridgeHandler.getState(anyString(), anyString(), any())).thenReturn(testState);
        fixture.refreshState();
        verify(consumer).accept(testState);
    }
}
