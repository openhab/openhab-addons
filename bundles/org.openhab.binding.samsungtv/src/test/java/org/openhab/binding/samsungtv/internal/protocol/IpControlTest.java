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
package org.openhab.binding.samsungtv.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Tests for parsing IP Control replies, using replies recorded from 2021 and 2022 Frame TV's
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class IpControlTest {

    @Test
    public void parsesPowerResult() throws IpControlException {
        JsonObject result = IpControl.parseResult("powerControl",
                "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"result\":{\"power\":\"powerOff\"}}");
        assertEquals("powerOff", result.get("power").getAsString());
    }

    @Test
    public void parsesAccessToken() throws IpControlException {
        JsonObject result = IpControl.parseResult("createAccessToken",
                "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"result\":{\"AccessToken\":\"abc123\"}}");
        assertEquals("abc123", result.get("AccessToken").getAsString());
    }

    @Test
    public void redactsAccessToken() {
        String redacted = IpControl
                .redactToken("{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"result\":{\"AccessToken\": \"abc123\"}}");
        assertFalse(redacted.contains("abc123"));
        assertTrue(redacted.contains("\"AccessToken\": \"***\""));
    }

    @Test
    public void missingResultIsEmpty() throws IpControlException {
        assertEquals(0, IpControl.parseResult("powerControl", "{\"jsonrpc\":\"2.0\",\"id\":\"1\"}").size());
    }

    @Test
    public void methodNotFoundIsNotTokenError() {
        IpControlException e = assertThrows(IpControlException.class, () -> IpControl.parseResult("inputSourceControl",
                "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"error\":{\"code\":-32601,\"message\":\"Method not found\"}}"));
        assertFalse(e.isTokenRejected());
        assertTrue(String.valueOf(e.getMessage()).contains("-32601"));
    }

    @Test
    public void unauthorizedIsTokenError() {
        IpControlException e = assertThrows(IpControlException.class, () -> IpControl.parseResult("powerControl",
                "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"error\":{\"code\":-32010,\"message\":\"Unauthorized\"}}"));
        assertTrue(e.isTokenRejected());
    }

    @Test
    public void topLevelErrorIsTokenError() {
        IpControlException e = assertThrows(IpControlException.class,
                () -> IpControl.parseResult("powerControl", "{\"code\":-32700,\"message\":\"Parse error\"}"));
        assertTrue(e.isTokenRejected());
    }

    @Test
    public void errorWithoutObjectIsError() {
        IpControlException e = assertThrows(IpControlException.class,
                () -> IpControl.parseResult("powerControl", "{\"error\":\"busy\"}"));
        assertFalse(e.isTokenRejected());
    }

    @Test
    public void invalidJsonIsError() {
        assertThrows(IpControlException.class, () -> IpControl.parseResult("powerControl", "<html>"));
        assertThrows(IpControlException.class, () -> IpControl.parseResult("powerControl", "[1,2]"));
        assertThrows(IpControlException.class, () -> IpControl.parseResult("powerControl", ""));
    }
}
