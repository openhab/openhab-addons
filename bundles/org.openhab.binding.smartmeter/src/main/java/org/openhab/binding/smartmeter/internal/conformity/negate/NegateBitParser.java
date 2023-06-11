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
package org.openhab.binding.smartmeter.internal.conformity.negate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;

/**
 * Parses the NegateBit property.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class NegateBitParser {

    /**
     * Parsing of negate bit property. This is in the format: {@literal <OBIS>:<POSITION>:<BIT_SET>"}
     * e.g. "1-0:1-8-0:5:1"
     *
     * @param negateProperty
     * @return The parsed model
     */
    public static NegateBitModel parseNegateProperty(String negateProperty) throws IllegalArgumentException {
        Pattern obisPattern = Pattern.compile(SmartMeterBindingConstants.OBIS_PATTERN_CHANNELID);
        try {
            Matcher matcher = obisPattern.matcher(negateProperty);
            if (matcher.find()) {
                String obis = matcher.group();
                String substring = negateProperty.substring(matcher.end() + 1, negateProperty.length());
                String[] split = substring.split(":");
                int negatePosition = Integer.parseInt(split[0]);
                boolean negateBit = Integer.parseInt(split[1]) == 0 ? false : true;
                boolean status = split.length > 2 ? split[2].equalsIgnoreCase("status") : false;
                return new NegateBitModel((byte) negatePosition, negateBit, obis, status);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Negate property cannot be parsed: " + negateProperty, e);
        }
        throw new IllegalArgumentException("Negate property cannot be parsed: " + negateProperty);
    }
}
