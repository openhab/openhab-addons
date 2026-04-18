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
package org.openhab.binding.mideaac.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HumidityResponse} handles the unsolicited 0xA0 humidity report messages
 * from the device
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class HumidityResponse {
    private final byte[] rawData;
    private Logger logger = LoggerFactory.getLogger(HumidityResponse.class);
    private int version = 3;

    /**
     * Initialization
     * 
     * @param rawData as bytes
     */
    public HumidityResponse(byte[] rawData) {
        this.rawData = rawData;
        if (logger.isDebugEnabled()) {
            logger.debug("Humidity: {}", getHumidity());
            logger.debug("Power from 0xA0 {}", getPowerState());
            logger.debug("Operational Mode from 0xA0 {}", getOperationalMode());
            logger.debug("Target Temperature from 0xA0 {}", getTargetTemperature());
            logger.debug("Fan Speed from 0xA0 {}", getFanSpeed());
            logger.debug("Swing Mode from 0xA0 {}", getSwingMode());
        }
    }

    /**
     * Reported Room Humidity from 0xA0 message type
     * 
     * @return humidity
     */
    public int getHumidity() {
        return (rawData[13] & (byte) 0x7f);
    }

    /**
     * Power from 0xA0 message type
     * not used in channels
     * 
     * @return power status
     */
    public boolean getPowerState() {
        return (rawData[0x01] & 0x1) > 0;
    }

    /**
     * Cool, Heat, Fan Only, etc. from 0xA0 message type
     * Not used in Channels
     * 
     * @return Cool, Heat, Fan Only, etc.
     */
    public OperationalMode getOperationalMode() {
        return OperationalMode.fromId((rawData[0x02] & 0xe0) >> 5);
    }

    /**
     * Target Temperature from 0xA0 message type - Different
     * Not used in Channels
     * 
     * @return current setpoint in degrees C
     */
    public float getTargetTemperature() {
        return ((rawData[0x01] & 0x3E) >> 1) + 12.0f + (((rawData[0x01] & 0x40) >> 6 > 0) ? 0.5f : 0.0f);
    }

    /**
     * Low, Medium, High, Auto etc. See Command Base class
     * From message 0XA0; assumed version 3 for this message
     * Not Used in Channels
     * 
     * @return Low, Medium, High, Auto etc.
     */
    public FanSpeed getFanSpeed() {
        return FanSpeed.fromId(rawData[0x03] & 0x7f, version);
    }

    /**
     * Status of the vertical and/or horzontal louver
     * From message 0XA0; assumed version 3 for this message
     * Not Used in Channels
     * 
     * @return Vertical, Horizontal, Off, Both
     */
    public SwingMode getSwingMode() {
        return SwingMode.fromId(rawData[0x07] & 0x3f, version);
    }
}
