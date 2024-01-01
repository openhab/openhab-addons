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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link SensorEnum} lists all available digitalSTROM sensor types.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 * @see <a href="http://developer.digitalstrom.org/Architecture/ds-basics.pdf">ds-basics.pdf, Table 40: Sensor Types
 *      from ds-basic.pdf from 19.08.2015</a>
 */
public enum SensorEnum {
    /*
     * | Sensor Type | Description | Unit | Min | 12 Bit Max | 12 Bit Resolution |
     * -----------------------------------------------------------------------------------------------------------------
     * -------------------------------------------------------------
     * | 4 | Active power | Watts (W) | 0 | 4095 | 1 |
     * | 5 | Output current | Ampere (mA) | 0 | 4095 | 1 |
     * | 6 | Electric meter | Kilowatt hours (kWh) | 0 | 40,95 | 0,01 |
     * | 9 | Temperature indoors | Kelvin (K) | 230 | 332,375 | 0,25 | (not correct)
     * | 10 | Temperature outdoors | Kelvin (K) | 230 | 332,375 | 0,25 | (not correct)
     * | 11 | Brightness indoors | Lux (Lx) | 1 | 131446,795 | logarithmic: lx = 10 * (x/800), x = 800 * log(lx) |
     * | 12 | Brightness outdoors | Lux (Lx) | 1 | 131446,795 | logarithmic: lx = 10 * (x/800), x = 800 * log(lx) |
     * | 13 | Relative humidity indoors | Percent (%) | 0 | 102,375 | 0,025 |
     * | 14 | Relative humidity outdoors | Percent (%) | 0 | 102,375 | 0,025 |
     * | 15 | Air pressure | Pascal (hPa) | 200 | 1223,75 | 0,25 |
     * | 18 | Wind speed | Meters per second (m/s) | 0 | 102,375 | 0,025 |
     * | 19 | Wind direction | degrees | 0 | 511,875 | 0,54 |
     * | 20 | Precipitation | Milliliter per square meter (ml/m2) | 0 | 102,375 | 0,025 |
     * | 21 | Carbon Dioxide | Parts per million (ppm) | 1 | 131446,795 | logarithmic: ppm = 10 * (x/800), x = 800 * log
     * (ppm) |
     * | 25 | Sound pressure level | Decibel (dB) | 0 | 255,938 | 0,25/4 |
     * | 50 | Room temperature set point | Kelvin (K) | 230 | 332,375 | 0,025 | (not correct)
     * | 51 | Room temperature control variable | Percent (%) | 0 | 102,375 | 0,025 |
     * | 64 | Output current (H) | Ampere (mA) | 0 | 16380 | 4 |
     * | 65 | Power consumption | Volt-Ampere (VA) | 0 | 4095 | 1 |
     *
     * Note: Point 20 precipitation, mm/m2 doesn't exist as unit, so it's liter or milliliter per square meter (ml/m2 or
     * l/m2)
     * otherwise it's only mm what is the same as l/m2)
     *
     */
    ACTIVE_POWER((short) 4, "watt", "W", 0, 4095, 1, "%d"),
    OUTPUT_CURRENT((short) 5, "ampere", "mA", 0, 4095, 1, "%d"),
    ELECTRIC_METER((short) 6, "kilowatt_hours", "kWh", 0, (float) 40.95, (float) 0.01, "%.3f"),
    TEMPERATURE_INDOORS((short) 9, "kelvin", "K", 230, (float) 332.375, (float) 0.25, "%.2f"),
    TEMPERATURE_OUTDOORS((short) 10, "kelvin", "K", 230, (float) 332.375, (float) 0.25, "%.2f"),
    BRIGHTNESS_INDOORS((short) 11, "lux", "Lx", 1, (float) 131446.795, 800, "%.3f"),
    BRIGHTNESS_OUTDOORS((short) 12, "lux", "Lx", 1, (float) 131446.795, 800, "%.3f"),
    RELATIVE_HUMIDITY_INDOORS((short) 13, "percent", "%", 0, (float) 102.375, (float) 0.025, "%.2f"),
    RELATIVE_HUMIDITY_OUTDOORS((short) 14, "percent", "%", 0, (float) 102.375, (float) 0.025, "%.2f"),
    AIR_PRESSURE((short) 15, "pascal", "hPa", 0, (float) 1223.75, (float) 0.25, "%.2f"),
    WIND_SPEED((short) 18, "meters_per_second", "m/s", 0, (float) 102.375, (float) 0.025, "%.2f"),
    WIND_DIRECTION((short) 19, "degrees", "°", 0, (float) 511.875, (float) 0.54, "%.2f"),
    PRECIPITATION((short) 20, "millimeter_per_square_meter", "mm/m2", 0, (float) 102.375, (float) 0.025, "%.3f"),
    CARBON_DIOXIDE((short) 21, "parts_per_million", "ppm", 1, (float) 131446.795, 800, "%.3f"),
    SOUND_PRESSURE_LEVEL((short) 25, "decibel", "dB", 0, (float) 255.938, (float) 0.0625, "%.2f"),
    ROOM_TEMPERATURE_SET_POINT((short) 50, "kelvin", "K", 230, (float) 332.375, (float) 0.025, "%.2f"),
    ROOM_TEMPERATURE_CONTROL_VARIABLE((short) 51, "kelvin", "K", 230, (float) 332.375, (float) 0.25, "%.2f"),
    OUTPUT_CURRENT_H((short) 64, "ampere", "mA", 0, 16380, 4, "%d"),
    POWER_CONSUMPTION((short) 65, "volt_ampere", "VA", 0, 4095, 1, "%d");

    private final Short sensorType;
    private final String unit;
    private final String unitShortcut;
    private final String pattern;
    private final Integer min;
    private final Float max;
    private final Float resolution;

    static final Map<Short, SensorEnum> SENSOR_ENUMS = new HashMap<>();

    SensorEnum(Short sensorType, String unit, String unitShortcut, int min, float max, float resolution,
            String plattern) {
        this.sensorType = sensorType;
        this.unit = unit;
        this.unitShortcut = unitShortcut;
        this.min = min;
        this.max = max;
        this.resolution = resolution;
        this.pattern = plattern;
    }

    static {
        for (SensorEnum sensor : SensorEnum.values()) {
            SENSOR_ENUMS.put(sensor.getSensorType(), sensor);
        }
    }

    /**
     * Returns true, if the given typeIndex contains in digitalSTROM, otherwise false.
     *
     * @param typeIndex to be checked
     * @return true, if contains otherwise false
     */
    public static boolean containsSensor(Short typeIndex) {
        return SENSOR_ENUMS.keySet().contains(typeIndex);
    }

    /**
     * Return true, if the given {@link SensorEnum} is a climate sensor, otherwise false.
     *
     * @param sensorType to check
     * @return true, if {@link SensorEnum} is climate sensor, otherwise false.
     */
    public static boolean isClimateSensor(SensorEnum sensorType) {
        if (sensorType != null) {
            switch (sensorType) {
                case TEMPERATURE_INDOORS:
                case TEMPERATURE_OUTDOORS:
                case BRIGHTNESS_INDOORS:
                case BRIGHTNESS_OUTDOORS:
                case RELATIVE_HUMIDITY_INDOORS:
                case RELATIVE_HUMIDITY_OUTDOORS:
                case AIR_PRESSURE:
                case WIND_SPEED:
                case WIND_DIRECTION:
                case PRECIPITATION:
                case CARBON_DIOXIDE:
                case SOUND_PRESSURE_LEVEL:
                case ROOM_TEMPERATURE_SET_POINT:
                case ROOM_TEMPERATURE_CONTROL_VARIABLE:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * Return true, if the given {@link SensorEnum} is a power sensor, otherwise false.<br>
     * <b>Power sensors are:</b><br>
     * - {@link #ACTIVE_POWER}<br>
     * - {@link #OUTPUT_CURRENT}<br>
     * - {@link #ELECTRIC_METER}<br>
     * - {@link #OUTPUT_CURRENT_H}<br>
     * - {@link #POWER_CONSUMPTION}
     *
     * @param sensorType to check
     * @return true, if {@link SensorEnum} is power sensor, otherwise false.
     */
    public static boolean isPowerSensor(SensorEnum sensorType) {
        if (sensorType != null) {
            switch (sensorType) {
                case ACTIVE_POWER:
                case OUTPUT_CURRENT:
                case ELECTRIC_METER:
                case OUTPUT_CURRENT_H:
                case POWER_CONSUMPTION:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * Returns the {@link SensorEnum} for the given typeIndex, otherwise null.
     *
     * @param typeIndex of the {@link SensorEnum}
     * @return SensorEnum or null
     */
    public static SensorEnum getSensor(Short typeIndex) {
        return SENSOR_ENUMS.get(typeIndex);
    }

    /**
     * Returns the typeIndex of this {@link SensorEnum} object.
     *
     * @return typeIndex
     */
    public Short getSensorType() {
        return this.sensorType;
    }

    /**
     * Returns the unit of this {@link SensorEnum} object.
     *
     * @return unit
     */
    public String getUnit() {
        return this.unit;
    }

    /**
     * Returns the unit shortcut of this {@link SensorEnum} object.
     *
     * @return unit shortcut
     */
    public String getUnitShortcut() {
        return this.unitShortcut;
    }

    /**
     * Returns the pattern of this {@link SensorEnum} object for display.
     *
     * @return pattern
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Returns the minimum sensor value.
     *
     * @return the min value
     */
    public Integer getMin() {
        return min;
    }

    /**
     * Returns the maximum sensor value.
     *
     * @return the max value
     */
    public Float getMax() {
        return max;
    }

    /**
     * Returns resolution for this sensor to get the float value of the dS-value (e.g. dSsensorValue * getResolution()).
     * <br>
     * <br>
     * <b>Note:</b><br>
     * If the resolution is 800, than you have to calculate "10 * (dSsensorValue/800)".
     *
     * @return the resolution
     */
    public Float getResolution() {
        return resolution;
    }
}
