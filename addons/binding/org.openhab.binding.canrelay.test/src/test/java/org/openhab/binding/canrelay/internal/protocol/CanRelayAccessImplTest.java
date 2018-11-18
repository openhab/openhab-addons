/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.canrelay.internal.canbus.CanBusCommunicationException;
import org.openhab.binding.canrelay.internal.canbus.CanBusDevice;
import org.openhab.binding.canrelay.internal.canbus.CanBusDeviceStatus;
import org.openhab.binding.canrelay.internal.canbus.CanMessage;

/**
 * JUnit test of CanRelayAccessImpl
 *
 * @author Lubos Housa - Initial contribution
 */
public class CanRelayAccessImplTest {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    @Mock
    private CanBusDevice device;
    @Mock
    private CanRelayChangeListener listener;

    private CanRelayAccessImpl canRelayAccess;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        canRelayAccess = new CanRelayAccessImpl(device);
        canRelayAccess.registerListener(listener);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConnectUsesCorrectBaudrate() {
        when(device.connect("test", 50000)).thenReturn(CanBusDeviceStatus.CONNECTED);
        CanBusDeviceStatus status = canRelayAccess.connect("test");

        assertEquals(CanBusDeviceStatus.CONNECTED, status);
    }

    @Test
    public void testDisconnectDisconnectsDevice() {
        canRelayAccess.disconnect();
        verify(device, times(1)).disconnect();
    }

    @Test
    public void testHandleSwitchCommandNotConnectedDoNothing() {
        when(device.getStatus()).thenReturn(CanBusDeviceStatus.UNITIALIZED);

        assertFalse("handleSwitchCommand should have returned false for not connected device",
                canRelayAccess.handleSwitchCommand(1, OnOffType.ON));
    }

    @Test
    public void testHandleSwitchCommandReturnsFalseForCommException() throws Exception {
        when(device.getStatus()).thenReturn(CanBusDeviceStatus.CONNECTED);
        doThrow(CanBusCommunicationException.class).when(device).send(any());

        assertFalse("handleSwitchCommand should have returned false for exceptions from canBusDevice",
                canRelayAccess.handleSwitchCommand(1, OnOffType.ON));
    }

    @Test
    public void testHandleSwitchCommandSendsMessages() throws Exception {
        when(device.getStatus()).thenReturn(CanBusDeviceStatus.CONNECTED);

        assertTrue(canRelayAccess.handleSwitchCommand(1, OnOffType.ON));
        assertTrue(canRelayAccess.handleSwitchCommand(2, OnOffType.OFF));

        verify(device, times(1)).send(CanMessage.newBuilder().id(0x301).withDataByte(0x40).build());
        verify(device, times(1)).send(CanMessage.newBuilder().id(0x302).withDataByte(0x80).build());
    }

    @Test
    public void testOnDeviceFatalError() {
        canRelayAccess.onDeviceFatalError("fatal");

        verify(listener, times(1)).onCanRelayOffline("fatal");
        verify(device, times(1)).disconnect();
    }

    @Test
    public void testDetectLightStatesDeviceNotReady() throws Exception {
        when(device.getStatus()).thenReturn(CanBusDeviceStatus.UNITIALIZED);

        // keep signaling device ready, so that the next call resumes quickly
        Future<?> signal = executor.scheduleWithFixedDelay(() -> canRelayAccess.onDeviceReady(), 0, 5,
                TimeUnit.MILLISECONDS);

        Collection<LightState> lights = canRelayAccess.detectLightStates();
        signal.cancel(false);

        assertEquals(0, lights.size());
        verify(device, never()).send(any());
    }

    /**
     * This method simulates real device sending traffic back after some time in another thread
     */
    private void simulateCanRelayDevice() throws Exception {
        simulateCanRelayDevice(false);
    }

    private void simulateCanRelayDevice(boolean outputsOffOnGroundFloor) throws Exception {
        when(device.getStatus()).thenReturn(CanBusDeviceStatus.CONNECTED);
        doAnswer((invocation) -> {
            CanMessage requestMessage = invocation.getArgument(0);
            CanMessage replyMessage = null;
            int floorId = requestMessage.getId() & 0xFF;
            switch (requestMessage.getId() & 0b11100000000) {
                case 0x500: // mapping
                    replyMessage = CanMessage.newBuilder().id(floorId + 0x600).withDataByte(floorId + 0x01)
                            .withDataByte(0x01).withDataByte(floorId + 0x15).withDataByte(0x02).withDataByte(0xFF)
                            .withDataByte(0xFF).build();
                    break;
                case 0x300: // outputs
                    replyMessage = CanMessage.newBuilder().id(floorId + 0x400).withDataByte(2)
                            .withDataByte(outputsOffOnGroundFloor && floorId == 0 ? 0 : 0b11000000).withDataByte(0)
                            .withDataByte(0).withDataByte(0).withDataByte(0).withDataByte(0).withDataByte(0).build();
                    break;
            }

            // send this message back after some time so that this thread is locked and waiting for the message inside
            // can relay access
            final CanMessage message = replyMessage;
            if (message != null) {
                executor.schedule(() -> {
                    canRelayAccess.onMessage(message);
                }, 5, TimeUnit.MILLISECONDS);
            }
            return true;
        }).when(device).send(any());
    }

    @Test
    public void testDetectLightStates() throws Exception {
        simulateCanRelayDevice();

        Collection<LightState> lights = canRelayAccess.detectLightStates();

        assertEquals(4, lights.size());
        verify(device, times(4)).send(any());
        lights.forEach(lightState -> {
            assertEquals(OnOffType.ON, lightState.getState());
            int nodeIDWIthoutFloor = lightState.getNodeID() & 0b1111111;
            assertTrue("Received unexpected light nodeID", nodeIDWIthoutFloor == 0x01 || nodeIDWIthoutFloor == 0x15);
        });
    }

    @Test
    public void testInitCache() throws Exception {
        simulateCanRelayDevice();

        canRelayAccess.initCache();

        verify(device, times(4)).send(any());
        verify(listener, times(4)).onLightSwitchChanged(anyInt(), any());
    }

    @Test
    public void testRefreshCache() throws Exception {
        simulateCanRelayDevice();
        canRelayAccess.initCache();

        // after the above, simulate a change on CANBUS the canrelay does not know about - e.g. both outputs now OFF for
        // ground floor
        simulateCanRelayDevice(true);
        Collection<LightState> lights = canRelayAccess.refreshCache();

        verify(device, times(6)).send(any());
        assertEquals(2, lights.size()); // just the ground floor

        Iterator<LightState> it = lights.iterator();
        LightState lightState = it.next();
        assertEquals(OnOffType.OFF, lightState.getState());
        assertEquals(0x01, lightState.getNodeID());
        lightState = it.next();
        assertEquals(OnOffType.OFF, lightState.getState());
        assertEquals(0x15, lightState.getNodeID());
    }

    @Test
    public void testOnMessage() throws Exception {
        // need to assure we have initiated all, so that onMessage can process normal messages
        simulateCanRelayDevice();
        canRelayAccess.initCache();

        // first 5 messages should be processed normally and listener called, rest should be ignored (unknown nodeID,
        // not normal message type, complex get data operation - not processed)
        canRelayAccess.onMessage(CanMessage.newBuilder().id(0x01).withDataByte(0x00).build());
        canRelayAccess.onMessage(CanMessage.newBuilder().id(0x15).withDataByte(0x00).build());
        canRelayAccess.onMessage(CanMessage.newBuilder().id(0x81).withDataByte(0x80).build());
        canRelayAccess.onMessage(CanMessage.newBuilder().id(0x95).withDataByte(0x80).build());
        // toggle all on first floor
        canRelayAccess.onMessage(CanMessage.newBuilder().id(0x80).withDataByte(0x00).build());

        // invalid or not supported
        canRelayAccess.onMessage(CanMessage.newBuilder().id(0x33).withDataByte(0x00).build());
        canRelayAccess.onMessage(CanMessage.newBuilder().id(0x301).withDataByte(0x00).build());
        canRelayAccess.onMessage(CanMessage.newBuilder().id(0x01).withDataByte(0xFF).build());

        verify(listener, times(1)).onLightSwitchChanged(0x01, OnOffType.OFF);
        verify(listener, times(1)).onLightSwitchChanged(0x15, OnOffType.OFF);
        verify(listener, times(1)).onLightSwitchChanged(0x81, OnOffType.OFF);
        verify(listener, times(1)).onLightSwitchChanged(0x95, OnOffType.OFF);

        // first floor got called with 5th message above as well (all were in init)
        verify(listener, times(1)).onLightSwitchChanged(0x01, OnOffType.ON);
        verify(listener, times(1)).onLightSwitchChanged(0x15, OnOffType.ON);
        verify(listener, times(2)).onLightSwitchChanged(0x81, OnOffType.ON);
        verify(listener, times(2)).onLightSwitchChanged(0x95, OnOffType.ON);
    }
}
