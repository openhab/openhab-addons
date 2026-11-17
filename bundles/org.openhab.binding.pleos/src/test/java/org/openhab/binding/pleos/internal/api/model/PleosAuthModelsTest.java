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
package org.openhab.binding.pleos.internal.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pleos.internal.api.model.PleosAuthModels.TokenRequest;
import org.openhab.binding.pleos.internal.api.model.PleosAuthModels.TokenResponse;
import org.openhab.binding.pleos.internal.api.model.PleosAuthModels.TokenResponse.TokenData;

import com.google.gson.Gson;

/**
 * Unit tests for {@link PleosAuthModels}.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosAuthModelsTest {

    private final Gson gson = new Gson();

    @Test
    public void testTokenRequestSerialization() {
        TokenRequest request = new TokenRequest("myClientId", "myClientSecret");
        String json = gson.toJson(request);

        assertTrue(json.contains("\"clientId\":\"myClientId\""));
        assertTrue(json.contains("\"clientSecret\":\"myClientSecret\""));
    }

    @Test
    public void testTokenResponseDeserialization() {
        String json = """
                {
                    "data": {
                        "accessToken": "eySAMPLE_TOKEN",
                        "expiresIn": 3600
                    }
                }
                """;

        TokenResponse response = gson.fromJson(json, TokenResponse.class);
        assertNotNull(response);
        TokenData data = response.data();
        assertNotNull(data);
        assertEquals("eySAMPLE_TOKEN", data.accessToken());
        assertEquals(3600L, data.expiresIn());
    }
}
