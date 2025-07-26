/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * List of available states
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Paul Smedley - Updated States based on python-roborock
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
    EMPTYING_BIN(22, "Emptying the bin"),
    WASHING_MOP(23, "Washing the mop"), // on a46
    WASHING_MOP2(25, "Washing the mop"),
    GOING_WASH_MOP(26, "Going to wash the mop"),
    IN_CALL(28, "In call"),
    MAPPING(29, "Mapping"),
    EGG_ATTACK(30, "Egg attack"),
    PATROL(32, "Patrol"),
    ATTACH_MOP(33, "Attaching the mop"),
    DETACH_MOP(34, "Detaching the mop"),
    FULL(100, "Full"),
    OFFLINE(101, "Device Offline"),
    LOCKED(103, "Locked"),
    AIR_DRYING_STOPPED(202, "Air drying stopping"),
    STATUS_MOPPING(6301, "Robot status mopping"),
    CLEAN_MOP_CLEANING(6302, "Clean mop cleaning"),
    CLEAN_MOP_MOPPING(6303, "Clean mop mopping"),
    SEGMENT_MOPPING(6304, "Segment mopping"),
    SEGMENT_CLEAN_MOP_CLEANING(6305, "Segment clean mop cleaning"),
    SEGMENT_CLEAN_MOP_MOPPING(6306, "Segment clean mop mopping"),
    ZONED_MOPPING(6307, "Zoned mopping"),
    ZONED_CLEAN_MOP_CLEANING(6308, "Zoned clean mop cleaning"),
    ZONED_CLEAN_MOP_MOPPING(6309, "Zoned clean mop mopping"),
    BACK_TO_DOCK_WASHING_DUSTER(6310, "Back to dock washing duster");

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
