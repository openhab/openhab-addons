/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.converter.type;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.DATAPOINT_NAME_SENSOR;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.homematic.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Converts between a Homematic datapoint value and a openHab OnOffType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class OnOffTypeConverter extends AbstractTypeConverter<OnOffType> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean toBindingValidation(HmDatapoint dp) {
        return dp.isBooleanType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object toBinding(OnOffType type, HmDatapoint dp) throws ConverterException {
        return (type == OnOffType.OFF ? Boolean.FALSE : Boolean.TRUE) != isInvert(dp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean fromBindingValidation(HmDatapoint dp) {
        return dp.isBooleanType() && dp.getValue() instanceof Boolean;
    }

    /**
     * {@inheritDoc}
     */
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
