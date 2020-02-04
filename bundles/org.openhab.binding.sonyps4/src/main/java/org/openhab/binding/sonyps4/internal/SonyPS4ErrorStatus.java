/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sonyps4.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum of response error status.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
enum SonyPS4ErrorStatus {
    STATUS_OK(0x00, "Status OK."),
    STATUS_OSK_OPEN(0x04, "On screen keyboard open."),
    STATUS_COMMAND_NOT_GOOD(0x0b, "Command not good!"),
    STATUS_GAME_NOT_INSTALLED(0x0c, "Game not installed!"),
    STATUS_NOT_PAIRED(0x0e, "Not paired to PS4!"), // Not allowed?
    STATUS_LOGIN_PACKET_WRONG(0x11, "Login packet wrong!"),
    STATUS_MISSING_PAIRING_CODE(0x14, "Missing pairing-code!"), // ??
    STATUS_WRONG_USER_CREDENTIAL(0x15, "Wrong user-credential!"),
    STATUS_MISSING_PIN_CODE(0x16, "Missing pin-code!"),
    STATUS_WRONG_PAIRING_CODE(0x17, "Wrong pairing-code!"),
    STATUS_WRONG_PIN_CODE(0x18, "Wrong pin-code!"),
    STATUS_COULD_NOT_LOG_IN(0x1e, "Someone else is logging in now."),
    STATUS_ERROR_IN_COMMUNICATION(-1, "Error in comunication with PS4!");

    private static final Map<Integer, SonyPS4ErrorStatus> TAG_MAP = new HashMap<>();

    static {
        for (SonyPS4ErrorStatus cmd : SonyPS4ErrorStatus.values()) {
            TAG_MAP.put(cmd.value, cmd);
        }
    }

    public final int value;
    public final String message;

    private SonyPS4ErrorStatus(int value, String message) {
        this.value = value;
        this.message = message;
    }

    /**
     * get command from int
     *
     * @param tag the tag string
     * @return accessoryType or null if not found
     */
    public static @Nullable SonyPS4ErrorStatus valueOfTag(int value) {
        return TAG_MAP.get(value);
    }
}
