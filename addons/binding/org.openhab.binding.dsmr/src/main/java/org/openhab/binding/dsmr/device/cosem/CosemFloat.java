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
 * CosemFloat represents a float value
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class CosemFloat extends CosemValue<Float> {

    /**
     * Creates a new CosemFloat
     *
     * @param unit
     *            the unit of the value
     */
    public CosemFloat(String unit) {
        super(unit);
    }

    /**
     * Parses a String value (that represents a float) to a Float object
     *
     * @param cosemValue
     *            the value to parse
     * @return {@link Float} on success
     * @throws ParseException
     *             if parsing failed
     */
    @Override
    protected Float parse(String cosemValue) throws ParseException {
        try {
            return Float.parseFloat(cosemValue);
        } catch (NumberFormatException nfe) {
            throw new ParseException("Failed to parse value " + value + " as float", 0);
        }
    }

    /**
     * Returns an openHAB representation of this CosemFloat
     *
     * @return {@link DecimalType} representing the value of this CosemFloat
     */
    @Override
    public DecimalType getOpenHABValue() {
        return new DecimalType(value);
    }
}
