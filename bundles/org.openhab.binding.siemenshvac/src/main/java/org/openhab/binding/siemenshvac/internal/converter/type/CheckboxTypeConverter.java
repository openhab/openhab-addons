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
package org.openhab.binding.siemenshvac.internal.converter.type;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDataPoint;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.Type;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class CheckboxTypeConverter extends AbstractTypeConverter {
    @Override
    protected boolean toBindingValidation(Type type) {
        return true;
    }

    @Override
    protected @Nullable Object toBinding(Type type, ChannelType tp) throws ConverterException {
        throw new ConverterException("NIY");
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String unit, String type) {
        return true;
    }

    @Override
    protected DecimalType fromBinding(JsonElement value, String unit, String type, ChannelType tp, Locale locale)
            throws ConverterException {
        throw new ConverterException("NIY");
    }

    @Override
    public String getChannelType(SiemensHvacMetadataDataPoint dpt) {
        return "contact";
    }

    @Override
    public String getItemType(SiemensHvacMetadataDataPoint dpt) {
        return CoreItemFactory.CONTACT;
    }

    @Override
    public boolean hasVariant() {
        return false;
    }
}
