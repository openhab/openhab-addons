/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import com.digitaldan.jomnilinkII.MessageUtils;

/**
 *
 * @author Craig Hamilton
 *
 */
public enum TemperatureFormat {
    // Don't convert zero - it appears that is what omni returns when there is no value.
    CELSIUS(2) {
        @Override
        int omniToFormat(int omniNumber) {
            return MessageUtils.omniToC(omniNumber);
        }

        @Override
        int formatToOmni(int celsius) {
            return MessageUtils.CToOmni(celsius);
        }
    },
    FAHRENHEIT(1) {
        @Override
        int omniToFormat(int omniNumber) {
            return MessageUtils.omniToF(omniNumber);
        }

        @Override
        int formatToOmni(int fahrenheit) {
            return MessageUtils.FtoOmni(fahrenheit);
        }
    };

    private final int formatNumber;

    private TemperatureFormat(int formatNumber) {
        this.formatNumber = formatNumber;
    }

    /**
     * Convert a number represented by the omni to the format.
     *
     * @param omniNumber Number to convert
     * @return Number converted to appropriate format.
     */
    abstract int omniToFormat(int omniNumber);

    /**
     * Convert a number from this format into an omni number.
     *
     * @param format Number in the current format.
     * @return Omni formatted number.
     */
    abstract int formatToOmni(int format);

    /**
     * Get the number which identifies this format as defined by the omniprotocol.
     *
     * @return Number which identifies this temperature format.
     */
    public int getFormatNumber() {
        return formatNumber;
    }

    public static TemperatureFormat valueOf(int tempFormat) {
        if (tempFormat == CELSIUS.formatNumber) {
            return CELSIUS;
        } else if (tempFormat == FAHRENHEIT.formatNumber) {
            return FAHRENHEIT;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
