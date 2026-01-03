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

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Unit tests for {@link UuidDeserializer}
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class UuidDeserializerTest {

    @Test
    public void testDeserializeJellyfinUuidFormat() throws Exception {
        // Create ObjectMapper with our custom UUID deserializer and JavaTimeModule
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule uuidModule = new SimpleModule();
        uuidModule.addDeserializer(UUID.class, new UuidDeserializer());
        mapper.registerModule(uuidModule);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // Test JSON from Jellyfin with 32-character UUID (without hyphens)
        String jellyfinUserJson = """
                {
                    "Name": "testuser",
                    "ServerId": "f7873c7a09f94f358321478d31cf3f97",
                    "Id": "05e66d53183c4be4986c18d7e12694be",
                    "HasPassword": true,
                    "HasConfiguredPassword": true,
                    "HasConfiguredEasyPassword": false,
                    "EnableAutoLogin": false,
                    "LastLoginDate": "2024-11-06T00:40:18.8487902Z",
                    "LastActivityDate": "2024-11-06T00:40:18.8487902Z",
                    "Configuration": {},
                    "Policy": {}
                }
                """;

        // This should now work without throwing an exception
        UserDto user = mapper.readValue(jellyfinUserJson, UserDto.class);

        assertNotNull(user);
        assertEquals("testuser", user.getName());

        // Verify the UUID was properly parsed
        UUID expectedId = UUID.fromString("05e66d53-183c-4be4-986c-18d7e12694be");
        assertEquals(expectedId, user.getId());
    }

    @Test
    public void testDeserializeStandardUuidFormat() throws Exception {
        // Create ObjectMapper with our custom UUID deserializer and JavaTimeModule
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule uuidModule = new SimpleModule();
        uuidModule.addDeserializer(UUID.class, new UuidDeserializer());
        mapper.registerModule(uuidModule);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // Test JSON with standard 36-character UUID (with hyphens)
        String standardUserJson = """
                {
                    "Name": "testuser",
                    "ServerId": "f7873c7a-09f9-4f35-8321-478d31cf3f97",
                    "Id": "05e66d53-183c-4be4-986c-18d7e12694be",
                    "HasPassword": true,
                    "HasConfiguredPassword": true,
                    "HasConfiguredEasyPassword": false,
                    "EnableAutoLogin": false,
                    "LastLoginDate": "2024-11-06T00:40:18.8487902Z",
                    "LastActivityDate": "2024-11-06T00:40:18.8487902Z",
                    "Configuration": {},
                    "Policy": {}
                }
                """;

        // This should also work (standard format should still be supported)
        UserDto user = mapper.readValue(standardUserJson, UserDto.class);

        assertNotNull(user);
        assertEquals("testuser", user.getName());

        // Verify the UUID was properly parsed
        UUID expectedId = UUID.fromString("05e66d53-183c-4be4-986c-18d7e12694be");
        assertEquals(expectedId, user.getId());
    }
}
