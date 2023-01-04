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

import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Type;

/**
 * Converts between a Homematic datapoint value and an openHAB StringType.
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
