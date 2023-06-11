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
 * The {@link VenstarFanMode} represents the value of the fan mode returned
 * from the REST API.
 *
 * @author Matthew Davies - Initial contribution
 */
@NonNullByDefault
public enum VenstarFanMode {
    AUTO(0, "auto", "Auto"),
    ON(1, "on", "On");

    private int mode;
    private String name;
    private String friendlyName;

    VenstarFanMode(int mode, String name, String friendlyName) {
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

    public static VenstarFanMode fromInt(int mode) throws IllegalArgumentException {
        for (VenstarFanMode fm : values()) {
            if (fm.mode == mode) {
                return fm;
            }
        }

        throw (new IllegalArgumentException("Invalid fan mode " + mode));
    }
}
