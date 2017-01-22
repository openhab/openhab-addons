/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.device.cosem;

import java.text.ParseException;

import org.eclipse.smarthome.core.library.types.StringType;

/**
 * CosemString represents a string value
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class CosemString extends CosemValue<String> {
    /**
     * Creates a new CosemString
     *
     * @param unit
     *            the unit of the value
     */
    public CosemString(String unit) {
        super(unit);
    }

    /**
     * Creates a new CosemString with the specified value
     *
     * @param unit the unit of the value
     * @param value the value
     */
    public CosemString(String unit, String value) {
        super(unit);
        this.value = value;
    }

    /**
     * Parses a String value to a CosemString (does nothing in fact)
     *
     * @param cosemValue
     *            the value to parse
     * @return Parsed string representation of the cosemValue
     * @throws ParseException
     *             if parsing failed
     */
    @Override
    protected String parse(String cosemValue) throws ParseException {
        return cosemValue;
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
