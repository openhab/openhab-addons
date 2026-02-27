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
package org.openhab.binding.midea.internal.devices.ac;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.midea.internal.devices.ac.ACStringCommands.FanSpeed;
import org.openhab.binding.midea.internal.devices.ac.ACStringCommands.OperationalMode;
import org.openhab.binding.midea.internal.devices.ac.ACStringCommands.SwingMode;

/**
 * The {@link ACCommandSetTest} tests the methods in the ACCommandSet class
 * for correctness.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ACCommandSetTest {

    /**
     * Power State Test
     */
    @Test
    public void setPowerStateTest() {
        boolean status = true;
        boolean status1 = true;
        ACCommandSet commandSet = new ACCommandSet();
        commandSet.setPowerState(status);
        assertEquals(status1, commandSet.getPowerState());
    }

    /**
     * Target temperature tests
     */
    @Test
    public void testsetTargetTemperature() {
        ACCommandSet commandSet = new ACCommandSet();
        // Device is limited to 0.5 degree C increments. Check rounding too

        // Test case 1
        float targetTemperature1 = 25.4f;
        commandSet.setTargetTemperature(targetTemperature1);
        assertEquals(25.5f, commandSet.getTargetTemperature());

        // Test case 2
        float targetTemperature2 = 17.8f;
        commandSet.setTargetTemperature(targetTemperature2);
        assertEquals(18.0f, commandSet.getTargetTemperature());

        // Test case 3
        float targetTemperature3 = 21.26f;
        commandSet.setTargetTemperature(targetTemperature3);
        assertEquals(21.5f, commandSet.getTargetTemperature());

        // Test case 4
        float degreefahr = 72.0f;
        float targetTemperature4 = ((degreefahr + 40.0f) * (5.0f / 9.0f)) - 40.0f;
        commandSet.setTargetTemperature(targetTemperature4);
        assertEquals(22.0f, commandSet.getTargetTemperature());

        // Test case 5
        float degreefahr2 = 66.0f;
        float targetTemperature5 = ((degreefahr2 + 40.0f) * (5.0f / 9.0f)) - 40.0f;
        commandSet.setTargetTemperature(targetTemperature5);
        assertEquals(19.0f, commandSet.getTargetTemperature());
    }

    /**
     * AC Swing Mode test
     */
    @Test
    public void testHandleSwingMode() {
        SwingMode mode = SwingMode.VERTICAL3;
        int mode1 = 60;
        ACCommandSet commandSet = new ACCommandSet();
        commandSet.setSwingMode(mode);
        assertEquals(mode1, commandSet.getSwingMode());
    }

    /**
     * Fan Speed test
     */
    @Test
    public void testHandleFanSpeedCommand() {
        FanSpeed speed = FanSpeed.AUTO3;
        int speed1 = 102;
        ACCommandSet commandSet = new ACCommandSet();
        commandSet.setFanSpeed(speed);
        assertEquals(speed1, commandSet.getFanSpeed());
    }

    /**
     * AC Operational mode test
     */
    @Test
    public void testHandleOperationalMode() {
        OperationalMode mode = OperationalMode.COOL;
        int mode1 = 64;
        ACCommandSet commandSet = new ACCommandSet();
        commandSet.setOperationalMode(mode);
        assertEquals(mode1, commandSet.getOperationalMode());
    }

    /**
     * On timer test
     */
    @Test
    public void testHandleOnTimer() {
        ACCommandSet commandSet = new ACCommandSet();
        boolean on = true;
        int hours = 3;
        int minutes = 59;
        int bits = (int) Math.floor(minutes / 15);
        int time = 143;
        int remainder = (15 - (int) (minutes - bits * 15));
        commandSet.setOnTimer(on, hours, minutes);
        assertEquals(time, commandSet.getOnTimer());
        assertEquals(remainder, commandSet.getOnTimer2());
    }

    /**
     * On timer test3
     */
    @Test
    public void testHandleOnTimer2() {
        ACCommandSet commandSet = new ACCommandSet();
        boolean on = false;
        int hours = 3;
        int minutes = 60;
        int time = 127;
        int remainder = 0;
        commandSet.setOnTimer(on, hours, minutes);
        assertEquals(time, commandSet.getOnTimer());
        assertEquals(remainder, commandSet.getOnTimer2());
    }

    /**
     * On timer test3
     */
    @Test
    public void testHandleOnTimer3() {
        ACCommandSet commandSet = new ACCommandSet();
        boolean on = true;
        int hours = 0;
        int minutes = 14;
        int time = 128;
        int remainder = (15 - minutes);
        commandSet.setOnTimer(on, hours, minutes);
        assertEquals(time, commandSet.getOnTimer());
        assertEquals(remainder, commandSet.getOnTimer2());
    }

    /**
     * Off timer test
     */
    @Test
    public void testHandleOffTimer() {
        ACCommandSet commandSet = new ACCommandSet();
        boolean on = true;
        int hours = 3;
        int minutes = 59;
        int bits = (int) Math.floor(minutes / 15);
        int time = 143;
        int remainder = (15 - (int) (minutes - bits * 15));
        commandSet.setOffTimer(on, hours, minutes);
        assertEquals(time, commandSet.getOffTimer());
        assertEquals(remainder, commandSet.getOffTimer2());
    }

    /**
     * Off timer test2
     */
    @Test
    public void testHandleOffTimer2() {
        ACCommandSet commandSet = new ACCommandSet();
        boolean on = false;
        int hours = 3;
        int minutes = 60;
        int time = 127;
        int remainder = 0;
        commandSet.setOffTimer(on, hours, minutes);
        assertEquals(time, commandSet.getOffTimer());
        assertEquals(remainder, commandSet.getOffTimer2());
    }

    /**
     * Off timer test3
     */
    @Test
    public void testHandleOffTimer3() {
        ACCommandSet commandSet = new ACCommandSet();
        boolean on = true;
        int hours = 0;
        int minutes = 14;
        int time = 128;
        int remainder = (15 - minutes);
        commandSet.setOffTimer(on, hours, minutes);
        assertEquals(time, commandSet.getOffTimer());
        assertEquals(remainder, commandSet.getOffTimer2());
    }

    /**
     * Test screen display change command
     */
    @Test
    public void testSetScreenDisplayOff() {
        ACCommandSet commandSet = new ACCommandSet();
        commandSet.setScreenDisplay(true);
        byte[] frame = commandSet.getData();

        // Check the modified bytes
        assertEquals((byte) 0x20, frame[0x01]);
        assertEquals((byte) 0x03, frame[0x09]);
        assertEquals((byte) 0x41, frame[0x0a]);
        assertEquals((byte) 0x61, frame[0x0b]);
        assertEquals((byte) 0x00, frame[0x0c]);
        assertEquals((byte) 0xff, frame[0x0d]);
        assertEquals((byte) 0x02, frame[0x0e]);
        assertEquals((byte) 0x00, frame[0x0f]);
        assertEquals((byte) 0x02, frame[0x10]);
        assertEquals((byte) 0x00, frame[0x11]);
        assertEquals((byte) 0x00, frame[0x12]);
        assertEquals((byte) 0x00, frame[0x13]);
        assertEquals((byte) 0x00, frame[0x14]);

        // Check the length of the data array
        assertEquals(31, frame.length);
    }

    /**
     * Energy poll command Test
     * 
     */
    @Test
    public void testEnergyPoll() {
        ACCommandSet commandSet = new ACCommandSet();
        commandSet.energyPoll();
        byte[] frame = commandSet.getData();

        // Check the modified bytes
        assertEquals((byte) 0x20, frame[0x01]);
        assertEquals((byte) 0x03, frame[0x09]);
        assertEquals((byte) 0x41, frame[0x0a]);
        assertEquals((byte) 0x21, frame[0x0b]);
        assertEquals((byte) 0x01, frame[0x0c]);
        assertEquals((byte) 0x44, frame[0x0d]);
        assertEquals((byte) 0x00, frame[0x0e]);
        assertEquals((byte) 0x00, frame[0x0f]);
        assertEquals((byte) 0x00, frame[0x10]);
        assertEquals((byte) 0x00, frame[0x11]);
        assertEquals((byte) 0x00, frame[0x12]);
        assertEquals((byte) 0x00, frame[0x13]);
        assertEquals((byte) 0x00, frame[0x14]);

        // Check the length of the data array
        assertEquals(31, frame.length);
    }

    /**
     * Capabilities Command Test
     * 
     */
    @Test
    public void testCapabilities() {
        ACCommandSet commandSet = new ACCommandSet();
        commandSet.getCapabilities();
        byte[] frame = commandSet.getData();

        // Check the modified bytes
        assertEquals((byte) 0x0e, frame[0x01]);
        assertEquals((byte) 0x03, frame[0x09]);
        assertEquals((byte) 0xB5, frame[0x0a]);
        assertEquals((byte) 0x01, frame[0x0b]);
        assertEquals((byte) 0x00, frame[0x0c]);

        // Check the length of the data array
        assertEquals(13, frame.length);
    }

    /**
     * Additional Capabilities Command Test
     * 
     */
    @Test
    public void testAdditionalCapabilities() {
        ACCommandSet commandSet = new ACCommandSet();
        commandSet.getAdditionalCapabilities();
        byte[] frame = commandSet.getData();

        // Check the modified bytes
        assertEquals((byte) 0x0f, frame[0x01]);
        assertEquals((byte) 0x03, frame[0x09]);
        assertEquals((byte) 0xB5, frame[0x0a]);
        assertEquals((byte) 0x01, frame[0x0b]);
        assertEquals((byte) 0x01, frame[0x0c]);
        assertEquals((byte) 0x01, frame[0x0d]);

        // Check the length of the data array
        assertEquals(14, frame.length);
    }
}
