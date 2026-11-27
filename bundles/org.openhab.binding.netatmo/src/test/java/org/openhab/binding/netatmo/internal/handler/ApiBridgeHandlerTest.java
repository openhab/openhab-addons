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
        String body = "{}";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithNullErrorReturnsNull() {
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
        String body = "[]";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }

    @Test
    public void testRawErrorCodeWithPrimitiveRootReturnsNull() {
        // a single bare token is lenient-valid JSON, so this hits the isJsonObject() guard, not the catch
        String body = "ServiceUnavailable";

        assertNull(ApiBridgeHandler.extractRawErrorCode(body));
    }
}
