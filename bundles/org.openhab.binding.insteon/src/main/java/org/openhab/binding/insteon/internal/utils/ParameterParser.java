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
package org.openhab.binding.insteon.internal.utils;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parameter parser functions
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ParameterParser {
    @SuppressWarnings("unchecked")
    public static <T> @Nullable T getParameterAs(@Nullable String value, Class<T> type) throws NumberFormatException {
        if (value == null) {
            return null;
        }

        Object result = value;
        if (Integer.class.equals(type)) {
            result = value.startsWith("0x") ? ByteUtils.hexStringToInteger(value) : Integer.parseInt(value);
        } else if (Double.class.equals(type)) {
            result = Double.parseDouble(value);
        } else if (Long.class.equals(type)) {
            result = Long.parseLong(value);
        } else if (Boolean.class.equals(type)) {
            result = Boolean.valueOf(value);
        }

        return (T) result;
    }

    public static <T> T getParameterAsOrDefault(@Nullable String value, Class<T> type, @NonNull T defaultValue) {
        try {
            return Objects.requireNonNullElse(getParameterAs(value, type), defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
