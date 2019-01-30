/**
 * Copyright (c) 2010-2018 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal.converters;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSBooleanValue;

/**
 * DecimalType <-> WSBooleanValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSBooleanValueConverter implements Converter<WSBooleanValue, DecimalType> {

    @Override
    public DecimalType convertFromResourceValue(WSBooleanValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        return new DecimalType(from.booleanValue() ? 1 : 0);
    }

    @Override
    public WSBooleanValue convertFromOHType(DecimalType from, WSBooleanValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        value.setValue(from.intValue() > 0);
        return value;
    }
}
