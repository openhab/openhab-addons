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

import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;

/**
 * PercentType <-> WSIntegerValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class PercentTypeWSIntegerValueConverter implements Converter<WSIntegerValue, PercentType> {

    @Override
    public PercentType convertFromResourceValue(WSIntegerValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        return new PercentType(from.getInteger());
    }

    @Override
    public WSIntegerValue convertFromOHType(PercentType from, WSIntegerValue value, ConverterAdditionalInfo convertData)
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
