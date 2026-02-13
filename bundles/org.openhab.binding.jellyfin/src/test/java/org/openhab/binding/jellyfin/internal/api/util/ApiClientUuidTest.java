/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.ApiClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test to verify that the custom ApiClient correctly uses the UUID deserializer
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class ApiClientUuidTest {

    @Test
    public void testApiClientUsesCustomUuidDeserializer() throws Exception {
        // Create an ApiClient and get its ObjectMapper
        ApiClient client = new ApiClient();
        ObjectMapper mapper = client.getObjectMapper();

        // Test with a simple JSON containing a 32-character UUID
        String jsonWithJellyfinUuid = """
                {
                    "id": "05e66d53183c4be4986c18d7e12694be",
                    "name": "test"
                }
                """;

        // This should work without throwing an exception
        TestDto result = mapper.readValue(jsonWithJellyfinUuid, TestDto.class);

        assertNotNull(result);
        assertNotNull(result.id);
        assertEquals("test", result.name);
        // Verify the UUID is correctly converted
        assertEquals("05e66d53-183c-4be4-986c-18d7e12694be", result.id.toString());
    }

    @Test
    public void testApiClientCreateDefaultObjectMapper() throws Exception {
        // Test the static method directly
        ObjectMapper mapper = ApiClient.createDefaultObjectMapper();

        String jsonWithJellyfinUuid = """
                {
                    "id": "cc89f0df805247a5aab1fe3d27b19183",
                    "name": "openhab"
                }
                """;

        TestDto result = mapper.readValue(jsonWithJellyfinUuid, TestDto.class);

        assertNotNull(result);
        assertNotNull(result.id);
        assertEquals("openhab", result.name);
        assertEquals("cc89f0df-8052-47a5-aab1-fe3d27b19183", result.id.toString());
    }

    public static class TestDto {
        public java.util.UUID id;
        public String name;
    }
}
