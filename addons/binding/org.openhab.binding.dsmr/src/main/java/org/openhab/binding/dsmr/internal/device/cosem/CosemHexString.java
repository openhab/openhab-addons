/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;

import org.eclipse.smarthome.core.library.types.StringType;

/**
 * CosemHexString represents a string value stored as Hexadecimal values.
 *
 * So the value 'Test' is stored as 54657374
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class CosemHexString extends CosemValue<String> {
    /**
     * Creates a new CosemString
     *
     * @param unit
     *            the unit of the value
     */
    public CosemHexString(String unit) {
        super(unit);
    }

    /**
     * Parses a String value to a CosemString (does nothing in fact)
     *
     * @param cosemValue
     *            the value to parse
     * @return {@link String} on success
     * @throws ParseException
     *             if parsing failed
     */
    @Override
    protected String parse(String cosemHexValue) throws ParseException {
        if (cosemHexValue.length() % 2 != 0) {
            throw new ParseException(cosemHexValue + "is not a valid hexadecimal string", 0);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cosemHexValue.length(); i += 2) {
                sb.append((char) Integer.parseInt(cosemHexValue.substring(i, i + 2), 16));
            }
            return sb.toString();
        }
    }

    /**
     * Returns an openHAB representation of this String
     *
     * @return {@link StringType} representing the value of this CosemString
     */
    @Override
    public StringType getOpenHABValue() {
        return new StringType(value);
    }
}
