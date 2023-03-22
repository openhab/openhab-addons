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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;
import org.openhab.core.library.types.UpDownType;

/**
 * UpDownType <-> WSIntegerValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class UpDownTypeWSIntegerValueConverter implements Converter<WSIntegerValue, UpDownType> {

    @Override
    public UpDownType convertFromResourceValue(@NonNull WSIntegerValue from,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return from.value > 0 ^ convertData.getInverted() ? UpDownType.UP : UpDownType.DOWN;
    }

    @Override
    public WSIntegerValue convertFromOHType(@NonNull UpDownType from, @NonNull WSIntegerValue value,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        int newVal = from == UpDownType.UP ? value.maximumValue : value.minimumValue;

        if (convertData.getInverted()) {
            newVal = newVal == value.maximumValue ? value.minimumValue : value.maximumValue;
        }
        if (newVal >= value.minimumValue && newVal <= value.maximumValue) {
            return new WSIntegerValue(value.resourceID, newVal, value.minimumValue, value.maximumValue);
        } else {
            throw new ConversionException("Value is not between acceptable limits (min=" + value.minimumValue + ", max="
                    + value.maximumValue + ")");
        }
    }
}
