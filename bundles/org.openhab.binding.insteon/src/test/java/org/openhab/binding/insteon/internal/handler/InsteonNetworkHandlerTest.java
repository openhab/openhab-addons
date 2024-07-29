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
package org.openhab.binding.insteon.internal.handler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.InsteonBindingLegacyConstants;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for the {@InsteonNetworkHandler}
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class InsteonNetworkHandlerTest {

    private InsteonNetworkHandlerMock createAndInitHandler(final ThingHandlerCallback callback, final Bridge thing,
            @Nullable InsteonBinding mockedInsteonBinding) {
        final SerialPortManager serialPortManager = mock(SerialPortManager.class);

        final InsteonNetworkHandlerMock handler = spy(
                new InsteonNetworkHandlerMock(thing, serialPortManager, mockedInsteonBinding));

        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    private Bridge bridge = TestObjects.mockBridge(InsteonBindingLegacyConstants.NETWORK_THING_TYPE,
            "insteon-test-network");
    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private InsteonBinding mockedInsteonBinding = mock(InsteonBinding.class);

    @Test
    public void testInitNoConnection() {
        final InsteonNetworkHandlerMock handler = createAndInitHandler(callback, bridge, mockedInsteonBinding);

        try {
            verify(callback).statusUpdated(eq(bridge), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testInitSuccess() {
        when(mockedInsteonBinding.startPolling()).thenReturn(true);
        when(mockedInsteonBinding.isDriverInitialized()).thenReturn(true);
        doNothing().when(mockedInsteonBinding).logDeviceStatistics();

        final InsteonNetworkHandlerMock handler = createAndInitHandler(callback, bridge, mockedInsteonBinding);

        try {
            verify(callback).statusUpdated(eq(bridge), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(bridge), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
        } finally {
            handler.dispose();
        }
    }
}
