/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.converters;

import org.eclipse.smarthome.core.library.types.UpDownType;
import org.openhab.binding.ihc.ws.resourcevalues.WSBooleanValue;

/**
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class UpDownTypeWSBooleanValueConverter implements Converter<WSBooleanValue, UpDownType> {

    @Override
    public UpDownType convertFromResourceValue(WSBooleanValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        if (from.isValue()) {
            return convertData.getInverted() == false ? UpDownType.UP : UpDownType.DOWN;
        } else {
            return convertData.getInverted() == false ? UpDownType.DOWN : UpDownType.UP;
        }
    }

    @Override
    public WSBooleanValue convertFromOHType(UpDownType from, WSBooleanValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        boolean valON = convertData.getInverted() == false ? true : false;
        boolean valOFF = convertData.getInverted() == false ? false : true;
        value.setValue(from == UpDownType.UP ? valON : valOFF);
        return value;
    }
}
