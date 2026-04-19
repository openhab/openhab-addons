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
package org.openhab.binding.zwavejs.internal.type.capabilities;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.ChannelUID;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class RollerShutterCapabilityTest {

    private ChannelUID dimmer = new ChannelUID("zwavejs:device:bridge:node:dimmer");
    private ChannelUID up = new ChannelUID("zwavejs:device:bridge:node:up");
    private ChannelUID down = new ChannelUID("zwavejs:device:bridge:node:down");
    private RollerShutterCapability capability = new RollerShutterCapability(1, dimmer, up, down);

    @BeforeEach
    public void setUp() {
        capability = new RollerShutterCapability(1, dimmer, up, down);
    }

    @Test
    public void testInitialState() {
        assertFalse(capability.isMoving());
        assertFalse(capability.isMovingUp());
        assertFalse(capability.isMovingDown());
    }

    @Test
    public void testSetDirectionUp() {
        capability.setDirectionUp(true);
        assertTrue(capability.isMovingUp());
        assertFalse(capability.isMovingDown());
        assertTrue(capability.isMoving());
        capability.setDirectionUp(false);
        assertFalse(capability.isMovingUp());
        assertFalse(capability.isMoving());
    }

    @Test
    public void testSetDirectionDown() {
        capability.setDirectionDown(true);
        assertTrue(capability.isMovingDown());
        assertFalse(capability.isMovingUp());
        assertTrue(capability.isMoving());
        capability.setDirectionDown(false);
        assertFalse(capability.isMovingDown());
        assertFalse(capability.isMoving());
    }

    @Test
    public void testSetDirection() {
        capability.setDirection(true, false);
        assertTrue(capability.isMovingUp());
        assertFalse(capability.isMovingDown());
        capability.setDirection(false, true);
        assertFalse(capability.isMovingUp());
        assertTrue(capability.isMovingDown());
        capability.setDirection(false, false);
        assertFalse(capability.isMoving());
    }

    @Test
    public void testSetPositionNormal() {
        // Not inverted: position increases = moving down, decreases = moving up
        capability.setPosition(10, false); // from 0 to 10: moving down
        assertTrue(capability.isMovingDown());
        assertFalse(capability.isMovingUp());
        capability.setPosition(5, false); // from 10 to 5: moving up
        assertTrue(capability.isMovingUp());
        assertFalse(capability.isMovingDown());
        capability.setPosition(5, false); // no change
        assertFalse(capability.isMoving());
    }

    @Test
    public void testSetPositionInverted() {
        // Inverted: position increases = moving up, decreases = moving down
        capability.setPosition(10, true); // from 0 to 10: moving up
        assertTrue(capability.isMovingUp());
        assertFalse(capability.isMovingDown());
        capability.setPosition(5, true); // from 10 to 5: moving down
        assertTrue(capability.isMovingDown());
        assertFalse(capability.isMovingUp());
        capability.setPosition(5, true); // no change
        assertFalse(capability.isMoving());
    }

    @Test
    public void testToString() {
        String str = capability.toString();
        assertTrue(str.contains("dimmerChannel"));
        assertTrue(str.contains("upChannel"));
        assertTrue(str.contains("downChannel"));
    }
}
