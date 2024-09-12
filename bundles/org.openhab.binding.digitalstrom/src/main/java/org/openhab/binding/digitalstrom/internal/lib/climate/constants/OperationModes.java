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
package org.openhab.binding.digitalstrom.internal.lib.climate.constants;

/**
 * The {@link OperationModes} contains all digitalSTROM heating operation states.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum OperationModes {

    OFF((short) 0, "Off"),
    COMFORT((short) 1, "Comfort"),
    ECONEMY((short) 2, "Econemy"),
    NOT_USED((short) 3, "NotUsed"),
    NIGHT((short) 4, "Night"),
    HOLLYDAY((short) 5, "Holliday"),
    COOLING((short) 6, "Cooling"),
    COOLING_OFF((short) 7, "CoolingOff");

    private final Short id;
    private final String key;

    private static final OperationModes[] OPERATION_MODES = new OperationModes[OperationModes.values().length];

    static {
        for (OperationModes operationMode : OperationModes.values()) {
            OPERATION_MODES[operationMode.id] = operationMode;
        }
    }

    /**
     * Returns the {@link OperationModes} of the given operation mode id.
     *
     * @param id of the operation mode
     * @return operation mode
     */
    public static OperationModes getOperationMode(short id) {
        try {
            return OPERATION_MODES[id];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private OperationModes(short id, String key) {
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
}
