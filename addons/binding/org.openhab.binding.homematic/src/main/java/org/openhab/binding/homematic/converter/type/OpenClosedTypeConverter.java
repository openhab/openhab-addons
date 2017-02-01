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

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.binding.homematic.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Converts between a Homematic datapoint value and a openHab OpenClosedType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class OpenClosedTypeConverter extends AbstractTypeConverter<OpenClosedType> {

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
    protected Object toBinding(OpenClosedType type, HmDatapoint dp) throws ConverterException {
        return (type == OpenClosedType.CLOSED ? Boolean.FALSE : Boolean.TRUE) != isInvert(dp);
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
    protected OpenClosedType fromBinding(HmDatapoint dp) throws ConverterException {
        return (((Boolean) dp.getValue()) == Boolean.FALSE) != isInvert(dp) ? OpenClosedType.CLOSED
                : OpenClosedType.OPEN;
    }

    /**
     * Invert only values which are not from a sensor or a state from some devices.
     */
    private boolean isInvert(HmDatapoint dp) {
        return !DATAPOINT_NAME_SENSOR.equals(dp.getName()) && !isStateInvertDatapoint(dp);
    }

}
