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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSEnumValue;

/**
 * DecimalType <-> WSEnumValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSEnumValueConverter implements Converter<WSEnumValue, DecimalType> {

    @Override
    public DecimalType convertFromResourceValue(WSEnumValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        return new DecimalType(from.getEnumValueID());
    }

    @Override
    public WSEnumValue convertFromOHType(DecimalType from, WSEnumValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        value.setEnumValueID(from.intValue());
        return value;
    }
}
