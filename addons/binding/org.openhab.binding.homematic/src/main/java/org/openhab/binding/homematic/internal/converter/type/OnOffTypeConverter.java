/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.converter.type;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.DATAPOINT_NAME_SENSOR;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Converts between a Homematic datapoint value and a openHAB OnOffType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class OnOffTypeConverter extends AbstractTypeConverter<OnOffType> {
    @Override
    protected boolean toBindingValidation(HmDatapoint dp, Class<? extends Type> typeClass) {
        return dp.isBooleanType() && typeClass.isAssignableFrom(OnOffType.class);
    }

    @Override
    protected Object toBinding(OnOffType type, HmDatapoint dp) throws ConverterException {
        return (type == OnOffType.OFF ? Boolean.FALSE : Boolean.TRUE) != isInvert(dp);
    }

    @Override
    protected boolean fromBindingValidation(HmDatapoint dp) {
        return dp.isBooleanType() && dp.getValue() instanceof Boolean;
    }

    @Override
    protected OnOffType fromBinding(HmDatapoint dp) throws ConverterException {
        return (((Boolean) dp.getValue()) == Boolean.FALSE) != isInvert(dp) ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * If the item is a sensor or a state from some devices, then OnOff must be inverted.
     */
    private boolean isInvert(HmDatapoint dp) {
        return DATAPOINT_NAME_SENSOR.equals(dp.getName()) || isStateInvertDatapoint(dp);
    }

}
