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
 * The {@link VenstarScheduleMode} represents the value of the schedule mode returned
 * from the REST API.
 *
 * @author Matthew Davies - Initial contribution
 */
@NonNullByDefault
public enum VenstarScheduleMode {
    DISABLED(0, "disabled", "Disabled"),
    ENABLED(1, "enabled", "Enabled");

    private int mode;
    private String name;
    private String friendlyName;

    VenstarScheduleMode(int mode, String name, String friendlyName) {
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

    public static VenstarScheduleMode fromInt(int mode) throws IllegalArgumentException {
        for (VenstarScheduleMode sm : values()) {
            if (sm.mode == mode) {
                return sm;
            }
        }

        throw (new IllegalArgumentException("Invalid schedule mode " + mode));
    }
}
