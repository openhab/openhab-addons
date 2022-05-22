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
import org.openhab.binding.boschshc.internal.services.intrusion.dto.IntrusionDetectionSystemState;

/**
 * Unit tests for {@link IntrusionDetectionSystemStateService}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
class IntrusionDetectionSystemStateServiceTest {

    private IntrusionDetectionSystemStateService fixture;

    @Mock
    private BridgeHandler bridgeHandler;

    @Mock
    private Consumer<IntrusionDetectionSystemState> consumer;

    @Mock
    private IntrusionDetectionSystemState testState;

    @BeforeEach
    void beforeEach() {
        fixture = new IntrusionDetectionSystemStateService();
        fixture.initialize(bridgeHandler, BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION, consumer);
    }

    @Test
    void getState() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(bridgeHandler.getState(anyString(), any())).thenReturn(testState);
        IntrusionDetectionSystemState state = fixture.getState();
        verify(bridgeHandler).getState("intrusion/states/system", IntrusionDetectionSystemState.class);
        assertSame(testState, state);
    }
}
