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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSTimerValue;
import org.openhab.core.library.types.DecimalType;

/**
 * DecimalType <-> WSTimerValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSTimerValueConverter implements Converter<WSTimerValue, DecimalType> {

    @Override
    public DecimalType convertFromResourceValue(@NonNull WSTimerValue from,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return new DecimalType(from.milliseconds);
    }

    @Override
    public WSTimerValue convertFromOHType(@NonNull DecimalType from, @NonNull WSTimerValue value,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return new WSTimerValue(value.resourceID, from.longValue());
    }
}
