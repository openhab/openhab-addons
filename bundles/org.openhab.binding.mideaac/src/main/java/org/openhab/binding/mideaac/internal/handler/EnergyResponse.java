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

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergyResponse} handles the energy messages
 * from the device
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class EnergyResponse {
    private final byte[] rawData;
    private Logger logger = LoggerFactory.getLogger(EnergyResponse.class);

    /**
     * Initialization
     * 
     * @param rawData as bytes
     */
    public EnergyResponse(byte[] rawData) {
        this.rawData = rawData;

        if (logger.isDebugEnabled()) {
            logger.debug("Total Kilowatt Hours: {}", getKilowattHours());
            logger.debug("Current Amperes: {}", getAmperes());
            logger.debug("Power Watts: {}", getWatts());
            logger.debug("Total Kilowatt Hours BCD: {}", getKilowattHoursBCD());
            logger.debug("Current Amperes BCD: {}", getAmperesBCD());
            logger.debug("Power Watts BCD: {}", getWattsBCD());
        }
    }

    /**
     * Kilowatt Hours using binary
     * 
     * @return kilowatt Hours
     */
    public double getKilowattHours() {
        return ByteBuffer.wrap(rawData, 4, 4).getInt() / 100.00;
    }

    /**
     * Amperes in use using binary
     * 
     * @return amperes
     */
    public double getAmperes() {
        return ByteBuffer.wrap(rawData, 12, 4).getInt() / 10.0;
    }

    /**
     * Watts in use using binary
     * 
     * @return watts
     */
    public double getWatts() {
        return ((rawData[16] & 0xFF) << 16 | (rawData[17] & 0xFF) << 8 | (rawData[18] & 0xFF)) / 10.0;
    }

    /**
     * Kilowatt Hours using BCD
     * 
     * @return kilowatt Hours
     */
    public double getKilowattHoursBCD() {
        double kilowattHours = 0.0;
        kilowattHours = bcdToDecimal(rawData, 4, 4) / 100.00;
        return kilowattHours;
    }

    /**
     * Amperes using BCD
     * 
     * @return amperes
     */
    public double getAmperesBCD() {
        double amperes = 0.0;
        amperes = bcdToDecimal(rawData, 12, 4) / 10.0;
        return amperes;
    }

    /**
     * Watts Using BCD
     * 
     * @return watts
     */
    public double getWattsBCD() {
        double watts = 0.0;
        watts = bcdToDecimal(rawData, 16, 3) / 10.0;
        return watts;
    }

    private long bcdToDecimal(byte[] data, int offset, int length) {
        long decimalValue = 0;
        for (int i = 0; i < length; i++) {
            int byteValue = data[offset + i] & 0xFF; // Ensure byte is unsigned
            decimalValue = decimalValue * 100 + ((byteValue >> 4) * 10) + (byteValue & 0x0F);
        }
        return decimalValue;
    }
}
