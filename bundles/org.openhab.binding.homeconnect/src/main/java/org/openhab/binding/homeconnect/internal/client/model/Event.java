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
 * Event model
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class Event {

    private @Nullable final String key;
    private @Nullable final String value;
    private @Nullable final String unit;

    public Event(@Nullable String key, @Nullable String value, @Nullable String unit) {
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
        return value != null ? Boolean.valueOf(getValue()).booleanValue() : false;
    }

    public int getValueAsInt() {
        return value != null ? Float.valueOf(getValue()).intValue() : 0;
    }

    public @Nullable String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return "Event [key=" + key + ", value=" + value + ", unit=" + unit + "]";
    }

}
