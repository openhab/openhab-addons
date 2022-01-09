/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.revolt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;

/**
 * Converter for converting a line read from a SlowRF CUL to a RevoltReading.
 *
 * @author Volker Bier - Initial contribution
 */
public class RevoltReadingConverter implements JeeLinkReadingConverter<RevoltReading> {
    private static final Pattern LINE_P = Pattern
            .compile("r([0-9A-Za-z]{4})([0-9A-Za-z]{2})([0-9A-Za-z]{4})([0-9A-Za-z]{2})([0-9A-Za-z]{4})"
                    + "([0-9A-Za-z]{2})([0-9A-Za-z]{4})[0-9A-Za-z][0-9A-Za-z]");

    @Override
    public RevoltReading createReading(String inputLine) {
        if (inputLine != null) {
            Matcher matcher = LINE_P.matcher(inputLine);
            if (matcher.matches()) {
                /*
                 * r4F1BE400513206875B312F25
                 */
                String id = matcher.group(1); // 4F1B

                int voltage = toInt(matcher.group(2)); // 0xE4 = 228 => 228 V
                float current = toInt(matcher.group(3)) / 100f; // 0x0051 = 81 => 0,81 A
                int frequency = toInt(matcher.group(4)); // 0x32 = 50 => 50 Hz
                float power = toInt(matcher.group(5)) / 10f; // 0x0687 = 1671 => 167,1 W
                float powerFact = toInt(matcher.group(6)) / 100f; // 0x5B = 91 => 0,91 VA
                float consumption = toInt(matcher.group(7)) / 100f; // 0x312F = 12591 => 125,91 Wh

                return new RevoltReading(id, voltage, current, frequency, power, powerFact, consumption);
            }
        }

        return null;
    }

    private int toInt(String hex) {
        Integer i = Integer.parseInt(hex, 16);
        return i.intValue();
    }
}
