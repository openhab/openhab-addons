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
package org.openhab.binding.ecovacs.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public enum SpotAreaType {
    LIVING_ROOM(1),
    DINING_ROOM(2),
    BEDROOM(3),
    OFFICE(4),
    KITCHEN(5),
    BATHROOM(6),
    LAUNDRY_ROOM(7),
    LOUNGE(8),
    STORAGE_ROOM(9),
    CHILDS_ROOM(10),
    SUN_ROOM(11),
    CORRIDOR(12),
    BALCONY(13),
    GYM(14);

    private final int type;

    private SpotAreaType(int type) {
        this.type = type;
    }

    public SpotAreaType fromApiResponse(String response) throws NumberFormatException, IllegalArgumentException {
        int id = Integer.parseInt(response);
        for (SpotAreaType t : values()) {
            if (t.type == id) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown spot area type " + response);
    }
}
