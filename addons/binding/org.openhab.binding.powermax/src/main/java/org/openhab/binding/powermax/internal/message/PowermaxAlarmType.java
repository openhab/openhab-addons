/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

/**
 * All defined alarm types
 *
 * @author Laurent Garnier - Initial contribution
 */
public enum PowermaxAlarmType {

    ALARM_TYPE_1(0x01, "Intruder"),
    ALARM_TYPE_2(0x02, "Intruder"),
    ALARM_TYPE_3(0x03, "Intruder"),
    ALARM_TYPE_4(0x04, "Intruder"),
    ALARM_TYPE_5(0x05, "Intruder"),
    ALARM_TYPE_6(0x06, "Tamper"),
    ALARM_TYPE_7(0x07, "Tamper"),
    ALARM_TYPE_8(0x08, "Tamper"),
    ALARM_TYPE_9(0x09, "Tamper"),
    ALARM_TYPE_10(0x0B, "Panic"),
    ALARM_TYPE_11(0x0C, "Panic"),
    ALARM_TYPE_12(0x20, "Fire"),
    ALARM_TYPE_13(0x23, "Emergency"),
    ALARM_TYPE_14(0x49, "Gas"),
    ALARM_TYPE_15(0x4D, "Flood");

    private int code;
    private String label;

    private PowermaxAlarmType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * @return the code identifying the alarm type
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the label associated to the alarm type
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
    public static PowermaxAlarmType fromCode(int code) throws IllegalArgumentException {
        for (PowermaxAlarmType alarmType : PowermaxAlarmType.values()) {
            if (alarmType.getCode() == code) {
                return alarmType;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
