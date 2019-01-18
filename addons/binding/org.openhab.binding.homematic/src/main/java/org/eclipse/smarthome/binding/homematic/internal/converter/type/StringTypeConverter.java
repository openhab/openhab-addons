/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.internal.converter.type;

import org.eclipse.smarthome.binding.homematic.internal.converter.ConverterException;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointInfo;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Type;

/**
 * Converts between a Homematic datapoint value and a openHAB StringType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class StringTypeConverter extends AbstractTypeConverter<StringType> {
    @Override
    protected boolean toBindingValidation(HmDatapoint dp, Class<? extends Type> typeClass) {
        return (dp.isStringType() || dp.isEnumType()) && typeClass.isAssignableFrom(StringType.class);
    }

    @Override
    protected Object toBinding(StringType type, HmDatapoint dp) throws ConverterException {
        if (dp.isStringType()) {
            return type.toString();
        } else {
            int idx = dp.getOptionIndex(type.toString());
            if (idx == -1) {
                throw new ConverterException(String.format("Option value '%s' not found in datapoint '%s'",
                        type.toString(), new HmDatapointInfo(dp)));
            }
            return idx;
        }
    }

    @Override
    protected boolean fromBindingValidation(HmDatapoint dp) {
        return (dp.isStringType()) || (dp.isEnumType() && dp.getValue() instanceof Number);
    }

    @Override
    protected StringType fromBinding(HmDatapoint dp) throws ConverterException {
        if (dp.isStringType()) {
            return new StringType(String.valueOf(dp.getValue()));
        } else {
            String value = dp.getOptionValue();
            if (value == null) {
                throw new ConverterException(String.format("Option for value '%s' not found in datapoint '%s'",
                        dp.getValue(), new HmDatapointInfo(dp)));
            }
            return new StringType(value);
        }
    }

}
