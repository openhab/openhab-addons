/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.converters;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.binding.ihc.ws.resourcevalues.WSIntegerValue;

/**
 * DecimalType <-> WSIntegerValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSIntegerValueConverter implements Converter<WSIntegerValue, DecimalType> {

    @Override
    public DecimalType convertFromResourceValue(WSIntegerValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        return new DecimalType(from.getInteger());
    }

    @Override
    public WSIntegerValue convertFromOHType(DecimalType from, WSIntegerValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        if (from.intValue() >= value.getMinimumValue() && from.intValue() <= value.getMaximumValue()) {
            value.setInteger(from.intValue());
            return value;
        } else {
            throw new NumberFormatException("Value is not between accetable limits (min=" + value.getMinimumValue()
                    + ", max=" + value.getMaximumValue() + ")");
        }
    }
}
