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
package org.openhab.binding.velux.internal.things;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An enum that describes the various predefined Status Reply code values and their meanings.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public enum StatusReply {
    UNKNOWN_STATUS_REPLY(0x00),
    COMMAND_COMPLETED_OK(0x01),
    NO_CONTACT(0x02),
    MANUALLY_OPERATED(0x03),
    BLOCKED(0x04),
    WRONG_SYSTEMKEY(0x05),
    PRIORITY_LEVEL_LOCKED(0x06),
    REACHED_WRONG_POSITION(0x07),
    ERROR_DURING_EXECUTION(0x08),
    NO_EXECUTION(0x09),
    CALIBRATING(0x0A),
    POWER_CONSUMPTION_TOO_HIGH(0x0B),
    POWER_CONSUMPTION_TOO_LOW(0x0C),
    LOCK_POSITION_OPEN(0x0D),
    MOTION_TIME_TOO_LONG(0x0E),
    THERMAL_PROTECTION(0x0F),
    PRODUCT_NOT_OPERATIONAL(0x10),
    FILTER_MAINTENANCE_NEEDED(0x11),
    BATTERY_LEVEL(0x12),
    TARGET_MODIFIED(0x13),
    MODE_NOT_IMPLEMENTED(0x14),
    COMMAND_INCOMPATIBLE_TO_MOVEMENT(0x15),
    USER_ACTION(0x16),
    DEAD_BOLT_ERROR(0x17),
    AUTOMATIC_CYCLE_ENGAGED(0x18),
    WRONG_LOAD_CONNECTED(0x19),
    COLOUR_NOT_REACHABLE(0x1A),
    TARGET_NOT_REACHABLE(0x1B),
    BAD_INDEX_RECEIVED(0x1C),
    COMMAND_OVERRULED(0x1D),
    NODE_WAITING_FOR_POWER(0x1E),
    INFORMATION_CODE(0xDF),
    PARAMETER_LIMITED(0xE0),
    LIMITATION_BY_LOCAL_USER(0xE1),
    LIMITATION_BY_USER(0xE2),
    LIMITATION_BY_RAIN(0xE3),
    LIMITATION_BY_TIMER(0xE4),
    LIMITATION_BY_UPS(0xE6),
    LIMITATION_BY_UNKNOWN_DEVICE(0xE7),
    LIMITATION_BY_SAAC(0xEA),
    LIMITATION_BY_WIND(0xEB),
    LIMITATION_BY_MYSELF(0xEC),
    LIMITATION_BY_AUTOMATIC_CYCLE(0xED),
    LIMITATION_BY_EMERGENCY(0xEE);

    private final int code;

    private StatusReply(int code) {
        this.code = code;
    }

    /*
     * List of critical errors
     */
    private static final List<StatusReply> CRITICAL_ERRORS = List.of(BLOCKED, POWER_CONSUMPTION_TOO_HIGH,
            THERMAL_PROTECTION, LOCK_POSITION_OPEN, PRODUCT_NOT_OPERATIONAL, DEAD_BOLT_ERROR, FILTER_MAINTENANCE_NEEDED,
            BATTERY_LEVEL, NODE_WAITING_FOR_POWER);

    public int getCode() {
        return code;
    }

    private static final Map<Integer, StatusReply> LOOKUP = Stream.of(StatusReply.values())
            .collect(Collectors.toMap(StatusReply::getCode, Function.identity()));

    /**
     * Get the StatusReply value that corresponds to the given status code.
     *
     * @param statusReplyCode the status code value
     * @return the StatusReply value that corresponds to the status code
     */
    public static StatusReply fromCode(int statusReplyCode) {
        return LOOKUP.getOrDefault(statusReplyCode, UNKNOWN_STATUS_REPLY);
    }

    /**
     * Check if this Status Reply indicates an error.
     *
     * @return true if the status code is an error code.
     */
    public boolean isError() {
        return this != COMMAND_COMPLETED_OK;
    }

    /**
     * Check if this Status Reply indicates a critical error.
     *
     * @return true if the status code is a critical error code.
     */
    public boolean isCriticalError() {
        return isError() && CRITICAL_ERRORS.contains(this);
    }
}
