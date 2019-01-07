/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.state;

/**
 * All defined sensor types for Master panels
 *
 * @author Laurent Garnier - Initial contribution
 */
public enum PowermasterSensorType {

    SENSOR_TYPE_1((byte) 0x01, "Motion"),
    SENSOR_TYPE_2((byte) 0x04, "Camera"),
    SENSOR_TYPE_3((byte) 0x16, "Smoke"),
    SENSOR_TYPE_4((byte) 0x1A, "Temperature"),
    SENSOR_TYPE_5((byte) 0x2A, "Magnet"),
    SENSOR_TYPE_6((byte) 0xFE, "Wired");

    private byte code;
    private String label;

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
