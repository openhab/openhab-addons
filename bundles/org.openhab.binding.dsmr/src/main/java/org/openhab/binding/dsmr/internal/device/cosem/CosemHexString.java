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
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;

/**
 * {@link CosemHexString} represents a string value stored as Hexadecimal values.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Class now a factory instead of data containing class
 */
@NonNullByDefault
class CosemHexString extends CosemValueDescriptor<StringType> {

    public static final CosemHexString INSTANCE = new CosemHexString();

    private static final String NO_VALUE = "00";

    /**
     * Parses a String representing the hex value to a {@link StringType}.
     *
     * @param cosemValue the value to parse
     * @return {@link StringType} representing the value the cosem hex value
     * @throws ParseException if parsing failed
     */
    @Override
    protected StringType getStateValue(String cosemValue) throws ParseException {
        final String cosemHexValue = cosemValue.replaceAll("\\r\\n", "").trim();

        if (cosemHexValue.length() % 2 != 0) {
            throw new ParseException(cosemHexValue + " is not a valid hexadecimal string", 0);
        } else {
            final StringBuilder sb = new StringBuilder();

            for (int i = 0; i < cosemHexValue.length(); i += 2) {
                final String hexValue = cosemHexValue.substring(i, i + 2);

                if (!NO_VALUE.equals(hexValue)) {
                    try {
                        sb.append((char) Integer.parseInt(hexValue, 16));
                    } catch (NumberFormatException e) {
                        throw new ParseException("Failed to parse hex value from '" + cosemValue + "' as char", i);
                    }
                }
            }
            return new StringType(sb.toString());
        }
    }
}
