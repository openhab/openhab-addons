/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.converter.type;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.binding.homematic.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Converts between a Homematic datapoint value and a openHab DecimalType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DecimalTypeConverter extends AbstractTypeConverter<DecimalType> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean toBindingValidation(HmDatapoint dp) {
        return dp.isNumberType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object toBinding(DecimalType type, HmDatapoint dp) throws ConverterException {
        if (dp.isIntegerType()) {
            return type.intValue();
        }
        return round(type.doubleValue()).doubleValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean fromBindingValidation(HmDatapoint dp) {
        return dp.isNumberType() && dp.getValue() instanceof Number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DecimalType fromBinding(HmDatapoint dp) throws ConverterException {
        Number number = ((Number) dp.getValue()).doubleValue();
        if (dp.isIntegerType()) {
            return new DecimalType(new BigDecimal(number.intValue()));
        }
        return new DecimalType(round(number.doubleValue()));
    }

}
