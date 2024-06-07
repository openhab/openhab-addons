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
package org.openhab.binding.tapocontrol.internal.helpers.utils;

import static org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils.*;

import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;

/**
 * {@link TypeUtils} TypeUtils -
 * Utility Helper Functions handling type helper functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class TypeUtils {
    /**
     * Return OnOffType from bool
     * 
     * @param boolVal
     */
    public static OnOffType getOnOffType(@Nullable Boolean boolVal) {
        return boolVal != null ? OnOffType.from(boolVal) : OnOffType.OFF;
    }

    /**
     * Return OnOffType from Integer
     * 
     * @param intVal
     */
    public static OnOffType getOnOffType(Integer intVal) {
        return OnOffType.from(intVal != 0);
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
     * @return HSBType
     */
    public static HSBType getHSBType(Integer hue, Integer saturation, Integer brightness) {
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        PercentType b = new PercentType(brightness);
        return new HSBType(h, s, b);
    }

    /**
     * Return QuantityType with Time
     * 
     * @param numVal Number with value
     * @param unit TimeUnit
     * @return QuantityType of Type Time
     */
    public static QuantityType<Time> getTimeType(@Nullable Number numVal, Unit<Time> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Return QuantityType with Power
     * 
     * @param numVal Number with value
     * @param unit PowerUnit
     * @return QuantityType of Type Power
     */
    public static QuantityType<Power> getPowerType(@Nullable Number numVal, Unit<Power> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Return QuantityType with Energy
     * 
     * @param numVal Number with value
     * @param unit PowerUnit
     * @return QuantityType of Type Energy
     */
    public static QuantityType<Energy> getEnergyType(@Nullable Number numVal, Unit<Energy> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Return QuantityType with Temperature
     * 
     * @param numVal Number with value
     * @param unit TemperatureUnit
     * @return QuantityType of Type Temperature
     */
    public static QuantityType<Temperature> getTemperatureType(@Nullable Number numVal, Unit<Temperature> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }
}
