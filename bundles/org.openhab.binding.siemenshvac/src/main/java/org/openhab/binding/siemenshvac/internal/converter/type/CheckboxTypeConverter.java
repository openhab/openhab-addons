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

import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.Type;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class CheckboxTypeConverter extends AbstractTypeConverter {
    @Override
    protected boolean toBindingValidation(Type type) {
        return true;
    }

    @Override
    protected Object toBinding(Type type, ChannelType tp) throws ConverterException {
        throw new ConverterException("NIY");
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String type) {
        return true;
    }

    @Override
    protected DecimalType fromBinding(JsonElement value, String type, ChannelType tp) throws ConverterException {
        throw new ConverterException("NIY");
    }

    @Override
    public String getChannelType(boolean writeAccess) {
        return "contact";
    }

    @Override
    public String getItemType(boolean writeAccess) {
        return SiemensHvacBindingConstants.ITEM_TYPE_CONTACT;
    }

    @Override
    public boolean hasVariant() {
        return false;
    }
}
