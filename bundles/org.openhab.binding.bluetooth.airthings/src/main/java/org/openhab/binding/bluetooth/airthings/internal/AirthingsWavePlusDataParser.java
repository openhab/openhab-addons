/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AirthingsWavePlusDataParser} is responsible for parsing data from Wave Plus device format.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class AirthingsWavePlusDataParser {
    private static final int EXPECTED_DATA_LEN = 20;
    private static final int EXPECTED_VER = 1;

    private double humidity;
    private int radonShortTermAvg;
    private int radonLongTermAvg;
    private double temperature;
    private double pressure;
    private int co2;
    private int tvoc;

    public AirthingsWavePlusDataParser(int[] data) throws AirthingsParserException {
        parseData(data);
    }

    public double getHumidity() {
        return humidity;
    }

    public int getRadonShortTermAvg() {
        return radonShortTermAvg;
    }

    public int getRadonLongTermAvg() {
        return radonLongTermAvg;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public int getCo2() {
        return co2;
    }

    public int getTvoc() {
        return tvoc;
    }

    private void parseData(int[] data) throws AirthingsParserException {
        if (data.length == EXPECTED_DATA_LEN) {
            final int version = data[0];

            if (version == EXPECTED_VER) {
                humidity = data[1] / 2D;
                radonShortTermAvg = intFromBytes(data[4], data[5]);
                radonLongTermAvg = intFromBytes(data[6], data[7]);
                temperature = intFromBytes(data[8], data[9]) / 100D;
                pressure = intFromBytes(data[10], data[11]) / 50D;
                co2 = intFromBytes(data[12], data[13]);
                tvoc = intFromBytes(data[14], data[15]);
            } else {
                throw new AirthingsParserException(String.format("Unsupported data structure version '%d'", version));
            }
        } else {
            throw new AirthingsParserException(String.format("Illegal data structure length '%d'", data.length));
        }
    }

    private int intFromBytes(int lowByte, int highByte) {
        return (highByte & 0xFF) << 8 | (lowByte & 0xFF);
    }

    @Override
    public String toString() {
        return String.format(
                "[humidity=%.1f %%rH, radonShortTermAvg=%d Bq/m3, radonLongTermAvg=%d Bq/m3, temperature=%.1f °C, air pressure=%.2f mbar, co2=%d ppm, tvoc=%d ppb]",
                humidity, radonShortTermAvg, radonLongTermAvg, temperature, pressure, co2, tvoc);
    }
}
