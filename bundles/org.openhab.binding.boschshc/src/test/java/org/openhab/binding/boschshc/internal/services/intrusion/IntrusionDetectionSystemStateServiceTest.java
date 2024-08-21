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
package org.openhab.binding.boschshc.internal.services.intrusion;

import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.openhab.binding.boschshc.internal.services.intrusion.dto.IntrusionDetectionSystemState;

/**
 * Unit tests for {@link IntrusionDetectionSystemStateService}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class IntrusionDetectionSystemStateServiceTest {

    private @NonNullByDefault({}) IntrusionDetectionSystemStateService fixture;

    private @Mock @NonNullByDefault({}) BridgeHandler bridgeHandler;

    private @Mock @NonNullByDefault({}) Consumer<IntrusionDetectionSystemState> consumer;

    private @Mock @NonNullByDefault({}) IntrusionDetectionSystemState testState;

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
