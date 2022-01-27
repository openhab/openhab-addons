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
package org.openhab.binding.powermax.internal.state;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * All defined sensor types for Master panels
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum PowermasterSensorType {

    SENSOR_TYPE_1((byte) 0x01, "Motion"),
    SENSOR_TYPE_2((byte) 0x04, "Camera"),
    SENSOR_TYPE_3((byte) 0x16, "Smoke"),
    SENSOR_TYPE_4((byte) 0x1A, "Temperature"),
    SENSOR_TYPE_5((byte) 0x2A, "Magnet"),
    SENSOR_TYPE_6((byte) 0xFE, "Wired");

    private final byte code;
    private final String label;

    private PowermasterSensorType(byte code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * @return the code identifying the sensor type
     */
    public byte getCode() {
        return code;
    }

    /**
     * @return the label associated to the sensor type
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the ENUM value from its identifying code
     *
     * @param code the identifying code
     *
     * @return the corresponding ENUM value
     *
     * @throws IllegalArgumentException if no ENUM value corresponds to this code
     */
    public static PowermasterSensorType fromCode(byte code) throws IllegalArgumentException {
        for (PowermasterSensorType sensorType : PowermasterSensorType.values()) {
            if (sensorType.getCode() == code) {
                return sensorType;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
