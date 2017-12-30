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

import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * {@link CosemDouble} represents a double value.
 *
 * @author M. Volaart - Initial contribution
 */
public class CosemDouble extends CosemValue<Double> {

    /**
     * Creates a new {@link CosemDouble}.
     *
     * @param unit
     *            the unit of the value
     */
    public CosemDouble(String unit) {
        super(unit);
    }

    /**
     * Parses a String value (that represents a double) to a Double object
     *
     * @param cosemValue
     *            the value to parse
     * @return {@link Double} on success
     * @throws ParseException
     *             if parsing failed
     */
    @Override
    protected Double parse(String cosemValue) throws ParseException {
        try {
            return Double.parseDouble(cosemValue);
        } catch (NumberFormatException nfe) {
            throw new ParseException("Failed to parse value " + value + " as double", 0);
        }
    }

    /**
     * Returns a smart home representation of this {@link CosemDouble}
     *
     * @return {@link DecimalType} representing the value of this {@link CosemDouble}
     */
    @Override
    public DecimalType getOpenHABValue() {
        return new DecimalType(value);
    }
}
