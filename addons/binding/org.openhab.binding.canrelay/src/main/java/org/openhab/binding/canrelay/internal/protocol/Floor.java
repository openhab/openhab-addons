/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

/**
 * Floor enumeration for can relays
 *
 * @author Lubos Housa - Initial Contribution
 */
public enum Floor {
    GROUND(0x0),
    FIRST(0x80); // 0b10000000 binary

    private final int value;

    private Floor(int value) {
        this.value = value;
    }

    /**
     * Retrieve the integer value associated with this floor (typically used to send CANBUS traffic across)
     *
     * @return integer value of this floor
     */
    public int getValue() {
        return value;
    }

    /**
     * Return floor based on the in-passed value or null if no such value exists
     *
     * @param value value to find floor for
     * @return enum constant if found, null otherwise
     */
    public static Floor fromValue(int value) {
        if (value == GROUND.value) {
            return GROUND;
        } else if (value == FIRST.value) {
            return FIRST;
        } else {
            return null;
        }
    }

    /**
     * Return floor of the in passed nodeID
     *
     * @param nodeID nodeID to fnd the floor for
     * @return floor of the nodeID
     */
    public static Floor getFloorFromNodeID(int nodeID) {
        // each nodeIDs 1st highest bit = floor, so effectively erase its lower bits and then try from value method
        // above
        return fromValue(nodeID & FIRST.value);
    }
}
