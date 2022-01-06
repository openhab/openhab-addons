/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSFloatingPointValue;
import org.openhab.core.library.types.DecimalType;

/**
 * DecimalType <-> WSFloatingPointValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSFloatingPointValueConverter implements Converter<WSFloatingPointValue, DecimalType> {

    @Override
    public DecimalType convertFromResourceValue(@NonNull WSFloatingPointValue from,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        BigDecimal bd = new BigDecimal(from.value).setScale(2, RoundingMode.HALF_EVEN);
        return new DecimalType(bd);
    }

    @Override
    public WSFloatingPointValue convertFromOHType(@NonNull DecimalType from, @NonNull WSFloatingPointValue value,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        if (from.doubleValue() >= value.minimumValue && from.doubleValue() <= value.maximumValue) {
            return new WSFloatingPointValue(value.resourceID, from.doubleValue(), value.minimumValue,
                    value.maximumValue);
        } else {
            throw new ConversionException("Value is not between acceptable limits (min=" + value.minimumValue + ", max="
                    + value.maximumValue + ")");
        }
    }
}
