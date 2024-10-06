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
package org.openhab.binding.insteon.internal.utils;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ParameterParser} represents parameter parser functions
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ParameterParser {
    /**
     * Returns a parameter value as type
     *
     * @param value the parameter value
     * @param type the parameter type
     * @return the parameter value as type if not null, otherwise null
     * @throws NumberFormatException
     */
    @SuppressWarnings("unchecked")
    public static <@NonNull T> @Nullable T getParameterAs(@Nullable String value, Class<T> type)
            throws NumberFormatException {
        if (value == null) {
            return null;
        }

        Object result = value;
        if (Integer.class.equals(type)) {
            result = value.startsWith("0x") ? HexUtils.toInteger(value) : Integer.parseInt(value);
        } else if (Double.class.equals(type)) {
            result = Double.parseDouble(value);
        } else if (Long.class.equals(type)) {
            result = Long.parseLong(value);
        } else if (Boolean.class.equals(type)) {
            result = Boolean.valueOf(value);
        }

        return (T) result;
    }

    /**
     * Returns a parameter value as type or default value
     *
     * @param value the parameter value
     * @param type the parameter type
     * @param defaultValue the default value
     * @return the parameter value as type if not null, otherwise default value
     */
    public static <@NonNull T> T getParameterAsOrDefault(@Nullable String value, Class<T> type, T defaultValue) {
        try {
            return Objects.requireNonNullElse(getParameterAs(value, type), defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
