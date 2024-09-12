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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSBooleanValue;
import org.openhab.core.library.types.OnOffType;

/**
 * OnOffType {@literal <->} WSBooleanValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OnOffTypeWSBooleanValueConverter implements Converter<WSBooleanValue, OnOffType> {

    @Override
    public OnOffType convertFromResourceValue(@NonNull WSBooleanValue from,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return OnOffType.from(from.value ^ convertData.getInverted());
    }

    @Override
    public WSBooleanValue convertFromOHType(@NonNull OnOffType from, @NonNull WSBooleanValue value,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return new WSBooleanValue(value.resourceID, from == OnOffType.ON ^ convertData.getInverted());
    }
}
