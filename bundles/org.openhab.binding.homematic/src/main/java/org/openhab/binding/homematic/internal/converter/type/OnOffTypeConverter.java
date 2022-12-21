/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.converter.type;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.DATAPOINT_NAME_SENSOR;

import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Type;

/**
 * Converts between a Homematic datapoint value and an openHAB OnOffType.
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
        return Boolean.FALSE.equals(dp.getValue()) != isInvert(dp) ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * If the item is a sensor or a state from some devices, then OnOff must be inverted.
     */
    private boolean isInvert(HmDatapoint dp) {
        return DATAPOINT_NAME_SENSOR.equals(dp.getName()) || isStateInvertDatapoint(dp);
    }
}
