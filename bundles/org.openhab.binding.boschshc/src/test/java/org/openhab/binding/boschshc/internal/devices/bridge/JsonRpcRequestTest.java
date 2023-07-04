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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JsonRpcRequest}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class JsonRpcRequestTest {

    private @NonNullByDefault({}) JsonRpcRequest fixture;

    @BeforeEach
    protected void setUp() throws Exception {
        fixture = new JsonRpcRequest("2.0", "RE/longPoll", new String[] { "subscriptionId", "20" });
    }

    @Test
    void testConstructor() {
        assertEquals("2.0", fixture.getJsonrpc());
        assertEquals("RE/longPoll", fixture.getMethod());
        assertArrayEquals(new String[] { "subscriptionId", "20" }, fixture.getParams());
    }

    @Test
    void testNoArgConstructor() {
        fixture = new JsonRpcRequest();
        assertEquals("", fixture.getJsonrpc());
        assertEquals("", fixture.getMethod());
        assertArrayEquals(new String[0], fixture.getParams());
    }

    @Test
    void testSetJsonrpc() {
        fixture.setJsonrpc("test");
        assertEquals("test", fixture.getJsonrpc());
    }

    @Test
    void testSetMethod() {
        fixture.setMethod("RE/subscribe");
        assertEquals("RE/subscribe", fixture.getMethod());
    }

    @Test
    void testSetParams() {
        fixture.setParams(new String[] { "com/bosch/sh/remote/*", null });
        assertArrayEquals(new String[] { "com/bosch/sh/remote/*", null }, fixture.getParams());
    }
}
