/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link VenstarFanState} represents the state of the fan returned
 * from the REST API.
 *
 * @author Matthew Davies - Initial contribution
 */
@NonNullByDefault
public enum VenstarFanState {
    OFF(0, "off", "Off"),
    ON(1, "on", "On");

    private int state;
    private String name;
    private String friendlyName;

    VenstarFanState(int state, String name, String friendlyName) {
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

    public static VenstarFanState fromInt(int state) throws IllegalArgumentException {
        for (VenstarFanState fs : values()) {
            if (fs.state == state) {
                return fs;
            }
        }

        throw (new IllegalArgumentException("Invalid fan state " + state));
    }
}
