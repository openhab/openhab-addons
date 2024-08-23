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
package org.openhab.binding.bluetooth.airthings.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AirthingsDataParser} is responsible for parsing data from Wave Plus device format.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Kai Kreuzer - Added Airthings Wave Mini support
 * @author Arne Seime - Added Airthings Radon / Wave 2 support
 */
@NonNullByDefault
public class AirthingsDataParser {
    public static final String TVOC = "tvoc";
    public static final String CO2 = "co2";
    public static final String PRESSURE = "pressure";
    public static final String TEMPERATURE = "temperature";
    public static final String RADON_LONG_TERM_AVG = "radonLongTermAvg";
    public static final String RADON_SHORT_TERM_AVG = "radonShortTermAvg";
    public static final String HUMIDITY = "humidity";

    private static final int EXPECTED_DATA_LEN = 20;
    private static final int EXPECTED_VER_PLUS = 1;

    private AirthingsDataParser() {
    }

    public static Map<String, Number> parseWavePlusData(int[] data) throws AirthingsParserException {
        if (data.length == EXPECTED_DATA_LEN) {
            final Map<String, Number> result = new HashMap<>();

            final int version = data[0];

            if (version == EXPECTED_VER_PLUS) {
                result.put(HUMIDITY, data[1] / 2D);
                result.put(RADON_SHORT_TERM_AVG, intFromBytes(data[4], data[5]));
                result.put(RADON_LONG_TERM_AVG, intFromBytes(data[6], data[7]));
                result.put(TEMPERATURE, intFromBytes(data[8], data[9]) / 100D);
                result.put(PRESSURE, intFromBytes(data[10], data[11]) / 50D);
                result.put(CO2, intFromBytes(data[12], data[13]));
                result.put(TVOC, intFromBytes(data[14], data[15]));
                return result;
            } else {
                throw new AirthingsParserException(String.format("Unsupported data structure version '%d'", version));
            }
        } else {
            throw new AirthingsParserException(String.format("Illegal data structure length '%d'", data.length));
        }
    }

    public static Map<String, Number> parseWaveMiniData(int[] data) throws AirthingsParserException {
        if (data.length == EXPECTED_DATA_LEN) {
            final Map<String, Number> result = new HashMap<>();
            result.put(TEMPERATURE,
                    new BigDecimal(intFromBytes(data[2], data[3]))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                            .subtract(BigDecimal.valueOf(273.15)).doubleValue());
            result.put(HUMIDITY, intFromBytes(data[6], data[7]) / 100D);
            result.put(TVOC, intFromBytes(data[8], data[9]));
            return result;
        } else {
            throw new AirthingsParserException(String.format("Illegal data structure length '%d'", data.length));
        }
    }

    public static Map<String, Number> parseWaveRadonData(int[] data) throws AirthingsParserException {
        if (data.length == EXPECTED_DATA_LEN) {
            final Map<String, Number> result = new HashMap<>();
            result.put(HUMIDITY, data[1] / 2D);
            result.put(RADON_SHORT_TERM_AVG, intFromBytes(data[4], data[5]));
            result.put(RADON_LONG_TERM_AVG, intFromBytes(data[6], data[7]));
            result.put(TEMPERATURE, intFromBytes(data[8], data[9]) / 100D);
            return result;
        } else {
            throw new AirthingsParserException(String.format("Illegal data structure length '%d'", data.length));
        }
    }

    private static int intFromBytes(int lowByte, int highByte) {
        return (highByte & 0xFF) << 8 | (lowByte & 0xFF);
    }
}
