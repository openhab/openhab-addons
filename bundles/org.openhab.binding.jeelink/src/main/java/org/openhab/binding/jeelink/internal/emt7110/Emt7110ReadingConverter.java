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

/**
 * Handler for a EMT7110 energy Sensor thing.
 *
 * @author Timo Schober - Initial contribution
 */
public class Emt7110ReadingConverter implements JeeLinkReadingConverter<Emt7110Reading> {
    private static final Pattern READING_P = Pattern.compile(
            "OK EMT7110\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)");

    @Override
    public Emt7110Reading createReading(String inputLine) {
        // parse lines only if we have registered listeners
        if (inputLine != null) {
            Matcher matcher = READING_P.matcher(inputLine);
            if (matcher.matches()) {
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
}
