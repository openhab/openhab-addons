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
package org.openhab.binding.jellyfin.internal.client.api.client.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.jellyfin.internal.client.api.client.ApiClient;
import org.openhab.binding.jellyfin.internal.client.api.client.HttpClientOptions;
import org.openhab.binding.jellyfin.internal.client.api.client.HttpMethod;
import org.openhab.binding.jellyfin.internal.client.api.client.RawResponse;
import org.openhab.binding.jellyfin.internal.client.api.client.exception.MissingBaseUrlException;
import org.openhab.binding.jellyfin.internal.client.model.ClientInfo;
import org.openhab.binding.jellyfin.internal.client.model.DeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Jellyfin API client using Jetty HTTP client
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
@NonNullByDefault
public class JettyApiClient extends ApiClient {

    private final Logger logger = LoggerFactory.getLogger(JettyApiClient.class);
    private final HttpClient httpClient;

    private String baseUrl;
    private String accessToken;
    private ClientInfo clientInfo;
    private DeviceInfo deviceInfo;
    private final HttpClientOptions httpClientOptions;

    /**
     * Create a new Jetty-based API client
     *
     * @param httpClient The Jetty HTTP client
     * @param baseUrl The base URL of the Jellyfin server
     * @param accessToken Access token for authentication
     * @param clientInfo Client information
     * @param deviceInfo Device information
     * @param httpClientOptions HTTP client options
     */
    public JettyApiClient(HttpClient httpClient, String baseUrl, String accessToken, ClientInfo clientInfo,
            DeviceInfo deviceInfo, HttpClientOptions httpClientOptions) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
        this.clientInfo = clientInfo;
        this.deviceInfo = deviceInfo;
        this.httpClientOptions = httpClientOptions;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public HttpClientOptions getHttpClientOptions() {
        return httpClientOptions;
    }

    @Override
    public void update(String baseUrl, String accessToken, ClientInfo clientInfo, DeviceInfo deviceInfo) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
        this.clientInfo = clientInfo;
        this.deviceInfo = deviceInfo;
    }

    @Override
    public String createUrl(String pathTemplate, Map<String, Object> pathParameters,
            Map<String, Object> queryParameters, boolean ignorePathParameters) throws MissingBaseUrlException {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new MissingBaseUrlException();
        }

        try {
            // Simple URL builder for this example
            URI uri = new URI(baseUrl).resolve(pathTemplate);

            // Add query parameters if present
            if (!queryParameters.isEmpty()) {
                StringBuilder sb = new StringBuilder(uri.toString());
                sb.append(uri.toString().contains("?") ? "&" : "?");

                boolean first = true;
                for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {
                    if (!first) {
                        sb.append("&");
                    }
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                    first = false;
                }

                // Add access token as query param if available
                if (accessToken != null && !accessToken.isEmpty()) {
                    sb.append(first ? "" : "&").append(QUERY_ACCESS_TOKEN).append("=").append(accessToken);
                }

                return sb.toString();
            } else if (accessToken != null && !accessToken.isEmpty()) {
                // Add just the access token
                return uri + (uri.toString().contains("?") ? "&" : "?") + QUERY_ACCESS_TOKEN + "=" + accessToken;
            }

            return uri.toString();
        } catch (URISyntaxException e) {
            logger.error("Failed to create URL: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid URL template: " + pathTemplate, e);
        }
    }

    @Override
    public CompletableFuture<RawResponse> request(HttpMethod method, String pathTemplate,
            Map<String, Object> pathParameters, Map<String, Object> queryParameters, Object requestBody) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = createUrl(pathTemplate, pathParameters, queryParameters, false);

                Request request;
                switch (method) {
                    case GET:
                        request = httpClient.newRequest(url).method("GET");
                        break;
                    case POST:
                        request = httpClient.newRequest(url).method("POST");
                        break;
                    case PUT:
                        request = httpClient.newRequest(url).method("PUT");
                        break;
                    case DELETE:
                        request = httpClient.newRequest(url).method("DELETE");
                        break;
                    case PATCH:
                        request = httpClient.newRequest(url).method("PATCH");
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
                }

                // Add common headers
                request.header(HttpHeader.ACCEPT, HEADER_ACCEPT);
                request.header("X-Emby-Authorization", buildAuthHeader());

                // Add request body if present
                if (requestBody != null) {
                    if (requestBody instanceof String) {
                        request.content(new StringContentProvider((String) requestBody), "application/json");
                    } else {
                        // In a real implementation, a JSON serializer would be used here
                        request.content(new StringContentProvider(requestBody.toString()), "application/json");
                    }
                }

                // Set timeouts
                request.timeout(httpClientOptions.getRequestTimeout().toMillis(),
                        httpClientOptions.getSocketTimeout().toMillis());

                // Execute the request
                ContentResponse response = request.send();

                // Create response object
                Map<String, String> headers = new HashMap<>();
                response.getHeaders().forEach(field -> headers.put(field.getName(), field.getValue()));

                ByteBuffer body = ByteBuffer.wrap(response.getContent());

                return new RawResponse(response.getStatus(), headers, body);
            } catch (Exception e) {
                throw new CompletionException("Request failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Build the authentication header for Jellyfin requests
     *
     * @return The formatted authorization header value
     */
    private String buildAuthHeader() {
        StringBuilder sb = new StringBuilder();

        // MediaBrowser Client="ClientName", Device="DeviceName", DeviceId="DeviceId", Version="Version"
        sb.append("MediaBrowser ");
        sb.append("Client=\"").append(clientInfo.getName()).append("\", ");
        sb.append("Device=\"").append(deviceInfo.getName()).append("\", ");
        sb.append("DeviceId=\"").append(deviceInfo.getId()).append("\", ");
        sb.append("Version=\"").append(clientInfo.getVersion()).append("\"");

        if (accessToken != null && !accessToken.isEmpty()) {
            sb.append(", Token=\"").append(accessToken).append("\"");
        }

        return sb.toString();
    }
}
