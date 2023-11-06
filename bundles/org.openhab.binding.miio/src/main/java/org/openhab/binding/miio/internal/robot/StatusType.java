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
package org.openhab.binding.miio.internal.robot;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * List of available states
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum StatusType {

    UNKNOWN(0, "Unknown"),
    INITIATING(1, "Initiating"),
    SLEEPING(2, "Sleeping"),
    IDLE(3, "Idle"),
    REMOTE(4, "Remote Control"),
    CLEANING(5, "Cleaning"),
    RETURNING(6, "Returning Dock"),
    MANUAL(7, "Manual Mode"),
    CHARGING(8, "Charging"),
    CHARGING_ERROR(9, "Charging Error"),
    PAUSED(10, "Paused"),
    SPOTCLEAN(11, "Spot cleaning"),
    ERROR(12, "In Error"),
    SHUTTING_DOWN(13, "Shutting Down"),
    UPDATING(14, "Updating"),
    DOCKING(15, "Docking"),
    GOTO(16, "Go To"),
    ZONE(17, "Zone Clean"),
    ROOM(18, "Room Clean"),
    RETURNING_HOME(22, "Returning Home"),
    FULL(100, "Full"),
    OFFLINE(101, "Device Offline");

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
