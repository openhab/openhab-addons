/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.helpers;

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;

/**
 * {@link TapoUtils} TapoUtils -
 * Utility Helper Functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class TapoUtils {

    /************************************
     * CALCULATION UTILS
     ***********************************/
    /**
     * Limit Value between limits
     * 
     * @param value Integer
     * @param lowerLimit
     * @param upperLimit
     * @return
     */
    public static Integer limitVal(@Nullable Integer value, Integer lowerLimit, Integer upperLimit) {
        if (value == null || value < lowerLimit) {
            return lowerLimit;
        } else if (value > upperLimit) {
            return upperLimit;
        }
        return value;
    }

    /************************************
     * TYPE UTILS
     ***********************************/

    /**
     * Return OnOffType from bool
     * 
     * @param boolVal
     */
    public static OnOffType getOnOffType(@Nullable Boolean boolVal) {
        return (boolVal != null ? boolVal ? OnOffType.ON : OnOffType.OFF : OnOffType.OFF);
    }

    /**
     * Return OnOffType from bool
     * 
     * @param boolVal
     */
    public static OnOffType getOnOffType(Integer intVal) {
        return intVal == 0 ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * Return StringType from String
     * 
     * @param strVal
     */
    public static StringType getStringType(@Nullable String strVal) {
        return new StringType(strVal != null ? strVal : "");
    }

    /**
     * Return DecimalType from Double
     * 
     * @param numVal
     */
    public static DecimalType getDecimalType(@Nullable Double numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Return DecimalType from Integer
     * 
     * @param numVal
     */
    public static DecimalType getDecimalType(@Nullable Integer numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Return DecimalType from Long
     * 
     * @param numVal
     */
    public static DecimalType getDecimalTypel(@Nullable Long numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * 
     * @param numVal value 0-100
     * @return PercentType
     */
    public static PercentType getPercentType(@Nullable Integer numVal) {
        Integer val = limitVal(numVal, 0, 100);
        return new PercentType(val);
    }

    /**
     * Return HSBType from integers
     * 
     * @param hue integer hue-color
     * @param saturation integer saturation
     * @param brightness integer brightness
     * @returnvHSBType
     */
    public static HSBType getHSBType(Integer hue, Integer saturation, Integer brightness) {
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        PercentType b = new PercentType(brightness);
        return new HSBType(h, s, b);
    }
}
