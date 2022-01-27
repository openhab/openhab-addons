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
package org.openhab.binding.digitalstrom.internal.lib.climate.constants;

/**
 * The {@link ControlModes} contains all digitalSTROM heating control modes.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ControlModes {

    OFF((short) 0, "off"),
    PID_CONTROL((short) 1, "pid-control"),
    ZONE_FOLLOWER((short) 2, "zone-follower"),
    FIXED_VALUE((short) 3, "fixed-value"),
    MANUAL((short) 4, "manual");

    private final Short id;
    private final String key;

    private static final ControlModes[] CONTROL_MODES = new ControlModes[ControlModes.values().length];

    static {
        for (ControlModes controlMode : ControlModes.values()) {
            CONTROL_MODES[controlMode.id] = controlMode;
        }
    }

    private ControlModes(short id, String key) {
        this.id = id;
        this.key = key;
    }

    /**
     * Returns the key of the operation mode.
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the ID of the operation mode.
     *
     * @return ID
     */
    public Short getID() {
        return id;
    }

    /**
     * Returns the {@link ControlModes} of the given control mode id.
     *
     * @param id of the control mode
     * @return control mode
     */
    public static ControlModes getControlMode(short id) {
        try {
            return CONTROL_MODES[id];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
