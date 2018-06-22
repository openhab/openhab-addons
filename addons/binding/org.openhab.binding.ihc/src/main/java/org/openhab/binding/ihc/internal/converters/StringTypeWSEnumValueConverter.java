/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.converters;

import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.ihc.ws.projectfile.IhcEnumValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSEnumValue;

/**
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class StringTypeWSEnumValueConverter implements Converter<WSEnumValue, StringType> {

    @Override
    public StringType convertFromResourceValue(WSEnumValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        return new StringType(from.getEnumName());
    }

    @Override
    public WSEnumValue convertFromOHType(StringType from, WSEnumValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {

        if (convertData.getEnumValues() != null) {
            boolean found = false;
            for (IhcEnumValue item : convertData.getEnumValues()) {
                if (item.name.equals(value.toString())) {
                    value.setEnumValueID(item.id);
                    value.setEnumName(value.toString());
                    found = true;
                    break;
                }
            }
            if (found == false) {
                throw new NumberFormatException("Can't find enum value for string " + value.toString());
            }
            return value;
        } else {
            throw new NumberFormatException("Enum list is null");
        }
    }
}
