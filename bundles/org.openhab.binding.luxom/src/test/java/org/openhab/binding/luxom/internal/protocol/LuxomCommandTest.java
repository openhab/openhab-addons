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
package org.openhab.binding.luxom.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
class LuxomCommandTest {

    @Test
    void parsePulseCommand() {
        LuxomCommand command = new LuxomCommand("*P,0,1,04");

        assertEquals(LuxomAction.PING, command.getAction());
        assertEquals("1,04", command.getAddress());
        assertNull(command.getData());
    }

    @Test
    void parsePasswordRequest() {
        LuxomCommand command = new LuxomCommand(LuxomAction.PASSWORD_REQUEST.getCommand());

        assertEquals(LuxomAction.PASSWORD_REQUEST, command.getAction());
        assertNull(command.getData());
    }

    @Test
    void parseClearCommand() {
        LuxomCommand command = new LuxomCommand("*C,0,1,04");

        assertEquals(LuxomAction.CLEAR, command.getAction());
        assertEquals("1,04", command.getAddress());
        assertNull(command.getData());
    }

    @Test
    void parseClearResponse() {
        LuxomCommand command = new LuxomCommand("@1*C,0,1,04");

        assertEquals(LuxomAction.CLEAR_RESPONSE, command.getAction());
        assertEquals("1,04", command.getAddress());
        assertNull(command.getData());
    }

    @Test
    void parseClearResponse2() {
        LuxomCommand command = new LuxomCommand("@1*C,0,1,04");

        assertEquals(LuxomAction.CLEAR_RESPONSE, command.getAction());
        assertEquals("1,04", command.getAddress());
        assertNull(command.getData());
    }

    @Test
    void parseSetCommand() {
        LuxomCommand command = new LuxomCommand("*S,0,1,04");

        assertEquals(LuxomAction.SET, command.getAction());
        assertEquals("1,04", command.getAddress());
        assertNull(command.getData());
    }

    @Test
    void parseSetResponse() {
        LuxomCommand command = new LuxomCommand("@1*S,0,1,04");

        assertEquals(LuxomAction.SET_RESPONSE, command.getAction());
        assertEquals("1,04", command.getAddress());
        assertNull(command.getData());
    }

    @Test
    void parseDimCommand() {
        LuxomCommand command = new LuxomCommand("*A,0,1,04");

        assertEquals(LuxomAction.DATA, command.getAction());
        assertEquals("1,04", command.getAddress());
        assertNull(command.getData());
    }

    @Test
    void parseDataCommand() {
        LuxomCommand command = new LuxomCommand("*Z,048");

        assertEquals(LuxomAction.DATA_BYTE, command.getAction());
        assertEquals("048", command.getData());
        assertNull(command.getAddress());
    }

    @Test
    void parseDataResponseCommand() {
        LuxomCommand command = new LuxomCommand("@1*Z,048");

        assertEquals(LuxomAction.DATA_BYTE_RESPONSE, command.getAction());
        assertEquals("048", command.getData());
        assertNull(command.getAddress());
    }
}
