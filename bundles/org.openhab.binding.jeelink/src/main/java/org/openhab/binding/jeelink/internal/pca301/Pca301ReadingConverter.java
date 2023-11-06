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
package org.openhab.binding.jeelink.internal.pca301;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter for converting a line read from a pcaSerial sketch to a Pca301Reading.
 *
 * @author Volker Bier - Initial contribution
 */
public class Pca301ReadingConverter implements JeeLinkReadingConverter<Pca301Reading> {
    private static final Pattern READING_P = Pattern.compile(
            "OK\\s+24\\s+([0-9]+)\\s+4\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)");

    private final Logger logger = LoggerFactory.getLogger(Pca301ReadingConverter.class);

    @Override
    public Pca301Reading createReading(String inputLine) {
        // parse lines only if we have registered listeners
        if (inputLine != null) {
            Matcher matcher = READING_P.matcher(inputLine);
            if (matcher.matches()) {
                // Format
                //
                // OK 24 1 4 1 160 236 0 0 0 0 0
                // Interpretation:
                // OK 24: fixed
                // 1 Byte: channel
                // 1 Byte: command (04=retrieve measure data, 05=switch device, 06=identify device by toggling device
                // LED
                // 3 Byte: device address (UID)
                // 1 Byte: data -> 1 with command=4 resets device statistics
                // -> 0/1 with command=5 switches device off/on
                // 2 Byte: current consumption in watt (scale 1/10)
                // 2 Byte: total consumption in kWh (scale 1/100)
                logger.trace("Creating reading from: {}", inputLine);

                int channelId = Integer.parseInt(matcher.group(1));
                String sensorId = matcher.group(2) + "-" + matcher.group(3) + "-" + matcher.group(4);
                int data = Integer.parseInt(matcher.group(5));

                long con1 = Long.parseLong(matcher.group(6));
                long con2 = Long.parseLong(matcher.group(7));
                long consumptionCurrent = ((con1 << 8) + con2);

                con1 = Long.parseLong(matcher.group(8));
                con2 = Long.parseLong(matcher.group(9));
                long consumptionTotal = ((con1 << 8) + con2);

                return new Pca301Reading(sensorId, channelId, data == 1, consumptionCurrent / 10f,
                        consumptionTotal * 10);
            }
        }

        return null;
    }
}
