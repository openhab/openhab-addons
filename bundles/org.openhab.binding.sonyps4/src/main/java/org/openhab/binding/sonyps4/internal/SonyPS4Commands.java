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

/**
 * Enum of the possible commands.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
enum SonyPS4Commands {
    HELLO_REQ(0x6f636370),
    BYEBYE_REQ(0x04),
    LOGIN_RSP(0x07),
    APP_START_REQ(0x0a),
    APP_START_RSP(0x0b),
    OSK_START_REQ(0x0c),
    OSK_CHANGE_STRING_REQ(0x0e),
    OSK_CONTROL_REQ(0x10),
    SERVER_STATUS_RSP(0x12),
    STATUS_REQ(0x14),
    STANDBY_REQ(0x1a),
    STANDBY_RSP(0x1b),
    REMOTE_CONTROL_REQ(0x1c),
    LOGIN_REQ(0x1e),
    HANDSHAKE_REQ(0x20),
    LOGOUT_REQ(0x22),
    LOGOUT_RSP(0x23),
    APP_START2_REQ(0x24),
    APP_START2_RSP(0x25),
    COMMENT_VIEWER_START_RESULT(0x2b),
    COMMENT_VIEWER_NEW_COMMENT(0x2c),
    COMMENT_VIEWER_NEW_COMMENT2(0x2e),
    COMMENT_VIEWER_EVENT(0x30);

    public final int value;

    private SonyPS4Commands(int value) {
        this.value = value;
    }

}
