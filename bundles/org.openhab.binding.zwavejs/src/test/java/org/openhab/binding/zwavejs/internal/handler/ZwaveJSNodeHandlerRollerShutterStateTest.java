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
package org.openhab.binding.zwavejs.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.zwavejs.internal.api.dto.Args;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.handler.mock.ZwaveJSNodeHandlerMock;
import org.openhab.binding.zwavejs.internal.type.capabilities.RollerShutterCapability;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSNodeHandlerRollerShutterStateTest {
    private @NonNullByDefault({}) ZwaveJSNodeHandlerMock handler;
    private @NonNullByDefault({}) RollerShutterCapability capability;
    private @NonNullByDefault({}) Thing thing;

    // Helper to create a fake Event for a rollershutter channel update
    private static Event createRollerShutterDimmerEvent(String channelId, int value) {
        Args args = new Args();
        args.commandClass = 38; // Multilevel Switch
        args.commandClassName = "Multilevel Switch";
        args.endpoint = 0;
        args.propertyName = "currentValue";
        args.newValue = value;
        Event event = new Event();
        event.nodeId = 39;
        event.args = args;
        return event;
    }

    public static Event createRollerShutterUpEvent(String channelId, boolean value) {
        Args args = new Args();
        args.commandClass = 38; // Multilevel Switch
        args.commandClassName = "Multilevel Switch";
        args.propertyName = "On";
        args.newValue = value;
        Event event = new Event();
        event.nodeId = 39;
        event.args = args;
        return event;
    }

    public static Event createRollerShutterDownEvent(String channelId, boolean value) {
        Args args = new Args();
        args.commandClass = 38; // Multilevel Switch
        args.commandClassName = "Multilevel Switch";
        args.propertyName = "Off";
        args.newValue = value;
        Event event = new Event();
        event.nodeId = 39;
        event.args = args;
        return event;
    }

    @BeforeEach
    public void setup() {
        thing = ZwaveJSNodeHandlerMock.mockThing(39); // Node 39 is a rollershutter test node
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");
        // Get the first rollershutter capability
        capability = handler.getRollerShutterCapabilities().values().stream().findFirst().orElse(null);
        assertNotNull(capability);
    }

    @Test
    public void testDimmerChannelUpdate_noInversion() {
        // Simulate event: dimmer channel updated to 80
        Event event = createRollerShutterDimmerEvent(capability.dimmerChannel.getId(), 80);
        handler.onNodeStateChanged(event);
        assertEquals(80, capability.getCachedPosition());
        assertTrue(capability.isMovingDown());
        assertFalse(capability.isMovingUp());
    }

    // Without any inversion, 100% is fully open (up position) and 0% is fully closed (down position).
    @Test
    public void testUpChannelUpdate_noInversion() {
        // Simulate event: up channel ON
        Event event = createRollerShutterUpEvent(capability.upChannel.getId(), true);
        boolean successfulHandling = handler.onNodeStateChanged(event);

        assertTrue(successfulHandling);
        assertTrue(capability.isMovingUp());
        assertFalse(capability.isMovingDown());
    }

    @Test
    public void testDownChannelUpdate_noInversion() {
        // Simulate event: down channel ON
        Event event = createRollerShutterDownEvent(capability.downChannel.getId(), true);
        boolean successfulHandling = handler.onNodeStateChanged(event);

        assertTrue(successfulHandling);
        assertTrue(capability.isMovingDown());
        assertFalse(capability.isMovingUp());
    }

    @Test
    public void testDimmerChannelUpdate_withInversion() {
        // Enable inversion
        handler.setRollerShutterInversion(capability, true);
        // Simulate event: dimmer channel updated to 80
        Event event = createRollerShutterDimmerEvent(capability.dimmerChannel.getId(), 80);
        boolean successfulHandling = handler.onNodeStateChanged(event);

        assertTrue(successfulHandling);
        assertEquals(80, capability.getCachedPosition());
        // With inversion, increasing value means moving up
        assertTrue(capability.isMovingUp());
        assertFalse(capability.isMovingDown());
    }

    @Test
    public void testUpChannelUpdate_withInversion() {
        handler.setRollerShutterInversion(capability, true);
        Event event = createRollerShutterUpEvent(capability.upChannel.getId(), true);
        boolean successfulHandling = handler.onNodeStateChanged(event);

        assertTrue(successfulHandling);
        // With inversion, up channel ON means moving down
        assertTrue(capability.isMovingDown());
        assertFalse(capability.isMovingUp());
    }

    @Test
    public void testDownChannelUpdate_withInversion() {
        handler.setRollerShutterInversion(capability, true);
        Event event = createRollerShutterDownEvent(capability.downChannel.getId(), true);
        boolean successfulHandling = handler.onNodeStateChanged(event);

        assertTrue(successfulHandling);
        // With inversion, down channel ON means moving up
        assertTrue(capability.isMovingUp());
        assertFalse(capability.isMovingDown());
    }
}
