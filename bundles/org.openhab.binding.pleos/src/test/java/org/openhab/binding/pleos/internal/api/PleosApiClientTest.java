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
package org.openhab.binding.pleos.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pleos.internal.model.Brand;

/**
 * Unit tests for {@link PleosApiClient} error handling and parsing.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosApiClientTest {

    private PleosApiClient client = new PleosApiClient(new HttpClient(), "dummyClientId", "dummySecret", Brand.HYUNDAI);

    @BeforeEach
    public void setUp() {
        client = new PleosApiClient(new HttpClient(), "dummyClientId", "dummySecret", Brand.HYUNDAI);
    }

    @Test
    public void testBuildExceptionFromStructuredError() {
        String jsonError = """
                {
                    "error": {
                        "code": "40003",
                        "message": "Token expired"
                    }
                }
                """;

        PleosApiException ex = client.buildExceptionFromErrorResponse(400, jsonError, false);
        assertEquals("40003", ex.getErrorCode());
        String msg = ex.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("Token expired"));
        assertFalse(ex.isAuthError());
    }

    @Test
    public void testBuildExceptionWithDetails() {
        String jsonError = """
                {
                    "error": {
                        "code": "40001",
                        "message": "Invalid Parameter",
                        "details": [
                            { "field": "vin", "issue": "Must be 17 characters" }
                        ]
                    }
                }
                """;

        PleosApiException ex = client.buildExceptionFromErrorResponse(400, jsonError, false);
        assertEquals("40001", ex.getErrorCode());
        String msg = ex.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("vin: Must be 17 characters"));
        assertTrue(ex.isAuthError()); // 40001 is a fatal auth code
    }

    @Test
    public void testBuildExceptionFallbackForNonJson() {
        String rawError = "Internal Server Error";
        PleosApiException ex = client.buildExceptionFromErrorResponse(500, rawError, false);

        assertEquals("500", ex.getErrorCode());
        String msg = ex.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("HTTP Error 500: Internal Server Error"));
        assertFalse(ex.isAuthError());
    }

    @Test
    public void testBuildExceptionLoginCallIsAlwaysAuthFatal() {
        String jsonError = """
                {
                    "error": {
                        "code": "50000",
                        "message": "Something went wrong"
                    }
                }
                """;

        PleosApiException ex = client.buildExceptionFromErrorResponse(400, jsonError, true);
        assertTrue(ex.isAuthError());
    }
}
