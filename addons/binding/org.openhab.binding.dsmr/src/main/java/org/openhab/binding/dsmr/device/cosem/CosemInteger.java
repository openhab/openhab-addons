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

import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * CosemInteger represents an integer value
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class CosemInteger extends CosemValue<Integer> {

    /**
     * Creates a new CosemInteger
     *
     * @param unit
     *            the unit of the value
     */
    public CosemInteger(String unit) {
        super(unit);
    }

    /**
     * Parses a String value (that represents an integer) to an Integer object
     *
     * @param cosemValue
     *            the value to parse
     * @return {@link Integer} on success
     * @throws ParseException
     *             if parsing failed
     */
    @Override
    protected Integer parse(String cosemValue) throws ParseException {
        try {
            return Integer.parseInt(cosemValue);
        } catch (NumberFormatException nfe) {
            throw new ParseException("Failed to parse value " + value + " as integer", 0);
        }
    }

    /**
     * Returns an openHAB representation of this CosemInteger
     *
     * @return {@link DecimalType} representing the value of this CosemInteger
     */
    @Override
    public DecimalType getOpenHABValue() {
        return new DecimalType(value);
    }
}
