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
package org.openhab.binding.mideaac.internal.connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.mideaac.internal.callbacks.Callback;
import org.openhab.binding.mideaac.internal.devices.A1CommandBase;
import org.openhab.binding.mideaac.internal.devices.CommandBase;

/**
 * The {@link ConnectionManagerTest} tests the methods in the ConnectionManager
 * class with mock responses.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ConnectionManagerTest {

    @Test
    public void testGetStatusUsesA1CommandBase() throws Exception {
        // Arrange
        ConnectionManager manager = mock(ConnectionManager.class);

        doCallRealMethod().when(manager).getStatus(any());
        doCallRealMethod().when(manager).setDeviceType(anyString());
        doNothing().when(manager).sendCommand(any(), any());

        manager.setDeviceType("a1"); // package-private setter

        Callback callback = mock(Callback.class);

        // Act
        manager.getStatus(callback);

        // Assert
        ArgumentCaptor<CommandBase> captor = ArgumentCaptor.forClass(CommandBase.class);
        verify(manager).sendCommand(captor.capture(), eq(callback));

        assertTrue(captor.getValue() instanceof A1CommandBase);
    }
}
