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
package org.openhab.binding.venstarthermostat.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VenstarSystemMode} represents the value of the system mode returned
 * from the REST API.
 *
 * @author William Welliver - Initial contribution
 */
@NonNullByDefault
public enum VenstarSystemMode {
    OFF(0, "off", "Off"),
    HEAT(1, "heat", "Heat"),
    COOL(2, "cool", "Cool"),
    AUTO(3, "auto", "Auto");

    private int mode;
    private String name;
    private String friendlyName;

    VenstarSystemMode(int mode, String name, String friendlyName) {
        this.mode = mode;
        this.name = name;
        this.friendlyName = friendlyName;
    }

    public int mode() {
        return mode;
    }

    public String modeName() {
        return name;
    }

    public String friendlyName() {
        return friendlyName;
    }

    public static VenstarSystemMode fromInt(int mode) throws IllegalArgumentException {
        for (VenstarSystemMode sm : values()) {
            if (sm.mode == mode) {
                return sm;
            }
        }

        throw (new IllegalArgumentException("Invalid system mode " + mode));
    }
}
