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
package org.openhab.binding.jeelink.internal.lacrosse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter for converting a line read from a LGW to a LgwReading.
 *
 * @author Volker Bier - Initial contribution
 */
public class LgwReadingConverter implements JeeLinkReadingConverter<LgwReading> {
    private static final Pattern LINE_P = Pattern.compile(
            "OK\\s+WS\\s+([0-9]+)\\s+4\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)(?:\\s+255){8}?\\s+0\\s+([0-9]+)\\s+([0-9]+)(?:\\s+255){9}?");
    private final Logger logger = LoggerFactory.getLogger(LgwReadingConverter.class);

    @Override
    public LgwReading createReading(String inputLine) {
        // parse lines only if we have registered listeners
        if (inputLine != null) {
            Matcher matcher = LINE_P.matcher(inputLine);
            if (matcher.matches()) {
                /*
                 * Format
                 * OK WS 71 4 4 203 53 255 255 255 255 255 255 255 255 0 3 219 255 255 255 255 255 255 255 255 255
                 * OK WS 75 4 4 195 61 255 255 255 255 255 255 255 255 0 255 255 255 255 255 255 255 255 255 255 255
                 * OK WS 213 4 5 126 40 255 255 255 255 255 255 255 255 0 40 53 0 48 57
                 * OK WS ID XXX TTT TTT HHH RRR RRR DDD DDD SSS SSS GGG GGG FFF PPP PPP GAS GAS GAS DEB DEB DEB LUX LUX
                 * LUX
                 * | | | | | | | | | | | | | | | | | |-------------------------------------- Pressure LSB
                 * | | | | | | | | | | | | | | | | |------------------------------------------ Pressure MSB
                 * | | | | | | | | | | | | | | | |-- Fix 0
                 * | | | | | | |-------------------------------------- Humidity (1 ... 99 %rH) FF = none
                 * | | | | | |------------------------------------------ Temp * 10 + 1000 LSB (-40 ... +60 °C) FF/FF =
                 * none
                 * | | | | |---------------------------------------------- Temp * 10 + 1000 MSB
                 * | | | |-------------------------------------------------- fix "4"
                 * | | |------------------------------------------------------ Sensor ID (1 ... 63)
                 * | |--------------------------------------------------------- fix "WS"
                 * |------------------------------------------------------------ fix "OK"
                 */
                logger.trace("Creating reading from: {}", inputLine);

                int sensorId = Integer.parseInt(matcher.group(1));

                Float temperature = "255".equals(matcher.group(2)) ? null
                        : (float) (Integer.parseInt(matcher.group(2)) * 256 + Integer.parseInt(matcher.group(3)) - 1000)
                                / 10;

                Integer humidity = "255".equals(matcher.group(4)) ? null : Integer.parseInt(matcher.group(4));

                Integer pressure = null;
                if (!"255".equals(matcher.group(5))) {
                    pressure = Integer.parseInt(matcher.group(5)) * 256 + Integer.parseInt(matcher.group(6));
                }

                return new LgwReading(sensorId, temperature, humidity, pressure);
            }
        }

        return null;
    }
}
