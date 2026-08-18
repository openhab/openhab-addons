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
package org.openhab.binding.netatmo.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * @author Martin Littkovsky - Initial contribution
 */
public class ApiBridgeHandlerTest {

    @Test
    public void testRawErrorCodeAsNumber() {
        String body = "{\"error\":{\"code\":50,\"message\":\"Service temporarily unavailable\"}}";

        assertEquals("50", ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeAsString() {
        String body = "{\"error\":{\"code\":\"50\",\"message\":\"Service temporarily unavailable\"}}";

        assertEquals("50", ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeMissingReturnsNull() {
        String body = "{\"error\":{\"message\":\"Service temporarily unavailable\"}}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithEmptyErrorObjectReturnsNull() {
        String body = "{\"error\":{}}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithEmptyBodyReturnsNull() {
        // {} is the one shape of these edge cases that is actually live-reachable: deserializer.deserialize()
        // happily turns it into an ApiError with all defaults (code == UNKNOWN), so this body does reach
        // extractRawErrorCode() in the real call path.
        String body = "{}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithNullErrorReturnsNull() {
        // {"error": null} - "error" present but not an object; deserializing this into ApiError itself NPEs
        // elsewhere (a separate, pre-existing issue, see the PR's Notes for review) - extractRawErrorCode() must
        // not add a second failure mode of its own for the same shape.
        String body = "{\"error\":null}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithNonObjectErrorReturnsNull() {
        String body = "{\"error\":\"boom\"}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithNullCodeReturnsNull() {
        String body = "{\"error\":{\"code\":null}}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithObjectCodeReturnsNull() {
        String body = "{\"error\":{\"code\":{}}}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithArrayCodeReturnsNull() {
        String body = "{\"error\":{\"code\":[]}}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithNonJsonBodyReturnsNull() {
        String body = "not valid json";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithArrayRootReturnsNull() {
        // an array root is valid JSON, so this goes through the !root.isJsonObject() guard, not the catch -
        // sabotaging that guard (e.g. by removing it) must not slip past a catch-only test suite.
        String body = "[]";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithPrimitiveRootReturnsNull() {
        // a single unquoted token is lenient-valid JSON (a bare string primitive as the whole document), so this
        // also goes through !root.isJsonObject(), not the catch.
        String body = "ServiceUnavailable";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }
}
