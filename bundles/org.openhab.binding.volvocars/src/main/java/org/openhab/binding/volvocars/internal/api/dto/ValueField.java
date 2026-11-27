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
package org.openhab.binding.volvocars.internal.api.dto;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * A single value as returned by the Volvo Cars APIs, e.g.
 * <code>{"value": "47.3", "unit": "l", "timestamp": "2020-11-19T21:23:24.123Z"}</code>.
 * The Energy API v2 additionally reports a <code>status</code> and uses <code>updatedAt</code>
 * instead of <code>timestamp</code>.
 *
 * @author Michael Weger - Initial contribution
 */
public class ValueField {
    public @Nullable JsonElement value;
    public @Nullable String unit;
    public @Nullable String timestamp;
    public @Nullable String updatedAt;
    public @Nullable String status;
    /** Only reported for <code>availabilityStatus</code> if the vehicle is unavailable. */
    public @Nullable String unavailableReason;

    /**
     * @return true unless the Energy API reported an error (e.g. <code>NOT_SUPPORTED</code>) for this value
     */
    public boolean isValid() {
        return value != null && (status == null || "OK".equals(status));
    }

    public @Nullable String getString() {
        JsonElement localValue = value;
        return localValue instanceof JsonPrimitive primitive ? primitive.getAsString() : null;
    }

    /**
     * Numeric values are sometimes encoded as JSON strings, e.g. <code>"value": "47.3"</code>.
     */
    public @Nullable BigDecimal getNumber() {
        JsonElement localValue = value;
        if (localValue instanceof JsonPrimitive primitive && (primitive.isNumber() || primitive.isString())) {
            try {
                return new BigDecimal(primitive.getAsString().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ValueField[value=" + value + ", unit=" + unit + ", status=" + status + "]";
    }
}
