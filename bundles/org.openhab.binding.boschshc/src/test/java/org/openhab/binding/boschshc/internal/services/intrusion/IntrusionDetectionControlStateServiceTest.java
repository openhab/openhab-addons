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
package org.openhab.binding.boschshc.internal.services.intrusion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class IntrusionDetectionControlStateServiceTest {

    private @NonNullByDefault({}) IntrusionDetectionControlStateService fixture;

    private @Mock @NonNullByDefault({}) BridgeHandler bridgeHandler;

    private @Mock @NonNullByDefault({}) Consumer<IntrusionDetectionControlState> consumer;

    private @Mock @NonNullByDefault({}) IntrusionDetectionControlState testState;

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
        final String json = """
                {
                    "@type":"intrusionDetectionControlState",
                    "activeProfile":"0",
                    "alarmActivationDelayTime":30,
                    "actuators":[
                        {
                            "readonly":false,
                            "active":true,
                            "id":"intrusion:video"
                        },
                        {
                            "readonly":false,
                            "active":false,
                            "id":"intrusion:siren"
                        }
                    ],
                    "remainingTimeUntilArmed":28959,
                    "armActivationDelayTime":30,
                    "triggers":[
                        {
                            "readonly":false,
                            "active":true,
                            "id":"hdm:ZigBee:000d6f0422f42378"
                        }
                    ],
                    "value":"SYSTEM_ARMING"
                }\
                """;
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
