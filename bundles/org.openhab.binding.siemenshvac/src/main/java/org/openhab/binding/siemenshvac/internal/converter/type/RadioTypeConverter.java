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

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDataPoint;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class RadioTypeConverter extends AbstractTypeConverter {
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
    protected @Nullable Object commandToBinding(Command command, ChannelType tp) throws ConverterException {
        Object valUpdate = null;

        if (command instanceof DecimalType decimalValue) {
            valUpdate = decimalValue.toString();
        } else if (command instanceof OnOffType onOffValue) {
            if (onOffValue.equals(OnOffType.OFF)) {
                valUpdate = 0;
            } else if (onOffValue.equals(OnOffType.ON)) {
                valUpdate = 1;
            }
        }

        return valUpdate;
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String unit, String type) {
        return true;
    }

    @Override
    protected State fromBinding(JsonElement value, String unit, String type, ChannelType tp, Locale locale)
            throws ConverterException {
        State updateVal = UnDefType.UNDEF;
        String valueSt = value.getAsString();

        StateDescription sd = tp.getState();

        if (sd != null) {
            List<StateOption> options = sd.getOptions();
            StateOption offOpt = options.get(0);
            StateOption onOpt = options.get(1);

            if (valueSt.equals(onOpt.getLabel())) {
                updateVal = new DecimalType(1);
            } else if (valueSt.equals(offOpt.getLabel())) {
                updateVal = new DecimalType(0);
            }
        }

        return updateVal;
    }

    @Override
    public String getChannelType(SiemensHvacMetadataDataPoint dpt) {
        if (dpt.getWriteAccess()) {
            return "switch";
        } else {
            return "contact";
        }
    }

    @Override
    public String getItemType(SiemensHvacMetadataDataPoint dpt) {
        if (dpt.getWriteAccess()) {
            return CoreItemFactory.SWITCH;
        } else {
            return CoreItemFactory.CONTACT;
        }
    }

    @Override
    public boolean hasVariant() {
        return true;
    }
}
