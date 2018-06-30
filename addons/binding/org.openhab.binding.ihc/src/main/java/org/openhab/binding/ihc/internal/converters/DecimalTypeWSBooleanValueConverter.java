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
import org.openhab.binding.ihc.ws.resourcevalues.WSBooleanValue;

/**
 * DecimalType <-> WSBooleanValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSBooleanValueConverter implements Converter<WSBooleanValue, DecimalType> {

    @Override
    public DecimalType convertFromResourceValue(WSBooleanValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        return new DecimalType(from.isValue() ? 1 : 0);
    }

    @Override
    public WSBooleanValue convertFromOHType(DecimalType from, WSBooleanValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        value.setValue(from.intValue() > 0 ? true : false);
        return value;
    }
}
