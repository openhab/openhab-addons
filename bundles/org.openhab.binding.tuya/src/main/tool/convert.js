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

/**
 * Converts the device-specific data from ioBroker.tuya to a binding compatible JSON
 *
 * @author Jan N. Klug - Initial contribution
 */
const http = require('https');
const fs = require('fs');

function normalizeUnit(productKey, orig_code, properties, orig_unit) {
    // Identifiers may be suffixed with an instance number if it's repeated.
    let code = orig_code.replaceAll(/_?\d+$/g, "");

    let unit = orig_unit
            .replaceAll(/[     ­.·]+/g, "") // Remove all forms of space and dots.
            .replaceAll(/μ/g, "µ") // Unicode has both the Greek letter and a micro sign - we prefer the latter.
            .replaceAll(/[˚º]/g, "°") // Degree sign rather than ring above or masculine ordinal indicator.
            .replaceAll(/㎡/g, "m²") // Avoid the single character m squared.
            .replaceAll(/(?<=[^-\d])2((?!\d)|$)/g, "²") // Superscript 2s
            .replaceAll(/(?<=[^-\d])3((?!\d)|$)/g, "³") // Superscript 3s
            .replaceAll(/℃/g, "°C") // Prefer two characters for degree Celsius.
            .replaceAll(/°c/g, "°C") // ... and capitalized.
            .replaceAll(/℉/g, "°F") // Prefer two characters for degree Fahrenheit.
            .replaceAll(/°f/g, "°F") // ... and capitalized.
            ;

    // If we are given a unit try and translate it into something comprehensible.
    switch (unit) {
        // Standard units accepted as-is. Anything not explicitly matched here, or fixed
        // up elsewhere, will cause an "Unsure ..." message to be logged and the unit will
        // be omitted from the curated schema.
        // Note that some standard units used are not listed here because they have special
        // handling further down.
        case "°": case "%": case "°C": case "°F": case "Ω":
        case "A": case "mA":
        case "Ah":
        case "bar": case "mbar":
        case "cal": case "kcal":
        case "d": case "h": case "min": case "ms": // "s" has special handling below
        case "dBm":
        case "g": case "kg": case "mg": case "mg/m³":
        case "gal": case "gal/h": case "gal/min":
        case "Hz": case "kHz": case "MHz":
        case "in":
        case "l": case "ml": case "l/min": case "ml/day":
        case "lx":
        case "m": case "m²": case "m³": case "m/s": case "cm": case "km": case "km/h": case "mm":
        case "mph":
        case "Nm":
        case "Pa": case "hPa": case "kPa":
        case "pH":
        case "ppm":
        case "psi":
        case "rpm":
        case "µS/cm":
        case "V": case "mV":
        case "var": case "kvar": case "kvarh":
        case "W": case "Wh": case "kW": case "kWh":
            return unit;

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
    // And not all units set are... well... sane.
    switch (code) {
        case "a_batterypencent":
            return "%";

        case "a_BatteryVoltage":
            return "V";

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

    if (unit !== "") {
        console.log("Unsure about unit " + orig_unit + " (mapped \"" + unit +"\") for " + orig_code + " in " + productKey
                + " min=" + properties.min
                + " max=" + properties.max
                + " scale=" + properties.scale
                + " step=" + properties.step
        );
    } else if (false) {
        console.log("No unit for " + orig_code + " in " + productKey
                + " min=" + properties.min
                + " max=" + properties.max
                + " scale=" + properties.scale
                + " step=" + properties.step
        );
    }
    return "";
}

const prevSchemas = require('../../../src/main/resources/schema.json');

const schemaJson = fs.createWriteStream("../../../target/in-schema.json");
http.get("https://raw.githubusercontent.com/Apollon77/ioBroker.tuya/master/lib/schema.json", function(response) {
    response.setEncoding('utf8');
    response.pipe(schemaJson);
    schemaJson.on('finish', () => {
        schemaJson.close();

        const knownSchemas = require('../../../target/in-schema.json');

        let productKey, value;
        let convertedSchemas = {};

        for (productKey in knownSchemas) {
            if (process.argv[2] == 'existing' && typeof prevSchemas[productKey] == 'undefined') {
                continue;
            }
            try {
                let schema = JSON.parse(knownSchemas[productKey].schema);
                let convertedSchema = {};
                for (value in schema) {
                    let entry = schema[value];
                    let convertedEntry;
                    if (entry.type === 'raw') {
                        convertedEntry = {id: entry.id, type: entry.type};
                    } else {
                        convertedEntry = {id: entry.id, type: entry.property.type};
                        if (convertedEntry.type === 'enum') {
                            convertedEntry['range'] = entry.property.range;
                        } else if (convertedEntry.type === 'value') {
                            if (entry.property.min !== null && entry.property.max !== null) {
                                convertedEntry['min'] = entry.property.min;
                                convertedEntry['max'] = entry.property.max;
                            }
                            if (typeof entry.property.scale !== 'undefined') {
                                if (entry.property.scale == 1 && entry.property.min == 0 && entry.property.max == 1 && (typeof entry.property.step == 'undefined' || entry.property.step >= 1)) {
                                    console.log('Ignoring scale=1 when min=0, max=1, step=' + (typeof entry.property.step == 'undefined' ? 1 : entry.property.step) + ' for ' + productKey + " " + entry.code);
                                } else if (entry.property.scale != 0) {
                                    convertedEntry['scale'] = entry.property.scale;
                                }
                            }
                            if (typeof entry.property.unit === 'undefined') {
                                entry.property.unit = "";
                            }
                            let unit = normalizeUnit(productKey, entry.code, entry.property, entry.property.unit);
                            if (unit !== "") {
                                convertedEntry['unit'] = unit;
                            }
                        }
                    }
                    convertedSchema[entry.code] = convertedEntry;
                }
                if (Object.keys(convertedSchema).length > 0) {
                    convertedSchemas[productKey] = convertedSchema;
                }
            } catch (err) {
                console.log('Parse Error in Schema for ' + productKey + ': ' + err);
            }
        }

        const replacer = (key, value) =>
            value instanceof Object && !(value instanceof Array) ?
                Object.keys(value)
                    .sort()
                    .reduce((sorted, key) => {
                        sorted[key] = value[key];
                        return sorted;
                    }, {}) : value;

        fs.writeFile('../resources/schema.json', JSON.stringify(convertedSchemas, replacer, '\t'), (err) => {
            if (err) throw err;
        });
    });
});
