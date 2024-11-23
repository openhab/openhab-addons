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
package org.openhab.binding.venstarthermostat.internal.dto;

/**
 * The {@link VenstarSystemMode} represents the value of the system mode returned
 * from the REST API.
 *
 * @author Matthew Davies - Initial contribution
 */
public enum VenstarAwayMode {
    HOME(0, "home", "Home"),
    AWAY(1, "away", "Away");

    private int mode;
    private String name;
    private String friendlyName;

    VenstarAwayMode(int mode, String name, String friendlyName) {
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

    public static VenstarAwayMode fromInt(int mode) throws IllegalArgumentException {
        for (VenstarAwayMode am : values()) {
            if (am.mode == mode) {
                return am;
            }
        }

        throw (new IllegalArgumentException("Invalid away mode " + mode));
    }
}
