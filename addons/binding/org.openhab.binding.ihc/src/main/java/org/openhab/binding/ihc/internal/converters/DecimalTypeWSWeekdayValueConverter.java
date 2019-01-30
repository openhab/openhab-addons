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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSWeekdayValue;

/**
 * DecimalType <-> WSWeekdayValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSWeekdayValueConverter implements Converter<WSWeekdayValue, DecimalType> {

    @Override
    public DecimalType convertFromResourceValue(WSWeekdayValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        return new DecimalType(from.getWeekdayNumber());
    }

    @Override
    public WSWeekdayValue convertFromOHType(DecimalType from, WSWeekdayValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        value.setWeekdayNumber(from.intValue());
        return value;
    }
}
