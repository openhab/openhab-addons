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
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;

/**
 * CosemInteger represents a decimal value
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Combined Integer and Double because {@link DecimalType} handles both
 */
@NonNullByDefault
class CosemDecimal extends CosemValueDescriptor<DecimalType> {

    public static final CosemDecimal INSTANCE = new CosemDecimal(false);
    public static final CosemDecimal INSTANCE_WITH_UNITS = new CosemDecimal(true);

    /**
     * If true it can be the input contains a unit. In that case the unit will be stripped before parsing.
     */
    private final boolean expectUnit;

    private CosemDecimal(boolean expectUnit) {
        this.expectUnit = expectUnit;
    }

    public CosemDecimal(String ohChannelId) {
        super(ohChannelId);
        this.expectUnit = false;
    }

    /**
     * Parses a String value (that represents a decimal) to a {@link DecimalType} object.
     *
     * @param cosemValue the value to parse
     * @return {@link DecimalType} representing the value of the cosem value
     * @throws ParseException if parsing failed
     */
    @Override
    protected DecimalType getStateValue(String cosemValue) throws ParseException {
        try {
            final String value;

            if (expectUnit) {
                final int sep = cosemValue.indexOf('*');
                value = sep > 0 ? cosemValue.substring(0, sep) : cosemValue;
            } else {
                value = cosemValue;
            }
            return new DecimalType(value);
        } catch (NumberFormatException nfe) {
            throw new ParseException("Failed to parse value '" + cosemValue + "' as integer", 0);
        }
    }
}
