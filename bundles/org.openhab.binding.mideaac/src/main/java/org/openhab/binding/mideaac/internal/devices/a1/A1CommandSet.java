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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.A1CommandBase;
import org.openhab.binding.mideaac.internal.devices.Timer.TimerData;
import org.openhab.binding.mideaac.internal.devices.a1.A1StringCommands.A1FanSpeed;
import org.openhab.binding.mideaac.internal.devices.a1.A1StringCommands.A1OperationalMode;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link A1CommandSet} class handles the allowed changes originating from
 * the items linked to the Midea dehumidifer channels. Not all devices
 * support all commands. The general process is to clear the
 * bit(s) the set them to the command value and change message type to command.
 *
 * @author Bob Eckhoff - - Initial contribution
 */
@NonNullByDefault
public class A1CommandSet extends A1CommandBase {
    private final Logger logger = LoggerFactory.getLogger(A1CommandSet.class);

    /**
     * Byte array structure for Command set
     */
    public A1CommandSet() {
        super();
        data[0x01] = (byte) 0x23;
        data[0x09] = (byte) 0x02; // Setting Mode
        data[0x0a] = (byte) 0x48; // Command Message for Dehumidifier

        byte[] extra = { 0x00, 0x00, 0x00 };
        byte[] newData = new byte[data.length + 3];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[data.length] = extra[0];
        newData[data.length + 1] = extra[1];
        newData[data.length + 2] = extra[2];
        data = newData;

        // Zap the old timestamp
        int oldTimestampIndex = data.length - 4;
        data[oldTimestampIndex] = 0x00;

        // Add the new timestamp
        applyTimestamp();
    }

    // Controllable items for Dehumidifier
    public static A1CommandSet fromResponse(A1Response dh) {
        A1CommandSet commandSet = new A1CommandSet();

        // Common fields
        commandSet.setPowerState(dh.getPowerState());
        commandSet.setOnTimer(dh.getOnTimerData());
        commandSet.setOffTimer(dh.getOffTimerData());
        commandSet.setPromptTone(dh.getPromptTone());

        // DH-specific fields
        commandSet.setA1FanSpeed(dh.getA1FanSpeed());
        commandSet.setA1OperationalMode(dh.getA1OperationalMode());
        commandSet.setA1MaximumHumidity(dh.getMaximumHumidity());
        commandSet.setA1ChildLock(dh.getA1ChildLock());
        commandSet.setA1SwingMode(dh.getA1SwingMode());
        commandSet.setA1Anion(dh.getA1Anion());
        commandSet.setTankSetpoint(dh.getTankSetpoint());

        return commandSet;
    }

    /**
     * Causes Midea device to beep when Set command received
     * 
     * @param feedbackEnabled Beep On or Off
     */
    public void setPromptTone(boolean feedbackEnabled) {
        if (!feedbackEnabled) {
            data[0x0b] &= ~(byte) 0x40; // Clear
        } else {
            data[0x0b] |= (byte) 0x40; // Set
        }
    }

    /**
     * Turns device On or Off (Use whole byte?)
     * 
     * @param state on or off
     */
    public void setPowerState(boolean state) {
        if (!state) {
            data[0x0b] &= ~0x01;
        } else {
            data[0x0b] |= 0x01;
        }
    }

    /**
     * For Testing assertion get result
     * 
     * @return true or false
     */
    public boolean getPowerState() {
        return (data[0x0b] & 0x1) > 0;
    }

    /**
     * Manual, Continuous, Auto, etc. See Command Base class
     * 
     * @param mode Manual, Continuous, Auto, etc.
     */
    public void setA1OperationalMode(A1OperationalMode mode) {
        data[0x0c] &= ~(byte) 0x0f;
        data[0x0c] |= (byte) mode.getId();
    }

    /**
     * For Testing assertion get result
     * 
     * @return operational mode
     */
    public int getA1OperationalMode() {
        return data[0x0c] & (byte) 0x0f;
    }

    /**
     * Low, Medium, High, Auto etc. See Command Base class
     * 
     * @param speed Set fan speed
     */
    public void setA1FanSpeed(A1FanSpeed speed) {
        data[0x0d] = (byte) (speed.getId());
    }

    /**
     * For Testing assertion get Fan Speed results
     * 
     * @return fan speed as a number
     */
    public int getA1FanSpeed() {
        return data[0x0d];
    }

    /**
     * Set the ON timer for Dehumidifer device start.
     * 
     * @param timerData status (On or Off), hours, minutes
     */
    public void setOnTimer(TimerData timerData) {
        setOnTimer(timerData.status, timerData.hours, timerData.minutes);
    }

    /**
     * Calculates remaining time until On
     * 
     * @param on is timer on
     * @param hours hours remaining
     * @param minutes minutes remaining
     */
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

    /**
     * For Testing assertion get On Timer result
     * 
     * @return timer data base
     */
    public int getOnTimer() {
        return (data[0x0e] & 0xff);
    }

    /**
     * For Testing assertion get On Timer result (subtraction amount)
     * 
     * @return timer data subtraction
     */
    public int getOnTimer2() {
        return ((data[0x10] & (byte) 0xf0) >> 4) & 0x0f;
    }

    /**
     * Set the timer for Humidifier device stop.
     * 
     * @param timerData status (On or Off), hours, minutes
     */
    public void setOffTimer(TimerData timerData) {
        setOffTimer(timerData.status, timerData.hours, timerData.minutes);
    }

    /**
     * Calculates remaining time until Off
     * 
     * @param on is timer on
     * @param hours hours remaining
     * @param minutes minutes remaining
     */
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

    /**
     * For Testing assertion get Off Timer result
     * 
     * @return hours and minutes
     */
    public int getOffTimer() {
        return (data[0x0f] & 0xff);
    }

    /**
     * For Testing assertion get Off Timer result (subtraction)
     * 
     * @return minutes to subtract
     */
    public int getOffTimer2() {
        return ((data[0x10] & (byte) 0x0f)) & 0x0f;
    }

    /**
     * Sets the Target Humidity for dehumidifier
     * 
     * @param humidity
     */
    public void setA1MaximumHumidity(int humidity) {
        data[0x11] &= ~(byte) 0xff;
        data[0x11] |= humidity;
    }

    /**
     * Child Lock setter for Dehumidifier
     * 
     * @param childLockEnabled sets child lock true or false
     */
    public void setA1ChildLock(boolean childLockEnabled) {
        if (!childLockEnabled) {
            data[0x12] &= ~0x80;
        } else {
            data[0x12] |= 0x80;
        }
    }

    /**
     * Anion setter for Dehumidifier
     * 
     * @param anionEnabled sets anion true or false
     */
    public void setA1Anion(boolean anionEnabled) {
        if (!anionEnabled) {
            data[0x13] &= ~0x40;
        } else {
            data[0x13] |= 0x40;
        }
    }

    /**
     * Swing mode setter for Dehumidifier
     * 
     * @param mode sets swing mode
     */
    public void setA1SwingMode(boolean swingModeEnabled) {
        if (!swingModeEnabled) {
            data[0x14] &= ~0x20;
        } else {
            data[0x14] |= 0x20;
        }
    }

    /**
     * For Testing assertion get Dehumidifier Swing result
     * 
     * @return swing mode
     */
    public boolean getA1SwingMode() {
        return (data[0x14] & 0x20) > 0;
    }

    /**
     * Sets the Humidifier Water tank level setpoint
     * 
     * @param humidity Water tank level setpoint
     */
    public void setTankSetpoint(int tankSetpoint) {
        data[0x17] &= ~(byte) 0xff;
        data[0x17] |= tankSetpoint;
    }

    /**
     * Creates the Initial Get Capability message
     * 
     * @return Capability message
     */
    public void getCapabilities() {
        modifyBytesForCapabilities();
        removeExtraCapabilityBytes();
        logger.debug("Set Capability Bytes before encrypt {}", HexUtils.bytesToHex(data));
    }

    private void modifyBytesForCapabilities() {
        data[0x01] = (byte) 0x0E;
        data[0x09] = (byte) 0x03;
        data[0x0a] = (byte) 0xB5;
        data[0x0b] = (byte) 0x01;
        data[0x0c] = (byte) 0x00;
    }

    private void removeExtraCapabilityBytes() {
        byte[] newData = new byte[data.length - 21];
        System.arraycopy(data, 0, newData, 0, newData.length);
        data = newData;
    }

    /**
     * Creates the Additional Get capability message
     * 
     * @return Additional Capability message
     */
    public void getAdditionalCapabilities() {
        modifyBytesForAdditionalCapabilities();
        removeExtraAdditionalCapabilityBytes();
        logger.trace("Set Additional Capability Bytes before encrypt {}", HexUtils.bytesToHex(data));
    }

    private void modifyBytesForAdditionalCapabilities() {
        data[0x01] = (byte) 0x0F;
        data[0x09] = (byte) 0x03;
        data[0x0a] = (byte) 0xB5;
        data[0x0b] = (byte) 0x01;
        data[0x0c] = (byte) 0x01;
        data[0x0d] = (byte) 0x01;
    }

    private void removeExtraAdditionalCapabilityBytes() {
        byte[] newData = new byte[data.length - 20];
        System.arraycopy(data, 0, newData, 0, newData.length);
        data = newData;
    }
}
