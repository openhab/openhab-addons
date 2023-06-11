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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSBooleanValue;
import org.openhab.core.library.types.OpenClosedType;

/**
 * OpenClosedType <-> WSBooleanValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OpenClosedTypeWSBooleanValueConverter implements Converter<WSBooleanValue, OpenClosedType> {

    @Override
    public OpenClosedType convertFromResourceValue(@NonNull WSBooleanValue from,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return from.value ^ convertData.getInverted() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    @Override
    public WSBooleanValue convertFromOHType(@NonNull OpenClosedType from, @NonNull WSBooleanValue value,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return new WSBooleanValue(value.resourceID, from == OpenClosedType.OPEN ^ convertData.getInverted());
    }
}
