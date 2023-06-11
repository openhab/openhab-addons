/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.projectfile.IhcEnumValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSEnumValue;
import org.openhab.core.library.types.StringType;

/**
 * StringType <-> WSEnumValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class StringTypeWSEnumValueConverter implements Converter<WSEnumValue, StringType> {

    @Override
    public StringType convertFromResourceValue(@NonNull WSEnumValue from, @NonNull ConverterAdditionalInfo convertData)
            throws ConversionException {
        return new StringType(from.enumName);
    }

    @Override
    public WSEnumValue convertFromOHType(@NonNull StringType from, @NonNull WSEnumValue value,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        if (convertData.getEnumValues() != null) {
            for (IhcEnumValue item : convertData.getEnumValues()) {
                if (item.getName().equals(from.toString())) {
                    return new WSEnumValue(value.resourceID, value.definitionTypeID, item.getId(), item.getName());
                }
            }
            throw new ConversionException("Can't find enum value for string " + value.toString());
        } else {
            throw new ConversionException("Enum list is null");
        }
    }
}
