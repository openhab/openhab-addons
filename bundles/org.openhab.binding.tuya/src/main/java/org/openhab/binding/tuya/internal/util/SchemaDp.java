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
package org.openhab.binding.tuya.internal.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SchemaDp} is a wrapper for the information of a single datapoint
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SchemaDp {
    private static final Map<String, String> REMOTE_LOCAL_TYPE_MAP = Map.of( //
            "Boolean", "bool", //
            "Enum", "enum", //
            "Integer", "value", //
            "String", "string", //
            "Json", "string");

    public int id = 0;
    public String code = "";
    public String type = "";
    public String label = "";
    public String unit = "";
    public @Nullable Double min;
    public @Nullable Double max;
    public Integer scale = 0;
    public @Nullable List<String> range;
    public @Nullable Unit<?> parsedUnit;

    public static SchemaDp fromRemoteSchema(Gson gson, DeviceSchema.Description function) {
        SchemaDp schemaDp = new SchemaDp();
        schemaDp.code = function.code.replace("_v2", "");
        schemaDp.id = function.dp_id;
        schemaDp.type = REMOTE_LOCAL_TYPE_MAP.getOrDefault(function.type, "raw"); // fallback to raw

        if ("enum".equalsIgnoreCase(schemaDp.type) && function.values.contains("range")) {
            schemaDp.range = Objects.requireNonNull(
                    gson.fromJson(function.values.replaceAll("\\\\", ""), DeviceSchema.EnumRange.class)).range;
        } else if ("value".equalsIgnoreCase(schemaDp.type) && function.values.contains("min")
                && function.values.contains("max")) {
            DeviceSchema.NumericRange numericRange = Objects.requireNonNull(
                    gson.fromJson(function.values.replaceAll("\\\\", ""), DeviceSchema.NumericRange.class));
            schemaDp.min = numericRange.min;
            schemaDp.max = numericRange.max;

            if (numericRange.scale == 1 && numericRange.min == 0 && numericRange.max == 1 && numericRange.step == 1) {
                LoggerFactory.getLogger(SchemaDp.class).warn("Ignoring scale=1 when min=0, max=1, step=1 for {}",
                        schemaDp.code);
            } else {
                schemaDp.scale = numericRange.scale;
            }

            schemaDp.unit = normalizeUnit(schemaDp.code, numericRange);
        }

        return schemaDp;
    }

    // This is very like the normalizeUnit in convert.js. We really only need to deal with _likely_ garbage
    // in _remote_ schemas here because the local schemas are handled by convert.js however assuming the
    // same mistakes aren't going to be repeated ad infinitum is probably unwise.
    private static String normalizeUnit(String code, DeviceSchema.NumericRange properties) {
        // Identifiers may be suffixed with an instance number if it's repeated.
        code = code.replaceFirst("_?\\d+$", "");

        String unit = properties.unit //
                .replaceAll("[     ­.·]+", "") // Remove all forms of space and dots.
                .replaceAll("μ", "µ") // Unicode has both the Greek letter and a micro sign - we prefer the latter.
                .replaceAll("[˚º]", "°") // Degree sign rather than ring above or masculine ordinal indicator.
                .replaceAll("㎡", "m²") // Avoid the single character m squared.
                .replaceAll("(?<=[^-\\d])2((?!\\d)|$)", "²") // Superscript 2s.
                .replaceAll("(?<=[^-\\d])3((?!\\d)|$)", "³") // Superscript 3s.
                .replaceAll("℃", "°C") // Prefer two characters for degree Celsius.
                .replaceAll("°c", "°C") // ... and capitalized.
                .replaceAll("℉", "°F") // Prefer two characters for degree Fahrenheit.
                .replaceAll("°f", "°F") // ... and capitalized.
        ;

        // If we are given a unit try and translate it into something comprehensible.
        switch (unit) {
            case "0":
            case "1":
            case "Number":
            case "step":
            case "times":
            case "x":
            case "X":
            case "PF": // Used as an abbreviation for power factor.
            case "pf":
            case "ppt": // Probably Parts Per Thousand but not available as a unit in openHAB.
            case "SG": // Specific Gravity but not available as a unit in openHAB.
            case "sg":
            case "份": // share/part/copy/portion
            case "次": // number
            case "段": // part/segment
            case "无": // none
            case "步": // step/pace/footsteps
            case "元": // Yuan
                return "";

            case "%RH":
            case "RH%":
            case "％":
            case "%LEL":
            case "LEL":
            case "百分比（%）":
                return "%";

            case "°C/°F":
            case "°C°F":
            case "°C/F":
            case "摄氏度或华氏度":
                // FIXME: some devices seem to be switchable between °C and °F. We may
                // need code in the handler to deal with these.
                // return "°C°F"
                return "";

            case "01s":
                switch (properties.scale) {
                    case 0:
                        properties.scale = 1;
                    case 1:
                        return "s";
                }
                break;

            case "Amp":
                return "A";

            case "mAh":
                switch (code) {
                    case "cur_voltage": // At least one product has a cur_voltage with a unit of "mAh"!
                        return "V";
                }
                return unit;

            case "C": // FIXME: could be Coulombs...?
            case "c":
            case "摄氏度":
                return "°C";

            case "CM":
            case "厘米":
                return "cm";

            case "day":
            case "天":
                return "d";

            case "F": // FIXME: could be Farads...?
            case "华氏度":
                return "°F";

            case "mg/L":
                return "mg/l";

            case "µg/m³":
            case "ug/m³":
            case "um/m³": // Seen on some particulate matter standard functions.
                return "µg/m³";

            case "HZ":
                return "Hz";

            case "hour":
            case "H":
            case "Hour":
            case "小时":
            case "小时（暂定）":
                return "h";

            case "hPa/mb":
                return "hPa";

            case "inch":
                return "in";

            case "Kcal":
                return "kcal";

            case "KG":
            case "Kg":
                return "kg";

            case "Klux":
                return "klx";

            case "KM":
                return "km";

            case "KM/h":
                return "km/h";

            case "Kpa":
                return "kPa";

            case "kVar":
            case "Kvar":
            case "KVar":
                return "kvar";

            case "kVarh":
            case "Kvarh":
            case "KVarh":
                return "kvarh";

            case "kw":
            case "Kw":
            case "KW":
                return "kW";

            case "kwh":
            case "Kwh":
            case "KwH":
            case "KWh":
            case "KWH":
            case "kW*H":
            case "kW*h":
            case "KW*H":
            case "k-Wh":
                return "kWh";

            case "L":
            case "升":
            case "升（L）":
                return "l";

            case "L/min":
            case "升/分钟":
                return "l/min";

            case "lux":
            case "Lux":
            case "LUX":
                return "lx";

            case "M":
            case "米":
                return "m";

            case "mL":
                return "ml";

            case "M²":
                return "m²";

            case "立方米":
                return "m³";

            case "Mhz":
                return "MHz";

            case "Min":
            case "MIN":
            case "mins":
            case "minute":
            case "Minute":
            case "minutes":
            case "mintues":
            case "分钟":
            case "分钟m":
                return "min";

            case "Mpa":
                return "MPa";

            case "MPH":
                return "mph";

            case "ph":
                return "pH";

            case "PPM":
                return "ppm";

            case "PSI":
                return "psi";

            case "RPM":
            case "r/s":
                return "rpm";

            case "s":
            case "S": // Siemens or seconds?
                if (code.startsWith("ec")) { // ec = Electrical Conductance
                    return "S";
                }
                return "s";

            case "sec":
            case "Sec":
            case "second":
            case "Seconds":
            case "seconds":
            case "SECOND":
            case "秒":
                return "s";

            case "µs":
            case "µS":
            case "uS":
            case "us": // Siemens or seconds?
                if (code.startsWith("ec")) { // ec = Electrical Conductance
                    return "µS";
                }
                return "µs";

            case "mS/cm":
            case "ms/cm": // Siemens or seconds? ms/cm seems unlikely...
                return "mS/cm";

            case "uS/cm":
            case "us/cm": // Siemens or seconds? µs/cm seems unlikely...
                return "µS/cm";

            case "v":
            case "VAC":
            case "VDC":
                return "V";

            case "w":
            case "Watt":
                return "W";

            case "wh":
                return "Wh";

            case "欧姆":
                return "Ω";

            case "度": // degree/time/counter for ...
                switch (code) {
                    case "KCWD":
                    case "JLWD":
                    case "SSWD": // window open/comfort/econ temperatures with min=5.0 max=30.0
                        return "°C";
                }
                break;

            case "分": // point/minute/fraction/part/tenth
                switch (code) {
                    case "alarm_sound_duration":
                    case "Clean_time":
                    case "clean_time":
                        return "min";
                }
                break;

            case "档": // files/pigeonhole
                switch (code) {
                    case "fan_speed":
                        return "rpm";

                    case "sensitivity":
                    case "move_sensitivity":
                    case "presence_sensitivity":
                        return "";
                }
                break;
        }

        // Not all standard functions have a default unit set and not all developers set one.
        // Not all custom functions are... well... sane.
        switch (code) {
            case "add_ele":
                if (properties.scale == 3) {
                    return "kWh";
                } else if (properties.scale == 0) {
                    return "Wh";
                }
                break;

            case "countdown":
                if (properties.min == 0 && properties.max == 86400) {
                    return "s";
                }
                break;

            case "time_zone":
                // A couple of products say unit="z" but have a range of 0-23. Mind you, no negative
                // offset and scale=0, step=1 so it has restricted utility anyway.
                if (properties.min == 0 && properties.max == 23) {
                    return "h";
                } else {
                    switch (unit) {
                        case "z":
                            return "";
                    }
                }
                break;

            case "wind_dir360":
                if (properties.min == 0 && (properties.max == 360 || properties.max == 361)) {
                    return "°";
                }
                break;
        }

        // Use what it said. The unit will get checked for usability when the Channels are added to Things.
        return unit;
    }
}
