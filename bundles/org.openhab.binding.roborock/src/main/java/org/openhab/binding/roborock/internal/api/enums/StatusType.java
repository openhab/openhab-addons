/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

    /**
     * Tells whether the robot physically sits on its charging dock while in this state.
     * <p>
     * Included are the states that report the robot on the dock without further assumptions:
     * charging, charging error and completed charge ({@link #FULL}, a full battery rather than a
     * full bin), plus emptying the bin and washing the mop, both of which the dock performs on a
     * robot parked in it. States in which the robot is on its way there ({@link #RETURNING},
     * {@link #DOCKING}, {@link #GOING_WASH_MOP}, {@link #BACK_TO_DOCK_WASHING_DUSTER}) are
     * excluded - its position is not the dock's yet.
     * <p>
     * {@link #ATTACH_MOP}, {@link #DETACH_MOP} and {@link #AIR_DRYING_STOPPED} are dock chores on
     * the models that report them, but they are model-specific and this binding has no capture of
     * them, so they are left out rather than assumed. The two directions are not symmetric: a state
     * wrongly counted as docked publishes a wrong room that then stays, because no map is polled
     * while the robot is docked, whereas a docked state left out only keeps the behaviour this
     * channel had before.
     *
     * @return {@code true} if the robot is docked in this state
     */
    public boolean isAtDock() {
        return switch (this) {
            case CHARGING, CHARGING_ERROR, FULL, EMPTYING_BIN, WASHING_MOP, WASHING_MOP2 -> true;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return "Status " + Integer.toString(id) + ": " + description;
    }
}
