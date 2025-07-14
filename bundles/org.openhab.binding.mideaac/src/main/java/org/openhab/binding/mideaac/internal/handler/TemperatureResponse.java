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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TemperatureResponse} handles the unsolicited 0xA1 temperature report messages
 * from the device
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class TemperatureResponse {
    private final byte[] rawData;
    private Logger logger = LoggerFactory.getLogger(TemperatureResponse.class);

    /**
     * Initialization
     * 
     * @param rawData as bytes
     */
    public TemperatureResponse(byte[] rawData) {
        this.rawData = rawData;
        if (logger.isDebugEnabled()) {
            logger.debug("Humidity from 0xA1: {}", getHumidity());
            logger.debug("Indoor Temperature from 0xA1: {}", getIndoorTemperature());
            logger.debug("Outdoor Temperature from 0xA1: {}", getOutdoorTemperature());
            logger.debug("Current Work Time (minutes) from 0xA1: {}", getCurrentWorkTime());
        }
    }

    /**
     * Reported Room Humidity from 0xA1 message type
     * 
     * @return humidity
     */
    public int getHumidity() {
        return (rawData[17] & (byte) 0x7f);
    }

    /**
     * Current Work Time from 0xA1 message type
     * not validated from test
     * 
     * @return CurrentWorkTime
     */
    public int getCurrentWorkTime() {
        return ((((rawData[9] & 0xFF) << 8) & 0xFF00) | (rawData[10] & 0x00FF)) * 60 * 24 + (rawData[11] & 0xFF) * 60
                + (rawData[12] & 0xFF);
    }

    /**
     * Reported indoor temperature from 0xA1 message type
     * 
     * @return indoor temperature
     */
    public Float getIndoorTemperature() {
        Float indoorTemperatureValue = (float) 0.0;
        Float smallIndoorTemperatureValue = (float) 0.0;
        Float indoorTemperature = (float) 0.0;
        if ((rawData[13] & 0xFF) != 0x00 && (rawData[13] & 0xFF) != 0xFF) {
            indoorTemperatureValue = (float) ((rawData[13] & 0xFF) - 50.0f) / 2.0f;
            smallIndoorTemperatureValue = (float) (rawData[18] & 0x0F);
            indoorTemperature = indoorTemperatureValue + smallIndoorTemperatureValue / 10f;
        }
        return indoorTemperature;
    }

    /**
     * Reported Outdoor Temperature from 0xA1 message type
     * 
     * @return outdoor temperature
     */
    public Float getOutdoorTemperature() {
        Float outdoorTemperatureValue = (float) 0.0;
        Float smallOutdoorTemperatureValue = (float) 0.0;
        Float outdoorTemperature = (float) 0.0;
        if ((rawData[14] & 0xFF) != 0x00 && (rawData[14] & 0xFF) != 0xFF) {
            outdoorTemperatureValue = (float) ((rawData[14] & 0xFF) - 50.0f) / 2.0f;
            smallOutdoorTemperatureValue = (float) ((rawData[18] & 0xFF) >>> 4);
            outdoorTemperature = outdoorTemperatureValue + smallOutdoorTemperatureValue / 10.0f;
        }
        return outdoorTemperature;
    }
}
