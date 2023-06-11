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

import java.util.HashMap;
import java.util.Map;

/**
 * digitalSTROM Application Groups.
 * 
 * <pre>
    | Group ID | Name                  | Color   | Application                         |
    | -------- | --------------------- | ------- | ----------------------------------- |
    | 1        | Lights                | Yellow  | Room lights                         |
    | 2        | Blinds                | Gray    | Blinds, curtains, shades, awnings   |
    | 3        | Heating               | Blue    | Heating                             |
    | 9        | Cooling               | Blue    | Cooling                             |
    | 10       | Ventilation           | Blue    | Ventilation                         |
    | 11       | Window                | Blue    | Windows                             |
    | 12       | Recirculation         | Blue    | Ceiling fan, Fan coil units         |
    | 64       | Apartment Ventilation | Blue    | Ventilation system                  |
    | 48       | Temperature Control   | Blue    | Single room temperature control     |
    | 4        | Audio                 | Cyan    | Playing music or radio              |
    | 5        | Video                 | Magenta | TV, Video                           |
    | 8        | Joker                 | Black   | Configurable                        |
    | n/a      | Single Device         | White   | Various, individual per device      |
    | n/a      | Security              | Red     | Security related functions, Alarms  |
    | n/a      | Access                | Green   | Access related functions, door bell |
 * </pre>
 * 
 * @author Rouven Schürch - Initial contribution
 * @see <a href="https://developer.digitalstrom.org/Architecture/ds-basics.pdf">ds-basics.pdf</a> (Version 1.4/1.6),
 *      chapter 3.2 (Group), Table 2.
 *
 */
public enum ApplicationGroup {

    LIGHTS((short) 1, Color.YELLOW),
    BLINDS((short) 2, Color.GREY),
    HEATING((short) 3, Color.BLUE),
    COOLING((short) 9, Color.BLUE),
    VENTILATION((short) 10, Color.BLUE),
    WINDOW((short) 11, Color.BLUE),
    RECIRCULATION((short) 12, Color.BLUE),
    APARTMENT_VENTILATION((short) 64, Color.BLUE),
    TEMPERATURE_CONTROL((short) 48, Color.BLUE),
    AUDIO((short) 4, Color.CYAN),
    VIDEO((short) 5, Color.MAGENTA),
    JOKER((short) 8, Color.BLACK),
    SINGLE_DEVICE((short) -1, Color.WHITE),
    SECURITY((short) -2, Color.RED),
    ACCESS((short) -3, Color.GREEN),
    UNDEFINED(null, Color.UNDEFINED);

    public enum Color {
        YELLOW,
        GREY,
        BLUE,
        CYAN,
        MAGENTA,
        BLACK,
        WHITE,
        RED,
        GREEN,
        UNDEFINED
    }

    private Short groupId;

    static final Map<Short, ApplicationGroup> APPLICATION_GROUPS = new HashMap<>();

    private Color color;

    static {
        for (ApplicationGroup applications : ApplicationGroup.values()) {
            APPLICATION_GROUPS.put(applications.getId(), applications);
        }
    }

    private ApplicationGroup(Short groupId, Color color) {
        this.groupId = groupId;
        this.color = color;
    }

    public Short getId() {
        return groupId;
    }

    /**
     * Returns the corresponding ApplicationGroup or ApplicationGroup.UNDEFINED if
     * there is no ApplicationGroup for the given groupId.
     * 
     * @param groupId
     * @return ApplicationGroup or ApplicationGroup.UNDEFINED
     */
    public static ApplicationGroup getGroup(Short groupId) {
        return APPLICATION_GROUPS.containsKey(groupId) ? APPLICATION_GROUPS.get(groupId) : ApplicationGroup.UNDEFINED;
    }

    public Color getColor() {
        return color;
    }
}
