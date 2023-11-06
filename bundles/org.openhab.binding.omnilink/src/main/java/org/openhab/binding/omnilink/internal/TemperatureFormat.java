/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.omnilink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.digitaldan.jomnilinkII.MessageUtils;

/**
 * The {@link TemperatureFormat} defines some methods that are used to
 * convert OmniLink temperature values into Fahrenheit or Celsius.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public enum TemperatureFormat {
    // Don't convert zero - it appears that is what omni returns when there is no value.
    CELSIUS(2) {
        @Override
        public float omniToFormat(int omniNumber) {
            return MessageUtils.omniToC(omniNumber);
        }

        @Override
        public int formatToOmni(float celsius) {
            return MessageUtils.CToOmni(celsius);
        }
    },
    FAHRENHEIT(1) {
        @Override
        public float omniToFormat(int omniNumber) {
            return MessageUtils.omniToF(omniNumber);
        }

        @Override
        public int formatToOmni(float fahrenheit) {
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
    public abstract float omniToFormat(int omniNumber);

    /**
     * Convert a number from this format into an omni number.
     *
     * @param format Number in the current format.
     * @return Omni formatted number.
     */
    public abstract int formatToOmni(float format);

    /**
     * Get the number which identifies this format as defined by the OmniLink protocol.
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
            throw new IllegalArgumentException("Invalid temperature format!");
        }
    }
}
