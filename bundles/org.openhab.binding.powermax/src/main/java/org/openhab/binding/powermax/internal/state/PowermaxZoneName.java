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
package org.openhab.binding.powermax.internal.state;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * All panel zone names
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum PowermaxZoneName {

    ZONE_0(0, "Attic"),
    ZONE_1(1, "Back door"),
    ZONE_2(2, "Basement"),
    ZONE_3(3, "Bathroom"),
    ZONE_4(4, "Bedroom"),
    ZONE_5(5, "Child room"),
    ZONE_6(6, "Closet"),
    ZONE_7(7, "Den"),
    ZONE_8(8, "Dining room"),
    ZONE_9(9, "Downstairs"),
    ZONE_10(10, "Emergency"),
    ZONE_11(11, "Fire"),
    ZONE_12(12, "Front door"),
    ZONE_13(13, "Garage"),
    ZONE_14(14, "Garage door"),
    ZONE_15(15, "Guest room"),
    ZONE_16(16, "Hall"),
    ZONE_17(17, "Kitchen"),
    ZONE_18(18, "Laundry room"),
    ZONE_19(19, "Living room"),
    ZONE_20(20, "Master bathroom"),
    ZONE_21(21, "Master bedroom"),
    ZONE_22(22, "Office"),
    ZONE_23(23, "Upstairs"),
    ZONE_24(24, "Utility room"),
    ZONE_25(25, "Yard"),
    ZONE_26(26, "Custom 1"),
    ZONE_27(27, "Custom 2"),
    ZONE_28(28, "Custom 3"),
    ZONE_29(29, "Custom 4"),
    ZONE_30(30, "Custom 5"),
    ZONE_31(31, "Not Installed");

    private final int id;
    private String name;

    private PowermaxZoneName(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the zone id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the zone name
     */
    public String getName() {
        return name;
    }

    /**
     * Update the zone name
     *
     * @param name the new zone name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the ENUM value from its id
     *
     * @param id the zone id
     *
     * @return the corresponding ENUM value
     *
     * @throws IllegalArgumentException if no ENUM value corresponds to this id
     */
    public static PowermaxZoneName fromId(int id) throws IllegalArgumentException {
        for (PowermaxZoneName zone : PowermaxZoneName.values()) {
            if (zone.getId() == id) {
                return zone;
            }
        }

        throw new IllegalArgumentException("Invalid id: " + id);
    }
}
