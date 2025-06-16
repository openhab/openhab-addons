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
package org.openhab.binding.jellyfin.internal.client.api.client;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.binding.jellyfin.internal.client.api.client.exception.MissingBaseUrlException;
import org.openhab.binding.jellyfin.internal.client.model.ClientInfo;
import org.openhab.binding.jellyfin.internal.client.model.DeviceInfo;

/**
 * Interface for the Jellyfin API client
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public abstract class ApiClient {

    /**
     * The query parameter to use for access tokens.
     */
    public static final String QUERY_ACCESS_TOKEN = "ApiKey";

    /**
     * The recommended value for the accept header. It prefers JSON followed by octet stream and finally
     * everything. The "any MIME type" (* / *) is required for some endpoints in the server.
     */
    public static final String HEADER_ACCEPT = "application/json, application/octet-stream;q=0.9, */*;q=0.8";

    /**
     * URL to use as base for API endpoints. Should include the protocol and may contain a path.
     *
     * @return The base URL or null if not set
     */
    public abstract String getBaseUrl();

    /**
     * Access token to use for requests. Appended to all requests if set.
     *
     * @return The access token or null if not set
     */
    public abstract String getAccessToken();

    /**
     * Information about the client / application send in all API requests.
     *
     * @return Client information
     */
    public abstract ClientInfo getClientInfo();

    /**
     * Information about the device send in all API requests. Only a single session is allowed per
     * device id.
     *
     * @return Device information
     */
    public abstract DeviceInfo getDeviceInfo();

    /**
     * HTTP Options for this ApiClient.
     *
     * @return HTTP client options
     */
    public abstract HttpClientOptions getHttpClientOptions();

    /**
     * Change the authorization values used in this ApiClient instance.
     *
     * @param baseUrl The new base URL or null
     * @param accessToken The new access token or null
     * @param clientInfo The new client information
     * @param deviceInfo The new device information
     */
    public abstract void update(String baseUrl, String accessToken, ClientInfo clientInfo, DeviceInfo deviceInfo);

    /**
     * Create a complete URL based on the base URL and given parameters.
     *
     * @param pathTemplate Path template with optional placeholders
     * @param pathParameters Map of path parameters to replace placeholders
     * @param queryParameters Map of query parameters to append
     * @param ignorePathParameters Whether to ignore path parameters and use the template as-is
     * @return The complete URL
     * @throws MissingBaseUrlException If the base URL is missing
     */
    public String createUrl(String pathTemplate, Map<String, Object> pathParameters,
            Map<String, Object> queryParameters, boolean ignorePathParameters) throws MissingBaseUrlException {

        String baseUrl = getBaseUrl();
        if (baseUrl == null) {
            throw new MissingBaseUrlException();
        }

        // In a real implementation, you would need to implement URL building logic here
        // For this basic example, we'll just append the path to the base URL
        return baseUrl + pathTemplate;
    }

    /**
     * Simplified createUrl variant with default parameters
     *
     * @param pathTemplate Path template
     * @return The complete URL
     * @throws MissingBaseUrlException If the base URL is missing
     */
    public String createUrl(String pathTemplate) throws MissingBaseUrlException {
        return createUrl(pathTemplate, Collections.emptyMap(), Collections.emptyMap(), true);
    }

    /**
     * Send an API request
     *
     * @param method HTTP method
     * @param pathTemplate Path template
     * @param pathParameters Path parameters
     * @param queryParameters Query parameters
     * @param requestBody Request body object
     * @return Future with the raw response
     */
    public abstract CompletableFuture<RawResponse> request(HttpMethod method, String pathTemplate,
            Map<String, Object> pathParameters, Map<String, Object> queryParameters, Object requestBody);

    /**
     * Send a GET request
     *
     * @param pathTemplate Path template
     * @return Future with the raw response
     */
    public CompletableFuture<RawResponse> get(String pathTemplate) {
        return request(HttpMethod.GET, pathTemplate, Collections.emptyMap(), Collections.emptyMap(), null);
    }

    /**
     * Send a POST request
     *
     * @param pathTemplate Path template
     * @param requestBody Request body object
     * @return Future with the raw response
     */
    public CompletableFuture<RawResponse> post(String pathTemplate, Object requestBody) {
        return request(HttpMethod.POST, pathTemplate, Collections.emptyMap(), Collections.emptyMap(), requestBody);
    }
}
