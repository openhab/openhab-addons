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
package org.openhab.binding.mideaac.internal.devices.a1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.devices.a1.A1StringCommands.A1FanSpeed;
import org.openhab.binding.mideaac.internal.devices.a1.A1StringCommands.A1OperationalMode;

/**
 * The {@link A1CommandSetTest} tests the methods in the A1CommandSet class
 * for correctness.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class A1CommandSetTest {

    /**
     * Dehumidifier Swing Mode test
     * 
     */
    @Test
    public void testHandleA1SwingMode() {
        A1CommandSet commandSet = new A1CommandSet();
        commandSet.setA1SwingMode(true);
        byte[] frame = commandSet.getData();

        // Device type byte
        assertEquals((byte) 0xA1, frame[0x02]);

        // Swing mode bit should be ON (bit 5 = 0x20)
        assertEquals((byte) 0x20, frame[0x14] & 0x20);
    }

    /**
     * Dehumidifier Operational mode test
     */
    @Test
    public void testHandleA1OperationalMode() {
        A1CommandSet commandSet = new A1CommandSet();

        commandSet.setA1OperationalMode(A1OperationalMode.AUTO);
        byte[] frame = commandSet.getData();
        // Device type byte
        assertEquals((byte) 0xA1, frame[0x02]);

        // Low nibble should equal mode ID
        assertEquals(3, frame[0x0C] & 0x0F);

        // Getter should return the same value
        assertEquals(3, commandSet.getA1OperationalMode());
    }

    /**
     * Dehumidifier Fan Speed test
     */
    @Test
    public void testHandleA1FanSpeed() {
        A1CommandSet commandSet = new A1CommandSet();
        commandSet.setA1FanSpeed(A1FanSpeed.HIGH);
        byte[] frame = commandSet.getData();

        // Device type byte
        assertEquals((byte) 0xA1, frame[0x02]);

        // Getter should return the same value
        assertEquals(80, frame[0x0d]);
    }

    @Test
    public void testA1Capabilities() {
        A1CommandSet commandSet = new A1CommandSet();
        commandSet.getCapabilities();
        byte[] frame = commandSet.getData();

        // Device type byte
        assertEquals((byte) 0xA1, frame[0x02]);

        // Check the length of the data array
        assertEquals(13, frame.length);
    }
}
