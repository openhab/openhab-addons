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
package org.openhab.binding.homeconnect.internal.client.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Option model
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class Option {

    private final @Nullable String key;
    private final @Nullable String value;
    private final @Nullable String unit;

    public Option(@Nullable String key, @Nullable String value, @Nullable String unit) {
        this.key = key;
        this.value = value;
        this.unit = unit;
    }

    public @Nullable String getKey() {
        return key;
    }

    public @Nullable String getValue() {
        return value;
    }

    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(value);
    }

    public int getValueAsInt() {
        @Nullable
        String stringValue = value;
        return stringValue != null ? Integer.parseInt(stringValue) : 0;
    }

    public @Nullable String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return "Option [key=" + key + ", value=" + value + ", unit=" + unit + "]";
    }
}
