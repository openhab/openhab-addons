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

import java.math.BigDecimal;

import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Type;

/**
 * Converts between a Homematic datapoint value and an openHAB DecimalType.
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
