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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.InsteonBindingLegacyConstants;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for the HomeWizard Handler
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class InsteonLegacyDeviceHandlerTest {

    private static InsteonLegacyDeviceHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        final InsteonLegacyDeviceHandlerMock handler = spy(new InsteonLegacyDeviceHandlerMock(thing));

        final Bridge mockedBridge = TestObjects.mockBridge(InsteonBindingLegacyConstants.NETWORK_THING_TYPE,
        "insteon-test-network");
        final InsteonNetworkHandlerMock mockedBridgeHandler = spy(
                new InsteonNetworkHandlerMock(mockedBridge, mock(SerialPortManager.class), mock(InsteonBinding.class)));
        
        when(mockedBridge.getHandler()).thenReturn(mockedBridgeHandler);
        when(handler.getBridge()).thenReturn(mockedBridge);
        
        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    private Thing thing = TestObjects.mockThing(InsteonBindingLegacyConstants.DEVICE_THING_TYPE, "insteon-test-device");
    private @NonNullByDefault ThingHandlerCallback callback;

    @BeforeEach
    public void init() {
        callback = mock(ThingHandlerCallback.class);
    }

    @Test
    public void testUpdateChannels() {
        final InsteonLegacyDeviceHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
        } finally {
            handler.dispose();
        }
    }
}
