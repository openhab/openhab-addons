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
package org.openhab.binding.heos.internal.json.dto;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enum for the different documented error for HEOS responses
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public enum HeosErrorCode {
    UNRECOGNIZED_COMMAND(1, "Unrecognized Command"),
    INVALID_ID(2, "Invalid ID"),
    WRONG_NUMBER_OF_COMMAND_ARGUMENTS(3, "Wrong Number of Command Arguments"),
    REQUESTED_DATA_NOT_AVAILABLE(4, "Requested data not available"),
    RESOURCE_CURRENTLY_NOT_AVAILABLE(5, "Resource currently not available"),
    INVALID_CREDENTIALS(6, "Invalid Credentials"),
    COMMAND_COULD_NOT_BE_EXECUTED(7, "Command Could Not Be Executed"),
    USER_NOT_LOGGED_IN(8, "User not logged In"),
    PARAMETER_OUT_OF_RANGE(9, "Parameter out of range"),
    USER_NOT_FOUND(10, "User not found"),
    INTERNAL_ERROR(11, "Internal Error"),
    SYSTEM_ERROR(12, "System Error"),
    PROCESSING_PREVIOUS_COMMAND(13, "Processing Previous Command"),
    MEDIA_CANT_BE_PLAYED(14, "Media can't be played"),
    OPTION_NO_SUPPORTED(15, "Option no supported"),
    TOO_MANY_COMMANDS_IN_MESSAGE_QUEUE_TO_PROCESS(16, "Too many commands in message queue to process"),
    REACHED_SKIP_LIMIT(17, "Reached skip limit");

    private final int errorNumber;
    private final String msg;

    HeosErrorCode(int errorNumber, String msg) {
        this.errorNumber = errorNumber;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return String.format("#%d: %s", errorNumber, msg);
    }

    public static HeosErrorCode of(long errorNumber) {
        return Stream.of(values()).filter(v -> errorNumber == v.errorNumber).findAny()
                .orElseThrow(() -> new IllegalArgumentException("An unknown error " + errorNumber + " occurred"));
    }
}
