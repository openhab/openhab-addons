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
package org.openhab.binding.knx.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;
import org.openhab.binding.knx.internal.monitor.DataType;
import org.openhab.core.library.types.DecimalType;

import io.calimero.GroupAddress;

/**
 * Checks which Things the client asks for the data type of a group address.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ClientDataTypeTest {
    private static final GroupAddress TEMPERATURE = new GroupAddress(3, 1, 10);

    @Test
    void onlyListenersThatListenToTheAddressAreAsked() {
        GroupAddressListener other = mock(GroupAddressListener.class);
        when(other.listensTo(TEMPERATURE)).thenReturn(false);
        GroupAddressListener silent = mock(GroupAddressListener.class);
        when(silent.listensTo(TEMPERATURE)).thenReturn(true);
        GroupAddressListener user = mock(GroupAddressListener.class);
        when(user.listensTo(TEMPERATURE)).thenReturn(true);
        when(user.monitorDataType(TEMPERATURE)).thenReturn(new DataType("9.001", DecimalType.class));

        assertEquals(new DataType("9.001", DecimalType.class),
                AbstractKNXClient.monitorDataType(List.of(other, silent, user), TEMPERATURE));
        verify(other, never()).monitorDataType(any());
    }

    @Test
    void withoutAListenerThereIsNoType() {
        assertNull(AbstractKNXClient.monitorDataType(List.of(), TEMPERATURE));
    }
}
