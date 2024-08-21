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
package org.openhab.binding.dmx.internal;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dmx.internal.multiverse.DmxChannel;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;

/**
 * {@link Util} is a set of helper functions
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Util {
    /**
     * inRange checks if a value is between two other values
     *
     * @param value test value
     * @param min
     * @param max
     * @return true or false
     */
    public static boolean inRange(int value, int min, int max) {
        if (value < min) {
            return false;
        }
        return value <= max;
    }

    /**
     * coerce a value to fit in a range, write log with comment
     *
     * @param value
     * @param min
     * @param max
     * @param logger logger that shall be used
     * @param var name of the variable (used for logging)
     * @return coerced value
     */
    public static int coerceToRange(int value, int min, int max, @Nullable Logger logger, String var) {
        if (value < min) {
            if (logger != null) {
                logger.warn("coerced {} {} to allowed range {}-{}", var, value, min, max);
            }
            return min;
        }
        if (value > max) {
            if (logger != null) {
                logger.warn("coerced {} {} to allowed range {}-{}", var, value, min, max);
            }
            return max;
        }
        return value;
    }

    /**
     * coerce a value to fit in a range, write log
     *
     * @param value
     * @param min
     * @param max
     * @param logger logger that shall be used
     * @return coerced value
     */
    public static int coerceToRange(int value, int min, int max, @Nullable Logger logger) {
        return coerceToRange(value, min, max, logger, "");
    }

    /**
     * coerce a value to fit in a range
     *
     * @param value
     * @param min
     * @param max
     * @return coerced value
     */
    public static int coerceToRange(int value, int min, int max) {
        return coerceToRange(value, min, max, null, "");
    }

    /**
     * convert PercentType to DMX value and check range
     *
     * @param value value as PercentType
     * @return value as Integer (0-255)
     */
    public static int toDmxValue(PercentType value) {
        int intValue = (int) (value.doubleValue() * (DmxChannel.MAX_VALUE - DmxChannel.MIN_VALUE) / 100.0
                + DmxChannel.MIN_VALUE);
        return coerceToRange(intValue, DmxChannel.MIN_VALUE, DmxChannel.MAX_VALUE);
    }

    /**
     * check range of DMX value
     *
     * @param value value as Integer
     * @return value as Integer (0-255)
     */
    public static int toDmxValue(int value) {
        return coerceToRange(value, DmxChannel.MIN_VALUE, DmxChannel.MAX_VALUE);
    }

    /**
     * check range of DMX value
     *
     * @param value value as String
     * @return value as Integer (0-255)
     */
    public static int toDmxValue(String value) {
        return coerceToRange(Integer.valueOf(value), DmxChannel.MIN_VALUE, DmxChannel.MAX_VALUE);
    }

    /**
     * convert float to DMX value
     *
     * @param value value as float
     * @return value as Integer (0-255)
     */
    public static int toDmxValue(float value) {
        return toDmxValue(Math.round(value));
    }

    /**
     * convert DMX value to PercentType
     *
     * @param value value as Integer( 0-255)
     * @return value as PercentType
     */
    public static PercentType toPercentValue(int value) {
        if (value == DmxChannel.MIN_VALUE) {
            return PercentType.ZERO;
        } else {
            return new PercentType(new BigDecimal(
                    ((value - DmxChannel.MIN_VALUE) * 100.0) / (DmxChannel.MAX_VALUE - DmxChannel.MIN_VALUE)));
        }
    }

    /**
     * calculate a fraction of the fadeTime depending on current and target value
     *
     * @param currentValue current channel value as Integer
     * @param targetValue target channel value as Integer
     * @param fadeTime fadeTime in ms
     * @return fraction needed for fading
     */
    public static int fadeTimeFraction(int currentValue, int targetValue, int fadeTime) {
        return Math.abs(targetValue - currentValue) * fadeTime / (DmxChannel.MAX_VALUE - DmxChannel.MIN_VALUE);
    }
}
