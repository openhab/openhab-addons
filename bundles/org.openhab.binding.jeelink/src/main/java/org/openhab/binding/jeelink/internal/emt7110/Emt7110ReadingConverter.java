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
package org.openhab.binding.jeelink.internal.emt7110;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a EMT7110 energy Sensor thing.
 *
 * @author Timo Schober - Initial contribution
 */
public class Emt7110ReadingConverter implements JeeLinkReadingConverter<Emt7110Reading> {
    private static final Pattern READING_P = Pattern.compile(
            "OK EMT7110\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)");

    private final Logger logger = LoggerFactory.getLogger(Emt7110ReadingConverter.class);

    @Override
    public Emt7110Reading createReading(String inputLine) {
        // parse lines only if we have registered listeners
        if (inputLine != null) {
            Matcher matcher = READING_P.matcher(inputLine);
            if (matcher.matches()) {
                // Format
                // OK EMT7110 84 81 8 237 0 13 0 2 1 6 1 -> ID 5451 228,5V 13mA 2W 2,62kWh
                // OK EMT7110 84 162 8 207 0 76 0 7 0 0 1
                // OK EMT7110 ID ID VV VV AA AA WW WW KW KW Flags
                // | | | | | | | | | | |
                // | | | | | | | | | | `--- AccumulatedPower * 100 LSB
                // | | | | | | | | | `------ AccumulatedPower * 100 MSB
                // | | | | | | | | `--- Power (W) LSB
                // | | | | | | | `------ Power (W) MSB
                // | | | | | | `--- Current (mA) LSB
                // | | | | | `------ Current (mA) MSB
                // | | | | `--- Voltage (V) * 10 LSB
                // | | | `----- Voltage (V) * 10 MSB
                // | | `--- ID
                // | `------- ID
                // `--- fix "EMT7110"
                // logger.trace("Creating reading from: {}", inputLine);

                String id = matcher.group(1) + matcher.group(2);
                float voltage = (Integer.parseInt(matcher.group(3)) * 256 + Integer.parseInt(matcher.group(4))) / 10f;
                float current = (Integer.parseInt(matcher.group(5)) * 256 + Integer.parseInt(matcher.group(6)));
                float power = (Integer.parseInt(matcher.group(7)) * 256 + Integer.parseInt(matcher.group(8)));
                float aPower = (Integer.parseInt(matcher.group(9)) * 256 + Integer.parseInt(matcher.group(10))) / 100f;

                return new Emt7110Reading(id, voltage, current, power, aPower, true);
            }
        }

        return null;
    }
    /*
     * public static void main(String[] args){
     * Emt7110ReadingConverter emt = new Emt7110ReadingConverter();
     * emt.createReading("OK EMT7110 84 81 9 91 0 72 0 2 1 6 1");
     * }
     */
}
