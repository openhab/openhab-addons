/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;

/**
 * OnOffType <-> WSIntegerValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OnOffTypeWSIntegerValueConverter implements Converter<WSIntegerValue, OnOffType> {

    @Override
    public OnOffType convertFromResourceValue(@NonNull WSIntegerValue from,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {
        return from.value > 0 ^ convertData.getInverted() ? OnOffType.ON : OnOffType.OFF;
    }

    @Override
    public WSIntegerValue convertFromOHType(@NonNull OnOffType from, @NonNull WSIntegerValue value,
            @NonNull ConverterAdditionalInfo convertData) throws ConversionException {

        int onLevel = Math.min(value.maximumValue, getCommandLevel(value, convertData, OnOffType.ON));
        int newVal = from == OnOffType.ON ? onLevel : value.minimumValue;

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

    private int getCommandLevel(@NonNull WSIntegerValue value, @NonNull ConverterAdditionalInfo convertData,
            Command command) throws ConversionException {
        try {
            if (convertData.getCommandLevels() != null) {
                return (int) convertData.getCommandLevels().get(command);
            }
            return value.maximumValue;
        } catch (RuntimeException e) {
            throw new ConversionException(e);
        }
    }
}
