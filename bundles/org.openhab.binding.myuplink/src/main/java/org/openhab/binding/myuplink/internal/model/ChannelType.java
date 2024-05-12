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
package org.openhab.binding.myuplink.internal.model;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enum to map units to type names and internal data types.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public enum ChannelType {
    ENERGY("kWh", "type-energy", "Number:Energy"),
    PRESSURE("bar", "type-pressure", "Number:Pressure"),
    PERCENT("%", "type-percent", "Number:Dimensionless"),
    TEMPERATURE("°C", "type-temperature", "Number:Temperature"),
    FREQUENCY("Hz", "type-frequency", "Number:Frequency"),
    FLOW("l/m", "type-flow", "Number:Dimensionless"),
    ELECTRIC_CURRENT("A", "type-electric-current", "Number:ElectricCurrent"),
    TIME("h", "type-time", "Number:Time"),
    INTEGER("NUMBER", "type-number-integer", "Number"),
    DOUBLE("NUMBER", "type-number-double", "Number");

    private final String jsonUnit;
    private final String typeName;
    private final String acceptedType;

    ChannelType(String jsonUnit, String typeName, String acceptedType) {
        this.jsonUnit = jsonUnit;
        this.typeName = typeName;
        this.acceptedType = acceptedType;
    }

    /**
     * @return the jsonUnit
     */
    public String getJsonUnit() {
        return jsonUnit;
    }

    /**
     * @return the typeName
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return the acceptedType
     */
    public String getAcceptedType() {
        return acceptedType;
    }

    public static ChannelType fromJsonData(String jsonUnit, String jsonStrVal) {
        for (var channelType : ChannelType.values()) {
            if (channelType.getJsonUnit().equals(jsonUnit)) {
                return channelType;
            }
        }
        if (jsonStrVal.contains(JSON_VAL_DECIMAL_SEPARATOR)) {
            return DOUBLE;
        } else {
            return INTEGER;
        }
    }

    public static ChannelType fromTypeName(String typeName) {
        for (var channelType : ChannelType.values()) {
            if (channelType.getTypeName().equals(typeName)) {
                return channelType;
            }
        }
        return DOUBLE;
    }
}
