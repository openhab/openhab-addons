/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * Data model
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class Data {

    private final String name;
    private final @Nullable String value;
    private final @Nullable String unit;

    public Data(String name, @Nullable String value, @Nullable String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getValue() {
        return value;
    }

    public @Nullable String getUnit() {
        return unit;
    }

    public int getValueAsInt() {
        return value != null ? Float.valueOf(getValue()).intValue() : 0;
    }

    public boolean getValueAsBoolean() {
        return value != null ? Boolean.valueOf(getValue()).booleanValue() : false;
    }

    @Override
    public String toString() {
        return "Data [name=" + name + ", value=" + value + ", unit=" + unit + "]";
    }

}
