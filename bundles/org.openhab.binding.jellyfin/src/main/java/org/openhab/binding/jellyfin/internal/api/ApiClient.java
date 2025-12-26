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
public class ApiClient extends org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient {

    /**
     * Required dummy URI for constructor compatibility with generated code and factory pattern.
     * This value is not a real default, but a placeholder to satisfy the baseUri parameter.
     */
    private static final String INTERNAL_PLACEHOLDER_BASE_URI = "http://placeholder.invalid";

    /**
     * Create an instance of ApiClient.
     * <p>
     * <b>Note:</b> This parameterless constructor is <b>mandatory</b> for compatibility with the generated code.
     * The generated API infrastructure (e.g., {@code Configuration.getDefaultApiClient()} and the default
     * {@code apiClientFactory}) expects to instantiate {@code ApiClient} using a no-argument constructor via
     * {@code ApiClient::new}.
     * Removing or changing this constructor would break integration with the generated API classes and factory pattern.
     * </p>
     */
    public ApiClient() {
        // Use the custom constructor with our ObjectMapper
        this(createDefaultHttpClientBuilder(), createDefaultObjectMapper(), INTERNAL_PLACEHOLDER_BASE_URI);
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
        // Keep fully-qualified name to avoid colliding with this ApiClient subclass
        ObjectMapper mapper = org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient
                .createDefaultObjectMapper();

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
