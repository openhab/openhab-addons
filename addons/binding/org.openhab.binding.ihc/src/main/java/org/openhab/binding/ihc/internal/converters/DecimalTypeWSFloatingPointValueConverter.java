/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.converters;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSFloatingPointValue;

/**
 * DecimalType <-> WSFloatingPointValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSFloatingPointValueConverter implements Converter<WSFloatingPointValue, DecimalType> {

    @Override
    public DecimalType convertFromResourceValue(WSFloatingPointValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        double d = from.getFloatingPointValue();
        BigDecimal bd = new BigDecimal(d).setScale(2, RoundingMode.HALF_EVEN);
        return new DecimalType(bd);
    }

    @Override
    public WSFloatingPointValue convertFromOHType(DecimalType from, WSFloatingPointValue value,
            ConverterAdditionalInfo convertData) throws NumberFormatException {
        if (from.doubleValue() >= value.getMinimumValue() && from.doubleValue() <= value.getMaximumValue()) {
            value.setFloatingPointValue(from.doubleValue());
            return value;
        } else {
            throw new NumberFormatException("Value is not between accetable limits (min=" + value.getMinimumValue()
                    + ", max=" + value.getMaximumValue() + ")");
        }
    }
}
