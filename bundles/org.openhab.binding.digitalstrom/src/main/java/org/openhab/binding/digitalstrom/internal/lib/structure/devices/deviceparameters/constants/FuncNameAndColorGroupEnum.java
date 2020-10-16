/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
 * The {@link FuncNameAndColorGroupEnum} contains all digitalSTROM functional group names and links to their
 * {@link FunctionalColorGroupEnum}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 * @see <a href="http://developer.digitalstrom.org/Architecture/ds-basics.pdf">ds-basics.pdf
 *      "Table 1: digitalSTROM functional groups and their colors", page 9</a>
 */
public enum FuncNameAndColorGroupEnum {
    /*
     * | Number | Name | Color | Function |
     * --------------------------------------------------------------------------------------
     * | 1 | Lights | Yellow | Room lights |
     * | 2 | Blinds | Gray | Blinds or shades outside |
     * | 12 | Curtains | Gray | Curtains and blinds inside |
     * | 3 | Heating | Blue | Heating |
     * | 9 | Cooling | Blue | Cooling |
     * | 10 | Ventilation | Blue | Ventilation |
     * | 11 | Window | Blue | Window |
     * | 48 | Temperature Control | Blue | Single room temperature control |
     * | 4 | Audio | Cyan | Playing music or radio |
     * | 5 | Video | Magenta | TV, Video |
     * | 8 | Joker | Black | Configurable behaviour |
     * | n/a | Single Device | White | Various, individual per device |
     * | n/a | Security | Red | Security related functions, Alarms |
     * | n/a | Access | Green | Access related functions, door bell |
     *
     */
    LIGHTS((short) 1, FunctionalColorGroupEnum.getColorGroup((short) 1)),
    BLINDS((short) 2, FunctionalColorGroupEnum.getColorGroup((short) 2)),
    CURTAINS((short) 12, FunctionalColorGroupEnum.getColorGroup((short) 12)),
    HEATING((short) 3, FunctionalColorGroupEnum.getColorGroup((short) 3)),
    COOLING((short) 9, FunctionalColorGroupEnum.getColorGroup((short) 9)),
    VENTILATION((short) 10, FunctionalColorGroupEnum.getColorGroup((short) 10)),
    WINDOW((short) 11, FunctionalColorGroupEnum.getColorGroup((short) 11)),
    TEMPERATION_CONTROL((short) 48, FunctionalColorGroupEnum.getColorGroup((short) 48)),
    AUDIO((short) 4, FunctionalColorGroupEnum.getColorGroup((short) 4)),
    VIDEO((short) 5, FunctionalColorGroupEnum.getColorGroup((short) 5)),
    JOKER((short) 8, FunctionalColorGroupEnum.getColorGroup((short) 8)),
    SINGLE_DEVICE((short) -1, FunctionalColorGroupEnum.getColorGroup((short) -1)),
    SECURITY((short) -2, FunctionalColorGroupEnum.getColorGroup((short) -2)),
    ACCESS((short) -3, FunctionalColorGroupEnum.getColorGroup((short) -3));

    private final short colorGroup;
    private final FunctionalColorGroupEnum color;

    static final Map<Short, FuncNameAndColorGroupEnum> COLOR_GROUPS = new HashMap<>();

    static {
        for (FuncNameAndColorGroupEnum colorGroup : FuncNameAndColorGroupEnum.values()) {
            COLOR_GROUPS.put(colorGroup.getFunctionalColorGroup(), colorGroup);
        }
    }

    /**
     * Returns true, if contains the given output mode id in DigitalSTROM, otherwise false.
     *
     * @param functionalNameGroupID to be checked
     * @return true, if contains
     */
    public static boolean containsColorGroup(Short functionalNameGroupID) {
        return COLOR_GROUPS.keySet().contains(functionalNameGroupID);
    }

    /**
     * Returns the {@link FuncNameAndColorGroupEnum} of the given functional name group id.
     *
     * @param functionalNameGroupID of the {@link FuncNameAndColorGroupEnum}
     * @return FunctionalNameAndColorGroupEnum
     */
    public static FuncNameAndColorGroupEnum getMode(Short functionalNameGroupID) {
        return COLOR_GROUPS.get(functionalNameGroupID);
    }

    private FuncNameAndColorGroupEnum(short functionalColorGroupID, FunctionalColorGroupEnum functionalColorGroup) {
        this.colorGroup = functionalColorGroupID;
        this.color = functionalColorGroup;
    }

    /**
     * Returns the functional name group id form this Object.
     *
     * @return functional name group id
     */
    public Short getFunctionalColorGroup() {
        return colorGroup;
    }

    /**
     * Returns the {@link FunctionalColorGroupEnum} form this Object.
     *
     * @return FunctionalColorGroupEnum
     */
    public FunctionalColorGroupEnum getFunctionalColor() {
        return color;
    }
}
