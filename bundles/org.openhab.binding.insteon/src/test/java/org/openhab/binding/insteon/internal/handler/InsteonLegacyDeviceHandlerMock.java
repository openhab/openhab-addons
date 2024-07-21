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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.InsteonBindingLegacyConstants;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

/**
 * The {@link HomeWizardHandlerMock} is responsible for mocking {@link HomeWizardHandler}
 * 
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class InsteonLegacyDeviceHandlerMock extends InsteonLegacyDeviceHandler {

    public InsteonLegacyDeviceHandlerMock(Thing thing) {
        super(thing);

        executorService = Mockito.mock(ScheduledExecutorService.class);
        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).execute(any(Runnable.class));
    }

    @Override
    protected @Nullable Bridge getBridge() {
        final Bridge bridge = TestObjects.mockBridge(InsteonBindingLegacyConstants.NETWORK_THING_TYPE,
                "insteon-network-thing");
        final InsteonNetworkHandlerMock handler = spy(
                new InsteonNetworkHandlerMock(bridge, mock(SerialPortManager.class), mock(InsteonBinding.class)));

        when(bridge.getHandler()).thenReturn(handler);
        return bridge;
    }
}
