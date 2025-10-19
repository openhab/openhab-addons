/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ContactSensorType} enum defines the types of contact sensors supported by the binding.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public enum ContactSensorType {
    /**
     * 012C dry contact sensor using ReadDryChannelsRequest/Response protocol
     */
    SENSOR_012C("012c"),

    /**
     * DB00 multi-sensor with dry contacts using ReadNineInOneStatusRequest/Response protocol
     */
    MULTI_SENSOR_DB00("db00");

    private final String configValue;

    ContactSensorType(String configValue) {
        this.configValue = configValue;
    }

    /**
     * Get the configuration value for this sensor type.
     *
     * @return the configuration value
     */
    public String getConfigValue() {
        return configValue;
    }

    /**
     * Parse a configuration value into a ContactSensorType.
     *
     * @param value the configuration value
     * @return the corresponding ContactSensorType, or SENSOR_24Z if not recognized
     */
    public static ContactSensorType fromConfigValue(String value) {
        for (ContactSensorType type : values()) {
            if (type.configValue.equals(value)) {
                return type;
            }
        }
        return SENSOR_012C; // Default to 012C for backward compatibility or null values
    }
}
