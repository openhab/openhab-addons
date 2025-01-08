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
package org.openhab.binding.mideaac.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;

/**
 * The {@link CommandSetTest} compares example SET commands with the
 * expected results.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CommandSetTest {

    /**
     * Power State Test
     */
    @Test
    public void setPowerStateTest() {
        boolean status = true;
        boolean status1 = true;
        CommandSet commandSet = new CommandSet();
        commandSet.setPowerState(status);
        assertEquals(status1, commandSet.getPowerState());
    }

    /**
     * Target temperature tests
     */
    @Test
    public void testsetTargetTemperature() {
        CommandSet commandSet = new CommandSet();
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
     * Swing Mode test
     */
    @Test
    public void testHandleSwingMode() {
        SwingMode mode = SwingMode.VERTICAL3;
        int mode1 = 60;
        CommandSet commandSet = new CommandSet();
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
        CommandSet commandSet = new CommandSet();
        commandSet.setFanSpeed(speed);
        assertEquals(speed1, commandSet.getFanSpeed());
    }

    /**
     * Operational mode test
     */
    @Test
    public void testHandleOperationalMode() {
        OperationalMode mode = OperationalMode.COOL;
        int mode1 = 64;
        CommandSet commandSet = new CommandSet();
        commandSet.setOperationalMode(mode);
        assertEquals(mode1, commandSet.getOperationalMode());
    }

    /**
     * On timer test
     */
    @Test
    public void testHandleOnTimer() {
        CommandSet commandSet = new CommandSet();
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
        CommandSet commandSet = new CommandSet();
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
        CommandSet commandSet = new CommandSet();
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
        CommandSet commandSet = new CommandSet();
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
        CommandSet commandSet = new CommandSet();
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
        CommandSet commandSet = new CommandSet();
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
        CommandSet commandSet = new CommandSet();
        commandSet.setScreenDisplay(true);

        // Check the modified bytes
        assertEquals((byte) 0x20, commandSet.data[0x01]);
        assertEquals((byte) 0x03, commandSet.data[0x09]);
        assertEquals((byte) 0x41, commandSet.data[0x0a]);
        assertEquals((byte) 0x02, commandSet.data[0x0b] & 0x02); // Check if bit 1 is set
        assertEquals((byte) 0x00, commandSet.data[0x0b] & 0x80); // Check if bit 7 is cleared
        assertEquals((byte) 0x00, commandSet.data[0x0c]);
        assertEquals((byte) 0xff, commandSet.data[0x0d]);
        assertEquals((byte) 0x02, commandSet.data[0x0e]);
        assertEquals((byte) 0x00, commandSet.data[0x0f]);
        assertEquals((byte) 0x02, commandSet.data[0x10]);
        assertEquals((byte) 0x00, commandSet.data[0x11]);
        assertEquals((byte) 0x00, commandSet.data[0x12]);
        assertEquals((byte) 0x00, commandSet.data[0x13]);
        assertEquals((byte) 0x00, commandSet.data[0x14]);

        // Check the length of the data array
        assertEquals(31, commandSet.data.length);
    }
}
