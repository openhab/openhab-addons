/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.lacrosse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter for converting a line read from a LaCrosseITPlusReader sketch to a LaCrosseTemperatureReading.
 *
 * @author Volker Bier - Initial contribution
 */
public class Tx22ReadingConverter implements JeeLinkReadingConverter<Tx22Reading> {
    private static final Pattern LINE_P = Pattern.compile(
            "OK\\s+WS\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)");

    private final Logger logger = LoggerFactory.getLogger(Tx22ReadingConverter.class);

    @Override
    public Tx22Reading createReading(String inputLine) {
        // parse lines only if we have registered listeners
        if (inputLine != null) {
            Matcher matcher = LINE_P.matcher(inputLine);
            if (matcher.matches()) {
                /**
                 * Format
                 * 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
                 * -------------------------------------------------------------------
                 * OK WS 14 1 4 208 53 0 0 7 8 0 29 0 31 1 4 1 I D=0E 23.2°C 52%rH 0mm Dir.: 180.0° Wind:2.9m/s
                 * Gust:3.1m/s new Batt. 1025 hPa
                 * OK WS ID XXX TTT TTT HHH RRR RRR DDD DDD SSS SSS GGG GGG FFF PPP PPP
                 * | | | | | | | | | | | | | | | | | |-- Pressure LSB
                 * | | | | | | | | | | | | | | | | |------ Pressure MSB
                 * | | | | | | | | | | | | | | | |---------- Flags *
                 * | | | | | | | | | | | | | | |-------------- WindGust * 10 LSB (0.0 ... 50.0 m/s) FF/FF = none
                 * | | | | | | | | | | | | | |------------------ WindGust * 10 MSB
                 * | | | | | | | | | | | | |---------------------- WindSpeed * 10 LSB(0.0 ... 50.0 m/s) FF/FF = none
                 * | | | | | | | | | | | |-------------------------- WindSpeed * 10 MSB
                 * | | | | | | | | | | |------------------------------ WindDirection * 10 LSB (0.0 ... 365.0 Degrees)
                 * FF/FF = none
                 * | | | | | | | | | |---------------------------------- WindDirection * 10 MSB
                 * | | | | | | | | |-------------------------------------- Rain * 0.5mm LSB (0 ... 9999 mm) FF/FF = none
                 * | | | | | | | |------------------------------------------ Rain * 0.5mm MSB
                 * | | | | | | |---------------------------------------------- Humidity (1 ... 99 %rH) FF = none
                 * | | | | | |-------------------------------------------------- Temp * 10 + 1000 LSB (-40 ... +60 °C)
                 * FF/FF = none
                 * | | | | |------------------------------------------------------ Temp * 10 + 1000 MSB
                 * | | | |---------------------------------------------------------- Sensor type (1=TX22, 2=NodeSensor)
                 * | | |------------------------------------------------------------- Sensor ID (0 ... 63)
                 * | |---------------------------------------------------------------- fix "WS"
                 * |------------------------------------------------------------------- fix "OK"
                 *
                 * Flags: 128 64 32 16 8 4 2 1
                 * | | |
                 * | | |-- New battery
                 * | |------ ERROR
                 * |---------- Low battery
                 */

                logger.trace("Creating reading from: {}", inputLine);

                int sensorId = Integer.parseInt(matcher.group(1));
                int type = Integer.parseInt(matcher.group(2));

                float temperature = "255".equals(matcher.group(3)) ? Float.MAX_VALUE
                        : (float) (Integer.parseInt(matcher.group(3)) * 256 + Integer.parseInt(matcher.group(4)) - 1000)
                                / 10;

                int humidity = "255".equals(matcher.group(5)) ? Integer.MAX_VALUE : Integer.parseInt(matcher.group(5));

                int rain = "255".equals(matcher.group(6)) ? Integer.MAX_VALUE
                        : (Integer.parseInt(matcher.group(6)) * 256 + Integer.parseInt(matcher.group(7))) * 2;

                float windDirection = "255".equals(matcher.group(8)) ? Float.MAX_VALUE
                        : (float) (Integer.parseInt(matcher.group(8)) * 256 + Integer.parseInt(matcher.group(9))) / 10;
                float windSpeed = "255".equals(matcher.group(10)) ? Float.MAX_VALUE
                        : (float) (Integer.parseInt(matcher.group(10)) * 256 + Integer.parseInt(matcher.group(11)))
                                / 10;
                float windGust = "255".equals(matcher.group(12)) ? Float.MAX_VALUE
                        : (float) (Integer.parseInt(matcher.group(12)) * 256 + Integer.parseInt(matcher.group(13)))
                                / 10;

                byte flags = Byte.parseByte(matcher.group(14));
                float pressure = "255".equals(matcher.group(15)) ? Float.MAX_VALUE
                        : Integer.parseInt(matcher.group(15)) * 256 + Integer.parseInt(matcher.group(16));

                boolean batteryNew = (flags & (byte) 1) > 0;
                boolean batteryLow = (flags & (byte) 4) > 0;

                return new Tx22Reading(sensorId, type, 0, temperature, humidity, batteryNew, batteryLow, rain,
                        windDirection, windSpeed, windGust, pressure);
            }
        }

        return null;
    }
}
