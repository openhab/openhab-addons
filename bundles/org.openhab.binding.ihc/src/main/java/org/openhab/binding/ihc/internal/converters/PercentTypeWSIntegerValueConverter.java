/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;
import org.openhab.core.library.types.PercentType;

/**
 * PercentType {@literal <->} WSIntegerValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class PercentTypeWSIntegerValueConverter implements Converter<WSIntegerValue, PercentType> {

    @Override
    public PercentType convertFromResourceValue(@NonNull WSIntegerValue from,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return new PercentType(from.value);
    }

    @Override
    public WSIntegerValue convertFromOHType(@NonNull PercentType from, @NonNull WSIntegerValue value,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        if (from.intValue() >= value.minimumValue && from.intValue() <= value.maximumValue) {
            return new WSIntegerValue(value.resourceID, from.intValue(), value.minimumValue, value.maximumValue);
        } else {
            throw new ConversionException("Value is not between acceptable limits (min=" + value.minimumValue + ", max="
                    + value.maximumValue + ")");
        }
    }
}
