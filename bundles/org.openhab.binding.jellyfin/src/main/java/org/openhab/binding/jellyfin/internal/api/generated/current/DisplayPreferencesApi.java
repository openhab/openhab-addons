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
package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.DisplayPreferencesDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DisplayPreferencesApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public DisplayPreferencesApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DisplayPreferencesApi(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarObjectMapper = apiClient.getObjectMapper();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
        memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
    }

    protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
        String body = response.body() == null ? null : new String(response.body().readAllBytes());
        String message = formatExceptionMessage(operationId, response.statusCode(), body);
        return new ApiException(response.statusCode(), message, response.headers(), body);
    }

    private String formatExceptionMessage(String operationId, int statusCode, String body) {
        if (body == null || body.isEmpty()) {
            body = "[no body]";
        }
        return operationId + " call failed with: " + statusCode + " - " + body;
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param userId User id. (optional)
     * @return DisplayPreferencesDto
     * @throws ApiException if fails to make API call
     */
    public DisplayPreferencesDto getDisplayPreferences(@org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        ApiResponse<DisplayPreferencesDto> localVarResponse = getDisplayPreferencesWithHttpInfo(displayPreferencesId,
                client, userId);
        return localVarResponse.getData();
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;DisplayPreferencesDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DisplayPreferencesDto> getDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDisplayPreferencesRequestBuilder(displayPreferencesId, client,
                userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDisplayPreferences", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<DisplayPreferencesDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<DisplayPreferencesDto>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<DisplayPreferencesDto>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getDisplayPreferencesRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        // verify the required parameter 'displayPreferencesId' is set
        if (displayPreferencesId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesId' when calling getDisplayPreferences");
        }
        // verify the required parameter 'client' is set
        if (client == null) {
            throw new ApiException(400, "Missing the required parameter 'client' when calling getDisplayPreferences");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/DisplayPreferences/{displayPreferencesId}".replace("{displayPreferencesId}",
                ApiClient.urlEncode(displayPreferencesId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "client";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("client", client));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param userId User Id. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateDisplayPreferences(@org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        updateDisplayPreferencesWithHttpInfo(displayPreferencesId, client, displayPreferencesDto, userId);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param userId User Id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateDisplayPreferencesRequestBuilder(displayPreferencesId,
                client, displayPreferencesDto, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateDisplayPreferences", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder updateDisplayPreferencesRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // verify the required parameter 'displayPreferencesId' is set
        if (displayPreferencesId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesId' when calling updateDisplayPreferences");
        }
        // verify the required parameter 'client' is set
        if (client == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'client' when calling updateDisplayPreferences");
        }
        // verify the required parameter 'displayPreferencesDto' is set
        if (displayPreferencesDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesDto' when calling updateDisplayPreferences");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/DisplayPreferences/{displayPreferencesId}".replace("{displayPreferencesId}",
                ApiClient.urlEncode(displayPreferencesId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "client";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("client", client));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(displayPreferencesDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
