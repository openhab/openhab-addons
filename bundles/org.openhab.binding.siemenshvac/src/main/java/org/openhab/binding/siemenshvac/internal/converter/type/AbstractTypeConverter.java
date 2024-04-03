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

import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.binding.siemenshvac.internal.converter.TypeConverter;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Base class for all Converters with common methods.
 *
 * @author Laurent Arnal - Initial contribution
 */
public abstract class AbstractTypeConverter<T extends State> implements TypeConverter<T> {
    private final Logger logger = LoggerFactory.getLogger(AbstractTypeConverter.class);

    @SuppressWarnings("unchecked")
    @Override
    public Object convertToBinding(Type type, JsonObject dp) throws ConverterException {
        /*
         * if (type == UnDefType.NULL) {
         * return null;
         * } else if (type.getClass().isEnum() && !(this instanceof OnOffTypeConverter)
         * && !(this instanceof OpenClosedTypeConverter)) {
         * return commandToBinding((Command) type, dp);
         * } else if (!toBindingValidation(dp, type.getClass())) {
         * String errorMessage = String.format("Can't convert type %s with value '%s' to %s value with %s for '%s'",
         * type.getClass().getSimpleName(), type.toString(), dp.getType(), this.getClass().getSimpleName(),
         * new HmDatapointInfo(dp));
         * throw new ConverterTypeException(errorMessage);
         * }
         */
        return toBinding((T) type, dp);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convertFromBinding(JsonObject dp) throws ConverterException {

        String type = null;
        JsonElement value = null;

        if (dp.has("Type")) {
            type = dp.get("Type").getAsString().trim();
        }
        if (dp.has("Value")) {
            value = dp.get("Value");
        }
        if (dp.has("EnumValue")) {
            value = dp.get("EnumValue");
        }

        if (value == null) {
            return (T) UnDefType.NULL;
        }

        if (type == null) {
            logger.debug("siemensHvac:ReadDP:null type {}", dp);
            return (T) UnDefType.NULL;
        }

        if (!fromBindingValidation(value, type)) {
            logger.debug("Can't convert {} value '{}' with {} for '{}'", type, value, this.getClass().getSimpleName(),
                    dp);
            return (T) UnDefType.NULL;
        }

        return fromBinding(value, type);
    }

    /**
     * Converts an openHAB command to a SiemensHvacValue value.
     */
    protected Object commandToBinding(Command command, JsonObject dp) throws ConverterException {
        throw new ConverterException("Unsupported command " + command.getClass().getSimpleName() + " for "
                + this.getClass().getSimpleName());
    }

    /**
     * Returns true, if the conversion from openHAB to the binding is possible.
     */
    protected abstract boolean toBindingValidation(JsonObject dp, Class<? extends Type> typeClass);

    /**
     * Converts the type to a datapoint value.
     */
    protected abstract Object toBinding(T type, JsonObject dp) throws ConverterException;

    /**
     * Returns true, if the conversion from the binding to openHAB is possible.
     */
    protected abstract boolean fromBindingValidation(JsonElement value, String type);

    /**
     * Converts the datapoint value to an openHAB type.
     */
    protected abstract T fromBinding(JsonElement value, String type) throws ConverterException;
}
