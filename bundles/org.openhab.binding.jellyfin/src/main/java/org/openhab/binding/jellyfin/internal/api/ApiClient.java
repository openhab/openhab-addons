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
package org.openhab.binding.jellyfin.internal.api;

import java.net.http.HttpClient;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.util.UuidDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@NonNullByDefault
public class ApiClient extends org.openhab.binding.jellyfin.internal.api.generated.ApiClient {

    /**
     * Create an instance of ApiClient.
     */
    public ApiClient() {
        // Use the custom constructor with our ObjectMapper
        this(createDefaultHttpClientBuilder(), createDefaultObjectMapper(), "http://localhost");
    }

    /**
     * Create an instance of ApiClient.
     *
     * @param builder Http client builder.
     * @param mapper Object mapper.
     * @param baseUri Base URI
     */
    public ApiClient(HttpClient.Builder builder, ObjectMapper mapper, String baseUri) {
        super(builder, mapper, baseUri);
    }

    /**
     * Creates the default ObjectMapper with custom UUID deserializer for Jellyfin compatibility.
     *
     * <p>
     * This method extends the generated ApiClient's createDefaultObjectMapper to add a custom
     * UUID deserializer that can handle Jellyfin's 32-character UUID format.
     * </p>
     *
     * @return ObjectMapper configured for Jellyfin API compatibility
     */
    public static ObjectMapper createDefaultObjectMapper() {
        // Start with the default configuration from the generated ApiClient
        ObjectMapper mapper = org.openhab.binding.jellyfin.internal.api.generated.ApiClient.createDefaultObjectMapper();

        // Register custom UUID deserializer to handle Jellyfin's 32-character UUID format
        SimpleModule uuidModule = new SimpleModule();
        uuidModule.addDeserializer(UUID.class, new UuidDeserializer());
        mapper.registerModule(uuidModule);

        return mapper;
    }

    /**
     * Sets the API token for authenticating with the Jellyfin server.
     * This will add an Authorization header to all outgoing requests.
     *
     * @param token The API token to use for authentication.
     */
    public void authenticateWithToken(String token) {
        setRequestInterceptor(builder -> {
            builder.header("Authorization", "MediaBrowser Token=" + token);
        });
    }
}
