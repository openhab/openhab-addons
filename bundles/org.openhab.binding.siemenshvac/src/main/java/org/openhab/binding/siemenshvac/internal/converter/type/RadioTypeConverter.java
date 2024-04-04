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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.Type;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class RadioTypeConverter extends AbstractTypeConverter {
    @Override
    protected boolean toBindingValidation(Type type) {
        return true;
    }

    @Override
    protected Object toBinding(Type type) throws ConverterException {
        Object valUpdate = null;

        if (type instanceof DecimalType decimalValue) {
            valUpdate = decimalValue.toString();
        }

        return valUpdate;
    }

    @Override
    protected Object commandToBinding(Command command) throws ConverterException {
        Object valUpdate = null;

        if (command instanceof DecimalType decimalValue) {
            valUpdate = decimalValue.toString();
        } else if (command instanceof OnOffType onOffType) {
            if (onOffType.equals(OnOffType.OFF)) {
                valUpdate = 0;
            } else if (onOffType.equals(OnOffType.ON)) {
                valUpdate = 1;
            }
        }

        return valUpdate;
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String type) {
        return true;
    }

    @Override
    protected DecimalType fromBinding(JsonElement value, String type) throws ConverterException {
        DecimalType updateVal = new DecimalType();
        String valueSt = value.getAsString();

        if ("Non".equals(valueSt)) {
            updateVal = new DecimalType(0);
        } else if ("Arrêt".equals(valueSt)) {
            updateVal = new DecimalType(0);
        } else if ("Marche".equals(valueSt)) {
            updateVal = new DecimalType(1);
        } else if ("Oui".equals(valueSt)) {
            updateVal = new DecimalType(1);
        }

        return updateVal;
    }

    @Override
    public String getChannelType(boolean writeAccess) {
        if (writeAccess) {
            return "switch";
        } else {
            return "contact";
        }
    }

    @Override
    public String getItemType(boolean writeAccess) {
        if (writeAccess) {
            return SiemensHvacBindingConstants.ITEM_TYPE_SWITCH;
        } else {
            return SiemensHvacBindingConstants.ITEM_TYPE_CONTACT;
        }
    }

    @Override
    public boolean hasVariant() {
        return true;
    }
}
