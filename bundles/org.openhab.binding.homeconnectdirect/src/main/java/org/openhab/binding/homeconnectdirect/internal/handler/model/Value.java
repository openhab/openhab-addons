/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.handler.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ContentType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DataType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;

/**
 * Appliance value model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record Value(int uid, String key, Object value, Object rawValue, DeviceDescriptionType type,
        @Nullable ContentType contentType, @Nullable DataType dataType, @Nullable Integer enumerationType) {

    public int getValueAsInt() {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Double.valueOf(String.valueOf(value)).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double getValueAsDouble() {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public String getValueAsString() {
        return String.valueOf(value);
    }
}
