/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

/**
 * List of Errors
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum StatusType {

    UNKNOWN(0, "Unknown"),
    INITIATING(1, "Initiating"),
    SLEEPING(2, "Sleeping"),
    IDLE(3, "Idle"),
    UNKNOWN4(4, "Unknown Status 4"),
    CLEANING(5, "Cleaning"),
    RETURNING(6, "Returning Dock"),
    UNKNOWN7(7, "Unknown Status 7"),
    CHARGING(8, "Charging"),
    CHARGING_ERROR(9, "Charging Error"),
    PAUSED(10, "Paused"),
    SPOTCLEAN(11, "Spot cleaning"),
    ERROR(12, "In Error"),
    SHUTTING_DOWN(13, "Shutting Down"),
    UPDATING(14, "Updating"),
    DOCKING(15, "Docking"),
    FULL(100, "Full");

    private final int id;
    private final String description;

    StatusType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public static StatusType getType(int value) {
        for (StatusType st : StatusType.values()) {
            if (st.getId() == value) {
                return st;
            }
        }
        return UNKNOWN;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Status " + Integer.toString(id) + ": " + description;
    }
}
