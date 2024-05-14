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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TimeOfDayTypeConverter extends AbstractTypeConverter {
    @Override
    protected boolean toBindingValidation(Type type) {
        return true;
    }

    @Override
    protected @Nullable Object toBinding(Type type, ChannelType tp) throws ConverterException {
        Object valUpdate = null;

        if (type instanceof DecimalType decimalValue) {
            valUpdate = decimalValue.toString();
        }

        return valUpdate;
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String type) {
        return true;
    }

    @Override
    protected State fromBinding(JsonElement value, String type, ChannelType tp) throws ConverterException {
        if ("----".equals(value.getAsString())) {
            return UnDefType.UNDEF;
        } else {
            return new DecimalType(value.getAsInt());
        }
    }

    @Override
    public String getChannelType(boolean writeAccess) {
        return "number";
    }

    @Override
    public String getItemType(boolean writeAccess) {
        return CoreItemFactory.NUMBER;
    }

    @Override
    public boolean hasVariant() {
        return false;
    }
}
