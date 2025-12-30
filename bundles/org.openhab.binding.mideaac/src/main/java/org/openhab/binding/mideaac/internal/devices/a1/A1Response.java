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
import org.openhab.binding.mideaac.internal.devices.Timer;
import org.openhab.binding.mideaac.internal.devices.Timer.TimerData;
import org.openhab.binding.mideaac.internal.devices.a1.A1StringCommands.A1FanSpeed;
import org.openhab.binding.mideaac.internal.devices.a1.A1StringCommands.A1OperationalMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link A1Response} performs the byte data stream decoding
 * The original reference is
 * https://github.com/georgezhao2010/midea_ac_lan/blob/06fc4b582a012bbbfd6bd5942c92034270eca0eb/custom_components/midea_ac_lan/midea/devices/a1/message.py#L418
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Bob Eckhoff - OH addons changes, logging, TimerData addition
 */
@NonNullByDefault
public class A1Response {
    private Logger logger = LoggerFactory.getLogger(A1Response.class);
    private byte[] data;

    /**
     * Response class Parameters
     *
     * @param data byte array from device
     */
    public A1Response(byte[] data) {
        this.data = data;

        if (logger.isDebugEnabled()) {
            logger.debug("Power State: {}", getPowerState());
            logger.debug("Maximum Humidity: {}", getMaximumHumidity());
            logger.debug("Operational Mode: {}", getA1OperationalMode());
            logger.debug("Fan Speed: {}", getA1FanSpeed());
            logger.debug("Child Lock: {}", getA1ChildLock());
            logger.debug("Anion: {}", getA1Anion());
            logger.debug("Tank: {}", getTank());
            logger.debug("Water Level Set: {}", getTankSetpoint());
            logger.debug("Swing: {}", getA1SwingMode());
            logger.debug("Current Humidity: {}", getCurrentHumidity());
            logger.debug("Prompt Tone: {}", getPromptTone());
            logger.debug("Current Temperature: {}", getIndoorTemperature());
        }
    }

    /**
     * Device On or Off
     *
     * @return power state true or false
     */
    public boolean getPowerState() {
        return (data[0x01] & 0x1) > 0;
    }

    /**
     * Read only
     *
     * @return prompt tone true or false
     */
    public boolean getPromptTone() {
        return (data[0x01] & 0x40) > 0;
    }

    /**
     * Manual, Continuous, Auto, etc. See Command Base class
     *
     * @return Manual, Continuous, Auto, etc.
     */
    public A1OperationalMode getA1OperationalMode() {
        return A1OperationalMode.fromId(data[0x02] & 0x0f);
    }

    /**
     * Lowest, Low, Medium, High, Auto etc. See Command Base class
     *
     * @return Lowest, Low, Medium, High, Auto etc.
     */
    public A1FanSpeed getA1FanSpeed() {
        return A1FanSpeed.fromId(data[0x03] & 0x7f);
    }

    /**
     * Creates String representation of the On timer to the channel
     * 
     * @return String of HH:MM
     */
    public Timer getOnTimer() {
        return new Timer((data[0x04] & 0x80) > 0, ((data[0x04] & (byte) 0x7c) >> 2),
                ((data[0x04] & 0x3) * 15 + 15 - (((data[0x06] & (byte) 0xf0) >> 4) & 0x0f)));
    }

    /**
     * This is used to carry the current On Timer (last response) through
     * subsequent Set commands, so it is not overwritten.
     * 
     * @return status plus String of HH:MM
     */
    public TimerData getOnTimerData() {
        int hours = 0;
        int minutes = 0;
        Timer timer = new Timer(true, hours, minutes);
        boolean status = (data[0x04] & 0x80) > 0;
        hours = ((data[0x04] & (byte) 0x7c) >> 2);
        minutes = ((data[0x04] & 0x3) * 15 + 15 - (((data[0x06] & (byte) 0xf0) >> 4) & 0x0f));
        return timer.new TimerData(status, hours, minutes);
    }

    /**
     * Creates String representation of the Off timer to the channel
     * 
     * @return String of HH:MM
     */
    public Timer getOffTimer() {
        return new Timer((data[0x05] & 0x80) > 0, ((data[0x05] & (byte) 0x7c) >> 2),
                ((data[0x05] & 0x3) * 15 + 15 - (data[0x06] & (byte) 0xf)));
    }

    /**
     * This is used to carry the Off timer (last response) through
     * subsequent Set commands, so it is not overwritten.
     * 
     * @return status plus String of HH:MM
     */
    public TimerData getOffTimerData() {
        int hours = 0;
        int minutes = 0;
        Timer timer = new Timer(true, hours, minutes);
        boolean status = (data[0x05] & 0x80) > 0;
        hours = ((data[0x05] & (byte) 0x7c) >> 2);
        minutes = (data[0x05] & 0x3) * 15 + 15 - (((data[0x06] & (byte) 0xf) & 0x0f));
        return timer.new TimerData(status, hours, minutes);
    }

    /**
     * Setpoint for Humidity
     *
     * @return current setpoint in %
     */
    public int getMaximumHumidity() {
        return data[0x07] < 35 ? 35 : data[0x07];
    }

    /**
     * Child Lock
     *
     * @return child lock true or false
     */
    public boolean getA1ChildLock() {
        return (data[0x08] & 0x80) > 0;
    }

    /**
     * Anion Don't know what this is
     *
     * @return anion true or false
     */
    public boolean getA1Anion() {
        return (data[0x09] & 0x40) > 0;
    }

    /**
     * Tank Status, likely percentage full
     *
     * @return Tank
     */
    public int getTank() {
        return data[10] & 0x7F;
    }

    /**
     * Water Level Setpoint
     *
     * @return Water level setpoint
     */
    public int getTankSetpoint() {
        return data[15];
    }

    /**
     * Current Humidity in Room
     *
     * @return Current humidity
     */
    public int getCurrentHumidity() {
        return data[16];
    }

    /**
     * Device Indoor Temperature
     *
     * @return Indoor temperature
     */
    public float getIndoorTemperature() {
        if (((Byte.toUnsignedInt(data[17]) - 50) / 2.0) < -19) {
            return -19;
        }
        if (((Byte.toUnsignedInt(data[17]) - 50) / 2.0) > 50) {
            return 50;
        }
        return (Byte.toUnsignedInt(data[17]) - 50f) / 2.0f;
    }

    /**
     * Swing for Dehumidifer is boolean
     *
     * @return swing true or false
     */
    public boolean getA1SwingMode() {
        return (data[19] & 0x20) > 0;
    }
}
