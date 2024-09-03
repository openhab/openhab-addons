/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.handler.Timer.TimerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link CommandSet} class handles the allowed changes originating from
 * the items linked to the Midea AC channels. Not all devices
 * support all commands. The general process is to clear the
 * bit(s) the set them to the commanded value.
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public class CommandSet extends CommandBase {
    private final Logger logger = LoggerFactory.getLogger(CommandSet.class);

    public CommandSet() {
        data[0x01] = (byte) 0x23;
        data[0x09] = (byte) 0x02;
        // Set up Mode
        data[0x0a] = (byte) 0x40;

        byte[] extra = { 0x00, 0x00, 0x00 };
        byte[] newData = new byte[data.length + 3];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[data.length] = extra[0];
        newData[data.length + 1] = extra[1];
        newData[data.length + 2] = extra[2];
        data = newData;
    }

    /*
     * These provide continuity so a new command on another channel
     * doesn't delete the current states of the other channels
     */
    public static CommandSet fromResponse(Response response) {
        CommandSet commandSet = new CommandSet();

        commandSet.setPowerState(response.getPowerState());
        commandSet.setTargetTemperature(response.getTargetTemperature());
        commandSet.setOperationalMode(response.getOperationalMode());
        commandSet.setFanSpeed(response.getFanSpeed());
        commandSet.setFahrenheit(response.getFahrenheit());
        commandSet.setTurboMode(response.getTurboMode());
        commandSet.setSwingMode(response.getSwingMode());
        commandSet.setEcoMode(response.getEcoMode());
        commandSet.setSleepMode(response.getSleepFunction());
        commandSet.setOnTimer(response.getOnTimerData());
        commandSet.setOffTimer(response.getOffTimerData());
        return commandSet;
    }

    /*
     * Causes indoor device to beep when command received
     */
    public void setPromptTone(boolean feedbackEnabled) {
        if (!feedbackEnabled) {
            data[0x0b] &= ~(byte) 0x40; // Clear
        } else {
            data[0x0b] |= (byte) 0x40; // Set
        }
    }

    /*
     * Turns device On or Off
     */
    public void setPowerState(boolean state) {
        if (!state) {
            data[0x0b] &= ~0x01;
        } else {
            data[0x0b] |= 0x01;
        }
    }

    /*
     * For Testing assertion get result
     */
    public boolean getPowerState() {
        return (data[0x0b] & 0x1) > 0;
    }

    /*
     * Cool, Heat, Fan Only, etc. See Command Base class
     */
    public void setOperationalMode(OperationalMode mode) {
        data[0x0c] &= ~(byte) 0xe0;
        data[0x0c] |= ((byte) mode.getId() << 5) & (byte) 0xe0;
    }

    /*
     * For Testing assertion get result
     */
    public int getOperationalMode() {
        return data[0x0c] &= (byte) 0xe0;
    }

    /*
     * Clear, then set the temperature bits, including the 0.5 bit
     * This is all degrees C
     */
    public void setTargetTemperature(float temperature) {
        data[0x0c] &= ~0x0f;
        data[0x0c] |= (int) (Math.round(temperature * 2) / 2) & 0xf;
        setTemperatureDot5((Math.round(temperature * 2)) % 2 != 0);
    }

    /*
     * For Testing assertion get Setpoint results
     */
    public float getTargetTemperature() {
        return (data[0x0c] & 0xf) + 16.0f + (((data[0x0c] & 0x10) > 0) ? 0.5f : 0.0f);
    }

    /*
     * Low, Medium, High, Auto etc. See Command Base class
     */
    public void setFanSpeed(FanSpeed speed) {
        data[0x0d] = (byte) (speed.getId());
    }

    /*
     * For Testing assertion get Fan Speed results
     */
    public int getFanSpeed() {
        return data[0x0d];
    }

    /*
     * In cool mode sets Fan to Auto and temp to 24 C
     */
    public void setEcoMode(boolean ecoModeEnabled) {
        if (!ecoModeEnabled) {
            data[0x13] &= ~0x80;
        } else {
            data[0x13] |= 0x80;
        }
    }

    /*
     * If unit supports, set the vertical and/or horzontal louver
     */
    public void setSwingMode(SwingMode mode) {
        data[0x11] &= ~0x3f; // Clear the mode bits
        data[0x11] |= mode.getId() & 0x3f;
    }

    /*
     * For Testing assertion get Swing result
     */
    public int getSwingMode() {
        return data[0x11];
    }

    /*
     * Activates the sleep function. Setpoint Temp increases in first
     * two hours of sleep by 1 degree in Cool mode
     */
    public void setSleepMode(boolean sleepModeEnabled) {
        if (sleepModeEnabled) {
            data[0x14] |= 0x01;
        } else {
            data[0x14] &= (~0x01);
        }
    }

    /*
     * Sets the Turbo mode for maximum cooling or heat
     */
    public void setTurboMode(boolean turboModeEnabled) {
        if (turboModeEnabled) {
            data[0x14] |= 0x02;
        } else {
            data[0x14] &= (~0x02);
        }
    }

    /*
     * Set the Indoor Unit display to Fahrenheit from Celsius
     */
    public void setFahrenheit(boolean fahrenheitEnabled) {
        if (fahrenheitEnabled) {
            data[0x14] |= 0x04;
        } else {
            data[0x14] &= (~0x04);
        }
    }

    /*
     * Toggles the LED display.
     * This uses the request format, so needs modification, but need to keep
     * current beep and operating state.
     */
    public void setScreenDisplay(boolean screenDisplayToggle) {
        modifyBytesForDisplayOff();
        removeExtraBytes();
        logger.debug(" Set Bytes before crypt {}", Utils.bytesToHex(data));
    }

    private void modifyBytesForDisplayOff() {
        data[0x01] = (byte) 0x20;
        data[0x09] = (byte) 0x03;
        data[0x0a] = (byte) 0x41;
        data[0x0b] |= 0x02; // Set
        data[0x0b] &= ~(byte) 0x80; // Clear
        data[0x0c] = (byte) 0x00;
        data[0x0d] = (byte) 0xff;
        data[0x0e] = (byte) 0x02;
        data[0x0f] = (byte) 0x00;
        data[0x10] = (byte) 0x02;
        data[0x11] = (byte) 0x00;
        data[0x12] = (byte) 0x00;
        data[0x13] = (byte) 0x00;
        data[0x14] = (byte) 0x00;
    }

    private void removeExtraBytes() {
        byte[] newData = new byte[data.length - 3];
        System.arraycopy(data, 0, newData, 0, newData.length);
        data = newData;
    }

    /*
     * Add 0.5C to the temperature value. If needed
     * Target_temperature setter calls this method
     */
    private void setTemperatureDot5(boolean temperatureDot5Enabled) {
        if (temperatureDot5Enabled) {
            data[0x0c] |= 0x10;
        } else {
            data[0x0c] &= (~0x10);
        }
    }

    /*
     * Set the ON timer for AC device start.
     */
    public void setOnTimer(TimerData timerData) {
        setOnTimer(timerData.status, timerData.hours, timerData.minutes);
    }

    public void setOnTimer(boolean on, int hours, int minutes) {
        // Process minutes (1 bit = 15 minutes)
        int bits = (int) Math.floor(minutes / 15);
        int subtract = 0;
        if (bits != 0) {
            subtract = (15 - (int) (minutes - bits * 15));
        }
        if (bits == 0 && minutes != 0) {
            subtract = (15 - minutes);
        }
        data[0x0e] &= ~(byte) 0xff; // Clear
        data[0x10] &= ~(byte) 0xf0;
        if (on) {
            data[0x0e] |= 0x80;
            data[0x0e] |= (hours << 2) & 0x7c;
            data[0x0e] |= bits & 0x03;
            data[0x10] |= (subtract << 4) & 0xf0;
        } else {
            data[0x0e] = 0x7f;
        }
    }

    /*
     * For Testing assertion get On Timer result
     */
    public int getOnTimer() {
        return (data[0x0e] & 0xff);
    }

    /*
     * For Testing assertion get On Timer result (subtraction amount)
     */
    public int getOnTimer2() {
        return ((data[0x10] & (byte) 0xf0) >> 4) & 0x0f;
    }

    /*
     * Set the timer for AC device stop.
     */
    public void setOffTimer(TimerData timerData) {
        setOffTimer(timerData.status, timerData.hours, timerData.minutes);
    }

    public void setOffTimer(boolean on, int hours, int minutes) {
        int bits = (int) Math.floor(minutes / 15);
        int subtract = 0;
        if (bits != 0) {
            subtract = (15 - (int) (minutes - bits * 15));
        }
        if (bits == 0 && minutes != 0) {
            subtract = (15 - minutes);
        }
        data[0x0f] &= ~(byte) 0xff; // Clear
        data[0x10] &= ~(byte) 0x0f;
        if (on) {
            data[0x0f] |= 0x80;
            data[0x0f] |= (hours << 2) & 0x7c;
            data[0x0f] |= bits & 0x03;
            data[0x10] |= subtract & 0x0f;
        } else {
            data[0x0f] = 0x7f;
        }
    }

    /*
     * For Testing assertion get Off Timer result
     */
    public int getOffTimer() {
        return (data[0x0f] & 0xff);
    }

    /*
     * For Testing assertion get Off Timer result (subtraction)
     */
    public int getOffTimer2() {
        return ((data[0x10] & (byte) 0x0f)) & 0x0f;
    }
}
