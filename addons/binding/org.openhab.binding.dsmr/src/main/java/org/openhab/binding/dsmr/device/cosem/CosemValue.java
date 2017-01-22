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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.types.State;

/**
 * CosemValue represents the mapping between COSEM formatted values and openHAB
 * type values
 *
 * @author M. Volaart
 * @since 2.0.0
 * @param <S> the native type this CosemValue represent
 */
public abstract class CosemValue<S extends Object> {
    /** native value */
    protected S value;

    /* unit of this cosemValue */
    private final String unit;

    /**
     * Creates a CosemValue
     *
     * @param unit the unit of the value
     */
    protected CosemValue(String unit) {
        this.unit = unit;
    }

    /**
     * Parses the string value to the openHAB type
     *
     * @param cosemValue the COSEM value to parse
     * @return S the native object type of this COSEM value
     * @throws ParseException if parsing failed
     */
    protected abstract S parse(String cosemValue) throws ParseException;

    /**
     * Sets the value of this CosemValue
     *
     * This method will automatically parse the unit and the value of the COSEM
     * value string
     *
     * @param cosemValue the cosemValue
     * @throws ParseException if parsing failed
     */
    public void setValue(String cosemValue) throws ParseException {
        if (unit.length() > 0) {
            /*
             * Check if COSEM value has a unit, check and parse the value. We assume here numbers (float or integers)
             * The specification states that the delimiter between the value and the unit is a '*'-character.
             * We have seen on the Kaifa 0025 meter that both '*' and the '_' character are used.
             *
             * On the Kampstrup 162JxC in some CosemValues the seperator is missing
             *
             * The above quirks are supported
             *
             * We also support unit that do not follow the exact case.
             */
            Pattern p = Pattern.compile("^(\\d+\\.?\\d+)[\\*_]?" + unit + "$", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(cosemValue);

            if (m.matches()) {
                value = parse(m.group(1));
            } else {
                throw new ParseException("Unit of " + cosemValue + " is not " + unit, 0);
            }
        } else {
            // COSEM value does not have a unit, parse value
            value = parse(cosemValue);
        }
    }

    /**
     * Return the cosem Value
     *
     * @return native object value
     */
    public S getValue() {
        return value;
    }

    /**
     * Returns the OpenHAB state object representing this CosemValue
     *
     * @return OpenHAB state object representing this CosemValue
     */
    public abstract State getOpenHABValue();

    /**
     * Returns the unit of this COSEM value
     *
     * @return the unit of this COSEM value
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns String representation of this CosemValue
     *
     * @return String representation of this CosemValue
     */
    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return "CosemValue is not initialized yet";
        }
    }
}
