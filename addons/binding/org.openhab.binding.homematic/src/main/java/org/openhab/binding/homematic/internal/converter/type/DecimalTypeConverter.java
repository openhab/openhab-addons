/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.converter.type;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Converts between a Homematic datapoint value and a openHAB DecimalType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DecimalTypeConverter extends AbstractTypeConverter<DecimalType> {
    @Override
    protected boolean toBindingValidation(HmDatapoint dp, Class<? extends Type> typeClass) {
        return dp.isNumberType() && typeClass.isAssignableFrom(DecimalType.class);
    }

    @Override
    protected Object toBinding(DecimalType type, HmDatapoint dp) throws ConverterException {
        if (dp.isIntegerType()) {
            return type.intValue();
        }
        return round(type.doubleValue()).doubleValue();
    }

    @Override
    protected boolean fromBindingValidation(HmDatapoint dp) {
        return dp.isNumberType() && dp.getValue() instanceof Number;
    }

    @Override
    protected DecimalType fromBinding(HmDatapoint dp) throws ConverterException {
        Number number = ((Number) dp.getValue()).doubleValue();
        if (dp.isIntegerType()) {
            return new DecimalType(new BigDecimal(number.intValue()));
        }
        return new DecimalType(round(number.doubleValue()));
    }

}
