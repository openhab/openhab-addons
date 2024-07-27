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
package org.openhab.binding.emotiva.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This enum is used to describe the value types from Emotiva.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum EmotivaDataType {
    DIMENSIONLESS_DECIBEL("decibel"),
    DIMENSIONLESS_PERCENT("percent"),
    FREQUENCY_HERTZ("hertz"),
    NUMBER("number"),
    NUMBER_TIME("number_time"),
    GOODBYE("goodbye"),
    NOT_IMPLEMENTED("not_implemented"),
    ON_OFF("boolean"),
    STRING("string"),
    UNKNOWN("unknown");

    private final String name;

    EmotivaDataType(String name) {
        this.name = name;
    }

    public static EmotivaDataType fromName(String name) {
        EmotivaDataType result = EmotivaDataType.UNKNOWN;
        for (EmotivaDataType m : EmotivaDataType.values()) {
            if (m.name.equals(name)) {
                result = m;
                break;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
