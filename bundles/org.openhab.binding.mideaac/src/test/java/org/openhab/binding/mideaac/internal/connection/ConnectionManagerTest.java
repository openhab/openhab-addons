/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.mideaac.internal.callbacks.Callback;
import org.openhab.binding.mideaac.internal.devices.CommandBase;
import org.openhab.binding.mideaac.internal.devices.a1.A1CommandBase;

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
        // Arrange: construct a real instance with dummy values
        ConnectionManager real = new ConnectionManager("127.0.0.1", 6444, 4, "", "", "", "", "", "000000000000", 3,
                false, "ac");
        ConnectionManager manager = spy(real);

        // Stub sendCommand so no real network call is made
        doNothing().when(manager).sendCommand(any(), any());

        manager.setDeviceType("a1");

        Callback callback = Objects.requireNonNull(mock(Callback.class));

        // Act
        manager.getStatus(callback);

        // Assert
        ArgumentCaptor<CommandBase> captor = ArgumentCaptor.forClass(CommandBase.class);
        verify(manager).sendCommand(captor.capture(), eq(callback));
        assertInstanceOf(A1CommandBase.class, captor.getValue());
    }
}
