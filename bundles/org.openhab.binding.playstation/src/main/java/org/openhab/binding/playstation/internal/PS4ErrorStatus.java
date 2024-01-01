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
package org.openhab.binding.playstation.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum of response error status.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
enum PS4ErrorStatus {
    STATUS_OK(0x00, "Status OK."),
    STATUS_UPDATE_APP(0x02, "Plugin needs to be updated."),
    STATUS_UPDATE_PS4(0x03, "PS4 needs to update."),
    STATUS_DO_LOGIN(0x06, "Log in on PS4."),
    STATUS_MAX_USERS(0x07, "Max users logged in on PS4."),
    STATUS_RESTART_APP(0x08, "Can not log in, restart plugin."),
    STATUS_COMMAND_NOT_GOOD(0x0b, "Command not good!"),
    STATUS_GAME_NOT_STARTED(0x0c, "Game not started!"), // Game/app not installed or other game running.
    STATUS_NOT_PAIRED(0x0e, "Not paired to PS4!"), // Not allowed?
    STATUS_OSK_NOT_OPENED(0x0f, "OSK not open right now."),
    STATUS_CLOSE_OTHER_APP(0x11, "Close the other app connected to PS4!"),
    STATUS_SOMEONE_ELSE_USING(0x12, "Someone else is using the PS4!"),
    STATUS_OSK_NOT_SUPPORTED(0x13, "Can't control OSK now!"),
    STATUS_MISSING_PAIRING_CODE(0x14, "Missing pairing-code!"), // ??
    STATUS_WRONG_USER_CREDENTIAL(0x15, "Wrong user-credential!"),
    STATUS_MISSING_PASS_CODE(0x16, "Missing pass-code!"),
    STATUS_WRONG_PAIRING_CODE(0x17, "Wrong pairing-code!"),
    STATUS_WRONG_PASS_CODE(0x18, "Wrong pass-code!"),
    STATUS_REGISTER_DEVICE_OVER(0x1a, "To many devices registered!"),
    STATUS_COULD_NOT_LOG_IN(0x1e, "Someone else is logging in now."),
    STATUS_CAN_NOT_PLAY_NOW(0x21, "You can not log in right now."),
    STATUS_ERROR_IN_COMMUNICATION(-1, "Error in comunication with PS4!");

    private static final Map<Integer, PS4ErrorStatus> TAG_MAP = Arrays.stream(PS4ErrorStatus.values())
            .collect(Collectors.toMap(status -> status.value, status -> status));

    public final int value;
    public final String message;

    private PS4ErrorStatus(int value, String message) {
        this.value = value;
        this.message = message;
    }

    /**
     * Get error status from value
     *
     * @param value the integer value of the status
     * @return error status or null if unknown
     */
    public static @Nullable PS4ErrorStatus valueOfTag(int value) {
        return TAG_MAP.get(value);
    }
}
