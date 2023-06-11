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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

/**
 * This enum contains all binary inputs with they id. <br>
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public enum DeviceBinarayInputEnum {

    /*
     * Taken from http://developer.digitalstrom.org/Architecture/ds-basics.pdf#5f
     *
     * Input Type | Assigned Index | Natural Device and Description
     * Presence | 1 | Presence detector
     * Brightness | 2 | ---
     * Presence in darkness | 3 | Presence detector with activated internal twilight sensor
     * Twilight | 4 | Twilight sensor
     * Motion | 5 | Motion detector
     * Motion in darkness | 6 | Motion detect or with activated internal twilight sensor
     * Smoke | 7 | Smoke Detector
     * Wind strength above limit | 8 | Wind monitor with user-adjusted wind strength threshold
     * Rain | 9 | Rain monitor
     * Sun radiation | 10 | Sun light above threshold
     * Temperature below limit | 11 | Room thermostat with used-adjusted temperature threshold
     * Battery status is low | 12 | electric battery is running out of power
     * Window is open | 13 | Window contact
     * Door is open | 14 | Door contact
     * Window is tilted | 15 | Window handle; window is tilted instead of fully opened
     * Garage door is open | 16 | Garage door contact
     * Sun protection | 17 | Protect against too much sun light
     * Frost | 18 | Frost detector
     * --dS-Basics Version: v1.3-branch August 19, 2015, Table 16: Binary input types--
     *
     * Heating operation on/off | 19 |
     * Change-over heating/cooling | 20 |
     * --dS-web-interface server-version: 1.21.1--
     *
     *
     * Target Function | Assigned Index
     * Joker | 8
     * --dS-Basics Version: v1.3-branch August 19, 2015, Table 17: Binary input target functions--
     */

    PRESENCE((short) 1),
    BRIGHTNESS((short) 2),
    PRESENCE_IN_DARKNESS((short) 3),
    TWILIGHT((short) 4),
    MOTION((short) 5),
    MOTION_IN_DARKNESS((short) 6),
    SMOKE((short) 7),
    WIND_STRENGHT_ABOVE_LIMIT((short) 8),
    RAIN((short) 9),
    SUN_RADIATION((short) 10),
    TEMPERATION_BELOW_LIMIT((short) 11),
    BATTERY_STATUS_IS_LOW((short) 12),
    WINDOW_IS_OPEN((short) 13),
    DOOR_IS_OPEN((short) 14),
    WINDOW_IS_TILTED((short) 15),
    GARAGE_DOOR_IS_OPEN((short) 16),
    SUN_PROTECTION((short) 17),
    FROST((short) 18),
    HEATING_OPERATION_ON_OFF((short) 19),
    CHANGE_OVER_HEATING_COOLING((short) 20);

    private final Short binaryInputType;
    private static DeviceBinarayInputEnum[] deviceBinarayInputs = new DeviceBinarayInputEnum[DeviceBinarayInputEnum
            .values().length];

    static {
        for (DeviceBinarayInputEnum deviceBinarayInput : DeviceBinarayInputEnum.values()) {
            deviceBinarayInputs[deviceBinarayInput.binaryInputType - 1] = deviceBinarayInput;
        }
    }

    private DeviceBinarayInputEnum(Short binaryInputType) {
        this.binaryInputType = binaryInputType;
    }

    /**
     * Returns the id of this {@link DeviceBinarayInputEnum}.
     *
     * @return id
     */
    public Short getBinaryInputType() {
        return binaryInputType;
    }

    /**
     * Returns the {@link DeviceBinarayInputEnum} of the given id or null, if no {@link DeviceBinarayInputEnum} exist
     * for the id.
     *
     * @param binaryInputTypeID of the {@link DeviceBinarayInputEnum}
     * @return the {@link DeviceBinarayInputEnum} of the id
     */
    public static DeviceBinarayInputEnum getdeviceBinarayInput(Short binaryInputTypeID) {
        try {
            return deviceBinarayInputs[binaryInputTypeID - 1];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
