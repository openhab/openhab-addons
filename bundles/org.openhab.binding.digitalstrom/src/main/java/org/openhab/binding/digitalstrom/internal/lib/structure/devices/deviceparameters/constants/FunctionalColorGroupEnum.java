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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link FunctionalColorGroupEnum} contains all digitalSTROM functional color groups.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 * @see <a href="http://developer.digitalstrom.org/Architecture/ds-basics.pdf">ds-basics.pdf,
 *      "Table 1: digitalSTROM functional groups and their colors", page 9 [04.09.2015]</a>
 */
public enum FunctionalColorGroupEnum {
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
    YELLOW(Arrays.asList((short) 1)),
    GREY(Arrays.asList((short) 2, (short) 12)),
    BLUE(Arrays.asList((short) 3, (short) 9, (short) 10, (short) 11, (short) 48)),
    CYAN(Arrays.asList((short) 4)),
    MAGENTA(Arrays.asList((short) 5)),
    BLACK(Arrays.asList((short) 8)),
    WHITE(Arrays.asList((short) -1)),
    RED(Arrays.asList((short) -2)),
    GREEN(Arrays.asList((short) -3));

    private final List<Short> colorGroup;

    static final Map<Short, FunctionalColorGroupEnum> COLOR_GROUPS = new HashMap<>();

    static {
        for (FunctionalColorGroupEnum colorGroup : FunctionalColorGroupEnum.values()) {
            for (Short colorGroupID : colorGroup.getFunctionalColorGroup()) {
                COLOR_GROUPS.put(colorGroupID, colorGroup);
            }
        }
    }

    /**
     * Returns true, if contains the given functional color group id in digitalSTROM exits, otherwise false.
     *
     * @param functionalColorGroupID to be checked
     * @return true, if contains
     */
    public static boolean containsColorGroup(Short functionalColorGroupID) {
        return COLOR_GROUPS.keySet().contains(functionalColorGroupID);
    }

    /**
     * Returns the {@link FunctionalColorGroupEnum} of the given color id.
     *
     * @param functionalColorGroupID of the {@link FunctionalColorGroupEnum}
     * @return {@link FunctionalColorGroupEnum} of the id
     */
    public static FunctionalColorGroupEnum getColorGroup(Short functionalColorGroupID) {
        return COLOR_GROUPS.get(functionalColorGroupID);
    }

    private FunctionalColorGroupEnum(List<Short> functionalColorGroupID) {
        this.colorGroup = functionalColorGroupID;
    }

    /**
     * Returns the functional color group id's as {@link List} of this {@link FunctionalColorGroupEnum}.
     *
     * @return functional color group id's
     */
    public List<Short> getFunctionalColorGroup() {
        return colorGroup;
    }
}
