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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VenstarSystemState} represents the value of the system state
 * returneda from the REST API.
 *
 * @author William Welliver - Initial contribution
 */
@NonNullByDefault
public enum VenstarSystemState {
    IDLE(0, "idle", "Idle"),
    HEATING(1, "heating", "Heating"),
    COOLING(2, "cooling", "Cooling"),
    LOCKOUT(3, "lockout", "Lockout"),
    ERROR(4, "error", "Error");

    private int state;
    private String name;
    private String friendlyName;

    VenstarSystemState(int state, String name, String friendlyName) {
        this.state = state;
        this.name = name;
        this.friendlyName = friendlyName;
    }

    public int state() {
        return state;
    }

    public String stateName() {
        return name;
    }

    public String friendlyName() {
        return friendlyName;
    }

    public static VenstarSystemState fromInt(int state) throws IllegalArgumentException {
        for (VenstarSystemState ss : values()) {
            if (ss.state == state) {
                return ss;
            }
        }

        throw (new IllegalArgumentException("Invalid system state " + state));
    }
}
