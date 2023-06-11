/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Enum of the possible commands.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
enum PS4Command {
    UNKNOWN1_REQ(0x02),
    BUFFER_SIZE_RSP(0x03),
    BYEBYE_REQ(0x04),
    LOGIN_RSP(0x07),
    SCREEN_SHOT_REQ(0x08),
    SCREEN_SHOT_RSP(0x09),
    APP_START_REQ(0x0a),
    APP_START_RSP(0x0b),
    OSK_START_REQ(0x0c),
    OSK_START_RSP(0x0d),
    OSK_CHANGE_STRING_REQ(0x0e),
    OSK_CONTROL_REQ(0x10),
    SERVER_STATUS_RSP(0x12),
    STATUS_REQ(0x14),
    HTTPD_STATUS_RSP(0x16),
    SCREEN_STATUS_RSP(0x18),
    STANDBY_REQ(0x1a),
    STANDBY_RSP(0x1b),
    REMOTE_CONTROL_REQ(0x1c),
    LOGIN_REQ(0x1e),
    HANDSHAKE_REQ(0x20),
    LOGOUT_REQ(0x22),
    LOGOUT_RSP(0x23),
    APP_START2_REQ(0x24),
    APP_START2_RSP(0x25),
    CLIENT_IDENTITY_REQ(0x26),
    COMMENT_VIEWER_START_REQ(0x2a),
    COMMENT_VIEWER_START_RESULT(0x2b),
    COMMENT_VIEWER_NEW_COMMENT(0x2c),
    COMMENT_VIEWER_NEW_COMMENT2(0x2e),
    COMMENT_VIEWER_EVENT(0x30),
    COMMENT_VIEWER_SEND(0x32),
    HELLO_REQ(0x6f636370);

    private static final Map<Integer, PS4Command> TAG_MAP = Arrays.stream(PS4Command.values())
            .collect(Collectors.toMap(command -> command.value, command -> command));

    public final int value;

    private PS4Command(int value) {
        this.value = value;
    }

    /**
     * Get command from value
     *
     * @param tag the tag string
     * @return accessoryType or null if not found
     */
    public static @Nullable PS4Command valueOfTag(int value) {
        return TAG_MAP.get(value);
    }
}
