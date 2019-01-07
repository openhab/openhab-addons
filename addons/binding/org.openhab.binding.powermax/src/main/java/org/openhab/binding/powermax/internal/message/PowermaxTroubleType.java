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
 * All defined trouble types
 *
 * @author Laurent Garnier - Initial contribution
 */
public enum PowermaxTroubleType {

    TROUBLE_TYPE_1(0x0A, "Communication"),
    TROUBLE_TYPE_2(0x0F, "General"),
    TROUBLE_TYPE_3(0x29, "Battery"),
    TROUBLE_TYPE_4(0x2B, "Power"),
    TROUBLE_TYPE_5(0x2D, "Battery"),
    TROUBLE_TYPE_6(0x2F, "Jamming"),
    TROUBLE_TYPE_7(0x31, "Communication"),
    TROUBLE_TYPE_8(0x33, "Telephone"),
    TROUBLE_TYPE_9(0x36, "Power"),
    TROUBLE_TYPE_10(0x38, "Battery"),
    TROUBLE_TYPE_11(0x3B, "Battery"),
    TROUBLE_TYPE_12(0x3C, "Battery"),
    TROUBLE_TYPE_13(0x40, "Battery"),
    TROUBLE_TYPE_14(0x43, "Battery");

    private int code;
    private String label;

    private PowermaxTroubleType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * @return the code identifying the trouble type
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the label associated to the trouble type
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
    public static PowermaxTroubleType fromCode(int code) throws IllegalArgumentException {
        for (PowermaxTroubleType troubleType : PowermaxTroubleType.values()) {
            if (troubleType.getCode() == code) {
                return troubleType;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
