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
package org.openhab.binding.modbus.ankersolix.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryListener;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit tests for Anker SOLIX discovery participant.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
class AnkerSolixDiscoveryParticipantTest {

    private final AnkerSolixDiscoveryParticipant participant = new AnkerSolixDiscoveryParticipant();

    @Test
    void getSupportedThingTypeUIDsShouldContainAllAnkerSolixThingTypes() {
        Set<ThingTypeUID> supported = participant.getSupportedThingTypeUIDs();

        assertEquals(5, supported.size());
    }

    @Test
    void startDiscoveryShouldFinishWhenEndpointIsNotInitialized() {
        ModbusEndpointThingHandler handler = mock(ModbusEndpointThingHandler.class);
        when(handler.getCommunicationInterface()).thenReturn(null);

        ModbusDiscoveryListener listener = mock(ModbusDiscoveryListener.class);

        participant.startDiscovery(handler, listener);

        verify(listener).discoveryFinished();
    }

    @Test
    void startDiscoveryShouldRunWhenEndpointIsAvailable() {
        ModbusEndpointThingHandler handler = mock(ModbusEndpointThingHandler.class);
        ModbusCommunicationInterface communicationInterface = mock(ModbusCommunicationInterface.class);
        when(handler.getCommunicationInterface()).thenReturn(communicationInterface);

        ModbusDiscoveryListener listener = mock(ModbusDiscoveryListener.class);

        participant.startDiscovery(handler, listener);

        verify(communicationInterface).submitOneTimePoll(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
