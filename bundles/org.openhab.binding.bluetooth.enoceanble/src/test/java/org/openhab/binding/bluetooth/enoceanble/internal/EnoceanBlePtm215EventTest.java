/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.enoceanble.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * {@link EnoceanBlePtm215EventTest} is testing the {@link EnoceanBlePtm215Event} class
 * using some sample manufacturerData
 *
 * @author Patrick Fink - Initial contribution
 */
@NonNullByDefault
public class EnoceanBlePtm215EventTest {

    @Test
    public void testButton1Dir1Pressed() {
        byte[] button1Dir2PressedBytes = { -38, 3, 53, 5, 0, 0, 17, 54, 81, 61, 54 };
        EnoceanBlePtm215Event button1Dir2PressedEvent = new EnoceanBlePtm215Event(button1Dir2PressedBytes);

        assertTrue(button1Dir2PressedEvent.isPressed());
        assertTrue(button1Dir2PressedEvent.isButton1());
        assertTrue(button1Dir2PressedEvent.isDir1());
    }

    @Test
    public void testButton1Dir1Released() {
        byte[] button1Dir2ReleasedBytes = { -38, 3, 54, 5, 0, 0, 16, -83, -25, 103, 3 };
        EnoceanBlePtm215Event button1Dir2ReleasedEvent = new EnoceanBlePtm215Event(button1Dir2ReleasedBytes);

        assertFalse(button1Dir2ReleasedEvent.isPressed());
        assertTrue(button1Dir2ReleasedEvent.isButton1());
        assertTrue(button1Dir2ReleasedEvent.isDir1());
    }

    @Test
    public void testButton1Dir2Pressed() {
        byte[] button1Dir2PressedBytes = { -38, 3, -97, 32, 0, 0, 9, -99, -15, 16, -74 };
        EnoceanBlePtm215Event button1Dir2PressedEvent = new EnoceanBlePtm215Event(button1Dir2PressedBytes);

        assertTrue(button1Dir2PressedEvent.isPressed());
        assertTrue(button1Dir2PressedEvent.isButton1());
        assertTrue(button1Dir2PressedEvent.isDir2());
    }

    @Test
    public void testButton1Dir2Released() {
        byte[] button1Dir2ReleasedBytes = { -38, 3, -96, 32, 0, 0, 8, -87, -96, 98, 74 };
        EnoceanBlePtm215Event button1Dir2ReleasedEvent = new EnoceanBlePtm215Event(button1Dir2ReleasedBytes);

        assertFalse(button1Dir2ReleasedEvent.isPressed());
        assertTrue(button1Dir2ReleasedEvent.isButton1());
        assertTrue(button1Dir2ReleasedEvent.isDir2());
    }

    @Test
    public void testButton2Dir1Pressed() {
        byte[] button2Dir1PressedBytes = { -38, 3, -91, 32, 0, 0, 5, 89, 104, 24, -67 };
        EnoceanBlePtm215Event button2Dir1PressedEvent = new EnoceanBlePtm215Event(button2Dir1PressedBytes);

        assertTrue(button2Dir1PressedEvent.isPressed());
        assertTrue(button2Dir1PressedEvent.isButton2());
        assertTrue(button2Dir1PressedEvent.isDir1());
    }

    @Test
    public void testButton2Dir1Released() {
        byte[] button2Dir1ReleasedBytes = { -38, 3, -90, 32, 0, 0, 4, 120, 52, -115, 61 };
        EnoceanBlePtm215Event button2Dir1ReleasedEvent = new EnoceanBlePtm215Event(button2Dir1ReleasedBytes);
        assertFalse(button2Dir1ReleasedEvent.isPressed());
        assertTrue(button2Dir1ReleasedEvent.isButton2());
        assertTrue(button2Dir1ReleasedEvent.isDir1());
    }

    @Test
    public void testButton2Dir2Pressed() {
        byte[] button2Dir2PressedBytes = { -38, 3, -95, 32, 0, 0, 3, -105, -33, 62, 45 };
        EnoceanBlePtm215Event button2Dir1PressedEvent = new EnoceanBlePtm215Event(button2Dir2PressedBytes);
        assertTrue(button2Dir1PressedEvent.isPressed());
        assertTrue(button2Dir1PressedEvent.isButton2());
        assertTrue(button2Dir1PressedEvent.isDir2());
    }

    @Test
    public void testButton2Dir2Released() {
        byte[] button2Dir2ReleasedBytes = { -38, 3, -94, 32, 0, 0, 2, 59, 118, 52, -69 };
        EnoceanBlePtm215Event button2Dir2ReleasedEvent = new EnoceanBlePtm215Event(button2Dir2ReleasedBytes);

        assertFalse(button2Dir2ReleasedEvent.isPressed());
        assertTrue(button2Dir2ReleasedEvent.isButton2());
        assertTrue(button2Dir2ReleasedEvent.isDir2());
    }
}
